package mathhelper.games.matify.common

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Path
import android.graphics.Point
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import mathhelper.games.matify.mathResolver.MathResolverNodeBase
import mathhelper.twf.platformdependent.abs
import java.lang.ref.WeakReference
import kotlin.math.*

class TrackingMathPointer : androidx.appcompat.widget.AppCompatImageView {
    private val TAG = "TrackingMathPointer"
    private lateinit var rotator: ObjectAnimator
    private var nodeRef: WeakReference<MathResolverNodeBase> = WeakReference(null)
    private val extraScale = 3.3f
    private var defaultScale = 1f
    var mode = TRACKING_STATIC

    companion object {
        const val TRACKING_STATIC = 0
        const val TRACKING_NODE = 1
    }

    constructor(context: Context) : super(context) {
        Logger.d(TAG, "constructor from context")
        setDefault(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        Logger.d(TAG, "constructor from attributes")
        setDefault(context)
    }

    private fun setDefault(context: Context) {
        Logger.d(TAG, "setDefault")
        visibility = View.INVISIBLE
        post {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            defaultScale = min(display.width, display.height) * 0.16f / max(width, height)
        }

        rotator = ObjectAnimator.ofFloat(this, "rotation", 360f)
        val path = Path()
        path.moveTo(1.1f, 1.1f)
        path.lineTo(0.9f, 0.9f)
        rotator.repeatCount = ObjectAnimator.INFINITE
        rotator.duration = 2700
        rotator.interpolator = LinearInterpolator()

        rotator.start()
    }

    fun setTrackerToExpression(node: MathResolverNodeBase, globalMathView: GlobalMathView) {
        nodeRef = WeakReference(node)
        mode = TRACKING_NODE
        followNode(globalMathView)
    }

    fun pointToStaticView(v: View, adaptSizeToView: Boolean = false) {
        mode = TRACKING_STATIC
        visibility = View.VISIBLE
        val loc = IntArray(2)
        v.getLocationInWindow(loc)
        val newScaleX: Float
        val newScaleY: Float
        if (adaptSizeToView) {
            val pointerDiameter = min(width * scaleX, height * scaleY)
            val viewDiameter = max(v.width * v.scaleX, v.height * v.scaleY)
            newScaleX = scaleX * viewDiameter / pointerDiameter
            newScaleY = scaleY * viewDiameter / pointerDiameter
        } else {
            newScaleX = defaultScale
            newScaleY = defaultScale
        }
        val vX = loc[0] + v.width / 2f - width / 2f
        val vY = loc[1] + v.height / 2f - height / 2f
        animate()
            .x(vX)
            .y(vY)
            .scaleX(newScaleX)
            .scaleY(newScaleY)
            .setDuration(100)
            .start()
    }

    fun followNode(globalMathView: GlobalMathView) {
        if (mode != TRACKING_NODE) {
            return
        }
        visibility = View.INVISIBLE
        val node = nodeRef.get() ?: return
        visibility = View.VISIBLE
        val (ltX, ltY) = globalMathView.getGlobalCoord(node.leftTop.x, node.leftTop.y)
        val (rbX, rbY) = globalMathView.getGlobalCoord(node.rightBottom.x + 1, node.rightBottom.y + 1)
        val diameter: Int = if (node.children.isEmpty()) {
            max(abs(ltX - rbX), abs(ltY - rbY))
        } else {
            val (deltaX, deltaY) = globalMathView.getGlobalCoord(node.leftTop.x + 1, node.leftTop.y + 1)
            max(abs(ltX - deltaX), abs(ltY - deltaY))
        }
        val centerX = (ltX + rbX) / 2 - width / 2
        val centerY = (ltY + rbY) / 2 - height / 2

        val pointerDiameter = min(width * scaleX, height * scaleY)

        val scaleMultiplier = diameter * extraScale / (pointerDiameter)

        animate()
            .x(centerX.toFloat())
            .y(centerY.toFloat())
            .scaleX(scaleX * scaleMultiplier)
            .scaleY(scaleY * scaleMultiplier)
            .setDuration(0)
            .start()
    }

    fun resetTracker() {
        nodeRef = WeakReference(null)
        visibility = View.INVISIBLE
        mode = TRACKING_STATIC
    }
}