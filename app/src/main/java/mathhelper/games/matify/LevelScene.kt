package mathhelper.games.matify

import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mathhelper.games.matify.activities.LevelsActivity
import mathhelper.games.matify.activities.PlayActivity
import mathhelper.games.matify.level.*
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
                        val loaded = GlobalScene.shared.currentGame?.load(value) ?: false
                        value.runOnUiThread {
                            if (loaded) {
                                levels = GlobalScene.shared.currentGame!!.levels
                                value.onLevelsLoaded()
                            } else {
                                value.loading = false
                                Log.e(TAG, "Error while LevelsActivity initializing")
                                Toast.makeText(value, "Something went wrong, please retry", Toast.LENGTH_LONG).show()
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
                    Handler().postDelayed({
                        if (PlayScene.shared.playActivity == null) {
                            levelsActivity?.startActivity(Intent(levelsActivity, PlayActivity::class.java))
                        } else {
                            PlayScene.shared.playActivity!!.startCreatingLevelUI()
                        }
                    }, 100)
                }
                value < 0 -> {
                }
                value >= levels.size -> {
                }
            }
        }

    fun wasLevelPaused(): Boolean {
        return currentLevel?.endless == true && currentLevel?.lastResult?.award?.value == AwardType.PAUSED
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
        levelsActivity?.finish()
    }
}