package mathhelper.games.matify.tutorial

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import mathhelper.games.matify.*
import mathhelper.games.matify.common.*
import kotlin.math.max
import kotlin.math.min

class TutorialPlayActivity : AppCompatActivity(), InstrumentSceneListener, PlaySceneListener {
    private val TAG = "TutorialPlayActivity"
    private var scale = 1.0f
    private var scaleListener = MathScaleListener()

    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var restartDialog: AlertDialog

    lateinit var tutorialDialog: AlertDialog
    lateinit var leaveDialog: AlertDialog

    override lateinit var globalMathView: GlobalMathView
    override lateinit var endExpressionViewLabel: TextView
    override lateinit var endExpressionMathView: SimpleMathView
    override lateinit var messageView: TextView
    override lateinit var rulesLinearLayout: LinearLayout
    override lateinit var rulesScrollView: ScrollView
    override lateinit var rulesMsg: TextView
    lateinit var mainView: ConstraintLayout
    lateinit var mainViewAnim: TransitionDrawable
    lateinit var bottomSheet: LinearLayout
    lateinit var timerView: TextView

    // lateinit var noRules: TextView
    private lateinit var buttonTable: TableLayout
    private lateinit var pointerMsgView: TextView
    private lateinit var pointerEndView: TextView
    private lateinit var pointerCentralView: TextView
    private lateinit var pointerBackView: TextView
    private lateinit var pointerRestartView: TextView
    private lateinit var pointerUndoView: TextView
    private lateinit var pointerInfoView: TextView
    private lateinit var pointerMultiselectView: TextView

