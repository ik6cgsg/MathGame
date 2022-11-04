package mathhelper.games.matify

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.text.Html
import android.text.SpannedString
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import mathhelper.twf.expressiontree.ExpressionSubstitution
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mathhelper.games.matify.common.*
import mathhelper.games.matify.tutorial.TutorialPlayActivity
import mathhelper.games.matify.game.Game
import mathhelper.games.matify.level.Level
import mathhelper.games.matify.tutorial.TutorialGamesActivity
import mathhelper.games.matify.tutorial.TutorialLevelsActivity
import java.lang.ref.WeakReference

class TutorialScene {
    companion object {
        private const val TAG = "TutorialScene"
        val shared: TutorialScene = TutorialScene()
        private const val duration = 600.toLong()
        private const val translate = -30f
    }

    var gamesActivityRef: WeakReference<TutorialGamesActivity> = WeakReference(null)
    fun initTGA(tga: TutorialGamesActivity?) {
        gamesActivityRef = WeakReference(tga)
        if (tga != null) {
            tutorialDialog = tga.dialog
            leaveDialog = tga.leave
            currentStep = -1
            nextStep()
        }
    }

    var levelsActivityRef: WeakReference<TutorialLevelsActivity> = WeakReference(null)
    fun initTLA(tla: TutorialLevelsActivity?) {
        levelsActivityRef = WeakReference(tla)
        if (tla != null) {
            tutorialDialog = tla.dialog
            leaveDialog = tla.leave
            GlobalScope.launch {
                val job = async {
                    val loaded = tutorialGame!!.load(tla)
                    tla.runOnUiThread {
                        if (loaded) {
                            tla.onLoad()
                            currentLevel = tutorialGame!!.levels[0]
                            nextStep()
                        }
                    }
                }
                job.await()
            }
        }
    }

    var playActivityRef: WeakReference<PlaySceneListener> = WeakReference(null)
    fun initTPA(tpa: TutorialPlayActivity?) {
        playActivityRef = WeakReference(tpa)
        if (tpa != null) {
            tutorialDialog = tpa.tutorialDialog
            leaveDialog = tpa.leaveDialog
        }
    }

    private fun getNotNullActivity(): AppCompatActivity? = if (gamesActivityRef.get() != null) {
        gamesActivityRef.get()
    } else if (levelsActivityRef.get() != null) {
        levelsActivityRef.get()
    } else if (playActivityRef.get() != null) {
        playActivityRef.get() as? AppCompatActivity
    } else null

    fun getResourceString(id: Int) = getNotNullActivity()?.resources?.getString(id)

    lateinit var currentLevel: Level
    var currLevelIndex = 0

    var tutorialDialog: AlertDialog? = null
    var leaveDialog: AlertDialog? = null
    var tutorialGame: Game? = null

    var wantedZoom = false
    var wantedClick = false
    var wantedRule = false

    private var currentAnim: AnimatorSet? = null
    private var currentAnimViewRef: WeakReference<View> = WeakReference(null)

    lateinit var steps: ArrayList<Step>
    var stepsSize = 0
    var currentStep = -1

    private var shouldFinishLevelsActivity = false
    private var shouldFinishPlayActivity = false

    class Step(var onEnter: () -> Unit, var onExit: () -> Unit) {
    }

    fun start(context: Context) {
        GlobalScene.shared.tutorialProcessing = true
        val input = context.assets.open("tutorial.json")
        val text = input.readBytes().toString(Charsets.UTF_8)
        input.close()
        val info = Gson().fromJson(text, TasksetInfo::class.java)
        tutorialGame = Game.create(info, context)
        if (tutorialGame == null) {
            Logger.d(TAG, "failed to load tutorial")
            return
        }
        context.startActivity(Intent(context, TutorialGamesActivity::class.java))
        steps = arrayListOf(
            // Games Layout
            Step({
                gamesActivityRef.get()!!.tellAboutGameLayout()
            }, {}),
            Step({
                gamesActivityRef.get()!!.waitForGameClick()
            }, {}),
            // Levels layout
            Step({
                shouldFinishLevelsActivity = true
                levelsActivityRef.get()!!.tellAboutLevelLayout()
            }, {}),
            Step({
                shouldFinishLevelsActivity = false
                levelsActivityRef.get()!!.waitForLevelClick()
            }, {}),
            // Play layout, basic
            Step({
                shouldFinishPlayActivity = true
                (playActivityRef.get() as TutorialPlayActivity).messageTutorial()
            }, {}),
            Step({
                shouldFinishPlayActivity = false
                (playActivityRef.get() as TutorialPlayActivity).endExpressionTutorial()
            }, {}),
            Step({
                (playActivityRef.get() as TutorialPlayActivity).centralExpressionTutorial()
            }, {}),
            Step({
                (playActivityRef.get() as TutorialPlayActivity).backTutorial()
            }, {}),
            Step({
                (playActivityRef.get() as TutorialPlayActivity).infoTutorial()
            }, {}),
            Step({
                (playActivityRef.get() as TutorialPlayActivity).restartTutorial()
            }, {}),
            Step({
                (playActivityRef.get() as TutorialPlayActivity).undoTutorial()
            }, {}),
            Step({
                if (currLevelIndex != 0) {
                    currLevelIndex = 0
                    currentLevel = tutorialGame!!.levels[currLevelIndex]
                    loadLevel()
                }
                (playActivityRef.get() as TutorialPlayActivity).startDynamicTutorial()
            }, {}),
            Step({
                if (currLevelIndex != 1) {
                    currLevelIndex = 1
                    currentLevel = tutorialGame!!.levels[currLevelIndex]
                    loadLevel()
                }
                val activity = playActivityRef.get() as TutorialPlayActivity
                activity.tutorialDialog.window!!.attributes.y += 500
                activity.explainMultiselectTutorial()
            }, {
                (playActivityRef.get() as TutorialPlayActivity).tutorialDialog.window!!.attributes.y -= 500
            }),
            Step({
                val activity = playActivityRef.get() as TutorialPlayActivity
                activity.tutorialDialog.window!!.attributes.y += 500
                activity.actionMultiselectTutorial()
            }, {
                (playActivityRef.get() as TutorialPlayActivity).tutorialDialog.window!!.attributes.y -= 500
            }),
            Step({
                (playActivityRef.get() as TutorialPlayActivity).startMultiselectTutorial()
            }, {
                InstrumentScene.shared.turnOffCurrentInstrument()
            })
        )
        stepsSize = steps.size - 2
        currentStep = -1
    }

