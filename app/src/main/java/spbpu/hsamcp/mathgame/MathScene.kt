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
import spbpu.hsamcp.mathgame.activities.TutorialActivity
import spbpu.hsamcp.mathgame.common.RuleMathView
import spbpu.hsamcp.mathgame.level.*
import spbpu.hsamcp.mathgame.mathResolver.MathResolver
import spbpu.hsamcp.mathgame.statistics.Statistics
import java.lang.ref.WeakReference
import java.util.*

class MathScene {
    companion object {
        private const val TAG = "MathScene"
        private const val messageTime: Long = 2000
        private val messageTimer = MessageTimer()

        private var stepsCount: Float = 0f
        private var currentTime: Long = 0
        lateinit var downTimer: MathDownTimer
            private set
        lateinit var upTimer: MathUpTimer
            private set
        private lateinit var history: History
        var currentRuleView: RuleMathView? = null
        var currentLevel: Level? = null
        lateinit var playActivity: WeakReference<PlayActivity>
        lateinit var levelsActivity: WeakReference<LevelsActivity>
        var tutorialProcessing = false

        fun init(playActivity: PlayActivity) {
            Log.d(TAG, "init")
            MathScene.playActivity = WeakReference(playActivity)
            history = History()
        }

        fun onRuleClicked() {
            Log.d(TAG, "onRuleClicked")
            if (tutorialProcessing) {
                TutorialScene.onRuleClicked(currentRuleView!!)
                return
            }
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
                        cancelTimers()
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
            if (tutorialProcessing) {
                TutorialScene.onFormulaClicked()
                return
            }
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
                currentLevel!!.fullyLoad(playActivity.get()!!)
            }
        }

        fun loadLevel(continueGame: Boolean): Boolean {
            Log.d(TAG, "loadLevel")
            var res = false
            val activity = playActivity.get()!!
            if (currentLevel != null) {
                clearRules()
                activity.endFormulaView.text = if (currentLevel!!.endPatternStr.isBlank()){
                    MathResolver.resolveToPlain(currentLevel!!.endFormula).matrix
                } else {
                    currentLevel!!.endFormulaStr
                }
                if (activity.endFormulaView.visibility != View.VISIBLE) {
                    activity.showEndFormula(null)
                }
                if (currentLevel!!.endless) {
                    loadEndless(continueGame)
                } else {
                    loadFinite()
                }
                history.clear()
                showMessage("\uD83C\uDF40 ${currentLevel!!.name} \uD83C\uDF40")
                Statistics.setStartTime()
                res = true
                Statistics.logStart()
            }
            return res
        }

        private fun loadFinite() {
            playActivity.get()!!.globalMathView.setFormula(currentLevel!!.startFormula.clone())
            stepsCount = 0f
            currentTime = 0
            downTimer = MathDownTimer(currentLevel!!.time, 1)
            downTimer.start()
        }

        private fun loadEndless(continueGame: Boolean) {
            val activity = playActivity.get()!!
            if (continueGame && currentLevel!!.lastResult != null &&
                    currentLevel!!.lastResult!!.award.value == AwardType.PAUSED) {
                stepsCount = currentLevel!!.lastResult!!.steps
                currentTime = currentLevel!!.lastResult!!.time
                activity.globalMathView.setFormula(currentLevel!!.lastResult!!.expression, currentLevel!!.type)
            } else {
                activity.globalMathView.setFormula(currentLevel!!.startFormula.clone())
                stepsCount = 0f
                currentTime = 0
            }
            upTimer = MathUpTimer(1)
            upTimer.start()
        }

        fun wasLevelPaused(): Boolean {
            return currentLevel!!.endless && (currentLevel!!.lastResult != null &&
                currentLevel!!.lastResult!!.award.value == AwardType.PAUSED)
        }

        fun nextLevel(): Boolean {
            cancelTimers()
            val level = levelsActivity.get()!!.getNextLevel()
            if (level == currentLevel!!) {
                return false
            }
            if (level.taskId == 0) {
                TutorialScene.start(levelsActivity.get()!!, level)
                playActivity.get()!!.finish()
            }
            currentLevel = level
            playActivity.get()!!.startCreatingLevelUI()
            return true
        }

        fun prevLevel(): Boolean {
            cancelTimers()
            val level = levelsActivity.get()!!.getPrevLevel()
            if (level == currentLevel!!) {
                return false
            }
            if (level.taskId == 0) {
                TutorialScene.start(levelsActivity.get()!!, level)
                playActivity.get()!!.finish()
            }
            currentLevel = level
            playActivity.get()!!.startCreatingLevelUI()
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
            cancelTimers()
            val activity = playActivity.get()!!
            Statistics.logRestart(stepsCount, activity.globalMathView.formula!!, activity.globalMathView.currentAtom)
            loadLevel(false)
        }

        fun menu(save: Boolean = true) {
            Log.d(TAG, "menu")
            cancelTimers()
            val activity = playActivity.get()!!
            if (save) {
                val newRes = Result(stepsCount, currentTime, Award.getPaused(),
                    expressionToString(activity.globalMathView.formula!!))
                currentLevel!!.lastResult = newRes
                currentLevel!!.save(activity)
                levelsActivity.get()!!.updateResult()
            } else if (wasLevelPaused()) {
                currentLevel!!.lastResult = null
                currentLevel!!.save(activity)
                levelsActivity.get()!!.updateResult()
            }
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

        private fun cancelTimers() {
            if (!currentLevel!!.endless) {
                downTimer.cancel()
            } else {
                upTimer.cancel()
            }
        }

        class MessageTimer : CountDownTimer(messageTime, messageTime) {
            override fun onTick(m: Long) {}
            override fun onFinish() {
                playActivity.get()!!.messageView.visibility = View.GONE
            }
        }

        class MathDownTimer(time: Long, interval: Long):
            CountDownTimer(time * 1000, interval * 1000) {
            private val TAG = "MathDownTimer"
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

        class MathUpTimer(val interval: Long) {
            private val TAG = "MathUpTimer"
            private lateinit var timer: Timer

            fun start() {
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        Log.d(TAG, "run")
                        currentTime++
                        val start = "⏰ "
                        val sec = "${currentTime % 60}".padStart(2, '0')
                        val text = SpannableString(start + currentTime / 60 + ":" + sec)
                        val steps = if (stepsCount < currentLevel!!.stepsNum) {
                            currentLevel!!.stepsNum.toFloat()
                        } else {
                            stepsCount
                        }
                        val award = currentLevel!!.getAward(currentTime, steps)
                        text.setSpan(ForegroundColorSpan(award.color), start.length,
                            text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        val activity = playActivity.get()!!
                        activity.runOnUiThread {
                            activity.timerView.text = text
                        }
                    }
                }, 0, interval * 1000)
            }

            fun cancel() {
                timer.cancel()
            }
        }
    }
}