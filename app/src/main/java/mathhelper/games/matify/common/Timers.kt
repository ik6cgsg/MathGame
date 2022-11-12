package mathhelper.games.matify.common

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import kotlinx.coroutines.Runnable
import mathhelper.games.matify.LevelScene
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.R
import mathhelper.games.matify.activities.PlayActivity
import java.lang.ref.WeakReference
import java.util.*

interface TimerListener {
    var messageView: TextView
    var timerView: TextView

    fun runOnUiThread(code: Runnable)
}

class MessageTimer(listener: TimerListener): CountDownTimer(PlayScene.messageTime, PlayScene.messageTime) {
    private var listenerRef = WeakReference(listener)
    override fun onTick(m: Long) {}
    override fun onFinish() {
        listenerRef.get()?.messageView?.visibility = View.GONE
    }
}

class MathDownTimer(listener: TimerListener, time: Int, interval: Long):
    CountDownTimer(time.toLong() * 1000, interval * 1000) {
    private val TAG = "MathDownTimer"
    private var listenerRef = WeakReference(listener)
    private val panicTime = 10

    override fun onTick(millisUntilFinished: Long) {
        Logger.d(TAG, "onTick")
        PlayScene.shared.currentTime++
        val secs = millisUntilFinished / 1000
        val start = "⏰ "
        val sec = "${secs % 60}".padStart(2, '0')
        val text = SpannableString(start + secs / 60 + ":" + sec)
        if (secs <= panicTime) {
            text.setSpan(
                ForegroundColorSpan(Color.RED), start.length,
                text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            text.setSpan(
                StyleSpan(Typeface.BOLD), start.length,
                text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }
        listenerRef.get()?.timerView?.text = text
    }

    override fun onFinish() {
        Logger.d(TAG, "onFinish")
        val activity = PlayScene.shared.listenerRef.get() as PlayActivity
        activity.timerView.text = activity.getString(R.string.time_out)
        PlayScene.shared.onLose()
    }
}

class MathUpTimer(listener: TimerListener, val interval: Long) {
    private val TAG = "MathUpTimer"
    private var listenerRef = WeakReference(listener)
    private lateinit var timer: Timer

    fun start(context: Context) {
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                Logger.d(TAG, "run")
                PlayScene.shared.currentTime++
                val start = "⏰ "
                val sec = "${PlayScene.shared.currentTime % 60}".padStart(2, '0')
                val text = SpannableString(start + PlayScene.shared.currentTime / 60 + ":" + sec)
                val steps = if (PlayScene.shared.stepsCount < LevelScene.shared.currentLevel!!.currentStepNum) {
                    LevelScene.shared.currentLevel!!.currentStepNum.toDouble()
                } else {
                    PlayScene.shared.stepsCount
                }
                //val award = LevelScene.shared.currentLevel!!.getAward(context, PlayScene.shared.currentTime, steps)
                /*text.setSpan(ForegroundColorSpan(award.color), start.length,
                    text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)*/
                listenerRef.get()?.let {
                    it.runOnUiThread {
                        it.timerView.text = text
                    }
                }
            }
        }, 0, interval * 1000)
    }

    fun cancel() {
        timer.cancel()
    }
}

class RequestTimer(val timeOutSec: Long) {
    private val TAG = "RequestTimer"
    private lateinit var timer: Timer

    fun start() {
        timer = Timer()
        Request.timeout = false
        timer.schedule(object : TimerTask() {
            override fun run() {
                Request.timeout = true
            }
        }, timeOutSec * 1000)
    }

    fun cancel() {
        timer.cancel()
        Request.timeout = false
    }
}