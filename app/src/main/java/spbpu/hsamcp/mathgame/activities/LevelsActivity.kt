package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import spbpu.hsamcp.mathgame.level.Level
import spbpu.hsamcp.mathgame.MathScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AndroidUtil
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.level.LevelField
import spbpu.hsamcp.mathgame.level.UndoPolicy
import spbpu.hsamcp.mathgame.statistics.AuthInfo
import spbpu.hsamcp.mathgame.statistics.Request
import spbpu.hsamcp.mathgame.statistics.Statistics
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class LevelsActivity: AppCompatActivity() {
    private val TAG = "LevelsActivity"
    private lateinit var levels: ArrayList<Level>
    private lateinit var levelViews: ArrayList<TextView>
    private lateinit var levelsList: LinearLayout
    private var currentLevelIndex = -1
    private var levelTouched: View? = null
    private lateinit var resetDialog: AlertDialog
    private lateinit var signInDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)
        MathScene.levelsActivity = WeakReference(this)
        val levelNames = assets.list("")!!
            .filter { """level\d+.json""".toRegex().matches(it) }
        levels = ArrayList()
        for (name in levelNames) {
            val loadedLevel = Level.create(name, this)
            if (loadedLevel != null) {
                levels.add(loadedLevel)
            }
        }
        levels.sortBy { it.difficulty }
        levelViews = ArrayList()
        levelsList = findViewById(R.id.levels_list)
        generateList()
        resetDialog = createResetAlert()
        signInDialog = createSignAlert()
        Request.startWorkCycle()
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(Constants.storage, MODE_PRIVATE)
        if (!prefs.contains(Constants.deviceId)) {
            val prefEdit = prefs.edit()
            prefEdit.putString(Constants.deviceId, UUID.randomUUID().toString())
            prefEdit.commit()
        }
        if (!prefs.getBoolean(AuthInfo.AUTHORIZED.str, false)) {
            AndroidUtil.showDialog(signInDialog)
        }
    }

    fun reset(v: View?) {
        AndroidUtil.showDialog(resetDialog)
    }

    fun settings(v: View?) {
        startActivity(Intent(this, SettingsActivity::class.java))
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
        levelViews[currentLevelIndex].text = "${levels[currentLevelIndex].name}\n${levels[currentLevelIndex].lastResult!!}"
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun generateList() {
        levels.forEachIndexed { i, level ->
            val levelView = createLevelView()
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
                            MathScene.currentLevel = level
                            currentLevelIndex = i
                            startActivity(Intent(this, PlayActivity::class.java))
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

    private fun createLevelView(): TextView {
        val levelView = TextView(this)
        levelView.typeface = Typeface.MONOSPACE
        levelView.textSize = Constants.levelDefaultSize
        levelView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        levelView.setLineSpacing(0f, Constants.levelLineSpacing)
        levelView.setPadding(
            Constants.defaultPadding, Constants.defaultPadding * 2,
            Constants.defaultPadding, Constants.defaultPadding * 2)
        val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, Constants.defaultPadding, 0, Constants.defaultPadding)
        levelView.layoutParams = layoutParams
        levelView.setTextColor(Constants.textColor)
        return levelView
    }

    private fun getBackgroundByDif(dif: Int): Drawable? {
        return when {
            dif < 3 -> getDrawable(R.drawable.level_easy)
            dif < 5 -> getDrawable(R.drawable.level_medium)
            dif < 9 -> getDrawable(R.drawable.level_hard)
            else -> getDrawable(R.drawable.level_insane)
        }
    }

    private fun restartLevelsActivity() {
        levels.forEachIndexed{ i, lvl ->
            lvl.lastResult = null
            levelViews[i].text = lvl.name
        }
        AndroidUtil.showDialog(signInDialog)
    }

    private fun createResetAlert(): AlertDialog {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("ARE YOU SURE?")
            .setMessage("This action will reset all your achievements")
            .setPositiveButton("Yes \uD83D\uDE22") { dialog: DialogInterface, id: Int ->
                val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
                val prefEdit = prefs.edit()
                for (key in prefs.all.keys) {
                    if (key.startsWith(LevelField.RESULT.str) || key.startsWith(AuthInfo.PREFIX.str)) {
                      prefEdit.remove(key)
                    }
                }
                prefEdit.commit()
                restartLevelsActivity()
            }
            .setNegativeButton("Cancel ☺") { dialog: DialogInterface, id: Int ->
            }
            .setCancelable(true)
        return builder.create()
    }

    private fun createSignAlert(): AlertDialog {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        val view = layoutInflater.inflate(R.layout.dialog_sign_in, null)
        builder
            .setView(view)
            .setTitle("Please sign in")
            .setPositiveButton("Sign in") { dialog: DialogInterface, id: Int -> }
            .setNegativeButton("Exit") { dialog: DialogInterface, id: Int ->
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
                startActivity(homeIntent)
            }
            .setCancelable(false)
        val dialog = builder.create()
        setOnSignClick(dialog, view)
        return dialog
    }

    private fun setOnSignClick(dialog: AlertDialog, view: View) {
        val loginView = view.findViewById<EditText>(R.id.login)
        val nameView = view.findViewById<EditText>(R.id.name)
        val surnameView = view.findViewById<EditText>(R.id.surname)
        val secondNameView = view.findViewById<EditText>(R.id.secondName)
        val groupView = view.findViewById<EditText>(R.id.group)
        val institutionView = view.findViewById<EditText>(R.id.institution)
        val ageView = view.findViewById<EditText>(R.id.age)
        val statisticsView = view.findViewById<Switch>(R.id.statistics)
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                if (loginView.text.isBlank() || nameView.text.isBlank() ||
                    surnameView.text.isBlank()) {
                    Toast.makeText(this, "Please, fill required fields!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
                val prefEdit = prefs.edit()
                if (ageView.text.toString().isNotBlank()) {
                    try {
                        val age = ageView.text.toString().toInt()
                        if (age !in 10..100) {
                            Toast.makeText(this, "Appearances are deceptive...", Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        } else {
                            prefEdit.putInt(AuthInfo.AGE.str, age)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Are you sure that's your age (even not integer...)?", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                }
                prefEdit.putString(AuthInfo.LOGIN.str, loginView.text.toString())
                prefEdit.putString(AuthInfo.NAME.str, nameView.text.toString())
                prefEdit.putString(AuthInfo.SURNAME.str, surnameView.text.toString())
                prefEdit.putString(AuthInfo.SECOND_NAME.str, secondNameView.text.toString())
                prefEdit.putString(AuthInfo.GROUP.str, groupView.text.toString())
                prefEdit.putString(AuthInfo.INSTITUTION.str, institutionView.text.toString())
                prefEdit.putBoolean(AuthInfo.STATISTICS.str, statisticsView.isChecked)
                prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, true)
                generateLevelsMultCoeffs(prefEdit)
                prefEdit.commit()
                Statistics.logSign(this)
                resetLevelsCoeffs()
                dialog.dismiss()
            }
        }
    }

    private fun resetLevelsCoeffs() {
        for (level in levels) {
            level.coeffsSet = false
        }
    }

    private fun generateLevelsMultCoeffs(prefEdit: SharedPreferences.Editor) {
        val undoCoeff = Random().nextInt(UndoPolicy.values().size)
        prefEdit.putInt(AuthInfo.UNDO_COEFF.str, undoCoeff)
        val timeCoeff = getByNormDist(1f, Constants.timeDeviation)
        prefEdit.putFloat(AuthInfo.TIME_COEFF.str, timeCoeff)
        val awardCoeff = getByNormDist(1f, Constants.awardDeviation)
        prefEdit.putFloat(AuthInfo.AWARD_COEFF.str, awardCoeff)
    }

    private fun getByNormDist(mean: Float, deviation: Float): Float {
        var res: Float
        val left = mean - deviation
        val right = mean + deviation
        do {
            res = Random().nextGaussian().toFloat() * deviation + mean
        } while (res !in left..right)
        return res
    }
}