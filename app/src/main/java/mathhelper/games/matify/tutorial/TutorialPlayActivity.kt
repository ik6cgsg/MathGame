package mathhelper.games.matify.tutorial

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.SpannedString
import android.view.*
import android.widget.*
import mathhelper.games.matify.*
import mathhelper.games.matify.activities.AbstractPlayableActivity
import mathhelper.games.matify.common.*
import java.lang.ref.WeakReference


class TutorialPlayActivity : AbstractPlayableActivity(), TutorialSceneListener {
    override val TAG = "TutorialPlayActivity"

    // lateinit var noRules: TextView
    private lateinit var pointerMsgView: TextView
    private lateinit var pointerEndView: TextView
    private lateinit var pointerCentralView: TextView
    private lateinit var pointerBackView: TextView
    private lateinit var pointerRestartView: TextView
    private lateinit var pointerUndoView: TextView
    private lateinit var pointerInfoView: TextView
    private lateinit var pointerView: TrackingMathPointer

    companion object {
        const val totalSteps = 8
    }

    private val steps: Array<() -> Unit> = arrayOf(
        this::messageTutorial,
        this::endExpressionTutorial,
        this::centralExpressionTutorial,
        this::backTutorial,
        this::infoTutorial,
        this::restartTutorial,
        this::undoTutorial,
        this::startDynamicTutorial
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
        pointerView = findViewById(R.id.pointer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt())
        setContentView(R.layout.tutorial_activity_play_new)
        setViews()
        val button: TableLayout = bottomSheet.findViewById(R.id.account_table)
        button.visibility = View.GONE
        TutorialScene.shared.createLeaveDialog(this)
        TutorialScene.shared.createTutorialDialog(this)
        TutorialScene.shared.createRestartDialog(this)
        timerView.text = "⏰ 1:23"
        globalMathView.text = ""
        endExpressionViewLabel.text = ""

        TutorialScene.shared.switchLevel(0)

        TutorialScene.shared.listenerRef = WeakReference(this)
        loadLevel()
        if (TutorialScene.shared.currentlyAdvancing) {
            currentStep = -1
            nextStep()
        } else {
            currentStep = steps.size
            prevStep()
        }
    }

    private fun loadLevel() {
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
        TutorialScene.shared.restartDialog?.let {
            AndroidUtil.showDialog(it)
        }
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

    private fun messageTutorial() {
        Logger.d(TAG, "messageTutorial")
        showMessage(R.string.tutorial_on_level_info)
        TutorialScene.shared.animateLeftUp(pointerMsgView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    private fun backTutorial() {
        Logger.d(TAG, "backTutorial")
        showMessage(R.string.tutorial_on_level_to_menu)
        TutorialScene.shared.animateLeftUp(pointerBackView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    private fun infoTutorial() {
        Logger.d(TAG, "infoTutorial")
        showMessage(R.string.tutorial_on_level_short_info)
        TutorialScene.shared.animateLeftUp(pointerInfoView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    private fun restartTutorial() {
        Logger.d(TAG, "restartTutorial")
        showMessage(R.string.tutorial_on_level_restart)
        TutorialScene.shared.animateUp(pointerRestartView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    private fun undoTutorial() {
        Logger.d(TAG, "undoTutorial")
        showMessage(R.string.tutorial_on_level_undo)
        TutorialScene.shared.animateUp(pointerUndoView)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(resources.getString(R.string.got_it))
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    private fun endExpressionTutorial() {
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

    private fun centralExpressionTutorial() {
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

    private fun startDynamicTutorial() {
        showMessage(R.string.tutorial_on_level_tap)
        val pluses = globalMathView.getNodesByString("+")
        if (pluses.isNotEmpty()) {
            pointerView.setTrackerToExpression(pluses[0], globalMathView)
        }
        wantedZoom = false
        wantedClick = true
    }

    private fun expressionClickSucceeded() {
        wantedClick = false
        showMessage(R.string.tutorial_on_level_select)
        wantedRule = true
    }

    private fun ruleClickSucceeded() {
        showMessage(R.string.tutorial_on_level_win)
        Handler().postDelayed({
            val pluses = globalMathView.getNodesByString("+")
            if (pluses.isNotEmpty()) {
                pointerView.setTrackerToExpression(pluses[0], globalMathView)
            } else {
                val stars = globalMathView.getNodesByString("*")
                if (stars.isNotEmpty()) {
                    pointerView.setTrackerToExpression(stars[0], globalMathView)
                }
            }
        }, 400)
    }

    private fun levelPassed() {
        Logger.d(TAG, "levelPassed")
        showMessage(R.string.congratulations)
        globalMathView.center()
        TutorialScene.shared.animateLeftUp(pointerCentralView)
        TutorialScene.shared.nextStep(this)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        pointerView.followNode(globalMathView)
        return true
    }

    override fun onRuleClicked(ruleView: RuleMathView) {
        Logger.d(TAG, "onRuleClicked")
        pointerView.resetTracker()
        val subst = ruleView.subst ?: return
        val res = globalMathView.performSubstitutionForMultiselect(subst)
        if (res != null) {
            if (TutorialScene.shared.currentLevel.checkEnd(res)) {
                levelPassed()
            } else if (wantedRule) {
                ruleClickSucceeded()
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
                showMessage(R.string.no_rules_try_another)
                if (!globalMathView.multiselectionMode) {
                    globalMathView.recolorCurrentAtom(Color.RED)
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
