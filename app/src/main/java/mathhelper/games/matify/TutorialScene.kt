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

class TutorialScene {
    companion object {
        private const val TAG = "TutorialScene"
        val shared: TutorialScene = TutorialScene()
        private const val duration = 600.toLong()
        private const val translate = -30f
    }

    var tutorialGamesActivity: TutorialGamesActivity? = null
        set(value) {
            field = value
            if (value != null) {
                tutorialDialog = value.dialog
                leaveDialog = value.leave
                currentStep = -1
                nextStep()
            }
        }
    var tutorialLevelsActivity: TutorialLevelsActivity? = null
        set(value) {
            field = value
            if (value != null) {
                tutorialDialog = value.dialog
                leaveDialog = value.leave
                GlobalScope.launch {
                    val job = async {
                        val loaded = tutorialGame!!.load(value)
                        value.runOnUiThread {
                            if (loaded) {
                                value.onLoad()
                                currentLevel = tutorialGame!!.levels[0]
                                nextStep()
                            }
                        }
                    }
                    job.await()
                }
            }
        }
    var tutorialPlayActivity: TutorialPlayActivity? = null
        set(value) {
            field = value
            if (value != null) {
                tutorialDialog = value.tutorialDialog
                leaveDialog = value.leaveDialog
            }
        }

    private fun getNotNullActivity() = if (tutorialGamesActivity != null) {
        tutorialGamesActivity
    } else if (tutorialLevelsActivity != null) {
        tutorialLevelsActivity
    } else if (tutorialPlayActivity != null) {
        tutorialPlayActivity
    } else null

    fun getResourceString(id: Int) = getNotNullActivity()?.resources?.getString(id)

    lateinit var currentLevel: Level
    var currLevelIndex = 0
    var instrumentProcessing: Boolean = false

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
                tutorialGamesActivity!!.tellAboutGameLayout()
            },
            {
                tutorialGamesActivity!!.waitForGameClick()
            },
            // Levels layout
            {
                shouldFinishLevelsActivity = true
                tutorialLevelsActivity!!.tellAboutLevelLayout()
            },
            {
                shouldFinishLevelsActivity = false
                tutorialLevelsActivity!!.waitForLevelClick()
            },
            // Play layout, basic
            {
                shouldFinishPlayActivity = true
                tutorialPlayActivity!!.messageTutorial()
            },
            {
                shouldFinishPlayActivity = false
                tutorialPlayActivity!!.endExpressionTutorial()
            },
            {
                tutorialPlayActivity!!.centralExpressionTutorial()
            },
            {
                tutorialPlayActivity!!.backTutorial()
            },
            {
                tutorialPlayActivity!!.infoTutorial()
            },
            {
                tutorialPlayActivity!!.restartTutorial()
            },
            {
                tutorialPlayActivity!!.undoTutorial()
            },
            {
                if (currLevelIndex != 0) {
                    currLevelIndex = 0
                    currentLevel = tutorialGame!!.levels[currLevelIndex]
                    loadLevel()
                }
                tutorialPlayActivity!!.startDynamicTutorial()
            },
            {
                if (currLevelIndex != 1) {
                    currLevelIndex = 1
                    currentLevel = tutorialGame!!.levels[currLevelIndex]
                    loadLevel()
                }
                tutorialPlayActivity!!.explainMultiselectTutorial()
            },
            {
                tutorialPlayActivity!!.actionMultiselectTutorial()
            },
            {
                tutorialPlayActivity!!.startMultiselectTutorial()
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
        val activity = tutorialPlayActivity!!
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

        tutorialPlayActivity!!.globalMathView.setExpression(
            currentLevel.startExpression.clone(),
            currentLevel.subjectType,
            true
        )
        tutorialPlayActivity!!.globalMathView.center()
    }

    fun onRuleClicked(ruleView: RuleMathView) {
        Logger.d(TAG, "onRuleClicked")
        val activity = tutorialPlayActivity!!
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
        val activity = tutorialPlayActivity!!
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
        val activity = tutorialPlayActivity!!
        activity.rulesLinearLayout.removeAllViews()
        for (r in rules) {
            try {
                val rule = RuleMathView(activity)
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
        val activity = tutorialPlayActivity!!
        activity.messageView.text = msg
        activity.messageView.visibility = View.VISIBLE
    }

    fun leave() {
        GlobalScene.shared.tutorialProcessing = false
        if (tutorialPlayActivity != null) {
            tutorialPlayActivity!!.finish()
        }
        if (tutorialLevelsActivity != null) {
            tutorialLevelsActivity!!.finish()
        }
        if (tutorialGamesActivity != null) {
            tutorialGamesActivity!!.finish()
        }
    }

    fun restart() {
        if (tutorialPlayActivity != null) {
            tutorialPlayActivity!!.finish()
        }
        if (tutorialLevelsActivity != null) {
            tutorialLevelsActivity!!.finish()
        }
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
                if (shouldFinishPlayActivity && tutorialPlayActivity != null) {
                    tutorialPlayActivity!!.finish()
                    currentStep--
                }
                if (shouldFinishLevelsActivity && tutorialLevelsActivity != null) {
                    tutorialLevelsActivity!!.finish()
                    currentStep--
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