package spbpu.hsamcp.mathgame

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import io.github.kexanie.library.MathView
import android.graphics.Point
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import com.twf.api.*
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import com.twf.factstransformations.Expression

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    var dX: Float = 0.toFloat()
    var dY: Float = 0.toFloat()
    var screenHeight: Int = 0
    var screenWidth: Int = 0
    var tv: TextView? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                // TODO: if inside MathView -> setHovered ??
                //                     else -> clear MathView
                Log.d(TAG, "MotionEvent.ACTION_MOVE")
            }
            MotionEvent.ACTION_UP -> {
                // TODO: if inside MathView -> subst
                Log.d(TAG, "MotionEvent.ACTION_UP")
            }
        }
        return false
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
        tv = findViewById(R.id.hello_twf)
        val form = "cos(3*x)^2"
        tv!!.text = form
    }

    fun makeSubst(view: View) {
        val expression = stringToExpression(tv!!.text.toString())
        val substitution: ExpressionSubstitution
        when(view.id) {
            R.id.subst_sin -> {
                substitution = expressionSubstitutionFromStrings("x", "sin(x)")
            }
            R.id.subst_cos -> {
                substitution = expressionSubstitutionFromStrings("x","cos(x)")
            }
            else -> substitution = expressionSubstitutionFromStrings("","")
        }
        val matchedPlaces = findSubstitutionPlacesInExpression(expression, substitution)
        val applicationResult = applySubstitution(expression, substitution, matchedPlaces.subList(0, 1))
        tv!!.text = expressionToString(expression)
    }
}
