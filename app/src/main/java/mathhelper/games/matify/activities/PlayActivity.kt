package mathhelper.games.matify.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BulletSpan
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import mathhelper.games.matify.LevelScene
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.R
import mathhelper.games.matify.common.*
import mathhelper.games.matify.level.StateType
import kotlin.math.max
import kotlin.math.min


class PlayActivity: AppCompatActivity() {
    private val TAG = "PlayActivity"
    private var scale = 1.0f
    private var needClear = false
    private var loading = false
    private var scaleListener = MathScaleListener()
    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var looseDialog: AlertDialog
    private lateinit var winDialog: AlertDialog
    private lateinit var backDialog: AlertDialog
    private lateinit var continueDialog: AlertDialog
    private lateinit var progress: ProgressBar

    lateinit var mainView: ConstraintLayout
    lateinit var globalMathView: GlobalMathView
    lateinit var endExpressionView: TextView
    lateinit var endExpressionViewLabel: TextView
    lateinit var messageView: TextView
    lateinit var rulesLinearLayout: LinearLayout
    lateinit var rulesScrollView: ScrollView
    lateinit var noRules: TextView
    lateinit var timerView: TextView
    lateinit var blurView: BlurView

    private lateinit var startStopMultiselectionMode: TextView
    private lateinit var restart: TextView
    private lateinit var back: TextView
    private lateinit var info: Button
    private lateinit var previous: TextView

