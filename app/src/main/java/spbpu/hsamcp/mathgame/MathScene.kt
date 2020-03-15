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
import spbpu.hsamcp.mathgame.activities.LevelsActivity
import spbpu.hsamcp.mathgame.activities.PlayActivity
import spbpu.hsamcp.mathgame.common.RuleMathView
import spbpu.hsamcp.mathgame.level.*
import spbpu.hsamcp.mathgame.mathResolver.MathResolver
import spbpu.hsamcp.mathgame.statistics.Statistics
import java.lang.ref.WeakReference

class MathScene {
    companion object {
        private const val TAG = "MathScene"
        private const val messageTime: Long = 2000
        private val messageTimer = MessageTimer()

        private var stepsCount: Float = 0f
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
            val place = activity.globalMathView.currentAtom!!.clone()
            val oldSteps = stepsCount
            var levelPassed = false
            if (currentRuleView!!.subst != null) {
                val res = activity.globalMathView.performSubstitution(currentRuleView!!.subst!!)
                if (res != null) {
                    stepsCount++
                    history.saveState(State(prev))
                    if (currentLevel!!.checkEnd(res)) {
                        timer.cancel()
                        levelPassed = true
                        Statistics.logRule(oldSteps, stepsCount, prev, activity.globalMathView.formula!!,
                            currentRuleView!!.subst, place)
                        onWin()
                    }
                    clearRules()
                } else {
                    showMessage(activity.getString(R.string.wrong_subs))
                }

            }
            if (!levelPassed) {
                Statistics.logRule(oldSteps, stepsCount, prev, activity.globalMathView.formula!!,
                    currentRuleView!!.subst, place)
            }
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
            Statistics.logPlace(stepsCount, activity.globalMathView.formula!!, activity.globalMathView.currentAtom!!)
        }

        fun preLoad() {
            if (!currentLevel!!.fullyLoaded) {
                currentLevel!!.loadExpressions()
            }
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
                stepsCount = 0f
                currentTime = 0
                timer = MathTimer(currentLevel!!.time, 1)
                timer.start()
                history.clear()
                showMessage("\uD83C\uDF40 ${currentLevel!!.name} \uD83C\uDF40")
                Statistics.setStartTime()
                res = true
                Statistics.logStart()
            }
            return res
        }

        fun nextLevel(): Boolean {
            timer.cancel()
            val level = levelsActivity.get()!!.getNextLevel()
            if (level == currentLevel!!) {
                return false
            }
            currentLevel = level
            playActivity.get()!!.createLevelUI()
            return true
        }

        fun prevLevel(): Boolean {
            timer.cancel()
            val level = levelsActivity.get()!!.getPrevLevel()
            if (level == currentLevel!!) {
                return false
            }
            currentLevel = level
            playActivity.get()!!.createLevelUI()
            return true
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
                val penalty = UndoPolicyHandler.getPenalty(currentLevel!!.undoPolicy, state.depth)
                stepsCount = stepsCount - 1 + penalty
            }
            Statistics.logUndo(oldSteps, stepsCount, oldFormula,
                activity.globalMathView.formula!!, activity.globalMathView.currentAtom)
        }

        fun restart() {
            Log.d(TAG, "restart")
            timer.cancel()
            val activity = playActivity.get()!!
            Statistics.logRestart(stepsCount, activity.globalMathView.formula!!, activity.globalMathView.currentAtom)
            loadLevel()
        }

        fun menu() {
            Log.d(TAG, "menu")
            timer.cancel()
            val activity = playActivity.get()!!
            Statistics.logMenu(stepsCount, activity.globalMathView.formula!!, activity.globalMathView.currentAtom)
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
                currentLevel!!.save(activity)
                levelsActivity.get()!!.updateResult()
            }
            activity.onWin(stepsCount, currentTime, award)
            Statistics.logWin(stepsCount, award)
        }

        private fun onLoose() {
            Log.d(TAG, "onLoose")
            val activity = playActivity.get()!!
            activity.onLoose()
            Statistics.logLoose(stepsCount, activity.globalMathView.formula!!, activity.globalMathView.currentAtom)
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