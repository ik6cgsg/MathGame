package spbpu.hsamcp.mathgame

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import io.github.kexanie.library.MathView
import android.graphics.Point
import android.view.WindowManager
import android.widget.TextView
import com.twf.api.*
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import com.twf.factstransformations.Expression

class MainActivity : AppCompatActivity() {
    var dX: Float = 0.toFloat()
    var dY: Float = 0.toFloat()
    var formula_two: MathView? = null
    var tex = "$$\\sum_{i=0}^n i^2 = \\frac{(n^2+n)(2n+1)}{6}$$"
    var screenHeight: Int = 0
    var screenWidth: Int = 0
    var tv: TextView? = null
    

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
        tv = findViewById<TextView>(R.id.hello_twf)
        var form = "tg(x)"
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
    /*

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
     */
}
