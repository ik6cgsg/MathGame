package spbpu.hsamcp.mathgame.common

import android.graphics.Color
import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import spbpu.hsamcp.mathgame.LevelScene
import spbpu.hsamcp.mathgame.PlayScene
import spbpu.hsamcp.mathgame.R
import java.util.*

class MessageTimer: CountDownTimer(PlayScene.messageTime, PlayScene.messageTime) {
    override fun onTick(m: Long) {}
    override fun onFinish() {
        PlayScene.shared.playActivity?.messageView?.visibility = View.GONE
    }
}

class MathDownTimer(time: Long, interval: Long):
    CountDownTimer(time * 1000, interval * 1000) {
    private val TAG = "MathDownTimer"
    private val panicTime = 10

    override fun onTick(millisUntilFinished: Long) {
        Log.d(TAG, "onTick")
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
        PlayScene.shared.playActivity!!.timerView.text = text
    }

    override fun onFinish() {
        Log.d(TAG, "onFinish")
        val activity = PlayScene.shared.playActivity!!
        activity.timerView.text = activity.getString(R.string.time_out)
        PlayScene.shared.onLoose()
    }
}

class MathUpTimer(val interval: Long) {
    private val TAG = "MathUpTimer"
    private lateinit var timer: Timer

    fun start() {
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "run")
                PlayScene.shared.currentTime++
                val start = "⏰ "
                val sec = "${PlayScene.shared.currentTime % 60}".padStart(2, '0')
                val text = SpannableString(start + PlayScene.shared.currentTime / 60 + ":" + sec)
                val steps = if (PlayScene.shared.stepsCount < LevelScene.shared.currentLevel!!.stepsNum) {
                    LevelScene.shared.currentLevel!!.stepsNum.toFloat()
                } else {
                    PlayScene.shared.stepsCount
                }
                val award = LevelScene.shared.currentLevel!!.getAward(PlayScene.shared.currentTime, steps)
                text.setSpan(ForegroundColorSpan(award.color), start.length,
                    text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                val activity = PlayScene.shared.playActivity!!
                activity.runOnUiThread {
                    activity.timerView.text = text
                }
            }
        }, 0, interval * 1000)
    }

    fun cancel() {
        timer.cancel()
    }
}