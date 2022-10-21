package mathhelper.games.matify

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Handler
import android.view.View
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import mathhelper.twf.expressiontree.SimpleComputationRuleParams
import mathhelper.twf.expressiontree.ExpressionSubstitution
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mathhelper.games.matify.common.*
import mathhelper.games.matify.game.FullTaskset
import mathhelper.games.matify.tutorial.TutorialPlayActivity
import mathhelper.games.matify.game.Game
import mathhelper.games.matify.level.Level
import mathhelper.games.matify.mathResolver.MathResolver
import mathhelper.games.matify.mathResolver.TaskType
import mathhelper.games.matify.parser.GsonParser
import mathhelper.games.matify.statistics.Statistics
import mathhelper.games.matify.tutorial.TutorialGamesActivity
import mathhelper.games.matify.tutorial.TutorialLevelsActivity
import java.io.File

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
                                tutorialLevel = tutorialGame!!.levels[0]
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
                // TODO!: rethink this entire approach alongside similar uses of delaying
                Handler().postDelayed({
                    try {
                        loadLevel()
                    } catch (e: Exception) {
                        Logger.e(TAG, "Error while loading a level")
                        Toast.makeText(value, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                    }
                    nextStep()
                }, 100)
            }
        }

    fun getNotNullActivity() = if (tutorialGamesActivity != null) {
        tutorialGamesActivity
    } else if (tutorialLevelsActivity != null) {
        tutorialLevelsActivity
    } else if (tutorialPlayActivity != null) {
        tutorialPlayActivity
    } else null

    fun getResourceString(id: Int) = getNotNullActivity()?.resources?.getString(id) ?: null

    lateinit var tutorialLevel: Level

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
    private var currentStep = -1

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
            // Play layout
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
            { tutorialPlayActivity!!.startDynamicTutorial() }
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

    private fun stepToDisplay() = if (currentStep <= 1) {
        1
    } else if (currentStep > 3) {
        currentStep - 1
    } else 2

    fun loadLevel() {
        Logger.d(TAG, "loadLevel")
        val activity = tutorialPlayActivity!!
        clearRules()
        activity.endExpressionViewLabel.text = tutorialLevel.getDescriptionByLanguage(
            activity.resources.configuration.locale.language
        )
        activity.endExpressionViewLabel.visibility = View.VISIBLE

        activity.endExpressionView.text = tutorialLevel.goalExpressionStructureString
        activity.endExpressionView.visibility = View.VISIBLE
        /*if (activity.endExpressionView.visibility != View.VISIBLE) {
            activity.showEndExpression(null)
        }*/


        tutorialPlayActivity!!.globalMathView.setExpression(
            tutorialLevel.startExpression.clone(),
            tutorialLevel.subjectType,
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
                if (tutorialLevel.checkEnd(res)) {
                    activity.levelPassed()
                }
                clearRules()
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
            val substitutionApplication = tutorialLevel.getSubstitutionApplication(
                activity.globalMathView.currentAtoms,
                activity.globalMathView.expression!!
            )

            if (substitutionApplication == null) {
                showMessage(activity.getString(R.string.no_rules_try_another))
                clearRules()
                activity.globalMathView.recolorCurrentAtom(Color.YELLOW)
            } else {
                val rules =
                    tutorialLevel.getRulesFromSubstitutionApplication(substitutionApplication)
                activity.globalMathView.currentRulesToResult =
                    tutorialLevel.getResultFromSubstitutionApplication(substitutionApplication)

                activity.noRules.visibility = View.GONE
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


    fun clearRules() {
        val activity = tutorialPlayActivity!!
        activity.rulesScrollView.visibility = View.INVISIBLE
        activity.noRules.visibility = View.VISIBLE
    }

    private fun redrawRules(rules: List<ExpressionSubstitution>) {
        Logger.d(TAG, "redrawRules")
        val activity = tutorialPlayActivity!!
        activity.rulesLinearLayout.removeAllViews()
        for (r in rules) {
            val rule = RuleMathView(activity)
            rule.setSubst(r, tutorialLevel.subjectType)
            activity.rulesLinearLayout.addView(rule)
        }
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