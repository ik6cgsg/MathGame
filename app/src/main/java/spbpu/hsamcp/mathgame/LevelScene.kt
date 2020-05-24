package spbpu.hsamcp.mathgame

import android.content.Intent
import android.view.View
import com.twf.expressiontree.ExpressionSubstitution
import android.graphics.Color
import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import com.twf.api.expressionToString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import spbpu.hsamcp.mathgame.activities.LevelsActivity
import spbpu.hsamcp.mathgame.activities.PlayActivity
import spbpu.hsamcp.mathgame.common.RuleMathView
import spbpu.hsamcp.mathgame.level.*
import spbpu.hsamcp.mathgame.mathResolver.MathResolver
import spbpu.hsamcp.mathgame.mathResolver.TaskType
import spbpu.hsamcp.mathgame.statistics.Statistics
import java.lang.ref.WeakReference
import java.util.*

class LevelScene {
    companion object {
        private const val TAG = "LevelScene"
        val shared: LevelScene = LevelScene()
    }

    var levels: ArrayList<Level> = ArrayList()
    var levelsActivity: LevelsActivity? = null
        set(value) {
            field = value
            if (value != null) {
                GlobalScope.launch {
                    val job = async {
                        val loaded = GlobalScene.shared.currentGame!!.load(value)
                        value.runOnUiThread {
                            if (loaded) {
                                levels = GlobalScene.shared.currentGame!!.levels
                                value.onLevelsLoaded()
                            }
                        }
                    }
                    job.await()
                }
            }
        }
    var currentLevel: Level? = null
        private set
    var currentLevelIndex = 0
        set(value) {
            when {
                value >= 0 && value < levels.size -> {
                    field = value
                    currentLevel = levels[value]
                    if (PlayScene.shared.playActivity == null) {
                        levelsActivity?.startActivity(Intent(levelsActivity, PlayActivity::class.java))
                    } else {
                        PlayScene.shared.playActivity!!.startCreatingLevelUI()
                    }
                }
                value < 0 -> {
                }
                value >= levels.size -> {
                }
            }
        }

    fun wasLevelPaused(): Boolean {
        return currentLevel!!.endless && (currentLevel!!.lastResult != null &&
            currentLevel!!.lastResult!!.award.value == AwardType.PAUSED)
    }

    fun nextLevel(): Boolean {
        if (currentLevelIndex + 1 == levels.size) {
            return false
        }
        currentLevelIndex++
        return true
    }

    fun back() {
        GlobalScene.shared.currentGame?.rulePacks?.clear()
        GlobalScene.shared.currentGame?.levels?.clear()
        GlobalScene.shared.currentGame?.loaded = false
        levelsActivity!!.finish()
    }
}