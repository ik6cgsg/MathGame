package spbpu.hsamcp.mathgame.common

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.*
import android.widget.LinearLayout
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

        fun setOnTouchUpInsideWithCancel(view: View, func: (v: View?) -> Unit) {
            view.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundColor(Constants.lightGrey)
                    }
                    MotionEvent.ACTION_UP -> {
                        v.setBackgroundColor(Color.TRANSPARENT)
                        if (touchUpInsideView(v, event)) {
                            func(v)
                        }
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        v.setBackgroundColor(Color.TRANSPARENT)
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
        
        fun createButtonView(context: Context): TextView {
            val view = TextView(context)
            view.typeface = Typeface.MONOSPACE
            view.textSize = Constants.buttonDefaultSize
            view.textAlignment = View.TEXT_ALIGNMENT_CENTER
            view.setLineSpacing(0f, Constants.levelLineSpacing)
            view.setPadding(
                Constants.defaultPadding, Constants.defaultPadding * 2,
                Constants.defaultPadding, Constants.defaultPadding * 2)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(0, Constants.defaultPadding, 0, Constants.defaultPadding)
            view.layoutParams = layoutParams
            view.setTextColor(Constants.textColor)
            return view
        }
    }
}