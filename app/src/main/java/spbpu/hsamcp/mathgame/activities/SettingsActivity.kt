package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_play.view.*
import org.w3c.dom.Text
import spbpu.hsamcp.mathgame.BuildConfig
import spbpu.hsamcp.mathgame.MathScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AndroidUtil
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.statistics.AuthInfo
import spbpu.hsamcp.mathgame.statistics.MathGameLog
import spbpu.hsamcp.mathgame.statistics.Statistics

class SettingsActivity: AppCompatActivity() {
    private val TAG = "SettingsActivity"
    private lateinit var statisticSwitch: Switch
    private lateinit var reportProblem: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var reportDialog: AlertDialog

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val backView = findViewById<TextView>(R.id.back)
        AndroidUtil.setOnTouchUpInside(backView, ::back)
        statisticSwitch = findViewById(R.id.statistics)
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        statisticSwitch.isChecked = prefs.getBoolean(AuthInfo.STATISTICS.str, false)
        ratingBar = findViewById(R.id.rating)
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            val ratingDialog = createRatingDialog(rating)
            AndroidUtil.showDialog(ratingDialog)
        }
        ratingBar.setOnClickListener { v: View ->
            val ratingDialog = createRatingDialog(ratingBar.rating)
            AndroidUtil.showDialog(ratingDialog)
        }
        reportDialog = createReportDialog()
        reportProblem = findViewById(R.id.report)
        reportProblem.setOnTouchListener { v, event ->
            super.onTouchEvent(event)
            when {
                event.action == MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundColor(Constants.lightGrey)
                }
                event.action == MotionEvent.ACTION_UP -> {
                    v.setBackgroundColor(Color.TRANSPARENT)
                    if (AndroidUtil.touchUpInsideView(v, event)) {
                        AndroidUtil.showDialog(reportDialog)
                    }
                }
                event.action == MotionEvent.ACTION_CANCEL -> {
                    v.setBackgroundColor(Color.TRANSPARENT)
                }
            }
            true
        }
        val versionView = findViewById<TextView>(R.id.version)
        versionView.text = versionView.text.toString() + BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        statisticSwitch.isChecked = prefs.getBoolean(AuthInfo.STATISTICS.str, false)
    }

    fun back(v: View?) {
        finish()
    }

    fun switchStatistics(v: View?) {
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.putBoolean(AuthInfo.STATISTICS.str, statisticSwitch.isChecked)
        prefEdit.commit()
    }

    fun reportProblem(v: View?) {
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.putBoolean(AuthInfo.STATISTICS.str, statisticSwitch.isChecked)
        prefEdit.commit()
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
}