    fun nextStep() {
        Logger.d(TAG, "nextStep")
        if (currentStep != -1) {
            steps[currentStep].onExit()
        }
        currentStep++
        if (currentStep == steps.size) {
            return
        }
        tutorialDialog!!.setTitle("${getResourceString(R.string.tutorial) ?: "Tutorial"}: ${stepToDisplay()} / $stepsSize")
        steps[currentStep].onEnter()
    }

    fun prevStep() {
        Logger.d(TAG, "prevStep")
        steps[currentStep].onExit()
        currentStep--
        if (currentStep == -1) {
            AndroidUtil.showDialog(leaveDialog!!)
        } else {
            tutorialDialog!!.setTitle("${getResourceString(R.string.tutorial) ?: "Tutorial"}: ${stepToDisplay()} / $stepsSize")
            steps[currentStep].onEnter()
        }
    }

    fun stepToDisplay() = if (currentStep <= 1) {
        1
    } else if (currentStep > 3) {
        currentStep - 1
    } else 2

    fun loadLevel() {
        Logger.d(TAG, "loadLevel")
        val activity = (playActivityRef.get() as TutorialPlayActivity)
        activity.clearRules()

        activity.endExpressionViewLabel.text = Html.fromHtml(
            String.format(
                Html.toHtml(SpannedString(activity.getText(R.string.end_expression_opened))),
                currentLevel.getDescriptionByLanguage(activity.resources.configuration.locale.language)
            )
        )
        activity.endExpressionViewLabel.visibility = View.VISIBLE
        activity.endExpressionMathView.visibility = View.GONE
        if (!currentLevel.goalExpressionStructureString.isNullOrBlank()) {
            activity.endExpressionMathView.setExpression(currentLevel.goalExpressionStructureString!!, null)
            activity.endExpressionMathView.visibility = View.VISIBLE
        }

        activity.globalMathView.setExpression(
            currentLevel.startExpression.clone(),
            currentLevel.subjectType,
            true
        )
        activity.globalMathView.center()
    }

    fun onRuleClicked(ruleView: RuleMathView) {
        Logger.d(TAG, "onRuleClicked")
        val activity = (playActivityRef.get() as TutorialPlayActivity)
        if (ruleView.subst != null) {
            val res = activity.globalMathView.performSubstitutionForMultiselect(ruleView.subst!!)
            if (res != null) {
                if (wantedRule) {
                    activity.ruleClickSucceeded()
                }
                if (currentLevel.checkEnd(res)) {
                    if (currLevelIndex == 0) {
                        activity.levelPassed()
                    } else if (currLevelIndex == 1) {
                        activity.bothLevelsPassed()
                    }
                }
                activity.clearRules()
            } else {
                showMessage(activity.resources.getString(R.string.wrong_subs))
            }
        }
    }

