package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
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
import spbpu.hsamcp.mathgame.statistics.AuthInfo
import spbpu.hsamcp.mathgame.statistics.Statistics
import java.lang.Exception
import java.lang.ref.WeakReference

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
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(Constants.storage, MODE_PRIVATE)
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
        levelViews[currentLevelIndex].text = levels[currentLevelIndex].name + "\n" +
            levels[currentLevelIndex].lastResult!!.award.value.str
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun generateList() {
        levels.forEachIndexed { i, level ->
            val levelView = createLevelView()
            levelView.text = level.name
            if (level.lastResult != null) {
                levelView.text = level.name + "\n" + level.lastResult!!.award.value.str
            }
            levelView.setOnTouchListener { v, event ->
                super.onTouchEvent(event)
                when {
                    event.action == MotionEvent.ACTION_DOWN && levelTouched == null -> {
                        levelTouched = v
                        v.background = getDrawable(R.drawable.rect_shape_clicked)
                    }
                    event.action == MotionEvent.ACTION_UP && levelTouched == v -> {
                        v.background = getDrawable(R.drawable.rect_shape)
                        if (AndroidUtil.touchUpInsideView(v, event)) {
                            MathScene.currentLevel = level
                            currentLevelIndex = i
                            startActivity(Intent(this, PlayActivity::class.java))
                        }
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
        levelView.background = getDrawable(R.drawable.rect_shape)
        levelView.setTextColor(Constants.textColor)
        return levelView
    }

    private fun createResetAlert(): AlertDialog {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("ARE YOU SURE?")
            .setMessage("This action will reset all your achievements")
            .setPositiveButton("Yes \uD83D\uDE22") { dialog: DialogInterface, id: Int ->
                val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
                val prefEdit = prefs.edit()
                /*
                for (key in prefs.all.keys) {
                    if (key.startsWith(LevelField.RESULT.str)) {
                      prefEdit.remove(key)
                    }
                }
                */
                prefEdit.clear()
                prefEdit.commit()
                recreate()
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
        val loginView = view.findViewById(R.id.login) as EditText
        val nameView = view.findViewById(R.id.name) as EditText
        val surnameView = view.findViewById(R.id.surname) as EditText
        val secondNameView = view.findViewById(R.id.secondName) as EditText
        val groupView = view.findViewById(R.id.group) as EditText
        val institutionView = view.findViewById(R.id.institution) as EditText
        val ageView = view.findViewById(R.id.age) as EditText
        val statisticsView = view.findViewById(R.id.statistics) as Switch
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                if (loginView.text.isNotBlank() && nameView.text.isNotBlank() &&
                    surnameView.text.isNotBlank()) {
                    try {
                        val age = ageView.text.toString().toInt()
                        if (age in 10..100) {
                            val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
                            val prefEdit = prefs.edit()
                            prefEdit.putString(AuthInfo.LOGIN.str, loginView.text.toString())
                            prefEdit.putString(AuthInfo.NAME.str, nameView.text.toString())
                            prefEdit.putString(AuthInfo.SURNAME.str, surnameView.text.toString())
                            prefEdit.putString(AuthInfo.SECOND_NAME.str, secondNameView.text.toString())
                            prefEdit.putString(AuthInfo.GROUP.str, groupView.text.toString())
                            prefEdit.putString(AuthInfo.INSTITUTION.str, institutionView.text.toString())
                            prefEdit.putInt(AuthInfo.AGE.str, ageView.text.toString().toInt())
                            prefEdit.putBoolean(AuthInfo.STATISTICS.str, statisticsView.isChecked)
                            prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, true)
                            prefEdit.commit()
                            Statistics.logSign(this)
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this, "Appearances are deceptive...", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Are you sure that's your age (even not integer...)?", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Please, fill required fields!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}