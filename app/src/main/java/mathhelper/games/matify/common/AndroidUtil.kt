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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.constraintlayout.widget.ConstraintLayout
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import mathhelper.games.matify.R
import mathhelper.games.matify.game.Game
import mathhelper.games.matify.level.StateType
import kotlin.math.abs
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

        fun exteriorClickInside(view: View, event: MotionEvent): Boolean {
            val loc = IntArray(2)
            view.getLocationInWindow(loc)
            if (event.action == MotionEvent.ACTION_UP &&
                loc[0] <= event.x && event.x <= loc[0] + view.width * view.scaleX &&
                loc[1] <= event.y && event.y <= loc[1] + view.height * view.scaleY) {
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
                        tv.setTextColor(
                            ThemeController.shared.color(ColorName.PRIMARY_COLOR)
                        )
                    }
                    MotionEvent.ACTION_UP -> {
                        tv.setTextColor(
                            ThemeController.shared.color(ColorName.TEXT_COLOR)
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
            activity: AppCompatActivity? = null,
            setBackground: Boolean = true
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
            if (setBackground) {
                dialog.window!!.setBackgroundDrawableResource(R.drawable.alert_shape)
            } else {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            dialog.window!!.findViewById<TextView>(android.R.id.message)?.typeface = Typeface.MONOSPACE
            if (dialog.listView != null) {
                dialog.listView.divider = ColorDrawable(ThemeController.shared.color(ColorName.PRIMARY_COLOR))
                dialog.listView.dividerHeight = 1
            }
            if (backMode == BackgroundMode.NONE) {
                dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }
        
        fun createButtonView(context: Context): Button {
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
            view.setTextColor(ThemeController.shared.color(ColorName.TEXT_COLOR))
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

        @ColorInt fun darkenColor(@ColorInt color: Int/*, grade: Int*/): Int {
            //return if (grade == 0) color else
            return Color.HSVToColor(FloatArray(3).apply {
                Color.colorToHSV(color, this)
                this[2] *= (0.6f)//.pow(grade)
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

        fun insideParentWithOffset(v: View, difX: Float = 0f, difY: Float = 0f): Boolean {
            val loc = IntArray(2)
            v.getLocationInWindow(loc)
            val p = v.parent as ConstraintLayout
            val sw = v.width * v.scaleX
            val sh = v.height * v.scaleY
            if (loc[0] + difX < p.width - sw / 10f && loc[0] + difX + sw > sw / 10f &&
                loc[1] + difY < p.height - sh / 2f && loc[1] + difY + sh > sh / 2f) {
                return true
            }
            return false
        }

        fun getDrawableByLevelState(context: Context, state: StateType?): Drawable? {
            val d: Drawable? = when (state) {
                StateType.DONE -> context.getDrawable(R.drawable.green_tick)
                StateType.PAUSED -> context.getDrawable(R.drawable.pause)
                else -> null
            }
            d?.setBounds(0, 0, 80, 80)
            return d
        }

        fun setLeftDrawable(view: TextView, drawable: Drawable?) {
            view.setCompoundDrawables(drawable, null, null, null)
        }

        fun setRightDrawable(view: TextView, drawable: Drawable?) {
            view.setCompoundDrawables(null, null, drawable, null)
        }

        fun get3sizedLocale(context: Context?): String {
            return when(context?.resources?.configuration?.locale?.language) {
                "ru" -> "rus"
                "en" -> "eng"
                else -> "rus"
            }
        }

        fun generateGameView(
            context: Context,
            game: Game,
            onClick: (View) -> Unit,
            onLongClick: (View) -> Boolean,
        ): Button {
            val lang = context.resources.configuration.locale.language
            val gameView = createButtonView(context)
            val pin = if (game.isPinned) "📌 " else ""
            gameView.text = "$pin${game.getNameByLanguage(lang)}"
            /*if (game.recommendedByCommunity) {
                val d = getDrawable(R.drawable.tick)
                d!!.setBounds(0, 0, 70, 70)
                AndroidUtil.setRightDrawable(gameView, d)
            }*/
            gameView.setTextColor(ThemeController.shared.color(ColorName.TEXT_COLOR))
            if (game.lastResult != null) {
                gameView.text = "${gameView.text}\n${game.lastResult!!.toString().format(game.tasks.size)}"
            }
            gameView.background = context.getDrawable(R.drawable.button_rect)
            gameView.setTextColor(context.getColorStateList(R.color.text_simple_disableable))
            gameView.setOnClickListener { onClick(it) }
            gameView.isLongClickable = true
            gameView.setOnLongClickListener { onLongClick(it) }
            gameView.isEnabled = ConnectionChecker.shared.isConnected || !game.isPreview
            return gameView
        }
    }
}