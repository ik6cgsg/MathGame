package spbpu.hsamcp.mathgame

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.lang.ref.WeakReference

class LevelsActivity: AppCompatActivity() {
    private val TAG = "LevelsActivity"
    private lateinit var levels: ArrayList<Level>
    private lateinit var levelViews: ArrayList<TextView>
    private lateinit var levelsList: LinearLayout
    private var currentLevelIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)
        MathScene.levelsActivity = WeakReference(this)
        val levelNames = assets.list("")!!
            .filter { """level\d+.json""".toRegex().matches(it) }
        levels = ArrayList()
        for (name in levelNames) {
            val loadedLevel = Level.create(name, assets)
            if (loadedLevel != null) {
                levels.add(loadedLevel)
            }
        }
        levels.sortBy { it.difficulty }
        window.decorView.setOnSystemUiVisibilityChangeListener { v: Int ->
            if ((v and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                AndroidUtil.makeFullScreen(window)
            }
        }
        levelViews = ArrayList()
        levelsList = findViewById(R.id.levels_list)
        generateList()
    }

    override fun onResume() {
        super.onResume()
        AndroidUtil.makeFullScreen(window)
    }

    fun getNextLevel(): Level {
        if (currentLevelIndex + 1 == levels.size) {
            return levels[currentLevelIndex]
        }
        return levels[++currentLevelIndex]
    }

    fun getPrevLevel(): Level {
        if (currentLevelIndex == 0) {
            return levels[0]
        }
        return levels[--currentLevelIndex]
    }

    fun updateResult() {
        levelViews[currentLevelIndex].text = levels[currentLevelIndex].name + "\n" +
            levels[currentLevelIndex].lastResult!!.award.str
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun generateList() {
        levels.forEachIndexed { i, level ->
            val levelView = createLevelView()
            levelView.text = level.name
            levelView.setOnTouchListener { v, event ->
                super.onTouchEvent(event)
                when {
                    event.action == MotionEvent.ACTION_DOWN -> {
                        v.background = getDrawable(R.drawable.rect_shape_clicked)
                    }
                    event.action == MotionEvent.ACTION_UP -> {
                        v.background = getDrawable(R.drawable.rect_shape)
                        if (AndroidUtil.touchUpInsideView(v, event)) {
                            MathScene.currentLevel = level
                            currentLevelIndex = i
                            startActivity(Intent(this, PlayActivity::class.java))
                        }
                    }
                }
                true
            }
            levelsList.addView(levelView)
            levelViews.add(levelView)
        }
    }

    private fun createLevelView(): TextView {
        val levelView = TextView(this)
        levelView.typeface = Typeface.MONOSPACE
        levelView.textSize = Constants.levelDefaultSize
        levelView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        levelView.setLineSpacing(0f, Constants.levelLineSpacing)
        levelView.setPadding(Constants.defaultPadding, Constants.defaultPadding * 2,
            Constants.defaultPadding, Constants.defaultPadding * 2)
        val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, Constants.defaultPadding, 0, Constants.defaultPadding)
        levelView.layoutParams = layoutParams
        levelView.background = getDrawable(R.drawable.rect_shape)
        levelView.setTextColor(Constants.textColor)
        return levelView
    }
}