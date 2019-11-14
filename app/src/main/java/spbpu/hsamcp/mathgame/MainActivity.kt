package spbpu.hsamcp.mathgame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.graphics.Point
import android.util.Log
import android.view.WindowManager

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    var dX: Float = 0.toFloat()
    var dY: Float = 0.toFloat()
    var screenHeight: Int = 0
    var screenWidth: Int = 0
    var gmv: GlobalMathView? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                // TODO: if inside GlobalMathView -> setHovered ??
                //                     else -> clear GlobalMathView
                Log.d(TAG, "MotionEvent.ACTION_MOVE")
            }
            MotionEvent.ACTION_UP -> {
                // TODO: if inside GlobalMathView -> subst
                Log.d(TAG, "MotionEvent.ACTION_UP")
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.student)
        setContentView(R.layout.activity_main)
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        gmv = findViewById(R.id.hello_twf)
        val form = "((cos(x)*cos(y))-cos(x+y))/(cos(x-y)-(sin(x)*sin(y)))"
        gmv!!.text = form
        MathScene.globalFormula = gmv
    }
}
