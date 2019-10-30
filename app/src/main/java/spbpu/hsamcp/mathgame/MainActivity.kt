package spbpu.hsamcp.mathgame

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import io.github.kexanie.library.MathView
import android.graphics.Point
import android.view.WindowManager

class MainActivity : AppCompatActivity() {
    var dX: Float = 0.toFloat()
    var dY: Float = 0.toFloat()
    var formula_two: MathView? = null
    var tex = "$$\\sum_{i=0}^n i^2 = \\frac{(n^2+n)(2n+1)}{6}$$"
    var screenHeight: Int = 0
    var screenWidth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student)
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //formula_two = MathView(this, )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        formula_two = findViewById<MathView>(R.id.formula_two)
        formula_two!!.text = tex
        formula_two!!.setOnTouchListener{view: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.getX() - event.rawX
                    dY = view.getY() - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY
                    if ((newX > - view.width && newX < screenWidth) && (newY > 0 && newY < screenHeight - view.height)) {
                        view.animate()
                            .x(newX)
                            .y(newY)
                            .setDuration(0)
                            .start()
                    }
                }
                else -> false
            }
            true
        }
    }
}
