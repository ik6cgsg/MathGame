package mathhelper.games.matify.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BulletSpan
import android.util.Log
import android.view.*
import android.widget.*
import mathhelper.games.matify.level.Award
import mathhelper.games.matify.LevelScene
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.R
import mathhelper.games.matify.common.*
import java.lang.Exception
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

    lateinit var globalMathView: GlobalMathView
    lateinit var endExpressionView: TextView
    lateinit var endExpressionViewLabel: TextView
    lateinit var messageView: TextView
    lateinit var rulesLinearLayout: LinearLayout
    lateinit var rulesScrollView: ScrollView
    lateinit var noRules: TextView
    lateinit var timerView: TextView

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent")
        scaleDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
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
        globalMathView = findViewById(R.id.global_expression)
        endExpressionView = findViewById(R.id.end_expression_view)
        endExpressionViewLabel = findViewById(R.id.end_expression_label)
        messageView = findViewById(R.id.message_view)
        rulesLinearLayout = findViewById(R.id.rules_linear_layout)
        rulesScrollView = findViewById(R.id.rules_scroll_view)
        noRules = findViewById(R.id.no_rules)
        timerView = findViewById(R.id.timer_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt(this))
        setContentView(R.layout.activity_play)
        scaleDetector = ScaleGestureDetector(this, scaleListener)
        setViews()
        progress = findViewById(R.id.progress)
        looseDialog = createLooseDialog()
        winDialog = createWinDialog()
        backDialog = createBackDialog()
        continueDialog = createContinueDialog()
        PlayScene.shared.playActivity = this
        Handler().postDelayed({
            startCreatingLevelUI()
        }, 100)
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
            PlayScene.shared.previousStep()
        }
    }

    fun restart(v: View?) {
        if (!loading) {
            scale = 1f
            PlayScene.shared.restart(this, resources.configuration.locale.language)
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

    fun onWin(stepsCount: Float, currentTime: Long, award: Award) {
        Log.d(TAG, "onWin")
        val msgTitle = resources.getString(R.string.you_finished_level_with)
        val steps = "\n\t${resources.getString(R.string.steps)}: " + if (stepsCount.equals(stepsCount.toInt().toFloat())) {
            "${stepsCount.toInt()}"
        } else {
            "%.1f".format(stepsCount)
        }
        val sec = "${currentTime % 60}".padStart(2, '0')
        val time = "\n\t${resources.getString(R.string.time)}: ${currentTime / 60}:$sec"
        val spannable = SpannableString("$msgTitle$steps$time\n\n${resources.getString(R.string.award)}: $award")

        val themeName = Storage.shared.theme(this)
        val spanColor = ThemeController.shared.getColorByTheme(themeName, ColorName.PRIMARY_COLOR)

        spannable.setSpan(BulletSpan(5, spanColor), msgTitle.length + 1,
            msgTitle.length + steps.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(BulletSpan(5, spanColor),
            msgTitle.length + steps.length + 1, msgTitle.length + steps.length + time.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        winDialog.setMessage(spannable)
        AndroidUtil.showDialog(winDialog)
    }

    fun onLoose() {
        AndroidUtil.showDialog(looseDialog)
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
