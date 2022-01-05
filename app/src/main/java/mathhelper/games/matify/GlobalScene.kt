package mathhelper.games.matify

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.gson.Gson
import kotlinx.coroutines.*
import mathhelper.games.matify.activities.GamesActivity
import mathhelper.games.matify.activities.LevelsActivity
import mathhelper.games.matify.activities.SplashActivity
import mathhelper.games.matify.common.*
import mathhelper.games.matify.game.FilterTaskset
import mathhelper.games.matify.game.FullTaskset
import mathhelper.games.matify.game.Game
import mathhelper.games.matify.parser.GsonParser
import mathhelper.games.matify.statistics.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

enum class AuthStatus(val str: String) {
    GUEST("guest"),
    GOOGLE("google"),
    MATH_HELPER("math_helper"),
    GITHUB("github");

    companion object {
        fun value(value: String) = values().find { it.str == value }
    }
}

enum class SearchType {
    BY_NAME, BY_CODE, BY_SPACE
}

class GlobalScene {
    companion object {
        private const val TAG = "GlobalScene"
        private const val sigmaWidth = 3
        val shared: GlobalScene = GlobalScene()
    }

    var authStatus = AuthStatus.GUEST
    var googleSignInClient: GoogleSignInClient? = null
    var tutorialProcessing = false
    var gameMap = hashMapOf<String, Game>()
    var gameOrder = arrayListOf<String>()
    //private var games: ArrayList<Game> = ArrayList()
    var gamesActivity: GamesActivity? = null
        set(value) {
            field = value
            gameMap = hashMapOf()
            gameOrder = arrayListOf()
        }
    var currentGameIndex: Int = 0
        set(value) {
            field = if (value >= 0 && value < gameOrder.size) {
                value
            } else {
                0
            }
            currentGame = gameMap[gameOrder[value]]
            if (currentGame != null) {
                Handler().postDelayed({
                    gamesActivity?.startActivity(Intent(gamesActivity, LevelsActivity::class.java))
                }, 100)
            }
        }
    var currentGame: Game? = null
        private set
    var loadingElement: ProgressBar? = null
    private val activeJobs = arrayListOf<Job>()

    fun init() {
        Request.startWorkCycle()
        tutorialProcessing = false
        gameMap = hashMapOf()
        gameOrder = arrayListOf()
        authStatus = Storage.shared.authStatus()
    }

    fun cancelActiveJobs() {
        activeJobs.forEach { it.cancel() }
        activeJobs.clear()
    }

