package mathhelper.games.matify.common

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.renderscript.Allocation
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
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

enum class BackgroundMode {
    SHADOW, BLUR, NONE
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

        fun blur(blurView: BlurView, activity: AppCompatActivity) {
            blurView.setBlurEnabled(true)
            blurView.visibility = View.VISIBLE
            val radius = 3f
            val decorView = activity.window.decorView
            val rootView = decorView.findViewById(android.R.id.content) as ViewGroup
            val windowBackground: Drawable = decorView.background
            blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(RenderScriptBlur(activity))
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true)
        }

        fun unblur(blurView: BlurView) {
            blurView.setBlurEnabled(false)
            blurView.visibility = View.GONE
        }

        fun showDialog(
            dialog: AlertDialog,
            backMode: BackgroundMode = BackgroundMode.SHADOW,
            bottomGravity: Boolean = true,
            blurView: BlurView? = null,
            activity: AppCompatActivity? = null
        ) {
            if (backMode == BackgroundMode.BLUR) {
                blur(blurView!!, activity!!)
                dialog.setOnDismissListener { unblur(blurView) }
            }
            if (bottomGravity) {
                dialog.window!!.setGravity(Gravity.BOTTOM)
            }
            dialog.window!!.attributes.verticalMargin = 0.05f
            dialog.show()
            dialog.window!!.setBackgroundDrawableResource(R.drawable.alert_shape)
            dialog.window!!.findViewById<TextView>(android.R.id.message)?.typeface = Typeface.MONOSPACE
            if (dialog.listView != null) {
                dialog.listView.divider = ColorDrawable(ThemeController.shared.getColorByTheme(ThemeName.DARK, ColorName.PRIMARY_COLOR))
                dialog.listView.dividerHeight = 1
            }
            if (backMode == BackgroundMode.NONE) {
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

        fun vibrateLight(context: Context) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                vibrator.vibrate(50)
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

        fun toggleColor(v: TextView, color1: Int, color2: Int) {
            when (v.currentTextColor) {
                color1 -> v.setTextColor(color2)
                color2 -> v.setTextColor(color1)
                else -> return
            }
        }

        fun toggleVisibility(v: View) {
            v.visibility = when (v.visibility) {
                View.VISIBLE -> View.GONE
                View.GONE -> View.VISIBLE
                else -> View.GONE
            }
        }
    }
}