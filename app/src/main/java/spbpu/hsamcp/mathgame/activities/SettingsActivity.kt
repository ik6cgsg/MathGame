package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import spbpu.hsamcp.mathgame.*
import spbpu.hsamcp.mathgame.common.AndroidUtil
import spbpu.hsamcp.mathgame.common.AuthInfo
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.common.Storage
import spbpu.hsamcp.mathgame.statistics.Statistics

class SettingsActivity: AppCompatActivity() {
    private val TAG = "SettingsActivity"
    private lateinit var reportProblem: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var reportDialog: AlertDialog
    private lateinit var resetDialog: AlertDialog
    private lateinit var greetings: TextView
    private lateinit var reset: TextView
    private lateinit var editAccount: TextView
    private lateinit var changePassword: TextView

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
        if (Build.VERSION.SDK_INT < 24) {
            val settings = findViewById<TextView>(R.id.settings)
            settings.text = "\uD83D\uDD27 SETTINGS \uD83D\uDD27"
        }
        val versionView = findViewById<TextView>(R.id.version)
        versionView.text = versionView.text.toString() + BuildConfig.VERSION_NAME
        greetings = findViewById(R.id.greetings)
    }

    override fun onResume() {
        super.onResume()
        greetings.text = "\uD83D\uDC4B Hi, ${Storage.shared.login(this)}! \uD83D\uDC4B"
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
            rating < 2.5 -> "Tell us why it's so bad:"
            rating > 4.5 -> "Tell us why it's so perfect:"
            else -> "Tell us anything you're worried about:"
        }
        val commentView = view.findViewById<EditText>(R.id.comment)
        builder
            .setView(view)
            .setTitle("Thanx for rating!")
            .setMessage(msg)
            .setPositiveButton("Send") { dialog: DialogInterface, id: Int ->
                val mark = ratingBarDialog.rating
                val comment = commentView.text.toString()
                Statistics.logMark(this, mark, comment)
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, id: Int -> }
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
            .setTitle("Report bug \uD83D\uDC1B")
            .setMessage("Please, tell us about your problem!")
            .setPositiveButton("Send") { dialog: DialogInterface, id: Int ->
                val comment = commentView.text.toString()
                Statistics.logProblem(this, comment)
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, id: Int -> }
            .setCancelable(true)
        return builder.create()
    }

    private fun createResetAlert(): AlertDialog {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("ARE YOU SURE?")
            .setMessage("This action will reset all your achievements")
            .setPositiveButton("Yes \uD83D\uDE22") { dialog: DialogInterface, id: Int ->
                GlobalScene.shared.resetAll()
                finish()
            }
            .setNegativeButton("Cancel ☺") { dialog: DialogInterface, id: Int ->
            }
            .setCancelable(true)
        return builder.create()
    }
}