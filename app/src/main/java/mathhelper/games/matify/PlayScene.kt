package mathhelper.games.matify

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import api.expressionToStructureString
import expressiontree.ExpressionSubstitution
import expressiontree.SimpleComputationRuleParams
import mathhelper.games.matify.activities.PlayActivity
import mathhelper.games.matify.common.MathDownTimer
import mathhelper.games.matify.common.MathUpTimer
import mathhelper.games.matify.common.MessageTimer
import mathhelper.games.matify.common.RuleMathView
import mathhelper.games.matify.level.*
import mathhelper.games.matify.mathResolver.MathResolver
import mathhelper.games.matify.mathResolver.TaskType
import mathhelper.games.matify.statistics.Statistics

class PlayScene() {
    companion object {
        private const val TAG = "PlayScene"
        const val messageTime: Long = 2000
        val shared: PlayScene = PlayScene()
    }

    var playActivity: PlayActivity? = null
        set(value) {
            field = value
            if (value != null) {
                //tutorialProcessing = false
                history = History()
            }
        }
    /** GAME STATE */
    var currentRuleView: RuleMathView? = null
    fun setCurrentRuleView(context: Context, value: RuleMathView?) {
        currentRuleView = value
        if (value != null) {
            Handler().postDelayed({
                try {
                    onRuleClicked(context)
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "Error during rule usage: ${e.message}")
                    Toast.makeText(playActivity, R.string.misclick_happened_please_retry, Toast.LENGTH_LONG).show()
                }
            }, 100)
        }
    }
    var stepsCount: Float = 0f
    var currentTime: Long = 0
    private lateinit var history: History
    /** TIMERS */
    private val messageTimer = MessageTimer()
    var downTimer: MathDownTimer? = null
        private set
    var upTimer: MathUpTimer? = null
        private set

    private fun onRuleClicked(context: Context) {
        Log.d(TAG, "onRuleClicked")
        if (GlobalScene.shared.tutorialProcessing) {
            TutorialScene.shared.onRuleClicked(currentRuleView!!)
            return
        }
        if (playActivity == null) {
            return
        }
        val activity = playActivity!!
        val prev = activity.globalMathView.expression!!.clone()
        val place = activity.globalMathView.currentAtom!!.clone()
        val oldSteps = stepsCount
        var levelPassed = false
        if (currentRuleView!!.subst != null) {
            val res = activity.globalMathView.performSubstitution(currentRuleView!!.subst!!)
            if (res != null) {
                stepsCount++
                history.saveState(State(prev))
                if (LevelScene.shared.currentLevel!!.checkEnd(res)) {
                    levelPassed = true
                    Statistics.logRule(oldSteps, stepsCount, prev, activity.globalMathView.expression!!,
                        currentRuleView!!.subst, place)
                    onWin(context)
                }
                clearRules()
            } else {
                showMessage(activity.getString(R.string.wrong_subs))
            }

        }
        if (!levelPassed) {
            Statistics.logRule(oldSteps, stepsCount, prev, activity.globalMathView.expression!!,
                currentRuleView!!.subst, place)
        }
    }

    fun onExpressionClicked() {
        Log.d(TAG, "onExpressionClicked")
        if (GlobalScene.shared.tutorialProcessing) {
            TutorialScene.shared.onExpressionClicked()
            return
        }
        val activity = playActivity!!
        if (activity.globalMathView.currentAtom != null) {
            val rules = LevelScene.shared.currentLevel!!.getRulesFor(activity.globalMathView.currentAtom!!,
                activity.globalMathView.expression!!, SimpleComputationRuleParams(true)
            )
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
        Statistics.logPlace(stepsCount, activity.globalMathView.expression!!, activity.globalMathView.currentAtom!!)
    }

    fun loadLevel(context: Context, continueGame: Boolean, languageCode: String): Boolean {
        Log.d(TAG, "loadLevel")
        val currentLevel = LevelScene.shared.currentLevel!!
        val activity = playActivity!!
        clearRules()
        cancelTimers()
        activity.endExpressionView.text = if (currentLevel.endPatternStr.isBlank()) {
            when (currentLevel.type) {
                Type.SET -> MathResolver.resolveToPlain(currentLevel.endExpression, taskType = TaskType.SET).matrix
                else -> MathResolver.resolveToPlain(currentLevel.endExpression).matrix
            }
        } else {
            currentLevel.endExpressionStr
        }
        if (activity.endExpressionView.visibility != View.VISIBLE) {
            activity.showEndExpression(null)
        }
        if (currentLevel.endless) {
            loadEndless(context, continueGame)
        } else {
            loadFinite()
        }
        history.clear()
        showMessage("\uD83C\uDF40 ${currentLevel.getNameByLanguage(languageCode)} \uD83C\uDF40")
        Statistics.setStartTime()
        Statistics.logStart()
        return true
    }

    private fun loadFinite() {
        val currentLevel = LevelScene.shared.currentLevel!!
        playActivity!!.globalMathView.setExpression(currentLevel.startExpression.clone(), currentLevel.type)
        stepsCount = 0f
        currentTime = 0
        downTimer = MathDownTimer(currentLevel.time, 1)
        downTimer!!.start()
    }

    private fun loadEndless(context: Context, continueGame: Boolean) {
        val activity = playActivity!!
        val currentLevel = LevelScene.shared.currentLevel!!
        if (continueGame && currentLevel.lastResult != null &&
            currentLevel.lastResult!!.award.value == AwardType.PAUSED) {
            stepsCount = currentLevel.lastResult!!.steps
            currentTime = currentLevel.lastResult!!.time
            activity.globalMathView.setExpression(currentLevel.lastResult!!.expression, currentLevel.type)
        } else {
            activity.globalMathView.setExpression(currentLevel.startExpression.clone(), currentLevel.type)
            stepsCount = 0f
            currentTime = 0
        }
        upTimer = MathUpTimer(1)
        upTimer!!.start(context)
    }

    fun previousStep() {
        Log.d(TAG, "previousStep")
        val state = history.getPreviousStep()
        val activity = playActivity!!
        val oldExpression = activity.globalMathView.expression!!
        val oldSteps = stepsCount
        if (state != null) {
            clearRules()
            val currentLevel = LevelScene.shared.currentLevel!!
            activity.globalMathView.setExpression(state.expression, currentLevel.type, false)
            val penalty = UndoPolicyHandler.getPenalty(currentLevel.undoPolicy, state.depth)
            stepsCount = stepsCount - 1 + penalty
        }
        Statistics.logUndo(oldSteps, stepsCount, oldExpression,
            activity.globalMathView.expression!!, activity.globalMathView.currentAtom)
    }

    fun restart(context: Context, languageCode: String) {
        Log.d(TAG, "restart")
        val activity = playActivity!!
        Statistics.logRestart(stepsCount, activity.globalMathView.expression!!, activity.globalMathView.currentAtom)
        loadLevel(context,false, languageCode)
    }

    fun menu(context: Context, save: Boolean = true) {
        Log.d(TAG, "menu")
        val activity = playActivity!!
        val currentLevel = LevelScene.shared.currentLevel!!
        if (save) {
            val newRes = Result(stepsCount, currentTime, Award.getPaused(context),
                expressionToStructureString(activity.globalMathView.expression!!))
            currentLevel.lastResult = newRes
            currentLevel.save(activity)
            LevelScene.shared.levelsActivity!!.updateResult()
        } else if (LevelScene.shared.wasLevelPaused()) {
            currentLevel.lastResult = null
            currentLevel.save(activity)
            LevelScene.shared.levelsActivity!!.updateResult()
        }
        Statistics.logMenu(stepsCount, activity.globalMathView.expression!!, activity.globalMathView.currentAtom)
    }

    fun info(languageCode: String) {
        val currentLevel = LevelScene.shared.currentLevel!!
        showMessage("\uD83C\uDF40 ${currentLevel.getNameByLanguage(languageCode)} \uD83C\uDF40\n" +
            "\uD83D\uDC63 Steps: ${"%.1f".format(stepsCount)} \uD83D\uDC63")
    }

    fun clearRules() {
        val activity = playActivity!!
        activity.rulesScrollView.visibility = View.INVISIBLE
        activity.noRules.visibility = View.VISIBLE
    }

    private fun redrawRules(rules: List<ExpressionSubstitution>) {
        Log.d(TAG, "redrawRules")
        val activity = playActivity!!
        activity.rulesLinearLayout.removeAllViews()
        for (r in rules) {
            try {
                val rule = RuleMathView(activity)
                rule.setSubst(r, LevelScene.shared.currentLevel!!.type)
                activity.rulesLinearLayout.addView(rule)
            } catch (e: Exception) {
                Log.e(TAG, "Rule draw Error", e)
            }
        }
    }

    fun onWin(context: Context) {
        Log.d(TAG, "onWin")
        val activity = playActivity!!
        val currentLevel = LevelScene.shared.currentLevel!!
        val award = currentLevel.getAward(context, currentTime, stepsCount)
        val newRes = Result(stepsCount, currentTime, award)
        if (newRes.isBetter(currentLevel.lastResult)) {
            currentLevel.lastResult = newRes
            currentLevel.save(activity)
            LevelScene.shared.levelsActivity!!.updateResult()
        }
        activity.onWin(stepsCount, currentTime, award)
        Statistics.logWin(stepsCount, award)
    }

    fun onLoose() {
        Log.d(TAG, "onLoose")
        val activity = playActivity!!
        activity.onLoose()
        Statistics.logLoose(stepsCount, activity.globalMathView.expression!!, activity.globalMathView.currentAtom)
    }

    private fun showMessage(msg: String) {
        val activity = playActivity!!
        activity.messageView.text = msg
        activity.messageView.visibility = View.VISIBLE
        messageTimer.cancel()
        messageTimer.start()
    }

    fun cancelTimers() {
        if (!LevelScene.shared.currentLevel!!.endless) {
            downTimer?.cancel()
        } else {
            upTimer?.cancel()
        }
        upTimer = null
        downTimer = null
    }
}