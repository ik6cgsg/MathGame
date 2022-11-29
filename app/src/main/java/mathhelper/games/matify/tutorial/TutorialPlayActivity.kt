package mathhelper.games.matify.tutorial

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.text.Html
import android.text.SpannedString
import android.view.*
import android.widget.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mathhelper.games.matify.*
import mathhelper.games.matify.activities.AbstractPlayableActivity
import mathhelper.games.matify.common.*
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

class TutorialPlayActivity : AbstractPlayableActivity(), TutorialSceneListener, InstrumentSceneListener {
    override val TAG = "TutorialPlayActivity"
    private var scale = 1.0f
    private var scaleListener = MathScaleListener()

    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var restartDialog: AlertDialog

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

    companion object {
        val totalSteps = 11
    }

    private val steps: Array<() -> Unit> = arrayOf(
        this::messageTutorial,
        this::endExpressionTutorial,
        this::centralExpressionTutorial,
        this::backTutorial,
        this::infoTutorial,
        this::restartTutorial,
        this::undoTutorial,
        this::startDynamicTutorial,
        this::explainMultiselectTutorial,
        this::actionMultiselectTutorial,
        this::startMultiselectTutorial,
    )
    private var currentStep = -1

    var wantedZoom = false
    var wantedClick = false
    var wantedRule = false

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
        pointerMultiselectView = findViewById(R.id.pointer_multiselect)
        buttonTable = bottomSheet.findViewById(R.id.account_table)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt())
        setContentView(R.layout.tutorial_activity_play_new)
        scaleDetector = ScaleGestureDetector(this, scaleListener)
        setViews()
        TutorialScene.shared.createLeaveDialog(this)
        TutorialScene.shared.createTutorialDialog(this)
        restartDialog = createRestartDialog()
        timerView.text = "⏰ 1:23"
        globalMathView.text = ""
        endExpressionViewLabel.text = ""
        buttonTable.visibility = View.GONE

        TutorialScene.shared.listenerRef = WeakReference(this)
        nextStep()
    }

    fun loadLevel() {
        Logger.d(TAG, "loadLevel")
        clearRules()
        val currentLevel = TutorialScene.shared.currentLevel

        endExpressionViewLabel.text = Html.fromHtml(
            String.format(
                Html.toHtml(SpannedString(getText(R.string.end_expression_opened))),
                currentLevel.getDescriptionByLanguage(ctx.resources.configuration.locale.language)
            )
        )
        endExpressionViewLabel.visibility = View.VISIBLE
        endExpressionMathView.visibility = View.GONE
        if (!currentLevel.goalExpressionStructureString.isNullOrBlank()) {
            endExpressionMathView.setExpression(currentLevel.goalExpressionStructureString!!, null)
            endExpressionMathView.visibility = View.VISIBLE
        }

        globalMathView.setExpression(
            currentLevel.startExpression.clone(),
            currentLevel.subjectType,
            true
        )
        globalMathView.center()
    }

    override fun onBackPressed() {
        back(null)
    }

    fun back(v: View?) {
        TutorialScene.shared.leaveDialog?.let {
            AndroidUtil.showDialog(it)
        }
    }

    fun restart(v: View?) {
        AndroidUtil.showDialog(restartDialog)
    }

    override fun nextStep(): Boolean {
        currentStep++
        if (currentStep == steps.size) {
            return false
        }
        TutorialScene.shared.updateDialog(resources.getString(R.string.tutorial))
        steps[currentStep]()
        return true
    }

    override fun prevStep(): Boolean {
        currentStep--
        if (currentStep == -1) {
            return false
        }
        TutorialScene.shared.updateDialog(resources.getString(R.string.tutorial))
        steps[currentStep]()
        return true
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
                TutorialScene.shared.restart(this)
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int ->
            }
        return builder.create()
    }

    fun messageTutorial() {
        Logger.d(TAG, "messageTutorial")
        loadLevel()
        showMessage(R.string.tutorial_on_level_info)
        TutorialScene.shared.animateLeftUp(pointerMsgView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun backTutorial() {
        Logger.d(TAG, "backTutorial")
        showMessage(R.string.tutorial_on_level_to_menu)
        TutorialScene.shared.animateLeftUp(pointerBackView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun infoTutorial() {
        Logger.d(TAG, "infoTutorial")
        showMessage(R.string.tutorial_on_level_short_info)
        TutorialScene.shared.animateLeftUp(pointerInfoView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun restartTutorial() {
        Logger.d(TAG, "restartTutorial")
        showMessage(R.string.tutorial_on_level_restart)
        TutorialScene.shared.animateUp(pointerRestartView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun undoTutorial() {
        Logger.d(TAG, "undoTutorial")
        showMessage(R.string.tutorial_on_level_undo)
        TutorialScene.shared.animateUp(pointerUndoView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun endExpressionTutorial() {
        Logger.d(TAG, "endExpressionTutorial")
        showMessage(R.string.tutorial_on_level_goal)
        TutorialScene.shared.animateLeftUp(pointerEndView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_goal_explanation) +
                    resources.getString(R.string.got_it)
        )
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun centralExpressionTutorial() {
        Logger.d(TAG, "centralExpressionTutorial")
        showMessage(R.string.tutorial_on_level_main_element)
        TutorialScene.shared.animateLeftUp(pointerCentralView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
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
        showMessage(R.string.tutorial_on_level_tap)
        wantedZoom = false
        wantedClick = true
    }

    fun explainMultiselectTutorial() {
        val game = TutorialScene.shared.tutorialGame ?: return
        TutorialScene.shared.currLevelIndex = 1
        TutorialScene.shared.currentLevel = game.levels[1]
        loadLevel()
        buttonTable.visibility = View.VISIBLE

        Logger.d(TAG, "explainMultiselectTutorial")
        showMessage(R.string.tutorial_on_level_multiselect_explanation)
        TutorialScene.shared.animateUp(pointerMultiselectView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_multiselect_expression)
        )
        AndroidUtil.showDialog(tutorialDialog, bottomGravity = false, backMode = BackgroundMode.NONE)
    }

    fun actionMultiselectTutorial() {
        Logger.d(TAG, "actionMultiselectTutorial")
        showMessage(R.string.tutorial_on_level_multiselect_action)
        TutorialScene.shared.animateUp(pointerMultiselectView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_multiselect_details)
        )
        AndroidUtil.showDialog(tutorialDialog, bottomGravity = false, backMode = BackgroundMode.NONE)
    }

    fun startMultiselectTutorial() {
        showMessage(R.string.tutorial_on_level_multiselect_button)
        wantedZoom = false
        wantedClick = true
    }

    fun expressionClickSucceeded() {
        wantedClick = false
        if (TutorialScene.shared.currLevelIndex == 0) {
            showMessage(R.string.tutorial_on_level_select)
        } else {
            showMessage(R.string.tutorial_on_level_multiselect_select)
        }
        wantedRule = true
    }

    fun ruleClickSucceeded() {
        wantedRule = false
        if (TutorialScene.shared.currLevelIndex == 0) {
            showMessage(R.string.tutorial_on_level_win)
        } else {
            showMessage(R.string.tutorial_on_level_multiselect_click)
        }
    }

    fun levelPassed() {
        Logger.d(TAG, "levelPassed")
        showMessage(R.string.congratulations)
        globalMathView.center()
        TutorialScene.shared.animateLeftUp(pointerCentralView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.tutorial_on_level_basic_finished))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    fun bothLevelsPassed() {
        Logger.d(TAG, "tutorial over")
        showMessage(R.string.congratulations)
        globalMathView.center()
        TutorialScene.shared.animateLeftUp(pointerCentralView)

        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle("${resources.getString(R.string.tutorial)}: ${TutorialScene.shared.currentStep + 1} / ${TutorialScene.shared.totalSteps()}")
            .setMessage(resources.getString(R.string.tutorial_on_level_seems))
            .setPositiveButton(resources.getString(R.string.tutorial_on_level_i_am_pro)) { _: DialogInterface, _: Int ->
                TutorialScene.shared.leave()
            }
            .setNegativeButton(R.string.step_back) { _: DialogInterface, _: Int ->
                TutorialScene.shared.prevStep(this)
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

    override fun startInstrumentProcessing(setMSMode: Boolean) {
        super.startInstrumentProcessing(setMSMode)
        showMessage(R.string.tutorial_on_level_multiselect_partial_select)
    }

    override fun onRuleClicked(ruleView: RuleMathView) {
        Logger.d(TAG, "onRuleClicked")
        val subst = ruleView.subst ?: return
        val res = globalMathView.performSubstitutionForMultiselect(subst)
        if (res != null) {
            if (wantedRule) {
                ruleClickSucceeded()
            }
            if (TutorialScene.shared.currentLevel.checkEnd(res)) {
                if (TutorialScene.shared.currLevelIndex == 0) {
                    levelPassed()
                } else if (TutorialScene.shared.currLevelIndex == 1) {
                    bothLevelsPassed()
                }
            }
            clearRules()
        } else {
            showMessage(R.string.wrong_subs)
        }
    }

    override fun onAtomClicked() {
        Logger.d(TAG, "onAtomClicked")
        if (wantedZoom) {
            return
        }
        if (globalMathView.currentAtoms.isNotEmpty()) {
            val substitutionApplication = TutorialScene.shared.currentLevel.getSubstitutionApplication(
                globalMathView.currentAtoms,
                globalMathView.expression!!
            )

            if (substitutionApplication == null) {
                val atoms = globalMathView.currentAtoms
                val inMS = globalMathView.multiselectionMode
                if (TutorialScene.shared.currLevelIndex == 1 && atoms.size == 1 && atoms[0].toString() == "6" && inMS) {
                    showMessage(R.string.tutorial_on_level_multiselect_digit)
                } else {
                    showMessage(R.string.no_rules_try_another)
                    if (!globalMathView.multiselectionMode) {
                        globalMathView.recolorCurrentAtom(Color.RED)
                    }
                }
                clearRules()
            } else {
                val rules =
                    TutorialScene.shared.currentLevel.getRulesFromSubstitutionApplication(substitutionApplication)
                globalMathView.currentRulesToResult =
                    TutorialScene.shared.currentLevel.getResultFromSubstitutionApplication(substitutionApplication)

                if (wantedClick) {
                    expressionClickSucceeded()
                } else {
                    showMessage(R.string.a_good_choice)
                }
                redrawRules(rules, TutorialScene.shared.currentLevel.subjectType)
            }
        }
    }
}
