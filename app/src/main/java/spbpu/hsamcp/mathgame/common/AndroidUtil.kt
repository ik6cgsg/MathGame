package spbpu.hsamcp.mathgame.common

import android.app.ActionBar
import android.app.AlertDialog
import android.graphics.Typeface
import android.view.*
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

        fun setOnTouchUpInside(view: View, func: (v: View?) -> Unit) {
            view.setOnTouchListener { v, event ->
                val tv = v as TextView
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        tv.setTextColor(Constants.primaryColor)
                    }
                    MotionEvent.ACTION_UP -> {
                        tv.setTextColor(Constants.textColor)
                        if (touchUpInsideView(v, event)) {
                            func(v)
                        }
                    }
                }
                true
            }
        }

        fun showDialog(dialog: AlertDialog, shadowBack: Boolean = true) {
            dialog.window!!.setGravity(Gravity.BOTTOM)
            dialog.window!!.attributes.verticalMargin = 0.05f
            dialog.show()
            dialog.window!!.setBackgroundDrawableResource(R.drawable.alert_shape)
            dialog.window!!.findViewById<TextView>(android.R.id.message).typeface = Typeface.MONOSPACE
            if (!shadowBack) {
                dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }
    }
}