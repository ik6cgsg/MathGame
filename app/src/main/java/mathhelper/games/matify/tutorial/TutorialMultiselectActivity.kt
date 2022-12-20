package mathhelper.games.matify.tutorial

import android.app.AlertDialog
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
import mathhelper.games.matify.mathResolver.MathResolverNodeBase
import java.lang.ref.WeakReference


class TutorialMultiselectActivity : AbstractPlayableActivity(), TutorialSceneListener, InstrumentSceneListener {
    override val TAG = "TutorialMultiselectActivity"

    private lateinit var buttonTable: TableLayout
    private lateinit var pointerCentralView: TextView
    private lateinit var pointerView: TrackingMathPointer

    companion object {
        const val totalSteps = 3
    }

    private val steps: Array<() -> Unit> = arrayOf(
        this::explainMultiselectTutorial,
        this::actionMultiselectTutorial,
        this::startMultiselectTutorial,
    )
    private var currentStep = -1

    var wantedClick = false
    var wantedRule = false

    override fun setViews() {
        super.setViews()
        mainView = findViewById(R.id.tutorial_activity_play)
        mainViewAnim = mainView.background as TransitionDrawable

        // noRules = findViewById(R.id.no_rules)
        pointerCentralView = findViewById(R.id.pointer_central)
        pointerView = findViewById(R.id.pointer)
        buttonTable = bottomSheet.findViewById(R.id.account_table)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt())
        setContentView(R.layout.tutorial_activity_play_new)
        setViews()
        pointerView.visibility = View.INVISIBLE
        TutorialScene.shared.createLeaveDialog(this)
        TutorialScene.shared.createTutorialDialog(this)
        TutorialScene.shared.createRestartDialog(this)
        timerView.text = "⏰ 1:23"
        globalMathView.text = ""
        endExpressionViewLabel.text = ""

        TutorialScene.shared.switchLevel(1)

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
        centerMathViewAsync()
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

    private fun explainMultiselectTutorial() {
        buttonTable.visibility = View.VISIBLE

        Logger.d(TAG, "explainMultiselectTutorial")
        showMessage(R.string.tutorial_on_level_multiselect_explanation)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_multiselect_expression)
        )
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    private fun actionMultiselectTutorial() {
        Logger.d(TAG, "actionMultiselectTutorial")
        showMessage(R.string.tutorial_on_level_multiselect_action)
        val tutorialDialog = TutorialScene.shared.tutorialDialog ?: return
        tutorialDialog.setMessage(
            resources.getString(R.string.tutorial_on_level_multiselect_details)
        )
        AndroidUtil.showDialog(tutorialDialog, backMode = BackgroundMode.NONE)
    }

    private fun startMultiselectTutorial() {
        showMessage(R.string.tutorial_on_level_multiselect_button)
        pointerView.pointToStaticView(buttonTable)
        wantedClick = true
    }

    private fun expressionClickSucceeded() {
        wantedClick = false
        showMessage(R.string.tutorial_on_level_multiselect_select)
        wantedRule = true
    }

    private fun ruleClickSucceeded() {
        wantedRule = false
        showMessage(R.string.tutorial_on_level_multiselect_click)
        Handler().postDelayed({
            val plus = globalMathView.getNodesByString("+")
            if (plus.isNotEmpty()) {
                pointerView.setTrackerToExpression(plus[0], globalMathView)
            }
        }, 600)
    }

    private fun levelPassed() {
        Logger.d(TAG, "tutorial over")
        showMessage(R.string.congratulations)
        centerMathViewAsync()
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

    override fun startInstrumentProcessing(setMSMode: Boolean) {
        super.startInstrumentProcessing(setMSMode)
        showMessage(R.string.tutorial_on_level_multiselect_partial_select)
        findFirstNotSelectedSix()?.let {
            pointerView.setTrackerToExpression(it, globalMathView)
        }
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
            if (wantedRule) {
                ruleClickSucceeded()
            }
            if (TutorialScene.shared.currentLevel.checkEnd(res)) {
                levelPassed()
            }
            clearRules()
        } else {
            showMessage(R.string.wrong_subs)
        }
    }

    override fun onAtomClicked() {
        Logger.d(TAG, "onAtomClicked")
        pointerView.resetTracker()
        if (globalMathView.currentAtoms.isNotEmpty()) {
            val substitutionApplication = TutorialScene.shared.currentLevel.getSubstitutionApplication(
                globalMathView.currentAtoms,
                globalMathView.expression!!
            )

            if (substitutionApplication == null) {
                val atoms = globalMathView.currentAtoms
                val inMS = globalMathView.multiselectionMode
                if (atoms.size == 1 && atoms[0].toString() == "6" && inMS) {
                    showMessage(R.string.tutorial_on_level_multiselect_digit)
                    findFirstNotSelectedSix()?.let {
                        pointerView.setTrackerToExpression(it, globalMathView)
                    }
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

    private fun findFirstNotSelectedSix(): MathResolverNodeBase? {
        val sixes = globalMathView.getNodesByString("6")
        for (node in sixes) {
            if (!globalMathView.currentAtoms.contains(node.origin)) {
                return node
            }
        }
        return null
    }
}