    fun onAtomClicked() {
        Logger.d(TAG, "onAtomClicked")
        if (wantedZoom) {
            return
        }
        val activity = (playActivityRef.get() as TutorialPlayActivity)
        if (activity.globalMathView.currentAtoms.isNotEmpty()) {
            val substitutionApplication = currentLevel.getSubstitutionApplication(
                activity.globalMathView.currentAtoms,
                activity.globalMathView.expression!!
            )

            if (substitutionApplication == null) {
                val atoms = activity.globalMathView.currentAtoms
                val inMS = activity.globalMathView.multiselectionMode
                if (currLevelIndex == 1 && atoms.size == 1 && atoms[0].toString() == "6" && inMS) {
                    showMessage(activity.getString(R.string.tutorial_on_level_multiselect_digit))
                } else {
                    showMessage(activity.getString(R.string.no_rules_try_another))
                    if (!activity.globalMathView.multiselectionMode) {
                        activity.globalMathView.recolorCurrentAtom(Color.RED)
                    }
                }
                activity.clearRules()
            } else {
                val rules =
                    currentLevel.getRulesFromSubstitutionApplication(substitutionApplication)
                activity.globalMathView.currentRulesToResult =
                    currentLevel.getResultFromSubstitutionApplication(substitutionApplication)

                activity.rulesScrollView.visibility = View.VISIBLE
                if (wantedClick) {
                    activity.expressionClickSucceeded()
                } else {
                    showMessage(activity.resources.getString(R.string.a_good_choice))
                }
                redrawRules(rules)
            }
        }
    }


    private fun redrawRules(rules: List<ExpressionSubstitution>) {
        Logger.d(TAG, "redrawRules")
        val activity = playActivityRef.get()!!
        activity.rulesLinearLayout.removeAllViews()
        for (r in rules) {
            try {
                val rule = RuleMathView(activity as Context)
                rule.setSubst(r, currentLevel.subjectType)
                activity.rulesLinearLayout.addView(rule)
            } catch (e: Exception) {
                Logger.e(TAG, "Rule draw Error: $e")
            }
        }
        activity.halfExpandBottomSheet()
        activity.rulesMsg.text = if (rules.isEmpty()) activity.getString(R.string.no_rules_msg)
        else activity.getString(R.string.rules_found_msg)
    }

    fun showMessage(msg: String) {
        val activity = playActivityRef.get()!!
        activity.messageView.text = msg
        activity.messageView.visibility = View.VISIBLE
    }

    fun leave() {
        GlobalScene.shared.tutorialProcessing = false

        playActivityRef.get()?.let { (it as TutorialPlayActivity).finish() }
        levelsActivityRef.get()?.finish()
        gamesActivityRef.get()?.finish()
    }

    fun restart() {
        playActivityRef.get()?.let { (it as TutorialPlayActivity).finish() }
        levelsActivityRef.get()?.finish()
        currentStep = -1
        nextStep()
    }

    fun animateLeftUp(view: View) {
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
        currentAnimViewRef = WeakReference(view)
        view.visibility = View.VISIBLE
    }

    fun animateUp(view: View) {
        val animationY = ObjectAnimator.ofFloat(view, "translationY", translate)
        animationY.repeatMode = ValueAnimator.REVERSE
        animationY.repeatCount = ValueAnimator.INFINITE
        val set = AnimatorSet()
        set.play(animationY)
        set.duration = duration
        set.start()
        currentAnim = set
        currentAnimViewRef = WeakReference(view)
        view.visibility = View.VISIBLE
    }

    fun stopAnimation() {
        currentAnim?.let {
            it.removeAllListeners()
            it.end()
            it.cancel()
            currentAnim = null
            val currentAnimView = currentAnimViewRef.get()!!
            currentAnimView.translationY = 0f
            currentAnimView.translationX = 0f
            currentAnimView.visibility = View.GONE
        }
    }

    fun createTutorialDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(
            context, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle("")
            .setMessage(R.string.got_it)
            .setPositiveButton(R.string.yep) { dialog: DialogInterface, id: Int ->
                stopAnimation()
                Handler().postDelayed({
                    nextStep()
                }, 100)
            }
            .setNegativeButton(R.string.step_back) { dialog: DialogInterface, id: Int ->
                stopAnimation()
                playActivityRef.get()?.let {
                    if (shouldFinishPlayActivity) {
                        (it as TutorialPlayActivity).finish()
                        currentStep--
                    }
                }
                levelsActivityRef.get()?.let {
                    if (shouldFinishLevelsActivity) {
                        it.finish()
                        currentStep--
                    }
                }
                Handler().postDelayed({
                    prevStep()
                }, 100)
            }
            .setNeutralButton(R.string.leave) { dialog: DialogInterface, id: Int ->
                if (leaveDialog != null) {
                    AndroidUtil.showDialog(leaveDialog!!)
                } else {
                    leave()
                }
            }
            .setCancelable(false)
        return builder.create()
    }

    fun createLeaveDialog(context: Context): AlertDialog {
        Logger.d(TAG, "createLeaveDialog")
        val builder = AlertDialog.Builder(
            context, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.attention)
            .setMessage(R.string.wanna_leave)
            .setPositiveButton(R.string.yes) { dialog: DialogInterface, id: Int ->
                leave()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int ->
                if (currentStep != -1) {
                    currentStep--
                }
                nextStep()
            }
        return builder.create()
    }
}