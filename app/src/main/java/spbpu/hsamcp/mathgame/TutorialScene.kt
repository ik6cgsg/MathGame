package spbpu.hsamcp.mathgame

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.util.Log
import com.twf.expressiontree.ExpressionSubstitution
import spbpu.hsamcp.mathgame.activities.LevelsActivity
import spbpu.hsamcp.mathgame.activities.PlayActivity
import spbpu.hsamcp.mathgame.activities.TutorialActivity
import spbpu.hsamcp.mathgame.common.RuleMathView
import spbpu.hsamcp.mathgame.level.Level
import spbpu.hsamcp.mathgame.mathResolver.MathResolver
import spbpu.hsamcp.mathgame.statistics.Statistics
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
            MathScene.tutorialProcessing = true
            tutorialLevel = level
            levelsActivity = WeakReference(context)
            context.startActivity(Intent(context, TutorialActivity::class.java))
        }

        fun preLoad() {
            if (!tutorialLevel.fullyLoaded) {
                tutorialLevel.fullyLoad(tutorialActivity.get()!!)
            }
        }

        fun loadLevel() {
            Log.d(TAG, "loadLevel")
            val activity = tutorialActivity.get()!!
            clearRules()
            activity.endFormulaView.text = if (tutorialLevel.endPatternStr.isBlank()){
                MathResolver.resolveToPlain(tutorialLevel.endFormula).matrix
            } else {
                tutorialLevel.endFormulaStr
            }
            if (activity.endFormulaView.visibility != View.VISIBLE) {
                activity.showEndFormula(null)
            }
            tutorialActivity.get()!!.globalMathView.setFormula(tutorialLevel.startFormula.clone())
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

        fun onFormulaClicked() {
            Log.d(TAG, "onFormulaClicked")
            if (wantedZoom) {
                return
            }
            val activity = tutorialActivity.get()!!
            if (activity.globalMathView.currentAtom != null) {
                val rules = tutorialLevel.getRulesFor(activity.globalMathView.currentAtom!!,
                    activity.globalMathView.formula!!)
                if (rules != null) {
                    activity.noRules.visibility = View.GONE
                    activity.rulesScrollView.visibility = View.VISIBLE
                    if (wantedClick) {
                        activity.expressionClickSucceeded()
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
                rule.setSubst(r)
                activity.rulesLinearLayout.addView(rule)
            }
        }

        fun showMessage(msg: String) {
            val activity = tutorialActivity.get()!!
            activity.messageView.text = msg
            activity.messageView.visibility = View.VISIBLE
        }

        fun leave() {
            MathScene.tutorialProcessing = false
            tutorialActivity.get()!!.finish()
        }
    }
}