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
    private var currentAnimView: View? = null

    lateinit var steps: ArrayList<() -> Any>
    var stepsSize = 0
    var currentStep = -1

    private var shouldFinishLevelsActivity = false
    private var shouldFinishPlayActivity = false

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
            {
                gamesActivityRef.get()!!.tellAboutGameLayout()
            },
            {
                gamesActivityRef.get()!!.waitForGameClick()
            },
            // Levels layout
            {
                shouldFinishLevelsActivity = true
                levelsActivityRef.get()!!.tellAboutLevelLayout()
            },
            {
                shouldFinishLevelsActivity = false
                levelsActivityRef.get()!!.waitForLevelClick()
            },
            // Play layout, basic
            {
                shouldFinishPlayActivity = true
                (playActivityRef.get() as TutorialPlayActivity).messageTutorial()
            },
            {
                shouldFinishPlayActivity = false
                (playActivityRef.get() as TutorialPlayActivity).endExpressionTutorial()
            },
            {
                (playActivityRef.get() as TutorialPlayActivity).centralExpressionTutorial()
            },
            {
                (playActivityRef.get() as TutorialPlayActivity).backTutorial()
            },
            {
                (playActivityRef.get() as TutorialPlayActivity).infoTutorial()
            },
            {
                (playActivityRef.get() as TutorialPlayActivity).restartTutorial()
            },
            {
                (playActivityRef.get() as TutorialPlayActivity).undoTutorial()
            },
            {
                if (currLevelIndex != 0) {
                    currLevelIndex = 0
                    currentLevel = tutorialGame!!.levels[currLevelIndex]
                    loadLevel()
                }
                (playActivityRef.get() as TutorialPlayActivity).startDynamicTutorial()
            },
            {
                if (currLevelIndex != 1) {
                    currLevelIndex = 1
                    currentLevel = tutorialGame!!.levels[currLevelIndex]
                    loadLevel()
                }
                (playActivityRef.get() as TutorialPlayActivity).explainMultiselectTutorial()
            },
            {
                (playActivityRef.get() as TutorialPlayActivity).actionMultiselectTutorial()
            },
            {
                (playActivityRef.get() as TutorialPlayActivity).startMultiselectTutorial()
            }
        )
        stepsSize = steps.size - 2
        currentStep = -1
    }

    fun nextStep() {
        Logger.d(TAG, "nextStep")
        currentStep++
        if (currentStep == steps.size) {
            return
        }
        tutorialDialog!!.setTitle("${getResourceString(R.string.tutorial) ?: "Tutorial"}: ${stepToDisplay()} / $stepsSize")
        steps[currentStep]()
    }

    fun prevStep() {
        Logger.d(TAG, "prevStep")
        currentStep--
        if (currentStep == -1) {
            AndroidUtil.showDialog(leaveDialog!!)
        } else {
            tutorialDialog!!.setTitle("${getResourceString(R.string.tutorial) ?: "Tutorial"}: ${stepToDisplay()} / $stepsSize")
            steps[currentStep]()
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
                showMessage(activity.getString(R.string.no_rules_try_another))
                activity.clearRules()
                if (!activity.globalMathView.multiselectionMode) {
                    activity.globalMathView.recolorCurrentAtom(Color.RED)
                }
            } else {
                val rules =
                    currentLevel.getRulesFromSubstitutionApplication(substitutionApplication)
                activity.globalMathView.currentRulesToResult =
                    currentLevel.getResultFromSubstitutionApplication(substitutionApplication)

                // activity.noRules.visibility = View.GONE
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
        currentAnimView = view
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
        currentAnimView = view
    }

    fun stopAnimation() {
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