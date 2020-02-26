package spbpu.hsamcp.mathgame

import android.view.MotionEvent
import android.view.View
import android.view.Window

class AndroidUtil {
    companion object {
        fun makeFullScreen(window: Window) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        fun touchUpInsideView(view: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP &&
                view.left + event.x >= view.left && view.left + event.x <= view.right &&
                view.top + event.y >= view.top && view.top + event.y <= view.bottom) {
                return true
            }
            return false
        }
    }
}