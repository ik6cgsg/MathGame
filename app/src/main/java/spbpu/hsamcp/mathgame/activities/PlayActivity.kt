package spbpu.hsamcp.mathgame.activities

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BulletSpan
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import spbpu.hsamcp.mathgame.level.Award
import spbpu.hsamcp.mathgame.common.GlobalMathView
import spbpu.hsamcp.mathgame.MathScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AndroidUtil
import spbpu.hsamcp.mathgame.common.Constants
import kotlin.math.max
import kotlin.math.min

class PlayActivity: AppCompatActivity() {
    private val TAG = "PlayActivity"
    private var scale = 1.0f
    private var needClear = false
    private var loading = false
    private var scaleListener = MathScaleListener()
    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var looseDialog: AlertDialog
    private lateinit var winDialog: AlertDialog
    private lateinit var backDialog: AlertDialog
    private lateinit var continueDialog: AlertDialog
    private lateinit var progress: ProgressBar

    lateinit var globalMathView: GlobalMathView
    lateinit var endFormulaView: TextView
    lateinit var endFormulaViewLabel: TextView
    lateinit var messageView: TextView
    lateinit var rulesLinearLayout: LinearLayout
    lateinit var rulesScrollView: ScrollView
    lateinit var noRules: TextView
    lateinit var timerView: TextView

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent")
        scaleDetector.onTouchEvent(event)
        when {
            event.action == MotionEvent.ACTION_DOWN -> {
                needClear = true
            }
            event.action == MotionEvent.ACTION_UP -> {
                if (needClear) {
                    globalMathView.clearFormula()
                    MathScene.clearRules()
                }
            }
        }
        return true
    }

    private fun setViews() {
        globalMathView = findViewById(R.id.global_formula)
        endFormulaView = findViewById(R.id.end_formula_view)
        endFormulaViewLabel = findViewById(R.id.end_formula_label)
        messageView = findViewById(R.id.message_view)
        rulesLinearLayout = findViewById(R.id.rules_linear_layout)
        rulesScrollView = findViewById(R.id.rules_scroll_view)
        noRules = findViewById(R.id.no_rules)
        timerView = findViewById(R.id.timer_view)
        val res = findViewById<TextView>(R.id.restart)
        AndroidUtil.setOnTouchUpInside(res, ::restart)
        val prev = findViewById<TextView>(R.id.previous)
        AndroidUtil.setOnTouchUpInside(prev, ::previous)
        val back = findViewById<TextView>(R.id.back)
        AndroidUtil.setOnTouchUpInside(back, ::back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        scaleDetector = ScaleGestureDetector(this, scaleListener)
        setViews()
        MathScene.init(this)
        progress = findViewById(R.id.progress)
        looseDialog = createLooseDialog()
        winDialog = createWinDialog()
        backDialog = createBackDialog()
        continueDialog = createContinueDialog()
        startCreatingLevelUI()
    }

    override fun onBackPressed() {
        if (!loading) {
            back(null)
        }
    }

    fun startCreatingLevelUI() {
        if (MathScene.wasLevelPaused()) {
            AndroidUtil.showDialog(continueDialog)
        } else {
            createLevelUI(false)
        }
    }

    private fun createLevelUI(continueGame: Boolean) {
        loading = true
        timerView.text = ""
        globalMathView.text = ""
        endFormulaView.text = ""
        progress.visibility = View.VISIBLE
        GlobalScope.launch {
            val job = async {
                MathScene.preLoad()
                runOnUiThread {
                    MathScene.loadLevel(continueGame)
                    progress.visibility = View.GONE
                    loading = false
                }
            }
            job.await()
        }
    }

    private fun previous(v: View?) {
        if (!loading) {
            MathScene.previousStep()
        }
    }

    private fun restart(v: View?) {
        if (!loading) {
            scale = 1f
            MathScene.restart()
        }
    }

    private fun back(v: View?) {
        if (!loading) {
            if (MathScene.currentLevel!!.endless) {
                AndroidUtil.showDialog(backDialog)
            } else {
                returnToMenu(false)
            }
        }
    }

    private fun returnToMenu(save: Boolean) {
        MathScene.menu(save)
        finish()
    }

    fun showEndFormula(v: View?) {
        if (endFormulaView.visibility == View.GONE) {
            endFormulaViewLabel.text = getString(R.string.end_formula_opened)
            endFormulaView.visibility = View.VISIBLE
        } else {
            endFormulaViewLabel.text = getString(R.string.end_formula_closed)
            endFormulaView.visibility = View.GONE
        }
    }

    fun endFormulaHide(): Boolean {
        return endFormulaView.visibility != View.VISIBLE
    }

    fun onWin(stepsCount: Float, currentTime: Long, award: Award) {
        Log.d(TAG, "onWin")
        val msgTitle = "You finished level with:"
        val steps = "\n\tSteps: $stepsCount"
        val time = "\n\tTime: $currentTime"
        val spannable = SpannableString(msgTitle + steps + time + "\n\nAWARD: ${award.value.str}")
        spannable.setSpan(BulletSpan(5, Constants.primaryColor), msgTitle.length + 1,
            msgTitle.length + steps.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(BulletSpan(5, Constants.primaryColor),
            msgTitle.length + steps.length + 1, msgTitle.length + steps.length + time.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        winDialog.setMessage(spannable)
        AndroidUtil.showDialog(winDialog)
    }

    fun onLoose() {
        AndroidUtil.showDialog(looseDialog)
    }

    private fun createWinDialog(): AlertDialog {
        Log.d(TAG, "createWinDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Congratulations!")
            .setMessage("")
            .setPositiveButton("Next") { dialog: DialogInterface, id: Int -> }
            .setNeutralButton("Menu") { dialog: DialogInterface, id: Int ->
                MathScene.menu(false)
                finish()
            }
            .setNegativeButton("Previous") { dialog: DialogInterface, id: Int -> }
            .setCancelable(false)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            okButton.setOnClickListener {
                scale = 1f
                if (!MathScene.nextLevel()) {
                    Toast.makeText(this, "Sorry, that's last level!", Toast.LENGTH_SHORT).show()
                } else {
                    dialog.dismiss()
                }
            }
            negButton.setOnClickListener {
                scale = 1f
                if (!MathScene.prevLevel()) {
                    Toast.makeText(this, "Sorry, no negative levels!", Toast.LENGTH_SHORT).show()
                } else {
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    private fun createLooseDialog(): AlertDialog {
        Log.d(TAG, "createLooseDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Time out!")
            .setMessage("May be next time?")
            .setPositiveButton("Restart") { dialog: DialogInterface, id: Int ->
                restart(null)
            }
            .setNegativeButton("Menu") { dialog: DialogInterface, id: Int ->
                back(null)
            }
            .setCancelable(false)
        return builder.create()
    }

    private fun createBackDialog(): AlertDialog {
        Log.d(TAG, "createBackDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Attention!")
            .setMessage("Save your current state?")
            .setPositiveButton("Yes") { dialog: DialogInterface, id: Int ->
                returnToMenu(true)
            }
            .setNegativeButton("No") { dialog: DialogInterface, id: Int ->
                returnToMenu(false)
            }
            .setNeutralButton("Cancel") { dialog: DialogInterface, id: Int -> }
            .setCancelable(false)
        return builder.create()
    }

    private fun createContinueDialog(): AlertDialog {
        Log.d(TAG, "createContinueDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Welcome back!")
            .setMessage("Continue from where you stopped?")
            .setPositiveButton("Yes") { dialog: DialogInterface, id: Int ->
                createLevelUI(true)
            }
            .setNegativeButton("No") { dialog: DialogInterface, id: Int ->
                createLevelUI(false)
            }
            .setCancelable(false)
        return builder.create()
    }

    inner class MathScaleListener: ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            needClear = false
            scale *= detector.scaleFactor
            scale = max(
                Constants.ruleDefaultSize / Constants.centralFormulaDefaultSize,
                min(scale, Constants.centralFormulaMaxSize / Constants.centralFormulaDefaultSize))
            globalMathView.textSize = Constants.centralFormulaDefaultSize * scale
            return true
        }
    }
}
