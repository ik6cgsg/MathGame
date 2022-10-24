package mathhelper.games.matify.tutorial

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.*
import mathhelper.games.matify.InstrumentScene
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.R
import mathhelper.games.matify.TutorialScene
import mathhelper.games.matify.activities.GeneralPlayActivity
import mathhelper.games.matify.common.*
import kotlin.math.max
import kotlin.math.min

class TutorialPlayActivity : GeneralPlayActivity() {
    private val TAG = "TutorialPlayActivity"
    private var scale = 1.0f
    private var scaleListener = MathScaleListener()

    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var restartDialog: AlertDialog

    lateinit var tutorialDialog: AlertDialog
    lateinit var leaveDialog: AlertDialog

    // lateinit var noRules: TextView
    private lateinit var buttonTable: TableLayout
    private lateinit var pointerMsgView: TextView
    private lateinit var pointerEndView: TextView
    private lateinit var pointerCentralView: TextView
    private lateinit var pointerBackView: TextView
    private lateinit var pointerRestartView: TextView
    private lateinit var pointerUndoView: TextView
    private lateinit var pointerInfoView: TextView

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

    override fun setViews() {
        super.setViews()
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

        TutorialScene.shared.tutorialPlayActivity = this
        // TODO!: rethink this entire approach alongside similar uses of delaying
        Handler().postDelayed({
            try {
                TutorialScene.shared.loadLevel()
            } catch (e: Exception) {
                Logger.e(TAG, "Error while loading a level")
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            }
            TutorialScene.shared.nextStep()
        }, 100)
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
        pointerMsgView.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointerMsgView)
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun backTutorial() {
        Logger.d(TAG, "backTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_to_menu))
        pointerBackView.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointerBackView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun infoTutorial() {
        Logger.d(TAG, "infoTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_short_info))
        pointerInfoView.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointerInfoView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun restartTutorial() {
        Logger.d(TAG, "restartTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_restart))
        pointerRestartView.visibility = View.VISIBLE
        TutorialScene.shared.animateUp(pointerRestartView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun undoTutorial() {
        Logger.d(TAG, "restartTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_undo))
        pointerUndoView.visibility = View.VISIBLE
        TutorialScene.shared.animateUp(pointerUndoView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun endExpressionTutorial() {
        Logger.d(TAG, "endExpressionTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_goal))
        pointerEndView.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointerEndView)
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_goal_explanation) +
                    resources.getString(R.string.tutorial_on_level_goal_toggle)
        )
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun centralExpressionTutorial() {
        Logger.d(TAG, "centralExpressionTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_main_element))
        pointerCentralView.visibility = View.VISIBLE
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
        tutorialDialog.setMessage(resources.getString(R.string.tutorial_on_level_multiselect_explanation)
        )
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun actionMultiselectTutorial() {
        buttonTable.visibility = View.VISIBLE

        Logger.d(TAG, "actionMultiselectTutorial")
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_multiselect_action))
        tutorialDialog.setMessage(resources.getString(R.string.tutorial_on_level_multiselect_details)
        )
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun startMultiselectTutorial() {
        TutorialScene.shared.showMessage("")
        TutorialScene.shared.wantedZoom = false
        TutorialScene.shared.wantedClick = true
    }

    fun expressionClickSucceeded() {
        TutorialScene.shared.wantedClick = false
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_select))
        TutorialScene.shared.wantedRule = true
    }

    fun ruleClickSucceeded() {
        TutorialScene.shared.wantedRule = true
        TutorialScene.shared.showMessage(resources.getString(R.string.tutorial_on_level_win))
    }

    fun levelPassed() {
        Logger.d(TAG, "levelPassed")
        TutorialScene.shared.showMessage(resources.getString(R.string.congratulations))
        TutorialScene.shared.animateLeftUp(pointerCentralView)
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle("${resources.getString(R.string.tutorial)}: ${TutorialScene.shared.stepToDisplay()} / ${TutorialScene.shared.stepsSize}")
            .setMessage(resources.getString(R.string.tutorial_on_level_basic_finished))
            .setPositiveButton(resources.getString(R.string.tutorial_advanced_proceed)) { _: DialogInterface, _: Int ->
                TutorialScene.shared.nextStep()
            }
            .setNegativeButton(R.string.step_back) { _: DialogInterface, _: Int ->
                TutorialScene.shared.loadLevel()
                TutorialScene.shared.prevStep()
            }
            .setCancelable(false)
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog, backMode = BackgroundMode.NONE)
    }

    fun bothLevelsPassed() {
        Logger.d(TAG, "tutorial over")
        TutorialScene.shared.showMessage(resources.getString(R.string.congratulations))
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
        InstrumentScene.shared.clickInstrument(v.tag.toString(), this)
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
}
