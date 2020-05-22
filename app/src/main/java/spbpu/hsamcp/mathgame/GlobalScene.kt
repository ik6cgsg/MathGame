package spbpu.hsamcp.mathgame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import spbpu.hsamcp.mathgame.activities.AuthActivity
import spbpu.hsamcp.mathgame.activities.GamesActivity
import spbpu.hsamcp.mathgame.activities.LevelsActivity
import spbpu.hsamcp.mathgame.common.AuthInfo
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.game.Game
import spbpu.hsamcp.mathgame.level.UndoPolicy
import spbpu.hsamcp.mathgame.statistics.Request
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
                val prefs = value.getSharedPreferences(Constants.storage, AppCompatActivity.MODE_PRIVATE)
                authStatus = AuthStatus.value(prefs.getString(AuthInfo.AUTH_STATUS.str, AuthStatus.GUEST.str)!!)!!
            }
        }
    var currentGame: Game? = null
        set(value) {
            field = value
            if (value != null) {
                gamesActivity?.startActivity(Intent(gamesActivity, LevelsActivity::class.java))
                // TODO: send log about game started
            }
        }

    fun resetAll() {
        if (LevelScene.shared.levelsActivity != null) {
            LevelScene.shared.back()
        }
        games.map {
            val gamePrefs = gamesActivity!!.getSharedPreferences(it.gameCode, Context.MODE_PRIVATE)
            val gamePrefEdit = gamePrefs.edit()
            gamePrefEdit.clear()
            gamePrefEdit.commit()
        }
    }

    fun logout() {
        resetAll()
        val prefs = gamesActivity!!.getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        for (key in prefs.all.keys) {
            if (key.startsWith(AuthInfo.PREFIX.str)) {
                prefEdit.remove(key)
            }
        }
        if (authStatus == AuthStatus.GOOGLE) {
            googleSignInClient!!.signOut()
        }
        prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, false)
        prefEdit.commit()
        Request.stopWorkCycle()
        gamesActivity!!.recreate()
    }

    fun generateGamesMultCoeffs(prefEdit: SharedPreferences.Editor) {
        val undoCoeff = Random().nextInt(UndoPolicy.values().size)
        prefEdit.putInt(AuthInfo.UNDO_COEFF.str, undoCoeff)
        val timeCoeff = getByNormDist(1f, Constants.timeDeviation)
        prefEdit.putFloat(AuthInfo.TIME_COEFF.str, timeCoeff)
        val awardCoeff = getByNormDist(1f, Constants.awardDeviation)
        prefEdit.putFloat(AuthInfo.AWARD_COEFF.str, awardCoeff)
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
}