    private var needClear: Boolean = false
    override var instrumentProcessing: Boolean = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Logger.d(TAG, "onTouchEvent")
        if (globalMathView.onTouchEvent(event)) {
            needClear = false
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!globalMathView.multiselectionMode)
                    needClear = true
            }
            MotionEvent.ACTION_UP -> {
                if (needClear) {
                    try {
                        globalMathView.clearExpression()
                        clearRules()
                    } catch (e: Exception) {
                        Logger.e(TAG, "Error while clearing rules on touch: ${e.message}")
                        Toast.makeText(this, R.string.misclick_happened_please_retry, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        return true
    }

    fun setViews() {
        globalMathView = findViewById(R.id.global_expression)
        endExpressionMathView = findViewById(R.id.end_expression_math_view)
        endExpressionViewLabel = findViewById(R.id.end_expression_label)
        endExpressionViewLabel.visibility = View.GONE
        endExpressionMathView.visibility = View.GONE
        bottomSheet = findViewById(R.id.bottom_sheet)
        messageView = findViewById(R.id.message_view)
        timerView = findViewById(R.id.timer_view)

        rulesLinearLayout = bottomSheet.findViewById(R.id.rules_linear_layout)
        rulesScrollView = bottomSheet.findViewById(R.id.rules_scroll_view)
        rulesMsg = bottomSheet.findViewById(R.id.rules_msg)
        InstrumentScene.shared.init(bottomSheet, this)

        mainView = findViewById(R.id.tutorial_activity_play)
        mainViewAnim = mainView.background as TransitionDrawable

        // noRules = findViewById(R.id.no_rules)
        pointerMsgView = findViewById(R.id.pointer_message)
        pointerEndView = findViewById(R.id.pointer_end)
        pointerCentralView = findViewById(R.id.pointer_central)
        pointerBackView = findViewById(R.id.pointer_back)
        pointerRestartView = findViewById(R.id.pointer_restart)
        pointerUndoView = findViewById(R.id.pointer_undo)
        pointerInfoView = findViewById(R.id.pointer_info)
        pointerMultiselectView = findViewById(R.id.pointer_multiselect)
        buttonTable = bottomSheet.findViewById(R.id.account_table)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt())
        setContentView(R.layout.tutorial_activity_play_new)
        scaleDetector = ScaleGestureDetector(this, scaleListener)
        setViews()
        leaveDialog = TutorialScene.shared.createLeaveDialog(this)
        tutorialDialog = TutorialScene.shared.createTutorialDialog(this)
        restartDialog = createRestartDialog()
        timerView.text = "⏰ 1:23"
        globalMathView.text = ""
        endExpressionViewLabel.text = ""
        buttonTable.visibility = View.GONE

        TutorialScene.shared.initTPA(this)
        val tla = this
        lifecycleScope.launch {
            try {
                TutorialScene.shared.loadLevel()
            } catch (e: Exception) {
                Logger.e(TAG, "Error while loading a level")
                Toast.makeText(tla, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            }
            TutorialScene.shared.nextStep()
        }
    }

    override fun onBackPressed() {
        back(null)
    }

    override fun finish() {
        TutorialScene.shared.playActivityRef.clear()
        TutorialScene.shared.leaveDialog = TutorialScene.shared.levelsActivityRef.get()!!.leave
        super.finish()
    }

    fun back(v: View?) {
        AndroidUtil.showDialog(leaveDialog)
    }

    fun restart(v: View?) {
        AndroidUtil.showDialog(restartDialog)
    }

    fun showEndExpression(v: View?) {
        /*if (endExpressionView.visibility == View.GONE) {
            endExpressionViewLabel.text = getString(R.string.end_expression_opened)
            endExpressionView.visibility = View.VISIBLE
        } else {
            endExpressionViewLabel.text = getString(R.string.end_expression_closed)
            endExpressionView.visibility = View.GONE
        }*/
    }

    private fun createRestartDialog(): AlertDialog {
        Logger.d(TAG, "createRestartDialog")
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.attention)
            .setMessage(R.string.restart_tutorial)
            .setPositiveButton(R.string.yes) { dialog: DialogInterface, id: Int ->
                TutorialScene.shared.restart()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int ->
            }
        return builder.create()
    }

    fun messageTutorial() {
        Logger.d(TAG, "messageTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_info))
        TutorialScene.shared.animateLeftUp(pointerMsgView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun backTutorial() {
        Logger.d(TAG, "backTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_to_menu))
        TutorialScene.shared.animateLeftUp(pointerBackView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun infoTutorial() {
        Logger.d(TAG, "infoTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_short_info))
        TutorialScene.shared.animateLeftUp(pointerInfoView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun restartTutorial() {
        Logger.d(TAG, "restartTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_restart))
        TutorialScene.shared.animateUp(pointerRestartView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun undoTutorial() {
        Logger.d(TAG, "undoTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_undo))
        TutorialScene.shared.animateUp(pointerUndoView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun endExpressionTutorial() {
        Logger.d(TAG, "endExpressionTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_goal))
        TutorialScene.shared.animateLeftUp(pointerEndView)
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_goal_explanation) +
                    resources.getString(R.string.got_it)
        )
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun centralExpressionTutorial() {
        Logger.d(TAG, "centralExpressionTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_main_element))
        TutorialScene.shared.animateLeftUp(pointerCentralView)
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_main_element_current) +
                    resources.getString(R.string.tutorial_on_level_main_element_to_win) +
                    resources.getString(R.string.tutorial_on_level_main_element_touch) +
                    resources.getString(R.string.tutorial_on_level_main_element_find_rule) +
                    resources.getString(R.string.tutorial_on_level_main_element_make_subst) +
                    resources.getString(R.string.tutorial_on_level_main_element_repeat) +
                    resources.getString(R.string.got_it)
        )
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun startDynamicTutorial() {
        buttonTable.visibility = View.GONE
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_tap))
        TutorialScene.shared.wantedZoom = false
        TutorialScene.shared.wantedClick = true
    }

    fun explainMultiselectTutorial() {
        buttonTable.visibility = View.VISIBLE

        Logger.d(TAG, "explainMultiselectTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_multiselect_expression))
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_multiselect_explanation)
        )
        TutorialScene.shared.animateUp(pointerMultiselectView)
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun actionMultiselectTutorial() {
        Logger.d(TAG, "actionMultiselectTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_multiselect_action))
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_multiselect_details)
        )
        TutorialScene.shared.animateUp(pointerMultiselectView)
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun startMultiselectTutorial() {
        TutorialScene.shared.showMessage("")
        TutorialScene.shared.wantedZoom = false
        TutorialScene.shared.wantedClick = true
    }

    fun expressionClickSucceeded() {
        TutorialScene.shared.wantedClick = false
        if (TutorialScene.shared.currLevelIndex == 0) {
            TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_select))
        } else {
            TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_multiselect_select))
        }
        TutorialScene.shared.wantedRule = true
    }

    fun ruleClickSucceeded() {
        TutorialScene.shared.wantedRule = false
        if (TutorialScene.shared.currLevelIndex == 0) {
            TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_win))
        } else {
            TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_multiselect_click))
        }
    }

    fun levelPassed() {
        Logger.d(TAG, "levelPassed")
        TutorialScene.shared.showMessage(resources.getString(R.string.congratulations))
        globalMathView.center()
        TutorialScene.shared.animateLeftUp(pointerCentralView)
        tutorialDialog.setMessage(resources.getString(R.string.tutorial_on_level_basic_finished))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun bothLevelsPassed() {
        Logger.d(TAG, "tutorial over")
        TutorialScene.shared.showMessage(resources.getString(R.string.congratulations))
        globalMathView.center()
        TutorialScene.shared.animateLeftUp(pointerCentralView)

        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle("${resources.getString(R.string.tutorial)}: ${TutorialScene.shared.stepsSize} / ${TutorialScene.shared.stepsSize}")
            .setMessage(resources.getString(R.string.tutorial_on_level_seems))
            .setPositiveButton(resources.getString(R.string.tutorial_on_level_i_am_pro)) { _: DialogInterface, _: Int ->
                TutorialScene.shared.leave()
            }
            .setNegativeButton(R.string.step_back) { _: DialogInterface, _: Int ->
                TutorialScene.shared.stopAnimation()
                TutorialScene.shared.loadLevel()
                TutorialScene.shared.prevStep()
            }
            .setCancelable(false)
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog, backMode = BackgroundMode.NONE)
    }

    inner class MathScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            needClear = false
            scale *= detector.scaleFactor
            scale = max(
                Constants.ruleDefaultSize / Constants.centralExpressionDefaultSize,
                min(scale, Constants.centralExpressionMaxSize / Constants.centralExpressionDefaultSize)
            )
            globalMathView.textSize = Constants.centralExpressionDefaultSize * scale
            return true
        }
    }

    override fun showMessage(msg: String, flag: Boolean, ifFlagFalseMsg: String?) {}

    fun instrumentClick(v: View) {
        InstrumentScene.shared.clickInstrument(v.tag.toString())
    }

    override fun setMultiselectionMode(multi: Boolean) {
        if (multi) {
            globalMathView.multiselectionMode = true
            globalMathView.recolorCurrentAtom(ThemeController.shared.color(ColorName.MULTISELECTION_COLOR))
        } else {
            clearRules()
            globalMathView.clearExpression()
            globalMathView.multiselectionMode = false
        }
    }

    fun collapseBottomSheet() {
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun halfExpandBottomSheet() {
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    override fun clearRules() {
        rulesScrollView.visibility = View.GONE
        rulesMsg.text = getString(R.string.no_rules_msg)
        if (!instrumentProcessing) {
            collapseBottomSheet()
        }
    }

    override fun startInstrumentProcessing(setMSMode: Boolean) {
        instrumentProcessing = true
        clearRules()
        showMessage(getString(R.string.inst_enter))
        setMultiselectionMode(setMSMode)
        rulesMsg.text = getString(R.string.inst_rules_msg)
        AndroidUtil.vibrate(this)
        mainViewAnim.startTransition(300)
    }

    override fun endInstrumentProcessing(collapse: Boolean) {
        instrumentProcessing = false
        setMultiselectionMode(false)
        globalMathView.clearExpression()
        AndroidUtil.vibrate(this)
        mainViewAnim.reverseTransition(300)
        if (collapse) {
            collapseBottomSheet()
            clearRules()
        }
    }
}