    fun asyncTask(
        context: Activity,
        background: () -> (Unit),
        foreground: () -> (Unit),
        errorground: () -> (Unit),
        toastError: Boolean = true
    ) {
        loadingElement?.visibility = View.VISIBLE
        val task = GlobalScope.launch {
            try {
                background()
                if (isActive) {
                    context.runOnUiThread {
                        foreground()
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "asyncTask exception caught: $e")
                if (isActive) {
                    val toast = when (e) {
                        is Request.TimeoutException -> context.getString(R.string.problems_with_internet_connection)
                        is Request.TokenNotFoundException -> context.getString(R.string.bad_credentials_error)
                        is Request.UndefinedException -> context.getString(R.string.something_went_wrong)
                        is Request.UserMessageException -> e.message
                        else -> null
                    }
                    context.runOnUiThread {
                        if (toastError && toast != null) {
                            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
                        }
                        errorground()
                    }
                }
            } finally {
                context.runOnUiThread {
                    loadingElement?.visibility = View.INVISIBLE
                }
            }
        }
        activeJobs.add(task)
    }

    fun resetAll(success: () -> Unit, error: () -> Unit) {
        if (LevelScene.shared.levelsActivity != null) {
            LevelScene.shared.back()
        }
        asyncTask(gamesActivity!!, background = {
            val token = Storage.shared.serverToken()
            Request.resetHistory(RequestData(RequestPage.USER_HISTORY, token, RequestMethod.DELETE))
            Storage.shared.resetResults()
        }, foreground = {
            success()
            gamesActivity!!.startActivity(Intent(gamesActivity!!, SplashActivity::class.java))
            gamesActivity!!.finish()
        }, errorground = error)
    }

    fun logout() {
        if (LevelScene.shared.levelsActivity != null) {
            LevelScene.shared.back()
        }
        Storage.shared.resetResults()
        Storage.shared.clearUserInfo()
        Storage.shared.clearAllGames()
        if (authStatus == AuthStatus.GOOGLE) {
            googleSignInClient!!.signOut()
        }
        Request.stopWorkCycle()
        gamesActivity!!.startActivity(Intent(gamesActivity!!, SplashActivity::class.java))
        gamesActivity!!.finish()
    }

    private fun getByNormDist(mean: Float, sigma: Float): Float {
        var res: Float
        val left = mean - sigmaWidth * sigma
        val right = mean + sigmaWidth * sigma
        do {
            res = Random().nextGaussian().toFloat() * sigma + mean
        } while (res !in left..right)
        return res
    }

    fun signUp(context: Activity, userData: AuthInfoObjectBase) {
        val requestRoot = JSONObject()
        requestRoot.put("login", userData.login)
        requestRoot.put("password", userData.password)
        if (userData.name?.length != null && userData.name.length >= 3) {
            requestRoot.put("name", userData.name)
        }
        if (userData.fullName?.length != null && userData.fullName.length >= 3) {
            requestRoot.put("fullName", userData.fullName)
        }
        if (userData.additional?.length != null && userData.additional.length >= 3) {
            requestRoot.put("additional", userData.additional)
        }
        requestRoot.put("locale", AndroidUtil.get3sizedLocale(Storage.shared.context.get()))
        val req = RequestData(RequestPage.SIGNUP, body = requestRoot.toString())
        asyncTask(context, background = {
            val response = Request.signRequest(req)
            Storage.shared.setServerToken(response.getString("token"))
        }, foreground = {
            Statistics.logSign(context)
            context.finish()
        }, errorground = {
            Storage.shared.invalidateUser()
        })
    }

    fun parseDefaultAndLoadedGames() {
        GlobalScope.launch {
            parsePreloadedGames()
            gamesActivity!!.runOnUiThread {
                gamesActivity!!.generateList()
            }
        }
    }

    private fun parsePreloadedGames() {
        gameOrder = ArrayList(Storage.shared.getOrderedTasksetCodes())
        val needToFill = gameOrder.size == 0
        val pinned = Storage.shared.getPinnedTasksetCodes()
        val tasksets = Storage.shared.getAllSavedTasksets()
        for (taskset in tasksets) {
            val loadedGame = Game.create(taskset, gamesActivity!!)
            if (loadedGame != null) {
                if (loadedGame.code in pinned) {
                    loadedGame.isPinned = true
                }
                gameMap[loadedGame.code] = loadedGame
                if (needToFill) {
                    gameOrder.add(loadedGame.code)
                }
            }
        }
    }

    fun requestGamesByParams(
        toList: ArrayList<Game>,
        searchType: SearchType = SearchType.BY_SPACE,
        searchQuery: String = "global",
        success: () -> Unit, error: () -> Unit, toastError: Boolean = true
    ) {
        asyncTask(gamesActivity!!, {
            val tasksetsReqData = RequestData(RequestPage.TASKSETS_PREVIEW, method = RequestMethod.GET)
            tasksetsReqData.url += when(searchType) {
                SearchType.BY_NAME -> "&keywords=$searchQuery"
                SearchType.BY_SPACE -> "&namespace=$searchQuery"
                SearchType.BY_CODE -> "&code=$searchQuery"
            }
            val res = Request.doSyncRequest(tasksetsReqData)
            if (res.returnValue != 200) {
                throw Request.UserMessageException(gamesActivity!!.getString(R.string.games_load_failed))
            }
            val tasksets = GsonParser.parse<FilterTaskset>(res.body)?.tasksets
            if (tasksets.isNullOrEmpty()) {
                throw Request.UserMessageException(gamesActivity!!.getString(R.string.games_load_failed))
            }
            for (taskset in tasksets.orEmpty()) {
                if (taskset.has("code")) {
                    val info = TasksetInfo(taskset = taskset)
                    val loadedGame = Game.create(info, gamesActivity!!)
                    if (loadedGame != null) {
                        toList.add(loadedGame)
                    }
                }
            }
        }, success, error, toastError)
    }

    fun requestGameForPlay(game: Game, forceRefresh: Boolean = false, success: () -> Unit = {}, error: () -> Unit = {}) {
        asyncTask(gamesActivity!!, {
            if (forceRefresh || game.isPreview) {
                val token = Storage.shared.serverToken()
                val tasksetsReqData = RequestData(RequestPage.TASKSETS_FULL, method = RequestMethod.GET, securityToken = token)
                tasksetsReqData.url += "/${game.code}"
                val res = Request.doSyncRequest(tasksetsReqData)
                if (res.returnValue != 200) {
                    throw Request.UserMessageException(gamesActivity!!.getString(R.string.level_load_fail))
                }
                val fullTaskset: FullTaskset = GsonParser.parse(res.body) ?:
                throw Request.UserMessageException(gamesActivity!!.getString(R.string.level_load_fail))
                Storage.shared.saveTaskset(game.code, fullTaskset.taskset, fullTaskset.rulePacks, game.isDefault)
                game.updateWithJsons(fullTaskset.taskset, fullTaskset.rulePacks)
            }
            game.load(gamesActivity!!)
        }, success, error)
    }

    fun refreshGames() {
        if (!ConnectionChecker.shared.isConnected) return
        val defCodes = gameMap.filter { it.value.isDefault }.map { it.key }
        val codesToUpdate = gameMap.map { it.key }
        val pinned = Storage.shared.getPinnedTasksetCodes()
        val token = Storage.shared.serverToken()
        val tasksetsReqData = RequestData(RequestPage.TASKSETS_PREVIEW, method = RequestMethod.GET, securityToken = token)
        val base = "${tasksetsReqData.url}&code="
        GlobalScope.launch {
            try {
                for (code in codesToUpdate) {
                    tasksetsReqData.url = base + code
                    val res = Request.doSyncRequest(tasksetsReqData)
                    if (res.returnValue != 200) {
                        Logger.e(TAG, "failed to refresh game with code = $code")
                        continue
                        //throw Request.UserMessageException("Games refreshing failed. Try again later.")
                    }
                    val tasksets = GsonParser.parse<FilterTaskset>(res.body)?.tasksets
                    if (tasksets != null && tasksets.size == 1 && tasksets[0].has("code")) {
                        val isDefault = code in defCodes
                        Storage.shared.saveTaskset(tasksets[0].get("code").asString, tasksets[0], isDefault = isDefault)
                        val info = TasksetInfo(taskset = tasksets[0], isDefault = isDefault)
                        val loadedGame = Game.create(info, gamesActivity!!)
                        if (loadedGame != null) {
                            loadedGame.isPinned = loadedGame.code in pinned
                            gameMap[loadedGame.code] = loadedGame
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "failed to refresh game with exception = $e")
            } finally {
                gamesActivity!!.runOnUiThread {
                    gamesActivity!!.generateList()
                }
            }
        }
    }

    fun addGame(game: Game): Int {
        val activity = gamesActivity ?: return 0
        Storage.shared.saveTaskset(game.code, Gson().toJsonTree(game).asJsonObject)
        val i = gameOrder.size
        val gameView = AndroidUtil.generateGameView(activity, game, onClick = {
            if (!activity.isLoading) {
                currentGameIndex = i
            }
        }, onLongClick = {
            activity.currentLongClicked = game.code
            activity.alertInfo = AndroidUtil.showGameInfo(activity, game, activity.blurView)
            true
        })
        gameOrder.add(game.code)
        gameMap[game.code] = game
        activity.gamesList.addView(gameView)
        Storage.shared.saveOrder(gameOrder)
        return i
    }

    fun removeGame(code: String) {
        val activity = gamesActivity ?: return
        gameOrder.remove(code)
        gameMap.remove(code)
        Storage.shared.deleteTaskset(code)
        Storage.shared.deletePin(code)
        activity.generateList()
    }

    fun pinGame(code: String) {
        val activity = gamesActivity ?: return
        gameOrder.remove(code)
        val game = gameMap[code] ?: return
        game.isPinned = !game.isPinned
        if (game.isPinned) {
            Storage.shared.savePin(game.code)
            gameOrder.add(0, code)
        } else {
            Storage.shared.deletePin(game.code)
            gameOrder.add(code)
        }
        activity.generateList()
    }
}