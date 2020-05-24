package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import spbpu.hsamcp.mathgame.AuthStatus
import spbpu.hsamcp.mathgame.GlobalScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AuthInfo
import spbpu.hsamcp.mathgame.common.AuthInfoObjectBase
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.common.Storage
import spbpu.hsamcp.mathgame.game.Game
import spbpu.hsamcp.mathgame.statistics.Request
import spbpu.hsamcp.mathgame.statistics.RequestData

class SignUpActivity: AppCompatActivity() {
    private val TAG = "SignUpActivity"
    private lateinit var loginView: TextView
    private lateinit var addInfoSwitch: Switch
    private lateinit var addInfoList: ScrollView
    private lateinit var nameView: TextView
    private lateinit var surnameView: TextView
    private lateinit var secondNameView: TextView
    private lateinit var additionalView: TextView
    private lateinit var signButton: Button
    private lateinit var passwordView: TextView
    private lateinit var repeatView: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        loginView = findViewById(R.id.login)
        passwordView = findViewById(R.id.password)
        repeatView = findViewById(R.id.repeat)
        addInfoSwitch = findViewById(R.id.show_add)
        addInfoList = findViewById(R.id.additional_info_list)
        nameView = findViewById(R.id.name)
        surnameView = findViewById(R.id.surname)
        secondNameView = findViewById(R.id.second_name)
        additionalView = findViewById(R.id.additional)
        signButton = findViewById(R.id.sign_up)
    }

    override fun onResume() {
        super.onResume()
        loginView.text = Storage.shared.login(this)
        signButton.isEnabled = false
        loginView.doAfterTextChanged { checkInput() }
        passwordView.doAfterTextChanged { checkInput() }
        repeatView.doAfterTextChanged { checkInput() }
    }

    private fun checkInput() {
        signButton.isEnabled = !loginView.text.isNullOrBlank() && !passwordView.text.isNullOrBlank() && !repeatView.text.isNullOrBlank() &&
            passwordView.text.toString() == repeatView.text.toString()
    }

    fun toggleAdditionalInfo(v: View?) {
        if (addInfoSwitch.isChecked) {
            addInfoList.visibility = View.VISIBLE
        } else {
            addInfoList.visibility = View.GONE
        }
    }

    fun sign(v: View?) {
        val req = RequestData("/api/auth/signup")
        val token = Request.signUp(req)
        Storage.shared.setUserInfo(this, AuthInfoObjectBase(
            login = loginView.text.toString(),
            password = passwordView.text.toString(),
            name = nameView.text.toString(),
            surname = surnameView.text.toString(),
            secondName = secondNameView.text.toString(),
            additional = additionalView.text.toString(),
            serverToken = token
        ))
        finish()
    }

    fun cancel(v: View?) {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}