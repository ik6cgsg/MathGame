package mathhelper.games.matify.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BulletSpan
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import eightbitlab.com.blurview.BlurView
import kotlinx.android.synthetic.main.activity_play.*
import mathhelper.games.matify.*
import mathhelper.games.matify.common.*
import mathhelper.games.matify.level.StateType

abstract class GeneralPlayActivity: AppCompatActivity() {
    lateinit var mainView: ConstraintLayout
    lateinit var mainViewAnim: TransitionDrawable
    lateinit var globalMathView: GlobalMathView
    lateinit var endExpressionViewLabel: TextView
    lateinit var endExpressionMathView: SimpleMathView
    lateinit var messageView: TextView
    lateinit var bottomSheet: LinearLayout
    lateinit var rulesLinearLayout: LinearLayout
    lateinit var rulesScrollView: ScrollView
    lateinit var rulesMsg: TextView
    lateinit var timerView: TextView

    protected open fun setViews() {
        globalMathView = findViewById(R.id.global_expression)
        endExpressionMathView = findViewById(R.id.end_expression_math_view)
        endExpressionViewLabel = findViewById(R.id.end_expression_label)
        endExpressionViewLabel.visibility = View.GONE
        endExpressionMathView.visibility = View.GONE
        bottomSheet = findViewById(R.id.bottom_sheet)
        messageView = findViewById(R.id.message_view)
        timerView = findViewById(R.id.timer_view)

        rulesLinearLayout = bottomSheet.findViewById(R.id.rules_linear_layout)
        rulesScrollView = bottomSheet.findViewById(R.id.rules_scroll_view)
        rulesMsg = bottomSheet.findViewById(R.id.rules_msg)
        InstrumentScene.shared.init(bottomSheet, this)
    }

    protected var needClear: Boolean = false
    var instrumentProcessing: Boolean = false

    abstract fun showMessage(msg: String, flag: Boolean = true, ifFlagFalseMsg: String? = null)
    abstract fun setMultiselectionMode(multi: Boolean)

