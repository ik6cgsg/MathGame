package mathhelper.games.matify

import android.content.Intent
import android.os.Handler
import mathhelper.games.matify.activities.LevelsActivity
import mathhelper.games.matify.activities.PlayActivity
import mathhelper.games.matify.common.ConnectionChecker
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.game.GameResult
import mathhelper.games.matify.level.*
import java.lang.ref.WeakReference
import java.util.*

class LevelScene {
    companion object {
        private const val TAG = "LevelScene"
        val shared: LevelScene = LevelScene()
    }

    var levels: ArrayList<Level> = ArrayList()
    var levelsActivityRef: WeakReference<LevelsActivity> = WeakReference(null)
    fun setLA(la: LevelsActivity?) {
        levelsActivityRef = WeakReference(la)
        val curGame = GlobalScene.shared.currentGame
        if (la != null && curGame != null) {
            GlobalScene.shared.requestGameForPlay(curGame, success = {
                levels = curGame.levels
                levelsPassed = curGame.lastResult?.levelsPassed ?: 0
                levelsPaused = curGame.lastResult?.levelsPaused ?: 0
                la.onLevelsLoaded()
            }, error = {
                la.setLoading(false)
                Logger.e(TAG, "Error while LevelsActivity initializing")
                la.finish()
            })
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
                    val activity = PlayScene.shared.activityRef.get()
                    if (activity == null) {
                        levelsActivityRef.get()?.let { it.startActivity(Intent(it, PlayActivity::class.java)) }
                    } else {
                        (activity as PlayActivity).startCreatingLevelUI()
                    }
                }
                value < 0 -> {
                }
                value >= levels.size -> {
                }
            }
        }
    var levelsPassed: Int = 0
    var levelsPaused: Int = 0

    fun wasLevelPaused(): Boolean {
        return currentLevel?.endless == true && currentLevel?.lastResult?.state == StateType.PAUSED
    }

    fun nextLevel(): Boolean {
        if (currentLevelIndex + 1 == levels.size) {
            return false
        }
        currentLevelIndex++
        return true
    }

    fun updateGameResult() {
        val newRes = GameResult(levelsPassed, levelsPaused)
        if (GlobalScene.shared.currentGame?.lastResult != newRes) {
            GlobalScene.shared.gamesActivity?.updateResult(newRes)
        }
    }

    fun back() {
        GlobalScene.shared.currentGame?.rulePacks?.clear()
        GlobalScene.shared.currentGame?.levels?.clear()
        GlobalScene.shared.currentGame?.loaded = false
        levelsActivityRef.get()?.finish()
    }

    fun refreshGame() {
        if (!ConnectionChecker.shared.isConnected) return
        val activity = levelsActivityRef.get() ?: return
        GlobalScene.shared.requestGameForPlay(GlobalScene.shared.currentGame!!, forceRefresh = true, success = {
            levels = GlobalScene.shared.currentGame!!.levels
            levelsPassed = GlobalScene.shared.currentGame!!.lastResult?.levelsPassed ?: 0
            levelsPaused = GlobalScene.shared.currentGame!!.lastResult?.levelsPaused ?: 0
            activity.onLevelsLoaded()
        }, error = {
            activity.setLoading(false)
            Logger.e(TAG, "Error while LevelsActivity initializing")
            activity.finish()
        })
    }

    fun updateResult(result: LevelResult?) {
        levelsActivityRef.get()?.updateResult(result)
    }
}