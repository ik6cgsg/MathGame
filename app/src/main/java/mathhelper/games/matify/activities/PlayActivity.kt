package mathhelper.games.matify.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BulletSpan
import android.view.View
import android.widget.*
import mathhelper.games.matify.*
import mathhelper.games.matify.common.*
import mathhelper.games.matify.level.History
import mathhelper.games.matify.level.StateType
import mathhelper.games.matify.statistics.Statistics
import mathhelper.twf.expressiontree.ExpressionNode
import java.lang.ref.WeakReference

class PlayActivity : AbstractPlayableActivity(), ConnectionListener, PlaySceneListener, InstrumentSceneListener,
    TimerListener {
    override val TAG = "PlayActivity"
    private var loading = false
    private lateinit var loseDialog: AlertDialog
    private lateinit var winDialog: AlertDialog
    private lateinit var continueDialog: AlertDialog
    private lateinit var progress: ProgressBar

    lateinit var offline: TextView

    lateinit var back: TextView
    lateinit var info: TextView
    lateinit var restart: TextView
    override lateinit var previous: TextView

    private val messageTimer = MessageTimer(this)

    override fun setViews() {
        super.setViews()
        mainView = findViewById(R.id.activity_play)
        mainViewAnim = mainView.background as TransitionDrawable

        back = findViewById(R.id.back)
        restart = findViewById(R.id.restart)
        previous = findViewById(R.id.previous)
        previous.isEnabled = false
        info = findViewById(R.id.info)
        progress = findViewById(R.id.progress)
        offline = findViewById(R.id.offline)
        offline.visibility = View.GONE
        ConnectionChecker.shared.subscribe(this)
    }

    override fun showMessage(varDescr: Int) {
        super.showMessage(varDescr)
        messageTimer.cancel()
        messageTimer.start()
    }

    override fun showMessage(str: String) {
        messageView.text = str
        messageView.visibility = View.VISIBLE
        messageTimer.cancel()
        messageTimer.start()
    }

    private fun setLongClick() {
        back.setOnLongClickListener {
            showMessage(R.string.back_info)
            true
        }
        previous.setOnLongClickListener {
            showMessage(
                if (globalMathView.multiselectionMode)
                    R.string.previous_multiselect_info
                else
                    R.string.previous_info
            )
            true
        }
        restart.setOnLongClickListener {
            showMessage(R.string.restart_info)
            true
        }
        info.setOnLongClickListener {
            showMessage(R.string.i_info)
            true
        }
        globalMathView.setOnLongClickListener {
            if (!globalMathView.multiselectionMode) {
                InstrumentScene.shared.clickInstrument("multi")
            }
            AndroidUtil.vibrate(this)
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidUtil.setLanguage(this)
        setTheme(ThemeController.shared.currentTheme.resId)
        setContentView(R.layout.activity_play_new)
        setViews()
        messageView.visibility = View.GONE
        loseDialog = createLoseDialog()
        winDialog = createWinDialog()
        continueDialog = createContinueDialog()
        PlayScene.shared.listenerRef = WeakReference(this)
        PlayScene.shared.history = History()
        startCreatingLevelUI()
        setLongClick()
    }

    override fun onResume() {
        super.onResume()
        if (GlobalScene.shared.currentGame == null || LevelScene.shared.currentLevel == null) {
            finishAffinity()
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (!loading) {
            back(null)
        }
    }

    override fun finish() {
        PlayScene.shared.cancelTimers()
        PlayScene.shared.listenerRef.clear()
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        ConnectionChecker.shared.unsubscribe(this)
    }

    override fun onConnectionChange(type: ConnectionChangeType) {
        runOnUiThread {
            if (type == ConnectionChangeType.ESTABLISHED) {
                offline.visibility = View.GONE
            } else {
                offline.visibility = View.VISIBLE
            }
        }
    }

    override fun connectionBannerClicked(v: View?) {
        ConnectionChecker.shared.connectionBannerClicked(this, blurView, ActivityType.PLAY)
    }

    override fun connectionButtonClick(v: View) {
        ConnectionChecker.shared.connectionButtonClick(this, v)
    }

    override fun startCreatingLevelUI() {
        if (LevelScene.shared.wasLevelPaused()) {
            AndroidUtil.showDialog(continueDialog)
        } else {
            createLevelUI(false)
        }
    }

    private fun createLevelUI(continueGame: Boolean) {
        loading = true
        timerView.text = ""
        globalMathView.text = ""
        endExpressionViewLabel.text = ""
        progress.visibility = View.VISIBLE
        try {
            PlayScene.shared.loadLevel(this, continueGame, resources.configuration.locale.language)
        } catch (e: Exception) {
            Logger.e(TAG, "Error while level loading")
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
        }
        progress.visibility = View.GONE
        loading = false
    }

    fun clear(v: View?) {
        globalMathView.clearExpression()
        clearRules()
    }

    fun previous(v: View?) {
        if (!loading) {
            if (!globalMathView.multiselectionMode || globalMathView.currentAtoms.isEmpty())
                PlayScene.shared.previousStep()
            else {
                globalMathView.deleteLastSelect()
            }
        }
    }

    fun restart(v: View?) {
        if (!loading) {
            globalMathView.scale = 1f
            PlayScene.shared.restart(this, resources.configuration.locale.language)
        }
    }

    fun detailClick(v: View) {
        InstrumentScene.shared.clickDetail(v as Button)
    }

    fun keyboardClick(v: View) {
        InstrumentScene.shared.clickKeyboard(v as Button)
    }

    fun info(v: View?) {
        PlayScene.shared.info(resources.configuration.locale.language, this)
    }

    fun back(v: View?) {
        if (!loading) {
            returnToMenu()
        }
    }

    private fun returnToMenu() {
        PlayScene.shared.menu(this)
        finish()
    }

    override fun onWin(stepsCount: Double, currentTime: Long, state: StateType) {
        Logger.d(TAG, "onWin")
        val msgTitle = resources.getString(R.string.you_finished_level_with)
        val steps = "\n\t${resources.getString(R.string.steps)}: ${stepsCount.toInt()}"
        val sec = "${currentTime % 60}".padStart(2, '0')
        val time = "\n\t${resources.getString(R.string.time)}: ${currentTime / 60}:$sec"
        val spannable = SpannableString("$msgTitle$steps$time\n")
        val spanColor = ThemeController.shared.color(ColorName.PRIMARY_COLOR)
        spannable.setSpan(
            BulletSpan(5, spanColor), msgTitle.length + 1,
            msgTitle.length + steps.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            BulletSpan(5, spanColor),
            msgTitle.length + steps.length + 1, msgTitle.length + steps.length + time.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        winDialog.setMessage(spannable)
        AndroidUtil.showDialog(winDialog, backMode = BackgroundMode.BLUR, blurView = blurView, activity = this)
    }

    override fun onLose() {
        AndroidUtil.showDialog(loseDialog, backMode = BackgroundMode.BLUR, blurView = blurView, activity = this)
    }

    private fun createWinDialog(): AlertDialog {
        Logger.d(TAG, "createWinDialog")
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.congratulations)
            .setMessage("")
            .setPositiveButton(R.string.next) { dialog: DialogInterface, id: Int -> }
            .setNeutralButton(R.string.menu) { dialog: DialogInterface, id: Int ->
                PlayScene.shared.menu(this, false)
                finish()
            }
            .setNegativeButton(R.string.restart_label) { dialog: DialogInterface, id: Int ->
                globalMathView.scale = 1f
                PlayScene.shared.restart(this, resources.configuration.locale.language)
            }
            .setCancelable(false)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                globalMathView.scale = 1f
                if (!LevelScene.shared.nextLevel()) {
                    Toast.makeText(this, R.string.next_after_last_level_label, Toast.LENGTH_SHORT).show()
                } else {
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    private fun createLoseDialog(): AlertDialog {
        Logger.d(TAG, "createLooseDialog")
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.time_out)
            .setPositiveButton(R.string.restart) { _: DialogInterface, _: Int ->
                restart(null)
            }
            .setNegativeButton(R.string.menu) { _: DialogInterface, _: Int ->
                back(null)
            }
            .setCancelable(false)
        return builder.create()
    }

    private fun createContinueDialog(): AlertDialog {
        Logger.d(TAG, "createContinueDialog")
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.welkome_back)
            .setMessage(R.string.continue_from_where_you_stopped)
            .setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
                createLevelUI(true)
            }
            .setNegativeButton(R.string.no) { _: DialogInterface, _: Int ->
                createLevelUI(false)
            }
            .setCancelable(false)
        return builder.create()
    }

    override fun setMultiselectionMode(multi: Boolean) {
        previous.isEnabled = PlayScene.shared.history.isUndoable()
        super.setMultiselectionMode(multi)
    }

    override fun showEndExpression(v: View?) {
        val builder = AlertDialog.Builder(this, ThemeController.shared.alertDialogTheme)
        builder
            .setTitle(getString(R.string.goal_description))
            .setMessage(
                LevelScene.shared.currentLevel!!.getDescriptionByLanguage(
                    resources.configuration.locale.language,
                    true
                )
            )
            .setOnCancelListener { }
            .setCancelable(true)
        val alert = builder.create()
        AndroidUtil.showDialog(
            alert, bottomGravity = false, backMode = BackgroundMode.BLUR,
            blurView = blurView, activity = this
        )

    }

    override fun onRuleClicked(ruleView: RuleMathView) {
        Logger.d(TAG, "onRuleClicked")
        try {
            val prev = globalMathView.expression!!.clone()
            val places: List<ExpressionNode> = globalMathView.currentAtoms.toList()
            val oldSteps = PlayScene.shared.stepsCount
            var levelPassed = false
            val res = globalMathView.performSubstitutionForMultiselect(ruleView.subst!!)
            if (res != null) {
                PlayScene.shared.stepsCount++
                PlayScene.shared.history.saveState(PlayScene.shared.stepsCount, PlayScene.shared.currentTime, globalMathView.expression!!)
                previous.isEnabled = true
                if (LevelScene.shared.currentLevel!!.checkEnd(res)) {
                    levelPassed = true

                    Statistics.logRule(
                        oldSteps,
                        PlayScene.shared.stepsCount,
                        prev,
                        globalMathView.expression!!,
                        ruleView.subst,
                        places
                    )

                    PlayScene.shared.onWin()
                }
                clearRules()
                globalMathView.currentRulesToResult = null
            } else {
                showMessage(R.string.wrong_subs)
            }

            if (!levelPassed) {
                Statistics.logRule(
                    oldSteps, PlayScene.shared.stepsCount, prev, globalMathView.expression!!,
                    ruleView.subst, places
                )
            }
        } catch (e: java.lang.Exception) {
            Logger.e(TAG, "Error during rule usage: ${e.message}")
            Toast.makeText(this, R.string.misclick_happened_please_retry, Toast.LENGTH_LONG).show()
        }
    }

    override fun onAtomClicked() {
        Logger.d(TAG, "onAtomClicked")
        if (instrumentProcessing && InstrumentScene.shared.currentProcessingInstrument?.type != InstrumentType.MULTI) {
            InstrumentScene.shared.choosenAtom(globalMathView.currentAtoms, globalMathView.text)
        } else if (globalMathView.currentAtoms.isNotEmpty()) {
            val curLvl = LevelScene.shared.currentLevel ?: return
            if (globalMathView.multiselectionMode) {
                previous.isEnabled = true
            }
            val substitutionApplication = curLvl.getSubstitutionApplication(
                globalMathView.currentAtoms,
                globalMathView.expression!!
            )
            if (substitutionApplication == null) {
                showMessage(R.string.no_rules)
                clearRules()
                if (!globalMathView.multiselectionMode) {
                    globalMathView.clearExpression()
                }
            } else {
                val rules =
                    curLvl.getRulesFromSubstitutionApplication(substitutionApplication)
                globalMathView.currentRulesToResult =
                    curLvl.getResultFromSubstitutionApplication(substitutionApplication)
                redrawRules(rules, LevelScene.shared.currentLevel!!.subjectType)
            }
        } else {
            previous.isEnabled = PlayScene.shared.history.isUndoable()
        }
        Statistics.logPlace(PlayScene.shared.stepsCount, globalMathView.expression!!, globalMathView.currentAtoms)
    }
}
