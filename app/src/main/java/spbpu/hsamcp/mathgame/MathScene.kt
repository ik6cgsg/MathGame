package spbpu.hsamcp.mathgame

import android.view.View
import android.widget.HorizontalScrollView
import com.twf.expressiontree.ExpressionSubstitution
import android.graphics.Color
import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.MotionEvent
import spbpu.hsamcp.mathgame.mathResolver.MathResolver
import java.lang.ref.WeakReference

class MathScene {
    companion object {
        private val TAG = "MathScene"
        private val messageTime: Long = 2000

        private var stepsCount: Int = 0
        private var currentTime: Long = 0
        lateinit var timer: MathTimer
            private set
        private lateinit var history: History
        var currentRuleView: RuleMathView? = null
        var currentLevel: Level? = null
        lateinit var playActivity: WeakReference<PlayActivity>

        fun init(playActivity: PlayActivity) {
            Log.d(TAG, "init")
            MathScene.playActivity = WeakReference(playActivity)
            history = History()
        }

        fun onRuleClicked() {
            Log.d(TAG, "onRuleClicked")
            val activity = playActivity.get()!!
            if (currentRuleView!!.subst != null) {
                val prev = activity.globalMathView.formula!!.clone()
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
        }

        fun onFormulaClicked() {
            Log.d(TAG, "onFormulaClicked")
            val activity = playActivity.get()!!
            if (activity.globalMathView.currentAtom != null) {
                val rules = currentLevel!!.getRulesFor(activity.globalMathView.currentAtom!!)
                if (rules != null) {
                    activity.noRules.visibility = View.GONE
                    activity.rulesScrollView.visibility = View.VISIBLE
                    redrawRules(rules)
                } else {
                    clearRules()
                }
            }
        }

        fun loadLevel(fileName: String): Boolean {
            Log.d(TAG, "loadLevel")
            var res = false
            val activity = playActivity.get()!!
            currentLevel = Level.create(fileName, activity.assets)
            if (currentLevel != null) {
                clearRules()
                activity.globalMathView.setFormula(currentLevel!!.startFormula)
                activity.globalMathView.textSize = activity.globalMathView.defaultSize
                activity.endFormulaView.text = MathResolver.resolveToPlain(currentLevel!!.endFormula).matrix
                if (activity.endFormulaView.visibility != View.VISIBLE) {
                    activity.showEndFormula(null)
                }
                stepsCount = 0
                currentTime = 0
                timer = MathTimer(currentLevel!!.time.toLong(), 1)
                timer.start()
                history.clear()
                showMessage("\uD83C\uDF40 ${currentLevel!!.name} \uD83C\uDF40")
                res = true
            }
            return res
        }

        fun previousStep() {
            Log.d(TAG, "previousStep")
            val state = history.getPreviousStep()
            val activity = playActivity.get()!!
            if (state != null) {
                clearRules()
                activity.globalMathView.setFormula(state.formula)
                stepsCount--
            }
        }

        private fun clearRules() {
            val activity = playActivity.get()!!
            activity.rulesScrollView.visibility = View.INVISIBLE
            activity.noRules.visibility = View.VISIBLE
        }

        private fun redrawRules(rules: List<ExpressionSubstitution>) {
            Log.d(TAG, "redrawRules")
            val activity = playActivity.get()!!
            activity.rulesLinearLayout.removeAllViews()
            for (r in rules) {
                val horizontalScrollView = HorizontalScrollView(activity)
                val rule = RuleMathView(activity)
                rule.setSubst(r)
                horizontalScrollView.addView(rule)
                horizontalScrollView.setOnTouchListener {v, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        if (v.left + event.x >= v.left && v.left + event.x <= v.right &&
                            v.top + event.y >= v.top && v.top + event.y <= v.bottom) {
                            rule.onTouchEvent(event)
                        }
                        true
                    } else {
                        false
                    }
                }
                activity.rulesLinearLayout.addView(horizontalScrollView)
            }
        }

        private fun onWin() {
            Log.d(TAG, "onWin")
            val award = currentLevel!!.getAward(currentTime, stepsCount)
            currentLevel!!.lastResult = Result(stepsCount, currentTime, award)
            // TODO: saving
            currentLevel!!.save()
            playActivity.get()!!.onWin(stepsCount, currentTime, award.str)
        }

        private fun showMessage(msg: String) {
            val activity = playActivity.get()!!
            activity.messageView.text = msg
            activity.messageView.visibility = View.VISIBLE
            val time = object: CountDownTimer(messageTime, messageTime) {
                override fun onTick(m: Long) {}
                override fun onFinish() {
                    activity.messageView.visibility = View.GONE
                }
            }
            time.start()
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
                activity.onLoose()
            }
        }
    }
}