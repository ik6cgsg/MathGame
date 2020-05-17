package spbpu.hsamcp.mathgame

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.util.Log
import com.twf.expressiontree.ExpressionSubstitution
import spbpu.hsamcp.mathgame.activities.LevelsActivity
import spbpu.hsamcp.mathgame.activities.TutorialActivity
import spbpu.hsamcp.mathgame.common.RuleMathView
import spbpu.hsamcp.mathgame.level.Level
import spbpu.hsamcp.mathgame.level.Type
import spbpu.hsamcp.mathgame.mathResolver.MathResolver
import spbpu.hsamcp.mathgame.mathResolver.TaskType
import java.lang.ref.WeakReference

class TutorialScene {
    companion object {
        private const val TAG = "TutorialScene"
        lateinit var tutorialActivity: WeakReference<TutorialActivity>
        lateinit var levelsActivity: WeakReference<LevelsActivity>
        lateinit var tutorialLevel: Level
        var wantedZoom = false
        var wantedClick = false
        var wantedRule = false

        fun start(context: LevelsActivity, level: Level) {
            GlobalScene.shared.tutorialProcessing = true
            tutorialLevel = level
            levelsActivity = WeakReference(context)
            context.startActivity(Intent(context, TutorialActivity::class.java))
        }

        /*
        fun preLoad() {
            if (!tutorialLevel.fullyLoaded) {
                tutorialLevel.fullyLoad(tutorialActivity.get()!!)
            }
        }*/

        fun loadLevel() {
            Log.d(TAG, "loadLevel")
            val activity = tutorialActivity.get()!!
            clearRules()
            activity.endExpressionView.text = if (tutorialLevel.endPatternStr.isBlank()) {
                when (tutorialLevel.type) {
                    Type.SET -> MathResolver.resolveToPlain(tutorialLevel.endExpression, taskType = TaskType.SET).matrix
                    else -> MathResolver.resolveToPlain(tutorialLevel.endExpression).matrix
                }
            } else {
                tutorialLevel.endExpressionStr
            }
            if (activity.endExpressionView.visibility != View.VISIBLE) {
                activity.showEndExpression(null)
            }
            tutorialActivity.get()!!.globalMathView.setExpression(tutorialLevel.startExpression.clone(), tutorialLevel.type)
        }

        fun onRuleClicked(ruleView: RuleMathView) {
            Log.d(TAG, "onRuleClicked")
            val activity = tutorialActivity.get()!!
            if (ruleView.subst != null) {
                val res = activity.globalMathView.performSubstitution(ruleView.subst!!)
                if (res != null) {
                    if (wantedRule) {
                        activity.ruleClickSucceeded()
                    }
                    if (tutorialLevel.checkEnd(res)) {
                        activity.levelPassed()
                    }
                    clearRules()
                } else {
                    showMessage(activity.getString(R.string.wrong_subs))
                }

            }
        }

        fun onExpressionClicked() {
            Log.d(TAG, "onExpressionClicked")
            if (wantedZoom) {
                return
            }
            val activity = tutorialActivity.get()!!
            if (activity.globalMathView.currentAtom != null) {
                val rules = tutorialLevel.getRulesFor(activity.globalMathView.currentAtom!!,
                    activity.globalMathView.expression!!)
                if (rules != null) {
                    activity.noRules.visibility = View.GONE
                    activity.rulesScrollView.visibility = View.VISIBLE
                    if (wantedClick) {
                        activity.expressionClickSucceeded()
                    } else {
                        showMessage("\uD83D\uDC4F A good choice! \uD83D\uDC4F")
                    }
                    redrawRules(rules)
                } else {
                    showMessage("No rules for this place \uD83D\uDE05\nTry another one!")
                    clearRules()
                    activity.globalMathView.recolorCurrentAtom(Color.YELLOW)
                }
            }
        }

        fun clearRules() {
            val activity = tutorialActivity.get()!!
            activity.rulesScrollView.visibility = View.INVISIBLE
            activity.noRules.visibility = View.VISIBLE
        }

        private fun redrawRules(rules: List<ExpressionSubstitution>) {
            Log.d(TAG, "redrawRules")
            val activity = tutorialActivity.get()!!
            activity.rulesLinearLayout.removeAllViews()
            for (r in rules) {
                val rule = RuleMathView(activity)
                rule.setSubst(r, tutorialLevel.type)
                activity.rulesLinearLayout.addView(rule)
            }
        }

        fun showMessage(msg: String) {
            val activity = tutorialActivity.get()!!
            activity.messageView.text = msg
            activity.messageView.visibility = View.VISIBLE
        }

        fun leave() {
            GlobalScene.shared.tutorialProcessing = false
            tutorialActivity.get()!!.finish()
        }
    }
}