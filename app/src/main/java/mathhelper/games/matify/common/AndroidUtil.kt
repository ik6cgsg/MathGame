package mathhelper.games.matify.common

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import mathhelper.games.matify.R
import kotlin.math.pow

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

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

        @SuppressLint("ClickableViewAccessibility")
        fun setOnTouchUpInside(context: Context, view: View, func: (v: View?) -> Unit) {
            view.setOnTouchListener { v, event ->
                val tv = v as TextView
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val themeName = Storage.shared.theme(context)
                        tv.setTextColor(
                            ThemeController.shared.getColorByTheme(themeName, ColorName.PRIMARY_COLOR)
                        )
                    }
                    MotionEvent.ACTION_UP -> {
                        val themeName = Storage.shared.theme(context)
                        tv.setTextColor(
                            ThemeController.shared.getColorByTheme(themeName, ColorName.TEXT_COLOR)
                        )
                        if (touchUpInsideView(v, event)) {
                            func(v)
                        }
                    }
                }
                true
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setOnTouchUpInsideWithCancel(context: Context, view: View, func: (v: View?) -> Unit) {
            view.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val themeName = Storage.shared.theme(context)
                        v.setBackgroundColor(
                            ThemeController.shared.getColorByTheme(themeName, ColorName.ON_TOUCH_BACKGROUND_COLOR)
                        )
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
            val view = Button(context)
            view.typeface = Typeface.MONOSPACE
            view.textSize = Constants.buttonDefaultSize
            view.textAlignment = View.TEXT_ALIGNMENT_CENTER
            view.setLineSpacing(0f, Constants.levelLineSpacing)
            /*view.setPadding(
                Constants.defaultPadding, Constants.defaultPadding * 2,
                Constants.defaultPadding, Constants.defaultPadding * 2)*/
            view.isAllCaps = false
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(0, Constants.defaultPadding, 0, Constants.defaultPadding)
            view.layoutParams = layoutParams

            val themeName = Storage.shared.theme(context)
            view.setTextColor(ThemeController.shared.getColorByTheme(themeName, ColorName.TEXT_COLOR))
            return view
        }

        fun vibrate(context: Context) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else {
                vibrator.vibrate(200)
            }
        }

        @ColorInt fun darkenColor(@ColorInt color: Int, grade: Int): Int {
            return if (grade == 0) color else Color.HSVToColor(FloatArray(3).apply {
                Color.colorToHSV(color, this)
                this[2] *= (0.6f).pow(grade)
            })
        }

        @ColorInt fun lighterColor(@ColorInt color: Int, @ColorInt default: Int): Int? {
            val res = FloatArray(3).apply {
                Color.colorToHSV(color, this)
                this[2] /= (0.6f)
            }
            val def = FloatArray(3)
            Color.colorToHSV(default, def)
            return if (def[2] >= res[2]) Color.HSVToColor(res) else null
        }
    }
}