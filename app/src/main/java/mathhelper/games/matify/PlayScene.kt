package mathhelper.games.matify

import android.app.AlertDialog
import android.content.Context
import android.text.Html
import android.text.SpannedString
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.twf.expressiontree.ExpressionSubstitution
import mathhelper.games.matify.activities.PlayActivity
import mathhelper.games.matify.common.*
import mathhelper.games.matify.level.*
import mathhelper.games.matify.statistics.Statistics
import java.lang.ref.WeakReference

interface PlaySceneListener : TimerListener {
    var rulesLinearLayout: LinearLayout
    var endExpressionMathView: SimpleMathView
    var endExpressionViewLabel: TextView
    var rulesScrollView: ScrollView
    var globalMathView: GlobalMathView
    var rulesMsg: TextView

    var previous: TextView

    var instrumentProcessing: Boolean
    val ctx: Context

    fun clearRules()
    fun halfExpandBottomSheet()
    fun showMessage(varDescr: Int)
    fun getString(int: Int): String
    fun getText(int: Int): CharSequence

    fun onWin(stepsCount: Double, currentTime: Long, state: StateType)
    fun onLose()
    fun startCreatingLevelUI()
}

class PlayScene {
    companion object {
        private const val TAG = "PlayScene"
        const val messageTime: Long = 2000
        val shared: PlayScene = PlayScene()
    }

    var listenerRef: WeakReference<PlaySceneListener> = WeakReference(null)

