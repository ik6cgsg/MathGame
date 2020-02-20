package spbpu.hsamcp.mathgame

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Point
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BulletSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.w3c.dom.Text
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class PlayActivity : AppCompatActivity() {
    private val TAG = "PlayActivity"
    private var mRatio = 1.0f
    private var mBaseDist: Int = 0
    private var mBaseRatio: Float = 0.toFloat()
    private val step = 400f
    private lateinit var levels: List<String>
    private var currentLevel = 0
    private var needClear = false

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
        when {
            event.pointerCount == 2 -> {
                needClear = false
                val action = event.action
                val pureaction = action and MotionEvent.ACTION_MASK
                if (pureaction == MotionEvent.ACTION_POINTER_DOWN) {
                    mBaseDist = getDistance(event)
                    mBaseRatio = mRatio
                } else {
                    val delta = (getDistance(event) - mBaseDist) / step
                    val multi = 2.0.pow(delta.toDouble()).toFloat()
                    mRatio = min(globalMathView.maxSize, max(0.1f, mBaseRatio * multi))
                    globalMathView.textSize = mRatio + globalMathView.defaultSize
                }
            }
            event.pointerCount == 1 -> {
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
            }
        }
        return true
    }

    private fun getDistance(event: MotionEvent): Int {
        Log.d(TAG, "getDistance")
        val dx = (event.getX(0) - event.getX(1)).toInt()
        val dy = (event.getY(0) - event.getY(1)).toInt()
        return sqrt((dx * dx + dy * dy).toDouble()).toInt()
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
        setViews()
        MathScene.init(this)
        levels = assets.list("")!!
            .filter { """level\d+.json""".toRegex().matches(it) }
            .sorted()
        MathScene.loadLevel("level3.json")
        window.decorView.setOnSystemUiVisibilityChangeListener { v: Int ->
            if ((v and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                makeFullScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        makeFullScreen()
    }

    private fun makeFullScreen() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private fun previous(v: View?) {
        MathScene.previousStep()
    }

    private fun restart(v: View?) {
        mRatio = 1f
        MathScene.timer.cancel()
        MathScene.loadLevel(MathScene.currentLevel!!.fileName)
    }

    private fun back(v: View?) {
        // TODO:
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

    fun onWin(stepsCount: Int, currentTime: Long, award: String) {
        Log.d(TAG, "onWin")
        val msgTitle = "You finished level with:"
        val steps = "\n\tSteps: $stepsCount"
        val time = "\n\tTime: $currentTime"
        val spannable = SpannableString(msgTitle + steps + time + "\n\nAWARD: $award")
        spannable.setSpan(BulletSpan(5, Color.parseColor("#008577")), msgTitle.length + 1,
            msgTitle.length + steps.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(BulletSpan(5, Color.parseColor("#008577")),
            msgTitle.length + steps.length + 1, msgTitle.length + steps.length + time.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Congratulations!")
            .setMessage(spannable)
            .setPositiveButton("Next") { dialog: DialogInterface, id: Int ->
                mRatio = 1f
                if (currentLevel == levels.size - 1) {
                    MathScene.loadLevel(MathScene.currentLevel!!.fileName)
                } else {
                    currentLevel++
                    MathScene.loadLevel(levels[currentLevel])
                }
            }
            .setNeutralButton("Menu") { dialog: DialogInterface, id: Int ->
                // TODO:
                finish()
            }
            .setNegativeButton("Previous") { dialog: DialogInterface, id: Int ->
                mRatio = 1f
                if (currentLevel == 0) {
                    MathScene.loadLevel(MathScene.currentLevel!!.fileName)
                } else {
                    currentLevel--
                    MathScene.loadLevel(levels[currentLevel])
                }
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
                // TODO:
                finish()
            }
        showDialog(builder)
    }

    private fun showDialog(builder: AlertDialog.Builder) {
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
        dialog.window!!.setBackgroundDrawableResource(R.color.gray)
        dialog.window!!.findViewById<TextView>(android.R.id.message).typeface = Typeface.MONOSPACE
    }

    private fun setOnTouchUpInside(view: View, func: (v: View?) -> Unit) {
        view.setOnTouchListener { v, event ->
            val tv = v as TextView
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    tv.setTextColor(Color.parseColor("#008577"))
                }
                MotionEvent.ACTION_UP -> {
                    tv.setTextColor(Color.parseColor("#C5C5C5"))
                    if (v.left + event.x >= v.left && v.left + event.x <= v.right &&
                        v.top + event.y >= v.top && v.top + event.y <= v.bottom) {
                        func(v)
                    }
                }
            }
            true
        }
    }
}
