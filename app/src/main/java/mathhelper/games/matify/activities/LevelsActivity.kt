package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import mathhelper.games.matify.LevelScene
import mathhelper.games.matify.R
import mathhelper.games.matify.common.AndroidUtil
import kotlin.collections.ArrayList

class LevelsActivity: AppCompatActivity() {
    private val TAG = "LevelsActivity"
    var loading = false
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
        if (!loading) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    fun updateResult() {
        val i = LevelScene.shared.currentLevelIndex
        levelViews[i].text = "${LevelScene.shared.levels[i].getNameByLanguage(resources.configuration.locale.language)}" +
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
            levelView.text = level.getNameByLanguage(resources.configuration.locale.language)
            if (level.lastResult != null) {
                levelView.text = "${level.getNameByLanguage(resources.configuration.locale.language)}\n${level.lastResult!!}"
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
                            LevelScene.shared.currentLevelIndex = i
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
}