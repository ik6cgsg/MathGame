package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import spbpu.hsamcp.mathgame.AuthStatus
import spbpu.hsamcp.mathgame.GlobalScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AuthInfoObjectBase
import spbpu.hsamcp.mathgame.common.Storage
import spbpu.hsamcp.mathgame.statistics.Pages
import spbpu.hsamcp.mathgame.statistics.Request
import spbpu.hsamcp.mathgame.statistics.RequestData

class PasswordActivity: AppCompatActivity() {
    private val TAG = "PasswordActivity"
    private lateinit var oldPassView: TextView
    private lateinit var oldPassInputLayout: TextInputLayout
    private lateinit var newPassView: TextView
    private lateinit var newPassInputLayout: TextInputLayout
    private lateinit var repeatPassView: TextView
    private lateinit var repeatPassInputLayout: TextInputLayout
    private lateinit var confirmButton: Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pass)
        oldPassView = findViewById(R.id.old_password)
        oldPassInputLayout = findViewById(R.id.oldInputLayout)
        newPassView = findViewById(R.id.new_password)
        newPassInputLayout = findViewById(R.id.newInputLayout)
        newPassInputLayout.visibility = View.GONE
        repeatPassView = findViewById(R.id.repeat_password)
        repeatPassInputLayout = findViewById(R.id.repeatInputLayout)
        repeatPassInputLayout.visibility = View.GONE
        confirmButton = findViewById(R.id.confirm)
        if (GlobalScene.shared.authStatus == AuthStatus.GUEST) {
            oldPassInputLayout.visibility = View.GONE
            newPassInputLayout.visibility = View.VISIBLE
            repeatPassInputLayout.visibility = View.VISIBLE
        }
    }

    fun back(v: View?) {
        finish()
    }

    fun confirm(v: View?) {
        if (oldPassInputLayout.visibility == View.VISIBLE) {
            val oldPass = Storage.shared.password(this)
            if (oldPassView.text.toString() == oldPass) {
                oldPassInputLayout.visibility = View.GONE
                newPassInputLayout.visibility = View.VISIBLE
                repeatPassInputLayout.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Wrong password!", Toast.LENGTH_SHORT).show()
            }
        } else if (newPassView.text.toString() == repeatPassView.text.toString()) {
            Storage.shared.setUserInfo(this, AuthInfoObjectBase(
                password = newPassView.text.toString(),
                authStatus = AuthStatus.MATH_HELPER
            ))
            val userData = Storage.shared.getUserInfoBase(this)
            val requestRoot = JSONObject()
            requestRoot.put("password", userData.password)
            val req = RequestData(Pages.EDIT.value, Storage.shared.serverToken(this), body = requestRoot.toString())
            Request.editRequest(req)
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Different passwords", Toast.LENGTH_SHORT).show()
        }
    }
}