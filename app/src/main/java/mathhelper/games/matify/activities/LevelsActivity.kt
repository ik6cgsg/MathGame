package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import eightbitlab.com.blurview.BlurView
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.LevelScene
import mathhelper.games.matify.R
import mathhelper.games.matify.common.*
import mathhelper.games.matify.level.Level
import mathhelper.games.matify.level.LevelResult
import mathhelper.games.matify.level.StateType
import org.w3c.dom.Text
import kotlin.collections.ArrayList

class LevelsActivity: AppCompatActivity() {
    private val TAG = "LevelsActivity"
    var loading = false
    private lateinit var levelViews: ArrayList<TextView>
    private lateinit var levelsList: LinearLayout
    private lateinit var progress: ProgressBar
    lateinit var blurView: BlurView

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(ThemeController.shared.currentTheme.resId)
        setContentView(R.layout.activity_levels)
        progress = findViewById(R.id.progress)
        progress.visibility = View.VISIBLE
        loading = true
        if (Build.VERSION.SDK_INT < 24) {
            val settings = findViewById<TextView>(R.id.settings)
            settings.text = "\uD83D\uDD27"
        }
        blurView = findViewById(R.id.blurView)
        levelViews = ArrayList()
        levelsList = findViewById(R.id.levels_list)
        val title = findViewById<TextView>(R.id.levels)
        title.text = GlobalScene.shared.currentGame?.getNameByLanguage(resources.configuration.locale.language) ?: title.text
        LevelScene.shared.levelsActivity = this
    }

    override fun onResume() {
        super.onResume()
        if (GlobalScene.shared.currentGame == null) {
            finishAffinity()
            startActivity(Intent(this, SplashActivity::class.java))
        }
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

    fun updateResult(result: LevelResult?) {
        val i = LevelScene.shared.currentLevelIndex
        val oldRes = LevelScene.shared.currentLevel?.lastResult
        levelViews[i].text = "${LevelScene.shared.currentLevel?.getNameByLanguage(resources.configuration.locale.language)}" +
            if (result != null) {
                "\n${result}"
            } else {
                ""
            }
        when (result?.state) {
            StateType.DONE -> {
                when (oldRes?.state) {
                    StateType.NOT_STARTED, null -> LevelScene.shared.levelsPassed += 1
                    StateType.PAUSED -> {
                        LevelScene.shared.levelsPassed += 1
                        LevelScene.shared.levelsPaused -= 1
                    }
                }
            }
            StateType.PAUSED -> {
                when (oldRes?.state) {
                    StateType.NOT_STARTED, null -> LevelScene.shared.levelsPaused += 1
                    StateType.DONE -> {
                        LevelScene.shared.levelsPaused += 1
                        LevelScene.shared.levelsPassed -= 1
                    }
                }
            }
            StateType.NOT_STARTED, null -> {
                when (oldRes?.state) {
                    StateType.DONE -> LevelScene.shared.levelsPassed -= 1
                    StateType.PAUSED -> LevelScene.shared.levelsPaused -= 1
                }
            }
        }
        AndroidUtil.setLeftDrawable(levelViews[i], AndroidUtil.getDrawableByLevelState(this, result?.state))
        LevelScene.shared.currentLevel?.save(this, result)
        LevelScene.shared.updateGameResult()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun generateList() {
        LevelScene.shared.levels.forEachIndexed { i, level ->
            val levelView = AndroidUtil.createButtonView(this)
            levelView.text = level.getNameByLanguage(resources.configuration.locale.language)
            if (level.lastResult != null) {
                levelView.text = "${level.getNameByLanguage(resources.configuration.locale.language)}\n${level.lastResult!!}"
            }
            val themeName = Storage.shared.theme()
            levelView.setTextColor(ThemeController.shared.color(ColorName.TEXT_COLOR))
            levelView.background = getDrawable(R.drawable.button_rect)
            levelView.setOnClickListener {
                LevelScene.shared.currentLevelIndex = i
            }
            levelView.isLongClickable = true
            levelView.setOnLongClickListener { showInfo(level) }
            AndroidUtil.setLeftDrawable(levelView, AndroidUtil.getDrawableByLevelState(this, level.lastResult?.state))
            /*levelView.background = getBackgroundByDif(level.difficulty)
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
            }*/
            levelsList.addView(levelView)
            levelViews.add(levelView)
        }
    }

    private fun showInfo(lvl: Level): Boolean{
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle("Level Info")
            .setMessage("Name: ${lvl.nameEn}")
            .setCancelable(true)
        val alert = builder.create()
        AndroidUtil.showDialog(alert, backMode = BackgroundMode.BLUR, blurView = blurView, activity = this)
        return true
    }

    private fun getBackgroundByDif(dif: Double): Drawable? {
        return when {
            dif < 3 -> getDrawable(R.drawable.level_easy)
            dif < 5 -> getDrawable(R.drawable.level_medium)
            dif < 9 -> getDrawable(R.drawable.level_hard)
            else -> getDrawable(R.drawable.level_insane)
        }
    }
}