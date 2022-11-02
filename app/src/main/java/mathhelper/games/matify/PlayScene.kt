package mathhelper.games.matify

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
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

interface PlaySceneListener {
    // layout elements
    var rulesLinearLayout: LinearLayout
    var endExpressionMathView: SimpleMathView
    var endExpressionViewLabel: TextView
    var rulesScrollView: ScrollView
    var globalMathView: GlobalMathView
    var rulesMsg: TextView
    var messageView: TextView

    var instrumentProcessing: Boolean

    fun clearRules()
    fun getString(id: Int): String
    fun getText(id: Int): CharSequence
    fun halfExpandBottomSheet()
}

class PlayScene {
    companion object {
        private const val TAG = "PlayScene"
        const val messageTime: Long = 2000
        val shared: PlayScene = PlayScene()
    }

    var activityRef: WeakReference<PlaySceneListener> = WeakReference(null)

    /** GAME STATE */
    var currentRuleView: RuleMathView? = null
    fun setCurrentRuleView(context: Context, value: RuleMathView?) {
        currentRuleView = value
        if (value != null) {
            Handler().postDelayed({
                try {
                    onRuleClicked(context)
                } catch (e: java.lang.Exception) {
                    Logger.e(TAG, "Error during rule usage: ${e.message}")
                    activityRef.get()?.let {
                        Toast.makeText(it as Context, R.string.misclick_happened_please_retry, Toast.LENGTH_LONG).show()
                    }
                }
            }, 100)
        }
    }

    var stepsCount: Double = 0.0
    var currentTime: Long = 0
    lateinit var history: History

