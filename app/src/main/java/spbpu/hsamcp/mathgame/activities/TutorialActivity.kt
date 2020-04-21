package spbpu.hsamcp.mathgame.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import spbpu.hsamcp.mathgame.common.GlobalMathView
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.TutorialScene
import spbpu.hsamcp.mathgame.common.AndroidUtil
import spbpu.hsamcp.mathgame.common.Constants
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

class TutorialActivity: AppCompatActivity() {
    private val TAG = "TutorialActivity"
    private var scale = 1.0f
    private var needClear = false
    private var loading = false
    private var scaleListener = MathScaleListener()
    private var currentAnim: AnimatorSet? = null
    private var currentAnimView: View? = null
    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var leaveDialog: AlertDialog
    private lateinit var progress: ProgressBar

    lateinit var globalMathView: GlobalMathView
    lateinit var endFormulaView: TextView
    lateinit var endFormulaViewLabel: TextView
    lateinit var messageView: TextView
    lateinit var rulesLinearLayout: LinearLayout
    lateinit var rulesScrollView: ScrollView
    lateinit var noRules: TextView
    lateinit var timerView: TextView
    lateinit var pointerMsgView: TextView
    lateinit var pointerEndView: TextView
    lateinit var pointerCentralView: TextView
    lateinit var pointerBackView: TextView
    lateinit var pointerRestartView: TextView
    lateinit var pointerUndoView: TextView

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
                    TutorialScene.clearRules()
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
        pointerMsgView = findViewById(R.id.pointer_message)
        pointerEndView = findViewById(R.id.pointer_end)
        pointerCentralView = findViewById(R.id.pointer_central)
        pointerBackView = findViewById(R.id.pointer_back)
        pointerRestartView = findViewById(R.id.pointer_restart)
        pointerUndoView = findViewById(R.id.pointer_undo)
        val res = findViewById<TextView>(R.id.restart)
        //AndroidUtil.setOnTouchUpInside(res, ::restart)
        val prev = findViewById<TextView>(R.id.previous)
        //AndroidUtil.setOnTouchUpInside(prev, ::previous)
        val back = findViewById<TextView>(R.id.back)
        AndroidUtil.setOnTouchUpInside(back, ::back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        scaleDetector = ScaleGestureDetector(this, scaleListener)
        setViews()
        TutorialScene.tutorialActivity = WeakReference(this)
        progress = findViewById(R.id.progress)
        leaveDialog = createLeaveDialog()
        createLevelUI()
    }

    override fun onBackPressed() {
        back(null)
    }

    fun back(v: View?) {
        AndroidUtil.showDialog(leaveDialog)
    }

    private fun createLevelUI() {
        loading = true
        timerView.text = "⏰ 1:23"
        globalMathView.text = ""
        endFormulaView.text = ""
        progress.visibility = View.VISIBLE
        startDialog()
        GlobalScope.launch {
            val job = async {
                TutorialScene.preLoad()
                runOnUiThread {
                    TutorialScene.loadLevel()
                    progress.visibility = View.GONE
                    loading = false
                }
            }
            job.await()
        }
    }

    private fun animateLeftUp(view: View) {
        val animationX = ObjectAnimator.ofFloat(view, "translationX", -30f)
        animationX.repeatMode = ValueAnimator.REVERSE
        animationX.repeatCount = ValueAnimator.INFINITE
        val animationY = ObjectAnimator.ofFloat(view, "translationY", -30f)
        animationY.repeatMode = ValueAnimator.REVERSE
        animationY.repeatCount = ValueAnimator.INFINITE
        val set = AnimatorSet()
        set.play(animationX)
            .with(animationY)
        set.duration = 600
        set.start()
        currentAnim = set
        currentAnimView = view
    }

    private fun animateUp(view: View) {
        val animationY = ObjectAnimator.ofFloat(view, "translationY", -30f)
        animationY.repeatMode = ValueAnimator.REVERSE
        animationY.repeatCount = ValueAnimator.INFINITE
        val set = AnimatorSet()
        set.play(animationY)
        set.duration = 600
        set.start()
        currentAnim = set
        currentAnimView = view
    }

    private fun stopAnimation() {
        if (currentAnim != null) {
            currentAnim!!.cancel()
            currentAnimView!!.visibility = View.GONE
        }
    }

    fun showEndFormula(v: View?) {
        if (endFormulaView.isClickable) {
            if (endFormulaView.visibility == View.GONE) {
                endFormulaViewLabel.text = getString(R.string.end_formula_opened)
                endFormulaView.visibility = View.VISIBLE
            } else {
                endFormulaViewLabel.text = getString(R.string.end_formula_closed)
                endFormulaView.visibility = View.GONE
            }
        }
    }

