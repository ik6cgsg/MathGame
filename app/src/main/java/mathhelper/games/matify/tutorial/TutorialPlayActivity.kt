package mathhelper.games.matify.tutorial

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import mathhelper.games.matify.common.GlobalMathView
import mathhelper.games.matify.R
import mathhelper.games.matify.TutorialScene
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.Constants
import kotlin.math.max
import kotlin.math.min

class TutorialPlayActivity: AppCompatActivity() {
    private val TAG = "TutorialPlayActivity"
    private var scale = 1.0f
    private var needClear = false
    private var scaleListener = MathScaleListener()

    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var restartDialog: AlertDialog

    lateinit var tutorialDialog: AlertDialog
    lateinit var leaveDialog: AlertDialog

    lateinit var globalMathView: GlobalMathView
    lateinit var endExpressionView: TextView
    lateinit var endExpressionViewLabel: TextView
    lateinit var messageView: TextView
    lateinit var rulesLinearLayout: LinearLayout
    lateinit var rulesScrollView: ScrollView
    lateinit var noRules: TextView
    private lateinit var timerView: TextView
    private lateinit var pointerMsgView: TextView
    private lateinit var pointerEndView: TextView
    private lateinit var pointerCentralView: TextView
    private lateinit var pointerBackView: TextView
    private lateinit var pointerRestartView: TextView
    private lateinit var pointerUndoView: TextView
    private lateinit var pointerInfoView: TextView

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
                    TutorialScene.shared.clearRules()
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
        pointerInfoView = findViewById(R.id.pointer_info)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tutorial_activity_play)
        scaleDetector = ScaleGestureDetector(this, scaleListener)
        setViews()
        leaveDialog = TutorialScene.shared.createLeaveDialog(this)
        tutorialDialog = TutorialScene.shared.createTutorialDialog(this)
        restartDialog = createRestartDialog()
        timerView.text = "⏰ 1:23"
        globalMathView.text = ""
        endExpressionView.text = ""
        TutorialScene.shared.tutorialPlayActivity = this
    }

    override fun onBackPressed() {
        back(null)
    }

    override fun finish() {
        TutorialScene.shared.tutorialPlayActivity = null
        TutorialScene.shared.leaveDialog = TutorialScene.shared.tutorialLevelsActivity!!.leave
        super.finish()
    }

    fun back(v: View?) {
        AndroidUtil.showDialog(leaveDialog)
    }

    fun restart(v: View?) {
        AndroidUtil.showDialog(restartDialog)
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

    private fun createRestartDialog(): AlertDialog {
        Log.d(TAG, "createRestartDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("❗️ Attention ❗️")
            .setMessage("Restart tutorial?")
            .setPositiveButton("Yes") { dialog: DialogInterface, id: Int ->
                TutorialScene.shared.restart()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int ->
            }
        return builder.create()
    }

    fun messageTutorial() {
        Log.d(TAG, "messageTutorial")
        TutorialScene.shared.showMessage("\uD83C\uDF40 Welcome! \uD83C\uDF40\nThat's a place for short\nimportant (and funny) messages")
        pointerMsgView.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointerMsgView)
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    fun backTutorial() {
        Log.d(TAG, "backTutorial")
        TutorialScene.shared.showMessage("← This can return to menu... ←\nAnd even \uD83D\uDCBE your progress!")
        pointerBackView.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointerBackView)
        tutorialDialog.setMessage("Got it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    fun infoTutorial() {
        Log.d(TAG, "infoTutorial")
        TutorialScene.shared.showMessage("i Tap to see short\ninfo about level i")
        pointerInfoView.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointerInfoView)
        tutorialDialog.setMessage("Got it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    fun restartTutorial() {
        Log.d(TAG, "restartTutorial")
        TutorialScene.shared.showMessage("↺ Restart level button ↺")
        pointerRestartView.visibility = View.VISIBLE
        TutorialScene.shared.animateUp(pointerRestartView)
        tutorialDialog.setMessage("Got it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    fun undoTutorial() {
        Log.d(TAG, "restartTutorial")
        TutorialScene.shared.showMessage("↶ Undo your last operations ↶")
        pointerUndoView.visibility = View.VISIBLE
        TutorialScene.shared.animateUp(pointerUndoView)
        tutorialDialog.setMessage("Got it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    fun endExpressionTutorial() {
        Log.d(TAG, "endExpressionTutorial")
        TutorialScene.shared.showMessage("⬆️ Goal: transform expression to it ⬆️")
        pointerEndView.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointerEndView)
        tutorialDialog.setMessage("This is final goal for level\nAKA The Answer for current task!\n\n" +
            "P.S. You can toggle it with \uD83D\uDD3D if you need!\n\nGot it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    fun centralExpressionTutorial() {
        Log.d(TAG, "centralExpressionTutorial")
        TutorialScene.shared.showMessage("⬇️ Main element of our game ⬇️")
        pointerCentralView.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointerCentralView)
        tutorialDialog.setMessage("Here is your current game expression.\n" +
            "To win \uD83C\uDF89 you need:\n" +
            "1. Touch \uD83D\uDC46 on expression\n" +
            "2. Find \uD83D\uDD0D necessary rule\n" +
            "3. make substitution ~>\n" +
            "4. repeat until \uD83D\uDE0E\uD83C\uDF89\n" +
            "Got it?")
        AndroidUtil.showDialog(tutorialDialog, false)
    }

    fun startDynamicTutorial() {
        TutorialScene.shared.showMessage("\uD83D\uDE09 Let's check it out! \uD83D\uDE09\n1. Zoom expression to max \uD83D\uDD0E")
        TutorialScene.shared.wantedZoom = true
    }

    fun zoomSucceeded() {
        TutorialScene.shared.showMessage("1. Zoom expression to max ✅\n2. Click on some place \uD83D\uDC47")
        TutorialScene.shared.wantedZoom = false
        TutorialScene.shared.wantedClick = true
    }

    fun expressionClickSucceeded() {
        TutorialScene.shared.wantedClick = false
        TutorialScene.shared.showMessage("2. Click on some place ✅\n3. Now \uD83D\uDD0D rule from a list below")
        TutorialScene.shared.wantedRule = true
    }

    fun ruleClickSucceeded() {
        TutorialScene.shared.wantedRule = true
        TutorialScene.shared.showMessage("3. Choose rule ✅\n4. Win! \uD83D\uDE0E\uD83C\uDF89")
    }

    fun levelPassed() {
        Log.d(TAG, "levelPassed")
        TutorialScene.shared.showMessage("\uD83C\uDF89 Congratulations! \uD83C\uDF89")
        TutorialScene.shared.animateLeftUp(pointerCentralView)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Tutorial: ${TutorialScene.shared.currentStepToDisplay} / ${TutorialScene.shared.stepsSize}")
            .setMessage("Seems you got it all!\n")
            .setPositiveButton("Yep, now I'm pro too \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
                TutorialScene.shared.leave()
            }
            .setNegativeButton("Back") { dialog: DialogInterface, id: Int ->
                TutorialScene.shared.loadLevel()
                TutorialScene.shared.prevStep()
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
            if (TutorialScene.shared.wantedZoom && globalMathView.textSize / resources.displayMetrics.scaledDensity ==
                    Constants.centralExpressionMaxSize) {
                zoomSucceeded()
            }
            return true
        }
    }
}
