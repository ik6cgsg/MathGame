package mathhelper.games.matify

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.gson.Gson
import kotlinx.coroutines.*
import mathhelper.games.matify.activities.GamesActivity
import mathhelper.games.matify.activities.LevelsActivity
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.AuthInfoObjectBase
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.common.Storage
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

class GlobalScene {
    companion object {
        private const val TAG = "GlobalScene"
        private const val sigmaWidth = 3
        val shared: GlobalScene = GlobalScene()
    }

    var authStatus = AuthStatus.GUEST
    var googleSignInClient: GoogleSignInClient? = null
    var tutorialProcessing = false
    var games: ArrayList<Game> = ArrayList()
    var gamesActivity: GamesActivity? = null
        set(value) {
            field = value
            if (value != null) {
                Request.startWorkCycle()
                tutorialProcessing = false
                games = ArrayList()
                authStatus = Storage.shared.authStatus(value)
            }
        }
    var currentGameIndex: Int = 0
    var currentGame: Game? = null
        set(value) {
            field = value
            if (value != null) {
                Handler().postDelayed({
                    gamesActivity?.startActivity(Intent(gamesActivity, LevelsActivity::class.java))
                }, 100)
            }
        }
    var loadingElement: ProgressBar? = null
    private val activeJobs = arrayListOf<Job>()

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
            val token = Storage.shared.serverToken(gamesActivity!!)
            Request.resetHistory(RequestData(Pages.USER_HISTORY.value, token, RequestMethod.DELETE))
            Storage.shared.resetResults(gamesActivity!!)
        }, foreground = {
            success()
            gamesActivity!!.recreate()
        }, errorground = error)
    }

    fun logout() {
        if (LevelScene.shared.levelsActivity != null) {
            LevelScene.shared.back()
        }
        Storage.shared.resetResults(gamesActivity!!)
        Storage.shared.clearUserInfo(gamesActivity!!)
        Storage.shared.clearSettings(gamesActivity!!)
        if (authStatus == AuthStatus.GOOGLE) {
            googleSignInClient!!.signOut()
        }
        Request.stopWorkCycle()
        gamesActivity!!.recreate()
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
        requestRoot.put("locale", AndroidUtil.get3sizedLocale(gamesActivity!!))
        val req = RequestData(Pages.SIGNUP.value, body = requestRoot.toString())
        asyncTask(context, background = {
            val response = Request.signRequest(req)
            Storage.shared.setServerToken(context, response.getString("token"))
        }, foreground = {
            Statistics.logSign(context)
            context.finish()
        }, errorground = {
            Storage.shared.invalidateUser(context)
        })
    }

    fun parseLoadedOrRequestDefaultGames() {
        if (!Storage.shared.gotAnySavedTasksets(gamesActivity!!)) {
            requestGamesByParams(games, success = {
                if (games.isNotEmpty()) {
                    gamesActivity!!.generateList()
                }
            }, error = {})
        } else {
            GlobalScope.launch {
                val job = async {
                    parsePreloadedGames()
                    gamesActivity!!.runOnUiThread {
                        gamesActivity!!.generateList()
                    }
                }
                job.await()
            }
        }
    }

    private fun parsePreloadedGames() {
        val tasksets = Storage.shared.getAllSavedTasksets(gamesActivity!!)
        for (taskset in tasksets) {
            val loadedGame = Game.create(taskset, gamesActivity!!)
            if (loadedGame != null) {
                games.add(loadedGame)
            }
        }
    }

    fun requestGamesByParams(
        toList: ArrayList<Game>,
        namespaceCode: String = "global_test",
        keywords: String = "",
        success: () -> Unit, error: () -> Unit
    ) {
        asyncTask(gamesActivity!!, {
            val tasksetsReqData = RequestData(Pages.TASKSETS_PREVIEW.value, method = RequestMethod.GET)
            tasksetsReqData.url += "?namespace=$namespaceCode&keywords=$keywords"
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
                    Storage.shared.saveTaskset(gamesActivity!!, taskset.get("code").asString, taskset.toString())
                    val loadedGame = Game.create(taskset.toString(), gamesActivity!!)
                    if (loadedGame != null) {
                        toList.add(loadedGame)
                    }
                }
            }
        }, success, error)
    }

    fun requestGameForPlay(game: Game, success: () -> Unit, error: () -> Unit) {
        asyncTask(gamesActivity!!, {
            var fullTaskset = Storage.shared.tryGetFullTaskset(gamesActivity!!, game.code)
            if (fullTaskset == null) {
                val token = Storage.shared.serverToken(gamesActivity!!)
                val tasksetsReqData = RequestData(Pages.TASKSETS_FULL.value, method = RequestMethod.GET, securityToken = token)
                tasksetsReqData.url += "/${game.code}"
                val res = Request.doSyncRequest(tasksetsReqData)
                if (res.returnValue != 200) {
                    throw Request.UserMessageException(gamesActivity!!.getString(R.string.level_load_fail))
                }
                fullTaskset = GsonParser.parse(res.body)!!
                Storage.shared.saveTaskset(gamesActivity!!, game.code, fullTaskset.taskset.toString(), Gson().toJson(fullTaskset.rulePacks))
            }
            game.preparseRulePacks(fullTaskset.rulePacks)
            game.load(gamesActivity!!)
        }, success, error)
    }

    fun refreshGames(): ArrayList<Game> {
        games = arrayListOf()
        val codes = Storage.shared.getAllSavedTasksetCodes(gamesActivity!!)
        val token = Storage.shared.serverToken(gamesActivity!!)
        val tasksetsReqData = RequestData(Pages.TASKSETS_FULL.value, method = RequestMethod.GET, securityToken = token)
        val base = "${tasksetsReqData.url}/"
        GlobalScope.launch {
            val job = async {
                for (code in codes) {
                    tasksetsReqData.url = base + code
                    val res = Request.doSyncRequest(tasksetsReqData)
                    if (res.returnValue != 200) {
                        Logger.e(TAG, "failed to refresh game with code = $code")
                        //throw Request.UserMessageException("Games refreshing failed. Try again later.")
                    }
                    val fullTaskset = GsonParser.parse<FullTaskset>(res.body)!!
                    val taskset = fullTaskset.taskset.toString()
                    Storage.shared.saveTaskset(gamesActivity!!, code, taskset, Gson().toJson(fullTaskset.rulePacks))
                    val game = Game.create(taskset, gamesActivity!!)
                    //?: throw Request.UserMessageException("Games refreshing failed. Try again later.")
                    if (game == null) {
                        Logger.e(TAG, "failed to create refreshed game with code = $code")
                    } else {
                        game.preparseRulePacks(fullTaskset.rulePacks)
                        game.load(gamesActivity!!)
                        games.add(game)
                    }
                }
                gamesActivity!!.runOnUiThread {
                    gamesActivity!!.generateList()
                }
            }
            job.await()
        }

        return games
    }
}