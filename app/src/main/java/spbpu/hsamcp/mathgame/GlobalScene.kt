package spbpu.hsamcp.mathgame

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import spbpu.hsamcp.mathgame.activities.GamesActivity
import spbpu.hsamcp.mathgame.activities.LevelsActivity
import spbpu.hsamcp.mathgame.common.AuthInfo
import spbpu.hsamcp.mathgame.common.AuthInfoCoeffs
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.common.Storage
import spbpu.hsamcp.mathgame.game.Game
import spbpu.hsamcp.mathgame.level.UndoPolicy
import spbpu.hsamcp.mathgame.statistics.Request
import spbpu.hsamcp.mathgame.statistics.RequestData
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
                val gameNames = value.assets.list("")!!
                    .filter { """game.*.json""".toRegex(RegexOption.DOT_MATCHES_ALL).matches(it) }
                games = ArrayList()
                for (name in gameNames) {
                    val loadedGame = Game.create(name, value)
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
                gamesActivity?.startActivity(Intent(gamesActivity, LevelsActivity::class.java))
                    // ActivityOptions.makeSceneTransitionAnimation(gamesActivity).toBundle())
                // TODO: send log about game started
            }
        }
    var loadingElement: ProgressBar? = null

    fun resetAll() {
        if (LevelScene.shared.levelsActivity != null) {
            LevelScene.shared.back()
        }
        games.map { Storage.shared.resetGame(gamesActivity!!, it.gameCode) }
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

    fun generateGamesMultCoeffs() {
        Storage.shared.setUserCoeffs(gamesActivity!!, AuthInfoCoeffs(
            undoCoeff = Random().nextInt(UndoPolicy.values().size),
            timeCoeff = getByNormDist(1f, Constants.timeDeviation),
            awardCoeff = getByNormDist(1f, Constants.awardDeviation)
        ))
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

    fun request(context: Activity, background: () -> (Unit), foreground: () -> (Unit), initial: Boolean = false) {
        loadingElement?.visibility = View.VISIBLE
        GlobalScope.launch {
            try {
                background()
                context.runOnUiThread {
                    foreground()
                }
            } catch (e: Request.TimeoutException) {
                if (initial) Storage.shared.invalidateUser(context)
                context.runOnUiThread {
                    Toast.makeText(context, "Check your internet connection!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Request.TokenNotFoundException) {
                if (initial) Storage.shared.invalidateUser(context)
                context.runOnUiThread {
                    Toast.makeText(context, "Error, while connecting with our server", Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: Request.UndefinedException) {
                if (initial) Storage.shared.invalidateUser(context)
                context.runOnUiThread {
                    Toast.makeText(context, "Something went wrong, try later", Toast.LENGTH_LONG).show()
                }
            } finally {
                context.runOnUiThread {
                    loadingElement?.visibility = View.INVISIBLE
                }
            }
        }
    }
}