    private val messageTimer = MessageTimer()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent")
        scaleDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!globalMathView.multiselectionMode)
                    needClear = true
            }
            MotionEvent.ACTION_UP -> {
                if (needClear) {
                    try {
                        globalMathView.clearExpression()
                        PlayScene.shared.clearRules()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error while clearing rules on touch: ${e.message}")
                        Toast.makeText(this, R.string.misclick_happened_please_retry, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        return true
    }

    private fun setViews() {
        mainView = findViewById(R.id.activity_play)
        globalMathView = findViewById(R.id.global_expression)
        endExpressionView = findViewById(R.id.end_expression_view)
        endExpressionViewLabel = findViewById(R.id.end_expression_label)
        messageView = findViewById(R.id.message_view)
        rulesLinearLayout = findViewById(R.id.rules_linear_layout)
        rulesScrollView = findViewById(R.id.rules_scroll_view)
        noRules = findViewById(R.id.no_rules)
        timerView = findViewById(R.id.timer_view)
        back = findViewById(R.id.back)
        restart = findViewById(R.id.restart)
        previous = findViewById(R.id.previous)
        info = findViewById(R.id.info)
        startStopMultiselectionMode = findViewById(R.id.start_stop_multiselection_mode)
        progress = findViewById(R.id.progress)
        blurView = findViewById(R.id.blurView)
    }

    private fun setLongClick() {
        startStopMultiselectionMode.setOnLongClickListener {
            showMessage(
                getString(R.string.end_multiselect_info),
                globalMathView.multiselectionMode,
                getString(R.string.start_multiselect_info)
            )
            true
        }
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
                startStopMultiselectionMode(it)
            }
            AndroidUtil.vibrate(this)
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt(this))
        setContentView(R.layout.activity_play)
        scaleDetector = ScaleGestureDetector(this, scaleListener)
        setViews()
        looseDialog = createLooseDialog()
        winDialog = createWinDialog()
        backDialog = createBackDialog()
        continueDialog = createContinueDialog()
        PlayScene.shared.playActivity = this
        Handler().postDelayed({
            startCreatingLevelUI()
        }, 100)
        setLongClick()
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
        endExpressionView.text = ""
        progress.visibility = View.VISIBLE
        try {
            PlayScene.shared.loadLevel(this, continueGame, resources.configuration.locale.language)
        } catch (e: Exception) {
            Log.e(TAG, "Error while level loading")
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
        }
        progress.visibility = View.GONE
        loading = false
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
            scale = 1f
            PlayScene.shared.restart(this, resources.configuration.locale.language)
        }
    }

    fun startStopMultiselectionMode(v: View?) {
        val startStopView: TextView = findViewById(R.id.start_stop_multiselection_mode)
        val anim = mainView.background as TransitionDrawable
        globalMathView.multiselectionMode = !globalMathView.multiselectionMode
        if (!globalMathView.multiselectionMode) {
            startStopView.text = getText(R.string.start_multiselect)
            startStopView.setTextColor(ThemeController.shared.getColor(this, ColorName.PRIMARY_COLOR))
            globalMathView.clearExpression()
            PlayScene.shared.clearRules()
            AndroidUtil.vibrate(this)
            anim.reverseTransition(300)
        }
        else {
            startStopView.text = getText(R.string.end_multiselect)
            startStopView.setTextColor(Color.RED)
            showMessage(getString(R.string.msg_on_start_multiselect))
            AndroidUtil.vibrate(this)
            anim.startTransition(300)
            globalMathView.recolorCurrentAtom(ThemeController.shared.getColor(this, ColorName.MULTISELECTION_COLOR))
        }
    }

    fun info(v: View?) {
        PlayScene.shared.info(resources.configuration.locale.language)
    }

    fun back(v: View?) {
        if (!loading) {
            if (LevelScene.shared.currentLevel!!.endless && PlayScene.shared.stepsCount > 0) {
                AndroidUtil.showDialog(backDialog)
            } else {
                returnToMenu(false)
            }
        }
    }

    private fun showMessage(msg: String, flag: Boolean = true, ifFlagFalseMsg: String? = null) {
        if (flag)
            messageView.text = msg
        else
            messageView.text = ifFlagFalseMsg
        messageView.visibility = View.VISIBLE
        messageTimer.cancel()
        messageTimer.start()
    }

    private fun returnToMenu(save: Boolean) {
        PlayScene.shared.menu(this, save)
        finish()
    }

    fun showEndExpression(v: View?) {
        if (endExpressionView.visibility == View.GONE) {
            endExpressionViewLabel.text = getString(R.string.end_expression_opened)
            endExpressionView.visibility = View.VISIBLE
        } else {
            endExpressionViewLabel.text = getString(R.string.end_expression_closed)
            endExpressionView.visibility = View.GONE
        }
    }

    fun endExpressionHide(): Boolean {
        return endExpressionView.visibility != View.VISIBLE
    }

    fun onWin(stepsCount: Double, currentTime: Long, state: StateType) {
        Log.d(TAG, "onWin")
        val msgTitle = resources.getString(R.string.you_finished_level_with)
        val steps = "\n\t${resources.getString(R.string.steps)}: ${stepsCount.toInt()}"
        val sec = "${currentTime % 60}".padStart(2, '0')
        val time = "\n\t${resources.getString(R.string.time)}: ${currentTime / 60}:$sec"
        val spannable = SpannableString("$msgTitle$steps$time\n")
        val spanColor = ThemeController.shared.getColor(this, ColorName.PRIMARY_COLOR)
        spannable.setSpan(BulletSpan(5, spanColor), msgTitle.length + 1,
            msgTitle.length + steps.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(BulletSpan(5, spanColor),
            msgTitle.length + steps.length + 1, msgTitle.length + steps.length + time.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        winDialog.setMessage(spannable)
        AndroidUtil.showDialog(winDialog, backMode = BackgroundMode.BLUR, blurView = blurView, activity = this)
    }

    fun onLoose() {
        AndroidUtil.showDialog(looseDialog, backMode = BackgroundMode.BLUR, blurView = blurView, activity = this)
    }

    private fun createWinDialog(): AlertDialog {
        Log.d(TAG, "createWinDialog")
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.getAlertDialogByTheme(Storage.shared.theme(this))
        )
        builder
            .setTitle(R.string.congratulations)
            .setMessage("")
            .setPositiveButton(R.string.next) { dialog: DialogInterface, id: Int -> }
            .setNeutralButton(R.string.menu) { dialog: DialogInterface, id: Int ->
                PlayScene.shared.menu(this,false)
                finish()
            }
            .setNegativeButton(R.string.restart_label) { dialog: DialogInterface, id: Int ->
                scale = 1f
                PlayScene.shared.restart(this, resources.configuration.locale.language)
            }
            .setCancelable(false)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                scale = 1f
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
        Log.d(TAG, "createLooseDialog")
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.getAlertDialogByTheme(Storage.shared.theme(this))
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

    private fun createBackDialog(): AlertDialog {
        Log.d(TAG, "createBackDialog")
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.getAlertDialogByTheme(Storage.shared.theme(this))
        )
        builder
            .setTitle(R.string.attention)
            .setMessage(R.string.save_your_current_state)
            .setPositiveButton(R.string.yes) { dialog: DialogInterface, id: Int ->
                returnToMenu(true)
            }
            .setNegativeButton(R.string.no) { dialog: DialogInterface, id: Int ->
                returnToMenu(false)
            }
            .setNeutralButton(R.string.cancel) { dialog: DialogInterface, id: Int -> }
            .setCancelable(false)
        return builder.create()
    }

    private fun createContinueDialog(): AlertDialog {
        Log.d(TAG, "createContinueDialog")
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.getAlertDialogByTheme(Storage.shared.theme(this))
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

    inner class MathScaleListener: ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            needClear = false
            scale *= detector.scaleFactor
            scale = max(
                Constants.ruleDefaultSize / Constants.centralExpressionDefaultSize,
                min(scale, Constants.centralExpressionMaxSize / Constants.centralExpressionDefaultSize))
            globalMathView.textSize = Constants.centralExpressionDefaultSize * scale
            return true
        }
    }
}
