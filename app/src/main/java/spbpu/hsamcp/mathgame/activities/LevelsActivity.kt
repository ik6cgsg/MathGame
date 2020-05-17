package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import spbpu.hsamcp.mathgame.level.Level
import spbpu.hsamcp.mathgame.LevelScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.TutorialScene
import spbpu.hsamcp.mathgame.common.AndroidUtil
import spbpu.hsamcp.mathgame.common.Constants
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList

class LevelsActivity: AppCompatActivity() {
    private val TAG = "LevelsActivity"
    private var loading = false
    private lateinit var levelViews: ArrayList<TextView>
    private lateinit var levelsList: LinearLayout
    private var levelTouched: View? = null
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)
        progress = findViewById(R.id.progress)
        progress.visibility = View.VISIBLE
        loading = true
        if (Build.VERSION.SDK_INT < 24) {
            val settings = findViewById<TextView>(R.id.settings)
            settings.text = "\uD83D\uDD27"
        }
        levelViews = ArrayList()
        levelsList = findViewById(R.id.levels_list)
        LevelScene.shared.levelsActivity = this
    }

    fun onLevelsLoaded() {
        progress.visibility = View.GONE
        loading = false
        generateList()
    }

    override fun onBackPressed() {
        if (!loading) {
            back(null)
        }
    }

    override fun finish() {
        LevelScene.shared.levelsActivity = null
        super.finish()
    }

    fun back(v: View?) {
        if (!loading) {
            LevelScene.shared.back()
        }
    }

    fun settings(v: View?) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    fun updateResult() {
        val i = LevelScene.shared.currentLevelIndex
        levelViews[i].text = "${LevelScene.shared.levels[i].name}" +
            if (LevelScene.shared.levels[i].lastResult != null) {
                "\n${LevelScene.shared.levels[i].lastResult}"
            } else {
                ""
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun generateList() {
        LevelScene.shared.levels.forEachIndexed { i, level ->
            val levelView = AndroidUtil.createButtonView(this)
            levelView.text = level.name
            if (level.lastResult != null) {
                levelView.text = "${level.name}\n${level.lastResult!!}"
            }
            levelView.background = getBackgroundByDif(level.difficulty)
            levelView.setOnTouchListener { v, event ->
                super.onTouchEvent(event)
                when {
                    event.action == MotionEvent.ACTION_DOWN && levelTouched == null -> {
                        levelTouched = v
                        v.background = getDrawable(R.drawable.rect_shape_clicked)
                    }
                    event.action == MotionEvent.ACTION_UP && levelTouched == v -> {
                        v.background = getBackgroundByDif(level.difficulty)
                        if (AndroidUtil.touchUpInsideView(v, event)) {
                            if (level.levelCode == "Tutorial") {
                                TutorialScene.start(this, level)
                            } else {
                                LevelScene.shared.currentLevelIndex = i
                            }
                        }
                        levelTouched = null
                    }
                    event.action == MotionEvent.ACTION_CANCEL && levelTouched == v -> {
                        v.background = getBackgroundByDif(level.difficulty)
                        levelTouched = null
                    }
                }
                true
            }
            levelsList.addView(levelView)
            levelViews.add(levelView)
        }
    }

    private fun getBackgroundByDif(dif: Float): Drawable? {
        return when {
            dif < 3 -> getDrawable(R.drawable.level_easy)
            dif < 5 -> getDrawable(R.drawable.level_medium)
            dif < 9 -> getDrawable(R.drawable.level_hard)
            else -> getDrawable(R.drawable.level_insane)
        }
    }

    /*
    private fun restartLevelsActivity() {
        levels.forEachIndexed{ i, lvl ->
            lvl.lastResult = null
            levelViews[i].text = lvl.name
        }
        AndroidUtil.showDialog(signInDialog)
    }

    private fun resetLevelsCoeffs() {
        for (level in levels) {
            level.coeffsSet = false
        }
    }
     */
}