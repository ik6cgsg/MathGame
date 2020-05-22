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
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.game.Game

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
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        loginView.text = prefs.getString(AuthInfo.LOGIN.str, "")
        nameView.text = prefs.getString(AuthInfo.NAME.str, "")
        surnameView.text = prefs.getString(AuthInfo.SURNAME.str, "")
        secondNameView.text = prefs.getString(AuthInfo.SECOND_NAME.str, "")
        additionalView.text = prefs.getString(AuthInfo.ADDITIONAL.str, "")
        signButton.isEnabled = false
        loginView.doAfterTextChanged { checkInput() }
        passwordView.doAfterTextChanged { checkInput() }
        repeatView.doAfterTextChanged { checkInput() }
    }

    private fun checkInput() {
        if (!loginView.text.isNullOrBlank() && !passwordView.text.isNullOrBlank() && !repeatView.text.isNullOrBlank() &&
            passwordView.text.toString() == repeatView.text.toString()) {
            signButton.isEnabled = true
        } else {
            signButton.isEnabled = false
        }
    }

    fun toggleAdditionalInfo(v: View?) {
        if (addInfoSwitch.isChecked) {
            addInfoList.visibility = View.VISIBLE
        } else {
            addInfoList.visibility = View.GONE
        }
    }

    fun sign(v: View?) {
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, true)
        prefEdit.putString(AuthInfo.AUTH_STATUS.str, AuthStatus.MATH_HELPER.str)
        prefEdit.putString(AuthInfo.LOGIN.str, loginView.text.toString())
        // TODO: password ??
        prefEdit.putString(AuthInfo.PASSWORD.str, passwordView.text.toString())
        prefEdit.putString(AuthInfo.NAME.str, nameView.text.toString())
        prefEdit.putString(AuthInfo.SURNAME.str, surnameView.text.toString())
        prefEdit.putString(AuthInfo.SECOND_NAME.str, secondNameView.text.toString())
        prefEdit.putString(AuthInfo.ADDITIONAL.str, additionalView.text.toString())
        GlobalScene.shared.authStatus = AuthStatus.MATH_HELPER
        GlobalScene.shared.generateGamesMultCoeffs(prefEdit)
        prefEdit.commit()
        // TODO: server request: SIGN UP
        finish()
    }

    fun cancel(v: View?) {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}