package mathhelper.games.matify

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mathhelper.games.matify.activities.GamesActivity
import mathhelper.games.matify.activities.LevelsActivity
import mathhelper.games.matify.common.AuthInfoObjectBase
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.game.Game
import mathhelper.games.matify.statistics.Pages
import mathhelper.games.matify.statistics.Request
import mathhelper.games.matify.statistics.RequestData
import mathhelper.games.matify.statistics.Statistics
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
    var jsonPath = "active"
    var gamesActivity: GamesActivity? = null
        set(value) {
            field = value
            if (value != null) {
                Request.startWorkCycle()
                tutorialProcessing = false
                val gameNames = value.assets.list(jsonPath)!!
                    //.filter { """game.*.json""".toRegex(RegexOption.DOT_MATCHES_ALL).matches(it) }
                games = ArrayList()
                for (name in gameNames) {
                    val loadedGame = Game.create("$jsonPath/$name", value)
                    if (loadedGame != null) {
                        games.add(loadedGame)
                    }
                }
                authStatus = Storage.shared.authStatus(value)
            }
        }
    var currentGame: Game? = null
        set(value) {
            field = value
            if (value != null) {
                Handler().postDelayed({
                    gamesActivity?.startActivity(Intent(gamesActivity, LevelsActivity::class.java))
                }, 100)
                // ActivityOptions.makeSceneTransitionAnimation(gamesActivity).toBundle())
                // TODO: send log about game started
            }
        }
    var loadingElement: ProgressBar? = null

    fun resetAll() {
        if (LevelScene.shared.levelsActivity != null) {
            LevelScene.shared.back()
        }
        games.map { Storage.shared.resetGame(gamesActivity!!, it.code) }
    }

    fun logout() {
        resetAll()
        Storage.shared.clearUserInfo(gamesActivity!!)
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

    fun signUp (context: Activity, userData: AuthInfoObjectBase) {
        val requestRoot = JSONObject()
        requestRoot.put("login", userData.login)
        requestRoot.put("password", userData.password)
        requestRoot.put("name", userData.name)
        requestRoot.put("fullName", userData.fullName)
        requestRoot.put("additional", userData.additional)
        val req = RequestData(Pages.SIGNUP.value, body = requestRoot.toString())
        shared.request(context, background = {
            val response = Request.signRequest(req)
            Storage.shared.setServerToken(context, response.getString("token"))
        }, foreground = {
            Statistics.logSign(context)
            context.finish()
        }, errorground = {
            Storage.shared.invalidateUser(context)
        })
    }

    fun request(
        context: Activity,
        background: () -> (Unit),
        foreground: () -> (Unit),
        errorground: () -> (Unit)
    ) {
        loadingElement?.visibility = View.VISIBLE
        GlobalScope.launch {
            try {
                background()
                context.runOnUiThread {
                    foreground()
                }
            } catch (e: Exception) {
                when (e) {
                    is Request.TimeoutException -> {
                        context.runOnUiThread {
                            Toast.makeText(context, R.string.problems_with_internet_connection, Toast.LENGTH_LONG).show()
                        }
                    }
                    is Request.TokenNotFoundException -> {
                        context.runOnUiThread {
                            Toast.makeText(context, R.string.bad_credentials_error, Toast.LENGTH_LONG).show()
                        }
                    }
                    is Request.UndefinedException -> {
                        context.runOnUiThread {
                            Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                errorground()
            } finally {
                context.runOnUiThread {
                    loadingElement?.visibility = View.INVISIBLE
                }
            }
        }
    }
}