package spbpu.hsamcp.mathgame

import android.view.View
import com.twf.expressiontree.ExpressionSubstitution
import android.graphics.Color
import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import com.twf.api.expressionToString
import spbpu.hsamcp.mathgame.activities.LevelsActivity
import spbpu.hsamcp.mathgame.activities.PlayActivity
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.common.RuleMathView
import spbpu.hsamcp.mathgame.level.History
import spbpu.hsamcp.mathgame.level.Level
import spbpu.hsamcp.mathgame.level.Result
import spbpu.hsamcp.mathgame.level.State
import spbpu.hsamcp.mathgame.mathResolver.MathResolver
import spbpu.hsamcp.mathgame.statistics.Action
import spbpu.hsamcp.mathgame.statistics.MathGameLog
import spbpu.hsamcp.mathgame.statistics.Statistics
import java.lang.ref.WeakReference

class MathScene {
    companion object {
        private const val TAG = "MathScene"
        private const val messageTime: Long = 2000
        private val messageTimer = MessageTimer()

        private var stepsCount: Int = 0
        private var currentTime: Long = 0
        lateinit var timer: MathTimer
            private set
        private lateinit var history: History
        var currentRuleView: RuleMathView? = null
        var currentLevel: Level? = null
        lateinit var playActivity: WeakReference<PlayActivity>
        lateinit var levelsActivity: WeakReference<LevelsActivity>

        fun init(playActivity: PlayActivity) {
            Log.d(TAG, "init")
            MathScene.playActivity = WeakReference(playActivity)
            history = History()
        }

        fun onRuleClicked() {
            Log.d(TAG, "onRuleClicked")
            val activity = playActivity.get()!!
            val prev = activity.globalMathView.formula!!.clone()
            val place = expressionToString(activity.globalMathView.currentAtom!!)
            val oldSteps = stepsCount
            if (currentRuleView!!.subst != null) {
                val res = activity.globalMathView.performSubstitution(currentRuleView!!.subst!!)
                if (res != null) {
                    stepsCount++
                    history.saveState(State(prev))
                    if (currentLevel!!.checkEnd(res)) {
                        timer.cancel()
                        onWin()
                    }
                    clearRules()
                } else {
                    showMessage(activity.getString(R.string.wrong_subs))
                }
            }
            val curr = expressionToString(prev)
            val next = expressionToString(activity.globalMathView.formula!!)
            val rule = if (currentRuleView!!.subst == null) {
                ""
            } else {
                expressionToString(currentRuleView!!.subst!!.left) +
                    " : " + expressionToString(currentRuleView!!.subst!!.right)
            }
            val mathLog = MathGameLog(
                currStepsNumber = oldSteps,
                nextStepsNumber = stepsCount,
                currExpression = curr,
                nextExpression = next,
                currRule = rule,
                currSelectedPlace = place
            )
            mathLog.addInfoFrom(activity, currentLevel!!, Action.RULE)
            Statistics.sendLog(mathLog)
        }

        fun onFormulaClicked() {
            Log.d(TAG, "onFormulaClicked")
            val activity = playActivity.get()!!
            if (activity.globalMathView.currentAtom != null) {
                val rules = currentLevel!!.getRulesFor(activity.globalMathView.currentAtom!!,
                    activity.globalMathView.formula!!)
                if (rules != null) {
                    activity.noRules.visibility = View.GONE
                    activity.rulesScrollView.visibility = View.VISIBLE
                    redrawRules(rules)
                } else {
                    showMessage(activity.getString(R.string.no_rules))
                    clearRules()
                    activity.globalMathView.recolorCurrentAtom(Color.YELLOW)
                }
            }
            val curr = expressionToString(activity.globalMathView.formula!!)
            val mathLog = MathGameLog(
                currStepsNumber = stepsCount,
                nextStepsNumber = stepsCount,
                currExpression = curr,
                nextExpression = curr,
                currSelectedPlace = expressionToString(activity.globalMathView.currentAtom!!)
            )
            mathLog.addInfoFrom(activity, currentLevel!!, Action.PLACE)
            Statistics.sendLog(mathLog)
        }

        fun loadLevel(): Boolean {
            Log.d(TAG, "loadLevel")
            var res = false
            val activity = playActivity.get()!!
            if (currentLevel != null) {
                clearRules()
                activity.globalMathView.setFormula(currentLevel!!.startFormula.clone())
                activity.endFormulaView.text = MathResolver.resolveToPlain(currentLevel!!.endFormula).matrix
                if (activity.endFormulaView.visibility != View.VISIBLE) {
                    activity.showEndFormula(null)
                }
                stepsCount = 0
                currentTime = 0
                timer = MathTimer(currentLevel!!.time, 1)
                timer.start()
                history.clear()
                showMessage("\uD83C\uDF40 ${currentLevel!!.name} \uD83C\uDF40")
                Statistics.setStartTime()
                res = true
                // Logging...
                val exprStr = expressionToString(currentLevel!!.startFormula)
                val mathLog = MathGameLog(
                    currStepsNumber = 0,
                    nextStepsNumber = 0,
                    currExpression = exprStr,
                    nextExpression = exprStr
                )
                mathLog.addInfoFrom(activity, currentLevel!!, Action.START)
                Statistics.sendLog(mathLog)
            }
            return res
        }

        fun nextLevel() {
            timer.cancel()
            currentLevel = levelsActivity.get()!!.getNextLevel()
            loadLevel()
        }

        fun prevLevel() {
            timer.cancel()
            currentLevel = levelsActivity.get()!!.getPrevLevel()
            loadLevel()
        }

        fun previousStep() {
            Log.d(TAG, "previousStep")
            val state = history.getPreviousStep()
            val activity = playActivity.get()!!
            val oldFormula = activity.globalMathView.formula!!
            val oldSteps = stepsCount
            if (state != null) {
                clearRules()
                activity.globalMathView.setFormula(state.formula, false)
                // TODO: smek with undo policy
                stepsCount--
            }
            // Logging...
            val curr = expressionToString(oldFormula)
            val next = expressionToString(activity.globalMathView.formula!!)
            val place = if (activity.globalMathView.currentAtom == null) {
                ""
            } else {
                expressionToString(activity.globalMathView.currentAtom!!)
            }
            val mathLog = MathGameLog(
                currStepsNumber = oldSteps,
                nextStepsNumber = stepsCount,
                currExpression = curr,
                nextExpression = next,
                currSelectedPlace = place
            )
            mathLog.addInfoFrom(activity, currentLevel!!, Action.UNDO)
            Statistics.sendLog(mathLog)
        }

        fun restart() {
            Log.d(TAG, "restart")
            timer.cancel()
            val activity = playActivity.get()!!
            val curr = expressionToString(activity.globalMathView.formula!!)
            val next = expressionToString(currentLevel!!.startFormula)
            val place = if (activity.globalMathView.currentAtom == null) {
                ""
            } else {
                expressionToString(activity.globalMathView.currentAtom!!)
            }
            val mathLog = MathGameLog(
                currStepsNumber = stepsCount,
                nextStepsNumber = 0,
                currExpression = curr,
                nextExpression = next,
                currSelectedPlace = place
            )
            mathLog.addInfoFrom(activity, currentLevel!!, Action.RESTART)
            Statistics.sendLog(mathLog)
            loadLevel()
        }

        fun menu() {
            Log.d(TAG, "menu")
            timer.cancel()
            val activity = playActivity.get()!!
            val curr = expressionToString(activity.globalMathView.formula!!)
            val place = if (activity.globalMathView.currentAtom == null) {
                ""
            } else {
                expressionToString(activity.globalMathView.currentAtom!!)
            }
            val mathLog = MathGameLog(
                currStepsNumber = stepsCount,
                nextStepsNumber = stepsCount,
                currExpression = curr,
                nextExpression = curr,
                currSelectedPlace = place
            )
            mathLog.addInfoFrom(activity, currentLevel!!, Action.MENU)
            Statistics.sendLog(mathLog)
        }

        fun clearRules() {
            val activity = playActivity.get()!!
            activity.rulesScrollView.visibility = View.INVISIBLE
            activity.noRules.visibility = View.VISIBLE
        }

        private fun redrawRules(rules: List<ExpressionSubstitution>) {
            Log.d(TAG, "redrawRules")
            val activity = playActivity.get()!!
            activity.rulesLinearLayout.removeAllViews()
            for (r in rules) {
                val rule = RuleMathView(activity)
                rule.setSubst(r)
                activity.rulesLinearLayout.addView(rule)
            }
        }

        private fun onWin() {
            Log.d(TAG, "onWin")
            val activity = playActivity.get()!!
            val award = currentLevel!!.getAward(currentTime, stepsCount)
            val newRes = Result(stepsCount, currentTime, award)
            if (newRes.isBetter(currentLevel!!.lastResult)) {
                currentLevel!!.lastResult = newRes
                currentLevel!!.save()
                levelsActivity.get()!!.updateResult()
            }
            activity.onWin(stepsCount, currentTime, award)
            // Logging...
            val exprStr = expressionToString(currentLevel!!.endFormula)
            val mathLog = MathGameLog(
                currStepsNumber = stepsCount,
                nextStepsNumber = stepsCount,
                currAwardCoef = award.coeff.toFloat(),
                currExpression = exprStr,
                nextExpression = exprStr
            )
            mathLog.addInfoFrom(activity, currentLevel!!, Action.WIN)
            Statistics.sendLog(mathLog)
        }

        private fun onLoose() {
            Log.d(TAG, "onLoose")
            val activity = playActivity.get()!!
            activity.onLoose()
            // Logging...
            val exprStr = expressionToString(activity.globalMathView.formula!!)
            val mathLog = MathGameLog(
                currStepsNumber = stepsCount,
                nextStepsNumber = stepsCount,
                currExpression = exprStr,
                nextExpression = exprStr
            )
            mathLog.addInfoFrom(activity, currentLevel!!, Action.LOOSE)
            Statistics.sendLog(mathLog)
        }

        private fun showMessage(msg: String) {
            val activity = playActivity.get()!!
            activity.messageView.text = msg
            activity.messageView.visibility = View.VISIBLE
            messageTimer.cancel()
            messageTimer.start()
        }

        class MessageTimer : CountDownTimer(messageTime, messageTime) {
            override fun onTick(m: Long) {}
            override fun onFinish() {
                playActivity.get()!!.messageView.visibility = View.GONE
            }
        }

        class MathTimer(time: Long, interval: Long):
            CountDownTimer(time * 1000, interval * 1000) {
            private val TAG = "MathTimer"
            private val panicTime = 10

            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "onTick")
                currentTime++
                val secs = millisUntilFinished / 1000
                val start = "⏰ "
                val sec = "${secs % 60}".padStart(2, '0')
                val text = SpannableString(start + secs / 60 + ":" + sec)
                if (secs <= panicTime) {
                    text.setSpan(ForegroundColorSpan(Color.RED), start.length,
                        text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                    text.setSpan(StyleSpan(Typeface.BOLD), start.length,
                        text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
                playActivity.get()!!.timerView.text = text
            }

            override fun onFinish() {
                Log.d(TAG, "onFinish")
                val activity = playActivity.get()!!
                activity.timerView.text = activity.getString(R.string.time_out)
                onLoose()
            }
        }
    }
}