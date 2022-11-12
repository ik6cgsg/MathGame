package mathhelper.games.matify.tutorial

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mathhelper.games.matify.*
import mathhelper.games.matify.activities.AbstractPlayableActivity
import mathhelper.games.matify.common.*
import kotlin.math.max
import kotlin.math.min

class TutorialPlayActivity : AbstractPlayableActivity(), TutorialSceneListener, InstrumentSceneListener {
    override val TAG = "TutorialPlayActivity"
    private var scale = 1.0f
    private var scaleListener = MathScaleListener()

    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var restartDialog: AlertDialog

    override lateinit var tutorialDialog: AlertDialog
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
    private lateinit var pointerMultiselectView: TextView

    override fun setViews() {
        super.setViews()

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
        TutorialScene.shared.listenerRef.clear()
        TutorialScene.shared.leaveDialog = TutorialScene.shared.levelsActivityRef.get()!!.leave
        super.finish()
    }

    fun back(v: View?) {
        AndroidUtil.showDialog(leaveDialog)
    }

    fun restart(v: View?) {
        AndroidUtil.showDialog(restartDialog)
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

    override fun messageTutorial() {
        Logger.d(TAG, "messageTutorial")
        showMessage(resources.getString(R.string.tutorial_on_level_info))
        TutorialScene.shared.animateLeftUp(pointerMsgView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    override fun backTutorial() {
        Logger.d(TAG, "backTutorial")
        showMessage(resources.getString(R.string.tutorial_on_level_to_menu))
        TutorialScene.shared.animateLeftUp(pointerBackView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    override fun infoTutorial() {
        Logger.d(TAG, "infoTutorial")
        showMessage(resources.getString(R.string.tutorial_on_level_short_info))
        TutorialScene.shared.animateLeftUp(pointerInfoView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    override fun restartTutorial() {
        Logger.d(TAG, "restartTutorial")
        showMessage(resources.getString(R.string.tutorial_on_level_restart))
        TutorialScene.shared.animateUp(pointerRestartView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    override fun undoTutorial() {
        Logger.d(TAG, "undoTutorial")
        showMessage(resources.getString(R.string.tutorial_on_level_undo))
        TutorialScene.shared.animateUp(pointerUndoView)
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    override fun endExpressionTutorial() {
        Logger.d(TAG, "endExpressionTutorial")
        showMessage(resources.getString(R.string.tutorial_on_level_goal))
        TutorialScene.shared.animateLeftUp(pointerEndView)
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_goal_explanation) +
                    resources.getString(R.string.got_it)
        )
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    override fun centralExpressionTutorial() {
        Logger.d(TAG, "centralExpressionTutorial")
        showMessage(resources.getString(R.string.tutorial_on_level_main_element))
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

    override fun startDynamicTutorial() {
        buttonTable.visibility = View.GONE
        showMessage(resources.getString(R.string.tutorial_on_level_tap))
        TutorialScene.shared.wantedZoom = false
        TutorialScene.shared.wantedClick = true
    }

    override fun explainMultiselectTutorial() {
        buttonTable.visibility = View.VISIBLE

        Logger.d(TAG, "explainMultiselectTutorial")
        showMessage(resources.getString(R.string.tutorial_on_level_multiselect_expression))
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_multiselect_explanation)
        )
        TutorialScene.shared.animateUp(pointerMultiselectView)
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    override fun actionMultiselectTutorial() {
        Logger.d(TAG, "actionMultiselectTutorial")
        showMessage(resources.getString(R.string.tutorial_on_level_multiselect_action))
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_multiselect_details)
        )
        TutorialScene.shared.animateUp(pointerMultiselectView)
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    override fun startMultiselectTutorial() {
        showMessage("")
        TutorialScene.shared.wantedZoom = false
        TutorialScene.shared.wantedClick = true
    }

    override fun expressionClickSucceeded() {
        TutorialScene.shared.wantedClick = false
        if (TutorialScene.shared.currLevelIndex == 0) {
            showMessage(resources.getString(R.string.tutorial_on_level_select))
        } else {
            showMessage(resources.getString(R.string.tutorial_on_level_multiselect_select))
        }
        TutorialScene.shared.wantedRule = true
    }

    override fun ruleClickSucceeded() {
        TutorialScene.shared.wantedRule = false
        if (TutorialScene.shared.currLevelIndex == 0) {
            showMessage(resources.getString(R.string.tutorial_on_level_win))
        } else {
            showMessage(resources.getString(R.string.tutorial_on_level_multiselect_click))
        }
    }

    override fun levelPassed() {
        Logger.d(TAG, "levelPassed")
        showMessage(resources.getString(R.string.congratulations))
        globalMathView.center()
        TutorialScene.shared.animateLeftUp(pointerCentralView)
        tutorialDialog.setMessage(resources.getString(R.string.tutorial_on_level_basic_finished))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    override fun bothLevelsPassed() {
        Logger.d(TAG, "tutorial over")
        showMessage(resources.getString(R.string.congratulations))
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

    override fun showEndExpression(v: View?) {
        val builder = AlertDialog.Builder(this, ThemeController.shared.alertDialogTheme)
        builder
            .setTitle(getString(R.string.goal_description))
            .setMessage(
                TutorialScene.shared.currentLevel.getDescriptionByLanguage(resources.configuration.locale.language)
            )
            .setOnCancelListener { }
            .setCancelable(true)
        val alert = builder.create()
        AndroidUtil.showDialog(
            alert, bottomGravity = false, backMode = BackgroundMode.BLUR,
            blurView = blurView, activity = this
        )
    }
}