    private fun createLeaveDialog(): AlertDialog {
        Log.d(TAG, "createLeaveDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Attention ❗️")
            .setMessage("Wanna leave?")
            .setPositiveButton("Yes") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, id: Int ->
            }
        return builder.create()
    }

    private fun startDialog() {
        Log.d(TAG, "startDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("\uD83D\uDE4B\u200D♀ Hi! \uD83D\uDE4B\u200D♂")
            .setMessage("Welcome to our tutorial!\nWanna start?")
            .setPositiveButton("Yes! \uD83E\uDD29") { dialog: DialogInterface, id: Int ->
                messageTutorial()
            }
            .setNegativeButton("Nope, I'm pro \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setCancelable(false)
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog)
    }

    private fun messageTutorial() {
        Log.d(TAG, "messageTutorial")
        TutorialScene.showMessage("\uD83C\uDF40 Welcome! \uD83C\uDF40\nThat's a place for short\nimportant messages")
        pointerMsgView.visibility = View.VISIBLE
        animateLeftUp(pointerMsgView)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Tutorial: 1️⃣ / 7️⃣")
            .setMessage("Got it?")
            .setPositiveButton("Yep \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                messageView.text = ""
                stopAnimation()
                backTutorial()
            }
            .setNegativeButton("Leave tutorial") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setCancelable(false)
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog, false)
    }

    private fun backTutorial() {
        Log.d(TAG, "backTutorial")
        TutorialScene.showMessage("This can return to menu...\nAnd even save your progress!")
        pointerBackView.visibility = View.VISIBLE
        animateLeftUp(pointerBackView)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Tutorial: 2️⃣ / 7️⃣")
            .setMessage("Got it?")
            .setPositiveButton("Yep \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                messageView.text = ""
                stopAnimation()
                restartTutorial()
            }
            .setNegativeButton("Leave tutorial") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setCancelable(false)
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog, false)
    }

    private fun restartTutorial() {
        Log.d(TAG, "restartTutorial")
        TutorialScene.showMessage("Restart level button")
        pointerRestartView.visibility = View.VISIBLE
        animateUp(pointerRestartView)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Tutorial: 3️⃣ / 7️⃣")
            .setMessage("Got it?")
            .setPositiveButton("Yep \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                messageView.text = ""
                stopAnimation()
                undoTutorial()
            }
            .setNegativeButton("Leave tutorial") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setCancelable(false)
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog, false)
    }

    private fun undoTutorial() {
        Log.d(TAG, "restartTutorial")
        TutorialScene.showMessage("Undo your last operations")
        pointerUndoView.visibility = View.VISIBLE
        animateUp(pointerUndoView)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Tutorial: 4️⃣ / 7️⃣")
            .setMessage("Got it?")
            .setPositiveButton("Yep \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                messageView.text = ""
                stopAnimation()
                endFormulaTutorial()
            }
            .setNegativeButton("Leave tutorial") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setCancelable(false)
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog, false)
    }

    private fun endFormulaTutorial() {
        Log.d(TAG, "endFormulaTutorial")
        TutorialScene.showMessage("⬆️ Good old end formula ⬆️")
        pointerEndView.visibility = View.VISIBLE
        animateLeftUp(pointerEndView)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Tutorial: 5️⃣ / 7️⃣")
            .setMessage("This is final formula - the answer for current task!\n" +
                "You need to reduce central expression to this\n\n" +
                "P.S. You can toggle it with \uD83D\uDD3D if you need!\n\nGot it?")
            .setPositiveButton("Yep \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                messageView.text = ""
                stopAnimation()
                endFormulaView.isClickable = true
                centralFormulaTutorial()
            }
            .setNegativeButton("Leave tutorial") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setCancelable(false)
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog, false)
    }

    private fun centralFormulaTutorial() {
        Log.d(TAG, "centralFormulaTutorial")
        TutorialScene.showMessage("⬇️ Main element of our game ⬇️")
        pointerCentralView.visibility = View.VISIBLE
        animateLeftUp(pointerCentralView)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Tutorial: 6️⃣ / 7️⃣")
            .setMessage("Here is your current game expression.\n" +
                "Firstly, you need to choose some place in it:\n" +
                "* click operator to choose operation\n" +
                "* click operand to choose operand\n" +
                "Then you can select rule, make substitution and win \uD83E\uDD73\n" +
                "Got it?")
            .setPositiveButton("Yep \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                stopAnimation()
                TutorialScene.showMessage("Let's check it out!\n1. Zoom expression to max")
                TutorialScene.wantedZoom = true
            }
            .setNegativeButton("Leave tutorial") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setCancelable(false)
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog, false)
    }

    private fun zoomSucceeded() {
        TutorialScene.showMessage("1. Zoom expression to max ✅\n2. Click on some place")
        TutorialScene.wantedZoom = false
        TutorialScene.wantedClick = true
    }

    fun expressionClickSucceeded() {
        TutorialScene.wantedClick = false
        TutorialScene.showMessage("2. Click on some place ✅\n3. Now choose your rule from a scrollable list below")
        TutorialScene.wantedRule = true
    }

    fun ruleClickSucceeded() {
        TutorialScene.wantedRule = true
        TutorialScene.showMessage("3. Choose rule ✅\n4. Win!")
    }

    fun levelPassed() {
        Log.d(TAG, "levelPassed")
        TutorialScene.showMessage("\uD83E\uDD73 Congratulations! \uD83E\uDD73")
        animateLeftUp(pointerCentralView)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Tutorial: 7️⃣ / 7️⃣")
            .setMessage("Seems you got it all!\n")
            .setPositiveButton("Yep, now I'm pro too \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setCancelable(false)
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog, false)
    }

    inner class MathScaleListener: ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            needClear = false
            scale *= detector.scaleFactor
            scale = max(
                Constants.ruleDefaultSize / Constants.centralFormulaDefaultSize,
                min(scale, Constants.centralFormulaMaxSize / Constants.centralFormulaDefaultSize))
            globalMathView.textSize = Constants.centralFormulaDefaultSize * scale
            if (TutorialScene.wantedZoom && globalMathView.textSize / resources.displayMetrics.scaledDensity ==
                    Constants.centralFormulaMaxSize) {
                zoomSucceeded()
            }
            return true
        }
    }
}
