package mathhelper.games.matify.common

import android.util.Log

class Logger {
    companion object {
        fun d(tag: String, message: String) {
            Log.d(Constants.appCode, "[$tag] $message")
        }

        fun d(tag: String, funcName: String, message: String?) {
            Log.d(Constants.appCode, "[$tag] $funcName(): $message")
        }

        fun e(tag: String, message: String) {
            Log.e(Constants.appCode, "[$tag] $message")
        }
    }
}