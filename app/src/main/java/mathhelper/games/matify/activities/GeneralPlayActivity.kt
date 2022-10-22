package mathhelper.games.matify.activities

import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import mathhelper.games.matify.common.GlobalMathView
import mathhelper.games.matify.common.SimpleMathView

abstract class GeneralPlayActivity: AppCompatActivity() {
    lateinit var globalMathView: GlobalMathView
    lateinit var endExpressionViewLabel: TextView
    lateinit var endExpressionMathView: SimpleMathView
    lateinit var messageView: TextView
    lateinit var bottomSheet: LinearLayout
    lateinit var rulesLinearLayout: LinearLayout
    lateinit var rulesScrollView: ScrollView
    lateinit var rulesMsg: TextView
    lateinit var timerView: TextView
    protected var needClear = false

    abstract fun showMessage(msg: String, flag: Boolean = true, ifFlagFalseMsg: String? = null)
}