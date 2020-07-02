package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import mathhelper.games.matify.*
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.statistics.Statistics
import java.util.*

class SettingsActivity: AppCompatActivity() {
    private val TAG = "SettingsActivity"
    private lateinit var reportProblem: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var reportDialog: AlertDialog
    private lateinit var resetDialog: AlertDialog
    private lateinit var changeLanguageDialog: AlertDialog
    private lateinit var greetings: TextView
    private lateinit var reset: TextView
    private lateinit var editAccount: TextView
    private lateinit var changePassword: TextView
    private lateinit var changeLanguage: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val backView = findViewById<TextView>(R.id.back)
        AndroidUtil.setOnTouchUpInside(backView, ::back)
        ratingBar = findViewById(R.id.rating)
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            val ratingDialog = createRatingDialog(rating)
            AndroidUtil.showDialog(ratingDialog)
        }
        ratingBar.setOnClickListener { v: View ->
            val ratingDialog = createRatingDialog(ratingBar.rating)
            AndroidUtil.showDialog(ratingDialog)
        }
        editAccount = findViewById(R.id.edit_account)
        AndroidUtil.setOnTouchUpInsideWithCancel(editAccount) {
            finish()
            startActivity(Intent(this, AccountActivity::class.java))
        }
        reportDialog = createReportDialog()
        reportProblem = findViewById(R.id.report)
        AndroidUtil.setOnTouchUpInsideWithCancel(reportProblem) {
            AndroidUtil.showDialog(reportDialog)
        }
        resetDialog = createResetAlert()
        reset = findViewById(R.id.reset)
        AndroidUtil.setOnTouchUpInsideWithCancel(reset) {
            AndroidUtil.showDialog(resetDialog)
        }
        changePassword = findViewById(R.id.change_password)
        AndroidUtil.setOnTouchUpInsideWithCancel(changePassword) {
            startActivity(Intent(this, PasswordActivity::class.java))
        }
        changeLanguageDialog = createChangeLanguageAlert()
        changeLanguage = findViewById(R.id.change_language)
        AndroidUtil.setOnTouchUpInsideWithCancel(changeLanguage) {
            AndroidUtil.showDialog(changeLanguageDialog)
        }
        val tutorial = findViewById<TextView>(R.id.tutorial)
        AndroidUtil.setOnTouchUpInsideWithCancel(tutorial) {
            TutorialScene.shared.start(this)
        }
        if (Build.VERSION.SDK_INT < 24) {
            val settings = findViewById<TextView>(R.id.settings)
            settings.text = resources.getString(R.string.settings)
        }
        val versionView = findViewById<TextView>(R.id.version)
        versionView.text = versionView.text.toString() + BuildConfig.VERSION_NAME
        greetings = findViewById(R.id.greetings)
    }

    override fun onResume() {
        super.onResume()
        greetings.text = "\uD83D\uDC4B ${resources.getString(R.string.hi)}, ${Storage.shared.login(this)}! \uD83D\uDC4B"
        when (GlobalScene.shared.authStatus) {
            AuthStatus.MATH_HELPER, AuthStatus.GUEST -> changePassword.visibility = View.VISIBLE
            else -> changePassword.visibility = View.GONE
        }
        //statisticSwitch.isChecked = prefs.getBoolean(AuthInfo.STATISTICS.str, false)
    }

    fun back(v: View?) {
        finish()
    }

    private fun createRatingDialog(rating: Float): AlertDialog {
        Log.d(TAG, "createRatingDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        val view = layoutInflater.inflate(R.layout.dialog_rating, null)
        val ratingBarDialog = view.findViewById<RatingBar>(R.id.rating_dialog)
        ratingBarDialog.rating = rating
        val msg = when {
            rating < 2.5 -> R.string.tell_us_why_its_so_bad
            rating > 4.5 -> R.string.tell_us_why_its_so_perfect
            else -> R.string.tell_us_anything_you_are_worried_about
        }
        val commentView = view.findViewById<EditText>(R.id.comment)
        builder
            .setView(view)
            .setTitle(R.string.thanks_for_rating)
            .setMessage(msg)
            .setPositiveButton(R.string.send) { dialog: DialogInterface, id: Int ->
                val mark = ratingBarDialog.rating
                val comment = commentView.text.toString()
                Statistics.logMark(this, mark, comment)
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int -> }
            .setCancelable(true)
        return builder.create()
    }

    private fun createReportDialog(): AlertDialog {
        Log.d(TAG, "createReportDialog")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        val view = layoutInflater.inflate(R.layout.dialog_reporting, null)
        val commentView = view.findViewById<EditText>(R.id.problem)
        builder
            .setView(view)
            .setTitle(R.string.report_bug)
            .setMessage(R.string.please_tell_us_about_your_problem)
            .setPositiveButton(R.string.send) { dialog: DialogInterface, id: Int ->
                val comment = commentView.text.toString()
                Statistics.logProblem(this, comment)
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int -> }
            .setCancelable(true)
        return builder.create()
    }

    private fun createResetAlert(): AlertDialog {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle(R.string.are_you_sure)
            .setMessage(R.string.this_action_will_reset_all_your_achievements)
            .setPositiveButton(R.string.yes) { dialog: DialogInterface, id: Int ->
                GlobalScene.shared.resetAll()
                finish()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int ->
            }
            .setCancelable(true)
        return builder.create()
    }

    private fun createChangeLanguageAlert(): AlertDialog {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)

        val config = Configuration(resources.configuration)
        val languageToChoose = if (config.locale.language == "ru") {
            "en"
        } else {
            "ru"
        }

        builder
            .setTitle(R.string.change_language)
            .setMessage("${resources.getString(R.string.change_language_to)} '${languageToChoose.toUpperCase(config.locale)}'?")
            .setPositiveButton(R.string.yes) { dialog: DialogInterface, id: Int ->
                config.locale = Locale(languageToChoose); //locale
                resources.updateConfiguration(config, resources.displayMetrics)
                finish()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int ->
            }
            .setCancelable(true)
        return builder.create()
    }
}