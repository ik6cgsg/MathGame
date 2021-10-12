package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.R
import mathhelper.games.matify.common.AuthInfoObjectBase
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.common.ThemeController

class SignUpActivity: AppCompatActivity() {
    private val TAG = "SignUpActivity"
    private lateinit var loginView: TextView
    private lateinit var additionalSwitch: Switch
    private lateinit var additionalList: ScrollView
    private lateinit var nameView: TextView
    private lateinit var fullNameView: TextView
    private lateinit var additionalView: TextView
    private lateinit var signButton: Button
    private lateinit var passwordView: TextView
    private lateinit var repeatView: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(ThemeController.shared.currentTheme.resId)
        setContentView(R.layout.activity_sign_up)
        loginView = findViewById(R.id.login)
        passwordView = findViewById(R.id.password)
        repeatView = findViewById(R.id.repeat)
        additionalSwitch = findViewById(R.id.show_add)
        additionalList = findViewById(R.id.additional_info_list)
        nameView = findViewById(R.id.name)
        fullNameView = findViewById(R.id.full_name)
        additionalView = findViewById(R.id.additional)
        signButton = findViewById(R.id.sign_up)
        GlobalScene.shared.loadingElement = findViewById(R.id.progress)
    }

    override fun onResume() {
        super.onResume()
        loginView.text = Storage.shared.login()
        signButton.isEnabled = false
        loginView.doAfterTextChanged { checkInput() }
        passwordView.doAfterTextChanged { checkInput() }
        repeatView.doAfterTextChanged { checkInput() }
    }

    override fun onBackPressed() {
    }

    private fun checkInput() {
        signButton.isEnabled = !loginView.text.isNullOrBlank() && !passwordView.text.isNullOrBlank() && !repeatView.text.isNullOrBlank() &&
            passwordView.text.toString() == repeatView.text.toString()
    }

    fun toggleAdditionalInfo(v: View?) {
        if (additionalSwitch.isChecked) {
            additionalList.visibility = View.VISIBLE
        } else {
            additionalList.visibility = View.GONE
        }
    }

    fun sign(v: View?) {
        val userData = AuthInfoObjectBase(
            login = loginView.text.toString(),
            password = passwordView.text.toString(),
            name = nameView.text.toString(),
            fullName = fullNameView.text.toString(),
            additional = additionalView.text.toString()
        )
        Storage.shared.setUserInfo(userData)
        GlobalScene.shared.signUp(this, userData)
    }

    fun cancel(v: View?) {
        startActivity(Intent(this, AuthActivity::class.java))
        Storage.shared.invalidateUser()
        finish()
    }
}