    /** GAME STATE */
    var currentRuleView: RuleMathView? = null
    fun setCurrentRuleView(context: Context, value: RuleMathView?) {
        currentRuleView = value
        if (value != null) {
            try {
                onRuleClicked(context)
            } catch (e: java.lang.Exception) {
                Logger.e(TAG, "Error during rule usage: ${e.message}")
                listenerRef.get()?.let {
                    Toast.makeText(it.ctx, R.string.misclick_happened_please_retry, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var stepsCount: Double = 0.0
    var currentTime: Long = 0
    lateinit var history: History

    /** TIMERS */
    var downTimer: MathDownTimer? = null
        private set
    var upTimer: MathUpTimer? = null
        private set

    private fun onRuleClicked(context: Context) {
        Logger.d(TAG, "onRuleClicked")
        if (GlobalScene.shared.tutorialProcessing) {
            TutorialScene.shared.onRuleClicked(currentRuleView!!)
            return
        }
        val listener = listenerRef.get() ?: return
        val prev = listener.globalMathView.expression!!.clone()
        val places: List<ExpressionNode> = listener.globalMathView.currentAtoms.toList()
        val oldSteps = stepsCount
        var levelPassed = false
        currentRuleView?.let {
            val res = listener.globalMathView.performSubstitutionForMultiselect(it.subst!!)
            if (res != null) {
                stepsCount++
                history.saveState(stepsCount, currentTime, listener.globalMathView.expression!!)
                listener.previous.isEnabled = true
                if (LevelScene.shared.currentLevel!!.checkEnd(res)) {
                    levelPassed = true

                    Statistics.logRule(
                        oldSteps,
                        stepsCount,
                        prev,
                        listener.globalMathView.expression!!,
                        it.subst,
                        places
                    )

                    onWin()
                }
                listener.clearRules()
                listener.globalMathView.currentRulesToResult = null
            } else {
                listener.showMessage(R.string.wrong_subs)
            }

        }
        if (!levelPassed) {
            Statistics.logRule(
                oldSteps, stepsCount, prev, listener.globalMathView.expression!!,
                currentRuleView!!.subst, places
            )
        }
    }

    fun onAtomClicked() {
        Logger.d(TAG, "onAtomClicked")
        val listener = listenerRef.get() ?: return
        if (listener.instrumentProcessing && InstrumentScene.shared.currentProcessingInstrument?.type != InstrumentType.MULTI) {
            InstrumentScene.shared.choosenAtom(listener.globalMathView.currentAtoms, listener.globalMathView.text)
        } else if (listener.globalMathView.currentAtoms.isNotEmpty()) {
            val curLvl = LevelScene.shared.currentLevel?: return
            if (listener.globalMathView.multiselectionMode) {
                listener.previous.isEnabled = true
            }
            val substitutionApplication = curLvl.getSubstitutionApplication(
                listener.globalMathView.currentAtoms,
                listener.globalMathView.expression!!
            )
            if (substitutionApplication == null) {
                listener.showMessage(R.string.no_rules)
                listener.clearRules()
                if (!listener.globalMathView.multiselectionMode) {
                    listener.globalMathView.clearExpression()
                }
            } else {
                val rules =
                    curLvl.getRulesFromSubstitutionApplication(substitutionApplication)
                listener.globalMathView.currentRulesToResult =
                    curLvl.getResultFromSubstitutionApplication(substitutionApplication)
                listener.rulesScrollView.visibility = View.VISIBLE
                redrawRules(listener, rules)
            }
        } else {
            listener.previous.isEnabled = history.isUndoable()
        }
        Statistics.logPlace(stepsCount, listener.globalMathView.expression!!, listener.globalMathView.currentAtoms)
    }

    fun loadLevel(listener: PlaySceneListener, continueGame: Boolean, languageCode: String): Boolean {
        Logger.d(TAG, "loadLevel")
        val currentLevel = LevelScene.shared.currentLevel!!
        listener.clearRules()
        cancelTimers()
        listener.endExpressionViewLabel.text = Html.fromHtml(
            String.format(
                Html.toHtml(SpannedString(listener.getText(R.string.end_expression_opened))),
                currentLevel.getDescriptionByLanguage(languageCode)
            )
        )
        listener.endExpressionViewLabel.visibility = View.VISIBLE
        listener.endExpressionMathView.visibility = View.GONE
        if (!currentLevel.goalExpressionStructureString.isNullOrBlank()) {
            listener.endExpressionMathView.setExpression(currentLevel.goalExpressionStructureString!!, null)
            listener.endExpressionMathView.visibility = View.VISIBLE
        }
        if (currentLevel.endless) {
            loadEndless(listener, continueGame)
        } else {
            loadFinite(listener)
        }
        history.clear()
        history.saveState(stepsCount, currentTime, listener.globalMathView.expression!!)
        // listener.showMessage("\uD83C\uDF40 ${currentLevel.getNameByLanguage(languageCode)} \uD83C\uDF40")
        Statistics.setStartTime()
        Statistics.logStart()
        return true
    }

    private fun loadFinite(listener: PlaySceneListener) {
        val currentLevel = LevelScene.shared.currentLevel!!
        listener.globalMathView.setExpression(currentLevel.startExpression.clone(), currentLevel.subjectType)
        listener.globalMathView.center()
        stepsCount = 0.0
        currentTime = 0
        downTimer = MathDownTimer(listener, currentLevel.time, 1)
        downTimer!!.start()
    }

    private fun loadEndless(listener: PlaySceneListener, continueGame: Boolean) {
        val currentLevel = LevelScene.shared.currentLevel!!
        val lastRes = currentLevel.lastResult
        if (continueGame && lastRes != null &&
            lastRes.state == StateType.PAUSED
        ) {
            stepsCount = lastRes.steps
            currentTime = lastRes.time
            listener.globalMathView.setExpression(lastRes.expression, currentLevel.subjectType)
            listener.globalMathView.center()
        } else {
            LevelScene.shared.levelsActivityRef.get()?.updateResult(null)
            listener.globalMathView.setExpression(currentLevel.startExpression.clone(), currentLevel.subjectType)
            listener.globalMathView.center()
            stepsCount = 0.0
            currentTime = 0
        }
        upTimer = MathUpTimer(listener, 1)
        upTimer!!.start(listener.ctx)
    }

    fun previousStep() {
        Logger.d(TAG, "previousStep")
        val listener = listenerRef.get() ?: return

        if (listener.instrumentProcessing) {
            InstrumentScene.shared.turnOffCurrentInstrument()
        } else {
            val state = history.getPreviousStep()
            val oldExpression = listener.globalMathView.expression!!
            Logger.d(TAG, "${state?.expression} $oldExpression")
            val oldSteps = stepsCount
            if (state != null) {
                listener.clearRules()
                val currentLevel = LevelScene.shared.currentLevel!!
                listener.globalMathView.setExpression(state.expression, currentLevel.subjectType, false)
                //val penalty = UndoPolicyHandler.getPenalty(currentLevel.undoPolicy, state.depth)
                //stepsCount = stepsCount - 1 + penalty
                if (!history.isUndoable()) {
                    listener.previous.isEnabled = false
                }
            }
            Statistics.logUndo(
                oldSteps, stepsCount, oldExpression,
                listener.globalMathView.expression!!, listener.globalMathView.currentAtoms
            )
        }
    }

    fun restart(listener: PlaySceneListener, languageCode: String) {
        Logger.d(TAG, "restart")
        Statistics.logRestart(stepsCount, listener.globalMathView.expression!!, listener.globalMathView.currentAtoms)
        loadLevel(listener, false, languageCode)
    }

    fun menu(activity: PlayActivity, logAndSave: Boolean = true) {
        Logger.d(TAG, "menu")
        activity.setMultiselectionMode(false)
        if (logAndSave) { // TODO: && stepsCount > 0 (server is now saving state even if stepsCount == 0)
            history.saveState(stepsCount, currentTime, activity.globalMathView.expression!!)
            Statistics.logInterim(stepsCount, activity.globalMathView.expression!!)
        }
        Statistics.logMenu(stepsCount, activity.globalMathView.expression!!, activity.globalMathView.currentAtoms)
    }

    fun info(languageCode: String, activity: PlayActivity) {
        val currentLevel = LevelScene.shared.currentLevel!!
        val multi = activity.globalMathView.multiselectionMode
        val builder = AlertDialog.Builder(
            activity, ThemeController.shared.alertDialogTheme
        )
        val v = activity.layoutInflater.inflate(R.layout.level_info, null)
        v.findViewById<TextView>(R.id.game)?.text = currentLevel.game.getNameByLanguage(languageCode)
        v.findViewById<TextView>(R.id.name)?.text = currentLevel.getNameByLanguage(languageCode)
        v.findViewById<TextView>(R.id.description)?.text = currentLevel.getDescriptionByLanguage(languageCode)
        if (!currentLevel.goalExpressionStructureString.isNullOrBlank()) {
            v.findViewById<SimpleMathView>(R.id.info_end_math_view)
                .setExpression(currentLevel.goalExpressionStructureString!!, null)
            v.findViewById<View>(R.id.info_end_math_view_row).visibility = View.VISIBLE
        }
        v.findViewById<TextView>(R.id.steps)?.text = stepsCount.toInt().toString()
        v.findViewById<TextView>(R.id.mode)?.text =
            if (multi) activity.getString(R.string.multiselection_mode_is_on)
            else activity.getString(R.string.multiselection_mode_is_off)
        builder.setView(v)
        val alert = builder.create()
        AndroidUtil.showDialog(
            alert, bottomGravity = false, backMode = BackgroundMode.BLUR,
            blurView = activity.blurView, activity = activity
        )
    }

    private fun redrawRules(listener: PlaySceneListener, rules: List<ExpressionSubstitution>) {
        Logger.d(TAG, "redrawRules")
        listener.rulesLinearLayout.removeAllViews()
        for (r in rules) {
            try {
                val rule = RuleMathView(listener.ctx)
                rule.setSubst(r, LevelScene.shared.currentLevel!!.subjectType)
                listener.rulesLinearLayout.addView(rule)
            } catch (e: Exception) {
                Logger.e(TAG, "Rule draw Error: $e")
            }
        }
        listener.halfExpandBottomSheet()
        listener.rulesMsg.text = if (rules.isEmpty()) listener.getString(R.string.no_rules_msg)
        else listener.getString(R.string.rules_found_msg)
    }

    fun onWin() {
        Logger.d(TAG, "onWin")
        val listener = listenerRef.get() ?: return
        val currentLevel = LevelScene.shared.currentLevel!!
        //val award = currentLevel.getAward(context, currentTime, stepsCount)
        val newRes = LevelResult(stepsCount, currentTime, StateType.DONE)
        if (newRes.isBetter(currentLevel.lastResult)) {
            LevelScene.shared.levelsActivityRef.get()!!.updateResult(newRes)
        }
        listener.onWin(stepsCount, currentTime, StateType.DONE)
        Statistics.logWin(stepsCount, listener.globalMathView.expression!!)
    }

    fun onLose() {
        Logger.d(TAG, "onLose")
        val listener = listenerRef.get() ?: return
        listener.onLose()
        Statistics.logLoose(stepsCount, listener.globalMathView.expression!!, listener.globalMathView.currentAtoms)
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