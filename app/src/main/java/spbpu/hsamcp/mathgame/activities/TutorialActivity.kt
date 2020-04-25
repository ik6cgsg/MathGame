package spbpu.hsamcp.mathgame.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

class TutorialActivity: AppCompatActivity() {
    private val TAG = "TutorialActivity"
    private var scale = 1.0f
    private val duration = 600.toLong()
    private val translate = -30f
    private var needClear = false
    private var loading = false
    private var scaleListener = MathScaleListener()
    private var currentAnim: AnimatorSet? = null
    private var currentAnimView: View? = null
    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var leaveDialog: AlertDialog
    private lateinit var restartDialog: AlertDialog
    private lateinit var tutorialDialog: AlertDialog
    private lateinit var progress: ProgressBar
    private lateinit var steps: ArrayList<() -> Unit>
    private var currentStep = -1

    lateinit var globalMathView: GlobalMathView
    lateinit var endExpressionView: TextView
    lateinit var endExpressionViewLabel: TextView
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
                    globalMathView.clearExpression()
                    TutorialScene.clearRules()
                }
            }
        }
        return true
    }

    private fun setViews() {
        globalMathView = findViewById(R.id.global_expression)
        endExpressionView = findViewById(R.id.end_expression_view)
        endExpressionViewLabel = findViewById(R.id.end_expression_label)
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
        progress = findViewById(R.id.progress)
        val res = findViewById<TextView>(R.id.restart)
        AndroidUtil.setOnTouchUpInside(res, ::restart)
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
        leaveDialog = createLeaveDialog()
        tutorialDialog = createTutorialDialog()
        restartDialog = createRestartDialog()
        steps = arrayListOf(
            ::messageTutorial,
            ::endExpressionTutorial,
            ::centralExpressionTutorial,
            ::backTutorial,
            ::restartTutorial,
            ::undoTutorial,
            ::startDynamicTutorial
        )
        createLevelUI()
    }

    override fun onBackPressed() {
        back(null)
    }

    fun back(v: View?) {
        AndroidUtil.showDialog(leaveDialog)
    }

    fun restart(v: View?) {
        AndroidUtil.showDialog(restartDialog)
    }

    private fun createLevelUI() {
        loading = true
        timerView.text = "⏰ 1:23"
        globalMathView.text = ""
        endExpressionView.text = ""
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
        val animationX = ObjectAnimator.ofFloat(view, "translationX", translate)
        animationX.repeatMode = ValueAnimator.REVERSE
        animationX.repeatCount = ValueAnimator.INFINITE
        val animationY = ObjectAnimator.ofFloat(view, "translationY", translate)
        animationY.repeatMode = ValueAnimator.REVERSE
        animationY.repeatCount = ValueAnimator.INFINITE
        val set = AnimatorSet()
        set.play(animationX)
            .with(animationY)
        set.duration = duration
        set.start()
        currentAnim = set
        currentAnimView = view
    }

    private fun animateUp(view: View) {
        val animationY = ObjectAnimator.ofFloat(view, "translationY", translate)
        animationY.repeatMode = ValueAnimator.REVERSE
        animationY.repeatCount = ValueAnimator.INFINITE
        val set = AnimatorSet()
        set.play(animationY)
        set.duration = duration
        set.start()
        currentAnim = set
        currentAnimView = view
    }

    private fun stopAnimation() {
        if (currentAnim != null) {
            currentAnim!!.removeAllListeners()
            currentAnim!!.end()
            currentAnim!!.cancel()
            currentAnim = null
            currentAnimView!!.translationY = 0f
            currentAnimView!!.translationX = 0f
            currentAnimView!!.visibility = View.GONE
        }
    }

    fun showEndExpression(v: View?) {
        if (endExpressionView.visibility == View.GONE) {
            endExpressionViewLabel.text = getString(R.string.end_expression_opened)
            endExpressionView.visibility = View.VISIBLE
        } else {
            endExpressionViewLabel.text = getString(R.string.end_expression_closed)
            endExpressionView.visibility = View.GONE
        }
    }

    private fun createLeaveDialog(): AlertDialog {
        Log.d(TAG, "createLeaveDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("❗️ Attention ❗️")
            .setMessage("Wanna leave?")
            .setPositiveButton("Yes") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, id: Int ->
            }
        return builder.create()
    }

    private fun createRestartDialog(): AlertDialog {
        Log.d(TAG, "createRestartDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("❗️ Attention ❗️")
            .setMessage("Restart tutorial?")
            .setPositiveButton("Yes") { dialog: DialogInterface, id: Int ->
                TutorialScene.loadLevel()
                currentStep = -1
                nextStep()
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, id: Int ->
            }
        return builder.create()
    }

    private fun nextStep() {
        currentStep++
        if (currentStep == steps.size) {
            return
        }
        tutorialDialog.setTitle("Tutorial: ${currentStep + 1}⃣ / ${steps.size}⃣")
        steps[currentStep]()
    }

    private fun prevStep() {
        currentStep--
        if (currentStep == -1) {
            startDialog()
        } else {
            tutorialDialog.setTitle("Tutorial: ${currentStep + 1}⃣ / ${steps.size}⃣")
            steps[currentStep]()
        }
    }

    private fun createTutorialDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("")
            .setMessage("Got it?")
            .setPositiveButton("Yep \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                messageView.text = ""
                stopAnimation()
                Handler().postDelayed({
                    nextStep()
                }, 100)
            }
            .setNegativeButton("Step back") { dialog: DialogInterface, id: Int ->
                messageView.text = ""
                stopAnimation()
                Handler().postDelayed({
                    prevStep()
                }, 100)
            }
            .setNeutralButton("Leave") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setCancelable(false)
        return builder.create()
    }

    private fun startDialog() {
        Log.d(TAG, "startDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("\uD83D\uDE4B\u200D♀ Hi! \uD83D\uDE4B\u200D♂")
            .setMessage("Welcome to our tutorial!\nWanna start?")
            .setPositiveButton("Yes! \uD83D\uDE0D") { dialog: DialogInterface, id: Int ->
                nextStep()
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
        TutorialScene.showMessage("\uD83C\uDF40 Welcome! \uD83C\uDF40\nThat's a place for short\nimportant (and funny) messages")
        pointerMsgView.visibility = View.VISIBLE
        animateLeftUp(pointerMsgView)
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    private fun backTutorial() {
        Log.d(TAG, "backTutorial")
        TutorialScene.showMessage("← This can return to menu... ←\nAnd even \uD83D\uDCBE your progress!")
        pointerBackView.visibility = View.VISIBLE
        animateLeftUp(pointerBackView)
        tutorialDialog.setMessage("Got it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    private fun restartTutorial() {
        Log.d(TAG, "restartTutorial")
        TutorialScene.showMessage("↺ Restart level button ↺")
        pointerRestartView.visibility = View.VISIBLE
        animateUp(pointerRestartView)
        tutorialDialog.setMessage("Got it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    private fun undoTutorial() {
        Log.d(TAG, "restartTutorial")
        TutorialScene.showMessage("↶ Undo your last operations ↶")
        pointerUndoView.visibility = View.VISIBLE
        animateUp(pointerUndoView)
        tutorialDialog.setMessage("Got it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    private fun endExpressionTutorial() {
        Log.d(TAG, "endExpressionTutorial")
        TutorialScene.showMessage("⬆️ Good old end expression ⬆️")
        pointerEndView.visibility = View.VISIBLE
        animateLeftUp(pointerEndView)
        tutorialDialog.setMessage("This is final expression for level\nAKA The Answer for current task!\n\n" +
            "P.S. You can toggle it with \uD83D\uDD3D if you need!\n\nGot it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    private fun centralExpressionTutorial() {
        Log.d(TAG, "centralExpressionTutorial")
        TutorialScene.showMessage("⬇️ Main element of our game ⬇️")
        pointerCentralView.visibility = View.VISIBLE
        animateLeftUp(pointerCentralView)
        tutorialDialog.setMessage("Here is your current game expression.\n" +
            "To win \uD83C\uDF89 you need:\n" +
            "1. Touch \uD83D\uDC46 on expression\n" +
            "2. Find \uD83D\uDD0D necessary rule\n" +
            "3. make substitution ~>\n" +
            "4. repeat until \uD83D\uDE0E\uD83C\uDF89\n" +
            "Got it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    private fun startDynamicTutorial() {
        TutorialScene.showMessage("\uD83D\uDE09 Let's check it out! \uD83D\uDE09\n1. Zoom expression to max \uD83D\uDD0E")
        TutorialScene.wantedZoom = true
    }

    private fun zoomSucceeded() {
        TutorialScene.showMessage("1. Zoom expression to max ✅\n2. Click on some place \uD83D\uDC47")
        TutorialScene.wantedZoom = false
        TutorialScene.wantedClick = true
    }

    fun expressionClickSucceeded() {
        TutorialScene.wantedClick = false
        TutorialScene.showMessage("2. Click on some place ✅\n3. Now \uD83D\uDD0D rule from a list below")
        TutorialScene.wantedRule = true
    }

    fun ruleClickSucceeded() {
        TutorialScene.wantedRule = true
        TutorialScene.showMessage("3. Choose rule ✅\n4. Win! \uD83D\uDE0E\uD83C\uDF89")
    }

    fun levelPassed() {
        Log.d(TAG, "levelPassed")
        TutorialScene.showMessage("\uD83C\uDF89 Congratulations! \uD83C\uDF89")
        animateLeftUp(pointerCentralView)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Tutorial: ${currentStep + 1}⃣ / ${steps.size}⃣")
            .setMessage("Seems you got it all!\n")
            .setPositiveButton("Yep, now I'm pro too \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                TutorialScene.leave()
            }
            .setNegativeButton("Back") { dialog: DialogInterface, id: Int ->
                TutorialScene.loadLevel()
                prevStep()
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
                Constants.ruleDefaultSize / Constants.centralExpressionDefaultSize,
                min(scale, Constants.centralExpressionMaxSize / Constants.centralExpressionDefaultSize))
            globalMathView.textSize = Constants.centralExpressionDefaultSize * scale
            if (TutorialScene.wantedZoom && globalMathView.textSize / resources.displayMetrics.scaledDensity ==
                    Constants.centralExpressionMaxSize) {
                zoomSucceeded()
            }
            return true
        }
    }
}
