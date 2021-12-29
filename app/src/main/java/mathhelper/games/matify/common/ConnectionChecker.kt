package mathhelper.games.matify.common

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import eightbitlab.com.blurview.BlurView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mathhelper.games.matify.R
import mathhelper.games.matify.common.ActivityType.*
import java.lang.ref.WeakReference

enum class ConnectionChangeType {
    SAME, ESTABLISHED, LOST
}

enum class ActivityType {
    AUTH, GAMES, LEVELS, PLAY, SETTINGS, SEARCH
}

interface ConnectionListener {
    fun onConnectionChange(type: ConnectionChangeType)
    fun connectionBannerClicked(v: View?)
    fun connectionButtonClick(v: View)
}

class ConnectionChecker {
    companion object {
        val shared = ConnectionChecker()
    }

    var isConnected: Boolean = true
        private set
    var context: WeakReference<Context>? = null
        set(value) {
            field = value
            if (value != null)  {
                startCheckCycle()
            } else {
                job?.cancel()
            }
        }
    private var job: Job? = null
    private var listeners: ArrayList<ConnectionListener> = arrayListOf()
    private var currentAlert: AlertDialog? = null

    fun connectionBannerClicked(activity: AppCompatActivity, blurView: BlurView, activityType: ActivityType) {
        val builder = AlertDialog.Builder(
            activity, ThemeController.shared.alertDialogTheme
        )
        val v = activity.layoutInflater.inflate(R.layout.connection_alert, null)
        val textView = v.findViewById<TextView>(R.id.alert_text)
        textView.text = when (activityType) {
            AUTH -> activity.getString(R.string.connection_alert_text_auth)
            GAMES -> activity.getString(R.string.connection_alert_text_games)
            LEVELS -> activity.getString(R.string.connection_alert_text_levels)
            PLAY -> activity.getString(R.string.connection_alert_text_play)
            SETTINGS -> activity.getString(R.string.connection_alert_text)
            SEARCH -> activity.getString(R.string.connection_alert_text)
        }
        builder.setView(v)
            .setCancelable(true)
            .setOnCancelListener {
                currentAlert = null
            }
        currentAlert = builder.create()
        AndroidUtil.showDialog(currentAlert!!, backMode = BackgroundMode.BLUR, blurView = blurView, activity = activity, setBackground = false)
    }

    fun connectionButtonClick(activity: AppCompatActivity, v: View) {
        when (v.tag) {
            "connect" -> {
                activity.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            "cancel" -> {
                currentAlert?.dismiss()
                currentAlert = null
            }
            else -> {}
        }
    }

    fun subscribe(listener: ConnectionListener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener)
        }
        listeners.add(listener)
        // listener initialization
        val type = if (isConnected) ConnectionChangeType.ESTABLISHED else ConnectionChangeType.LOST
        listener.onConnectionChange(type)
    }

    fun unsubscribe(listener: ConnectionListener) {
        listeners.remove(listener)
    }

    private fun notify(type: ConnectionChangeType) {
        if (type == ConnectionChangeType.ESTABLISHED || type == ConnectionChangeType.LOST) {
            for (listener in listeners) {
                listener.onConnectionChange(type)
            }
            if (type == ConnectionChangeType.ESTABLISHED && currentAlert != null) {
                currentAlert?.dismiss()
            }
        }
    }

    private fun startCheckCycle() {
        job = GlobalScope.launch {
            while (true) {
                val wasConnected = isConnected
                isConnected = isNetworkAvailable()
                val type = when {
                    wasConnected && !isConnected -> ConnectionChangeType.LOST
                    !wasConnected && isConnected -> ConnectionChangeType.ESTABLISHED
                    else -> ConnectionChangeType.SAME
                }
                notify(type)
                delay(1000)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val context = context?.get() ?: return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

}