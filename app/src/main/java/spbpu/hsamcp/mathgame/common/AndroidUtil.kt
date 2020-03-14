package spbpu.hsamcp.mathgame.common

import android.app.AlertDialog
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.TextView
import spbpu.hsamcp.mathgame.R

class AndroidUtil {
    companion object {
        fun touchUpInsideView(view: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP &&
                view.left + event.x >= view.left && view.left + event.x <= view.right &&
                view.top + event.y >= view.top && view.top + event.y <= view.bottom) {
                return true
            }
            return false
        }

        fun showDialog(dialog: AlertDialog) {
            dialog.show()
            dialog.window!!.setBackgroundDrawableResource(R.color.gray)
            dialog.window!!.findViewById<TextView>(android.R.id.message).typeface = Typeface.MONOSPACE
        }
    }
}