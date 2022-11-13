package mathhelper.games.matify.activities

import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import eightbitlab.com.blurview.BlurView
import mathhelper.games.matify.InstrumentScene
import mathhelper.games.matify.InstrumentSceneListener
import mathhelper.games.matify.R
import mathhelper.games.matify.common.*

abstract class AbstractPlayableActivity : AppCompatActivity(), InstrumentSceneListener {
    protected abstract val TAG: String
    // layout elements
    lateinit var rulesLinearLayout: LinearLayout
    lateinit var endExpressionMathView: SimpleMathView
    lateinit var endExpressionViewLabel: TextView
    lateinit var rulesScrollView: ScrollView
    override lateinit var globalMathView: GlobalMathView
    lateinit var rulesMsg: TextView
    lateinit var messageView: TextView

    lateinit var mainView: ConstraintLayout
    lateinit var mainViewAnim: TransitionDrawable
    lateinit var bottomSheet: LinearLayout
    lateinit var timerView: TextView
    lateinit var blurView: BlurView

    var instrumentProcessing: Boolean = false
    protected var needClear: Boolean = false

    override val ctx: Context get() = this

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Logger.d(TAG, "onTouchEvent")
        if (globalMathView.onTouchEvent(event)) {
            needClear = false
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!globalMathView.multiselectionMode)
                    needClear = true
            }
            MotionEvent.ACTION_UP -> {
                if (needClear) {
                    try {
                        globalMathView.clearExpression()
                        clearRules()
                    } catch (e: Exception) {
                        Logger.e(TAG, "Error while clearing rules on touch: ${e.message}")
                        Toast.makeText(this, R.string.misclick_happened_please_retry, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        return true
    }

    open fun setViews() {
        globalMathView = findViewById(R.id.global_expression)
        endExpressionMathView = findViewById(R.id.end_expression_math_view)
        endExpressionViewLabel = findViewById(R.id.end_expression_label)
        endExpressionViewLabel.visibility = View.GONE
        endExpressionMathView.visibility = View.GONE
        bottomSheet = findViewById(R.id.bottom_sheet)
        messageView = findViewById(R.id.message_view)
        timerView = findViewById(R.id.timer_view)
        rulesLinearLayout = bottomSheet.findViewById(R.id.rules_linear_layout)
        rulesScrollView = bottomSheet.findViewById(R.id.rules_scroll_view)
        rulesMsg = bottomSheet.findViewById(R.id.rules_msg)
        InstrumentScene.shared.init(bottomSheet, this)
        blurView = findViewById(R.id.blurView)
    }

    fun clearRules() {
        rulesScrollView.visibility = View.GONE
        rulesMsg.text = getString(R.string.no_rules_msg)
        if (!instrumentProcessing) {
            collapseBottomSheet()
        }
    }

    fun instrumentClick(v: View) {
        InstrumentScene.shared.clickInstrument(v.tag.toString())
    }

    override fun showMessage(msg: String) {
        messageView.text = msg
        messageView.visibility = View.VISIBLE
    }

    abstract fun showEndExpression(v: View?)

    override fun halfExpandBottomSheet() {
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun collapseBottomSheet() {
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun setMultiselectionMode(multi: Boolean) {
        if (multi) {
            globalMathView.multiselectionMode = true
            globalMathView.recolorCurrentAtom(ThemeController.shared.color(ColorName.MULTISELECTION_COLOR))
        } else {
            clearRules()
            globalMathView.clearExpression()
            globalMathView.multiselectionMode = false
        }
    }

    override fun startInstrumentProcessing(setMSMode: Boolean) {
        instrumentProcessing = true
        clearRules()
        showMessage(getString(R.string.inst_enter))
        setMultiselectionMode(setMSMode)
        rulesMsg.text = getString(R.string.inst_rules_msg)
        AndroidUtil.vibrate(this)
        mainViewAnim.startTransition(300)
    }

    override fun endInstrumentProcessing(collapse: Boolean) {
        instrumentProcessing = false
        setMultiselectionMode(false)
        globalMathView.clearExpression()
        AndroidUtil.vibrate(this)
        mainViewAnim.reverseTransition(300)
        if (collapse) {
            collapseBottomSheet()
            clearRules()
        }
    }
}