package spbpu.hsamcp.mathgame.activities

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BulletSpan
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
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
    private var scaleListener = MathScaleListener()
    private lateinit var scaleDetector: ScaleGestureDetector

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
        setOnTouchUpInside(res, ::restart)
        val prev = findViewById<TextView>(R.id.previous)
        setOnTouchUpInside(prev, ::previous)
        val back = findViewById<TextView>(R.id.back)
        setOnTouchUpInside(back, ::back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        scaleDetector = ScaleGestureDetector(this, scaleListener)
        setViews()
        MathScene.init(this)
        MathScene.loadLevel()
        window.decorView.setOnSystemUiVisibilityChangeListener { v: Int ->
            if ((v and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                AndroidUtil.makeFullScreen(window)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        back(null)
    }

    override fun onResume() {
        super.onResume()
        AndroidUtil.makeFullScreen(window)
    }

    private fun previous(v: View?) {
        MathScene.previousStep()
    }

    private fun restart(v: View?) {
        scale = 1f
        MathScene.restart()
    }

    private fun back(v: View?) {
        MathScene.menu()
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

    fun onWin(stepsCount: Int, currentTime: Long, award: Award) {
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
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Congratulations!")
            .setMessage(spannable)
            .setPositiveButton("Next") { dialog: DialogInterface, id: Int ->
                scale = 1f
                MathScene.nextLevel()
            }
            .setNeutralButton("Menu") { dialog: DialogInterface, id: Int ->
                back(null)
            }
            .setNegativeButton("Previous") { dialog: DialogInterface, id: Int ->
                scale = 1f
                MathScene.prevLevel()
            }
        showDialog(builder)
    }

    fun onLoose() {
        Log.d(TAG, "onLoose")
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
        showDialog(builder)
    }

    private fun showDialog(builder: AlertDialog.Builder) {
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
        AndroidUtil.makeFullScreen(dialog.window!!)
        dialog.window!!.setBackgroundDrawableResource(R.color.gray)
        dialog.window!!.findViewById<TextView>(android.R.id.message).typeface = Typeface.MONOSPACE
    }

    private fun setOnTouchUpInside(view: View, func: (v: View?) -> Unit) {
        view.setOnTouchListener { v, event ->
            val tv = v as TextView
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    tv.setTextColor(Constants.primaryColor)
                }
                MotionEvent.ACTION_UP -> {
                    tv.setTextColor(Constants.textColor)
                    if (AndroidUtil.touchUpInsideView(v, event)) {
                        func(v)
                    }
                }
            }
            true
        }
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
