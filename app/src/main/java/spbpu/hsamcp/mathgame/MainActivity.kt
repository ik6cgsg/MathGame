package spbpu.hsamcp.mathgame

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.graphics.Point
import android.graphics.Typeface
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import spbpu.hsamcp.mathgame.mathResolver.MathResolver

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var screenHeight: Int = 0
    private var screenWidth: Int = 0
    private var gmv: GlobalMathView? = null

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
        //val form = "((cos(x)*cos(y))-cos(x+y))/(cos(x-y)-(sin(x)*sin(y)))"
        val form = "sin(x)/cos(x)"
        gmv!!.setFormula(form)
        MathScene.globalFormula = gmv
    }
}