    /** TIMERS */
    private val messageTimer = MessageTimer()
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
        val activity = activityRef.get() as PlayActivity
        val prev = activity.globalMathView.expression!!.clone()
        val places: List<ExpressionNode> = activity.globalMathView.currentAtoms.toList()
        val oldSteps = stepsCount
        var levelPassed = false
        if (currentRuleView!!.subst != null) {
            val res = activity.globalMathView.performSubstitutionForMultiselect(currentRuleView!!.subst!!)
            if (res != null) {
                stepsCount++
                history.saveState(stepsCount, currentTime, activity.globalMathView.expression!!)
                activity.previous.isEnabled = true
                if (LevelScene.shared.currentLevel!!.checkEnd(res)) {
                    levelPassed = true

                    Statistics.logRule(
                        oldSteps,
                        stepsCount,
                        prev,
                        activity.globalMathView.expression!!,
                        currentRuleView!!.subst,
                        places
                    )

                    onWin()
                }
                activity.clearRules()
                activity.globalMathView.currentRulesToResult = null
            } else {
                showMessage(activity.getString(R.string.wrong_subs))
            }

        }
        if (!levelPassed) {
            Statistics.logRule(
                oldSteps, stepsCount, prev, activity.globalMathView.expression!!,
                currentRuleView!!.subst, places
            )
        }
    }

    fun onAtomClicked() {
        Logger.d(TAG, "onAtomClicked")
        val activity = activityRef.get() as PlayActivity
        if (activity.instrumentProcessing && InstrumentScene.shared.currentProcessingInstrument?.type != InstrumentType.MULTI) {
            InstrumentScene.shared.choosenAtom(activity.globalMathView.currentAtoms, activity.globalMathView.text)
        } else if (activity.globalMathView.currentAtoms.isNotEmpty()) {
            if (activity.globalMathView.multiselectionMode) {
                activity.previous.isEnabled = true
            }
            val substitutionApplication = LevelScene.shared.currentLevel!!.getSubstitutionApplication(
                activity.globalMathView.currentAtoms,
                activity.globalMathView.expression!!
            )
            if (substitutionApplication == null) {
                showMessage(activity.getString(R.string.no_rules))
                activity.clearRules()
                if (!activity.globalMathView.multiselectionMode) {
                    activity.globalMathView.clearExpression()
                }
            } else {
                val rules =
                    LevelScene.shared.currentLevel!!.getRulesFromSubstitutionApplication(substitutionApplication)
                activity.globalMathView.currentRulesToResult =
                    LevelScene.shared.currentLevel!!.getResultFromSubstitutionApplication(substitutionApplication)
                activity.rulesScrollView.visibility = View.VISIBLE
                redrawRules(rules)
            }
        } else {
            activity.previous.isEnabled = !history.empty
        }
        Statistics.logPlace(stepsCount, activity.globalMathView.expression!!, activity.globalMathView.currentAtoms)
    }

    fun loadLevel(context: Context, continueGame: Boolean, languageCode: String): Boolean {
        Logger.d(TAG, "loadLevel")
        val currentLevel = LevelScene.shared.currentLevel!!
        val activity = activityRef.get()?:return false
        activity.clearRules()
        cancelTimers()
        // val text = activity.getString(R.string.end_expression_opened, currentLevel.getDescriptionByLanguage(languageCode))
        activity.endExpressionViewLabel.text = Html.fromHtml(
            String.format(
                Html.toHtml(SpannedString(activity.getText(R.string.end_expression_opened))),
                currentLevel.getDescriptionByLanguage(languageCode)
            )
        )
        activity.endExpressionViewLabel.visibility = View.VISIBLE
        activity.endExpressionMathView.visibility = View.GONE
        if (!currentLevel.goalExpressionStructureString.isNullOrBlank()) {
            activity.endExpressionMathView.setExpression(currentLevel.goalExpressionStructureString!!, null)
            activity.endExpressionMathView.visibility = View.VISIBLE
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
        val activity = activityRef.get()!!
        activity.globalMathView.setExpression(currentLevel.startExpression.clone(), currentLevel.subjectType)
        activity.globalMathView.center()
        stepsCount = 0.0
        currentTime = 0
        downTimer = MathDownTimer(currentLevel.time, 1)
        downTimer!!.start()
    }

    private fun loadEndless(context: Context, continueGame: Boolean) {
        val activity = activityRef.get()!!
        val currentLevel = LevelScene.shared.currentLevel!!
        if (continueGame && currentLevel.lastResult != null &&
            currentLevel.lastResult!!.state == StateType.PAUSED
        ) {
            stepsCount = currentLevel.lastResult!!.steps
            currentTime = currentLevel.lastResult!!.time
            activity.globalMathView.setExpression(currentLevel.lastResult!!.expression, currentLevel.subjectType)
            activity.globalMathView.center()
        } else {
            LevelScene.shared.levelsActivity?.updateResult(null)
            activity.globalMathView.setExpression(currentLevel.startExpression.clone(), currentLevel.subjectType)
            activity.globalMathView.center()
            stepsCount = 0.0
            currentTime = 0
        }
        upTimer = MathUpTimer(1)
        upTimer!!.start(context)
    }

    fun previousStep() {
        Logger.d(TAG, "previousStep")
        val activity = activityRef.get() as PlayActivity
        if (activity.instrumentProcessing) {
            InstrumentScene.shared.turnOffCurrentInstrument(activity as Context)
        } else {
            val state = history.getPreviousStep()
            val oldExpression = activity.globalMathView.expression!!
            val oldSteps = stepsCount
            if (state != null) {
                activity.clearRules()
                val currentLevel = LevelScene.shared.currentLevel!!
                activity.globalMathView.setExpression(state.expression, currentLevel.subjectType, false)
                //val penalty = UndoPolicyHandler.getPenalty(currentLevel.undoPolicy, state.depth)
                //stepsCount = stepsCount - 1 + penalty
                if (history.empty) {
                    activity.previous.isEnabled = false
                }
            }
            Statistics.logUndo(
                oldSteps, stepsCount, oldExpression,
                activity.globalMathView.expression!!, activity.globalMathView.currentAtoms
            )
        }
    }

    fun restart(context: Context, languageCode: String) {
        Logger.d(TAG, "restart")
        val activity = activityRef.get()!!
        Statistics.logRestart(stepsCount, activity.globalMathView.expression!!, activity.globalMathView.currentAtoms)
        loadLevel(context, false, languageCode)
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
            activity as Context, ThemeController.shared.alertDialogTheme
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

    private fun redrawRules(rules: List<ExpressionSubstitution>) {
        Logger.d(TAG, "redrawRules")
        val activity = activityRef.get()!!
        activity.rulesLinearLayout.removeAllViews()
        for (r in rules) {
            try {
                val rule = RuleMathView(activity as Context)
                rule.setSubst(r, LevelScene.shared.currentLevel!!.subjectType)
                activity.rulesLinearLayout.addView(rule)
            } catch (e: Exception) {
                Logger.e(TAG, "Rule draw Error: $e")
            }
        }
        activity.halfExpandBottomSheet()
        activity.rulesMsg.text = if (rules.isEmpty()) activity.getString(R.string.no_rules_msg)
        else activity.getString(R.string.rules_found_msg)
    }

    fun onWin() {
        Logger.d(TAG, "onWin")
        val activity = activityRef.get() as PlayActivity
        val currentLevel = LevelScene.shared.currentLevel!!
        //val award = currentLevel.getAward(context, currentTime, stepsCount)
        val newRes = LevelResult(stepsCount, currentTime, StateType.DONE)
        if (newRes.isBetter(currentLevel.lastResult)) {
            LevelScene.shared.levelsActivity!!.updateResult(newRes)
        }
        activity.onWin(stepsCount, currentTime, StateType.DONE)
        Statistics.logWin(stepsCount, activity.globalMathView.expression!!)
    }

    fun onLose() {
        Logger.d(TAG, "onLose")
        val activity = activityRef.get()!! as PlayActivity
        activity.onLose()
        Statistics.logLoose(stepsCount, activity.globalMathView.expression!!, activity.globalMathView.currentAtoms)
    }

    private fun showMessage(msg: String) {
        val activity = activityRef.get()!!
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