    fun collapseBottomSheet() {
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun halfExpandBottomSheet() {
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun clearRules() {
        rulesScrollView.visibility = View.GONE
        rulesMsg.text = getString(R.string.no_rules_msg)
        if (!instrumentProcessing) {
            collapseBottomSheet()
        }
    }
}

class PlayActivity: GeneralPlayActivity(), ConnectionListener {
    private val TAG = "PlayActivity"
    private var loading = false
    private lateinit var loseDialog: AlertDialog
    private lateinit var winDialog: AlertDialog
    private lateinit var continueDialog: AlertDialog
    private lateinit var progress: ProgressBar

    lateinit var blurView: BlurView
    lateinit var offline: TextView

    private lateinit var restart: TextView
    private lateinit var back: TextView
    private lateinit var info: TextView
    private lateinit var previous: TextView

    private val messageTimer = MessageTimer()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Logger.d(TAG, "onTouchEvent")
        if (globalMathView.onTouchEvent(event)) {
            needClear = false
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!globalMathView.multiselectionMode)
                    needClear = true
            }
            MotionEvent.ACTION_UP -> {
                if (needClear) {
                    try {
                        globalMathView.clearExpression()
                        clearRules()
                    } catch (e: Exception) {
                        Logger.e(TAG, "Error while clearing rules on touch: ${e.message}")
                        Toast.makeText(this, R.string.misclick_happened_please_retry, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        return true
    }

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
        blurView = findViewById(R.id.blurView)
        offline = findViewById(R.id.offline)
        offline.visibility = View.GONE
        ConnectionChecker.shared.subscribe(this)
    }

    private fun setLongClick() {
        back.setOnLongClickListener {
            showMessage(getString(R.string.back_info))
            true
        }
        previous.setOnLongClickListener {
            showMessage(
                getString(R.string.previous_multiselect_info),
                globalMathView.multiselectionMode,
                getString(R.string.previous_info)
            )
            true
        }
        restart.setOnLongClickListener {
            showMessage(getString(R.string.restart_info))
            true
        }
        info.setOnLongClickListener {
            showMessage(getString(R.string.i_info))
            true
        }
        globalMathView.setOnLongClickListener {
            if (!globalMathView.multiselectionMode) {
                InstrumentScene.shared.clickInstrument("multi", this)
            }
            AndroidUtil.vibrate(this)
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        AndroidUtil.setLanguage(this)
        setTheme(ThemeController.shared.currentTheme.resId)
        setContentView(R.layout.activity_play_new)
        setViews()
        messageView.visibility = View.GONE
        loseDialog = createLooseDialog()
        winDialog = createWinDialog()
        continueDialog = createContinueDialog()
        PlayScene.shared.playActivity = this
        Handler().postDelayed({
            startCreatingLevelUI()
        }, 100)
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
        PlayScene.shared.playActivity = null
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

    fun startCreatingLevelUI() {
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

    fun instrumentClick(v: View) {
        InstrumentScene.shared.clickInstrument(v.tag.toString(), this)
    }

    fun detailClick(v: View) {
        InstrumentScene.shared.clickDetail(v as Button)
    }

    fun keyboardClick(v: View) {
        InstrumentScene.shared.clickKeyboard(v as Button)
    }

    fun info(v: View?) {
        PlayScene.shared.info(resources.configuration.locale.language)
    }

    fun back(v: View?) {
        if (!loading) {
            returnToMenu()
        }
    }

    override fun showMessage(msg: String, flag: Boolean, ifFlagFalseMsg: String?) {
        if (flag)
            messageView.text = msg
        else
            messageView.text = ifFlagFalseMsg
        messageView.visibility = View.VISIBLE
        messageTimer.cancel()
        messageTimer.start()
    }

    private fun returnToMenu() {
        PlayScene.shared.menu()
        finish()
    }

    fun showEndExpression(v: View?) {
        val builder = AlertDialog.Builder(this, ThemeController.shared.alertDialogTheme)
        builder
            .setTitle(getString(R.string.goal_description))
            .setMessage(LevelScene.shared.currentLevel!!.getDescriptionByLanguage(resources.configuration.locale.language, true))
            .setOnCancelListener {  }
            .setCancelable(true)
        val alert = builder.create()
        AndroidUtil.showDialog(alert, bottomGravity = false, backMode = BackgroundMode.BLUR,
            blurView = blurView, activity = this)
    }

    fun onWin(stepsCount: Double, currentTime: Long, state: StateType) {
        Logger.d(TAG, "onWin")
        val msgTitle = resources.getString(R.string.you_finished_level_with)
        val steps = "\n\t${resources.getString(R.string.steps)}: ${stepsCount.toInt()}"
        val sec = "${currentTime % 60}".padStart(2, '0')
        val time = "\n\t${resources.getString(R.string.time)}: ${currentTime / 60}:$sec"
        val spannable = SpannableString("$msgTitle$steps$time\n")
        val spanColor = ThemeController.shared.color(ColorName.PRIMARY_COLOR)
        spannable.setSpan(BulletSpan(5, spanColor), msgTitle.length + 1,
            msgTitle.length + steps.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(BulletSpan(5, spanColor),
            msgTitle.length + steps.length + 1, msgTitle.length + steps.length + time.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        winDialog.setMessage(spannable)
        AndroidUtil.showDialog(winDialog, backMode = BackgroundMode.BLUR, blurView = blurView, activity = this)
    }

    fun onLoose() {
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
                PlayScene.shared.menu(false)
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

    private fun createLooseDialog(): AlertDialog {
        Logger.d(TAG, "createLooseDialog")
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.time_out)
            .setPositiveButton(R.string.restart) { dialog: DialogInterface, id: Int ->
                restart(null)
            }
            .setNegativeButton(R.string.menu) { dialog: DialogInterface, id: Int ->
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
            .setPositiveButton(R.string.yes) { dialog: DialogInterface, id: Int ->
                createLevelUI(true)
            }
            .setNegativeButton(R.string.no) { dialog: DialogInterface, id: Int ->
                createLevelUI(false)
            }
            .setCancelable(false)
        return builder.create()
    }

    override fun setMultiselectionMode(multi: Boolean) {
        previous.isEnabled = !PlayScene.shared.history.empty
        if (multi) {
            globalMathView.multiselectionMode = true
            globalMathView.recolorCurrentAtom(ThemeController.shared.color(ColorName.MULTISELECTION_COLOR))
        } else {
            clearRules()
            globalMathView.clearExpression()
            globalMathView.multiselectionMode = false
        }
    }
}
