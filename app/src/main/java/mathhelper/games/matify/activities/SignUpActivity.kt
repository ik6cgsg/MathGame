package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import org.json.JSONObject
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.R
import mathhelper.games.matify.common.AuthInfoObjectBase
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.statistics.Pages
import mathhelper.games.matify.statistics.Request
import mathhelper.games.matify.statistics.RequestData

class SignUpActivity: AppCompatActivity() {
    private val TAG = "SignUpActivity"
    private lateinit var loginView: TextView
    private lateinit var addInfoSwitch: Switch
    private lateinit var addInfoList: ScrollView
    private lateinit var nameView: TextView
    private lateinit var fullNameView: TextView
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
        fullNameView = findViewById(R.id.full_name)
        additionalView = findViewById(R.id.additional)
        signButton = findViewById(R.id.sign_up)
        GlobalScene.shared.loadingElement = findViewById(R.id.progress)
    }

    override fun onResume() {
        super.onResume()
        loginView.text = Storage.shared.login(this)
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
        if (addInfoSwitch.isChecked) {
            addInfoList.visibility = View.VISIBLE
        } else {
            addInfoList.visibility = View.GONE
        }
    }

    fun sign(v: View?) {
        Storage.shared.setUserInfo(this, AuthInfoObjectBase(
            login = loginView.text.toString(),
            password = passwordView.text.toString(),
            name = nameView.text.toString(),
            fullName = fullNameView.text.toString(),
            additional = additionalView.text.toString()
        ))
        val userData = Storage.shared.getUserInfoBase(this)
        val requestRoot = JSONObject()
        requestRoot.put("login", userData.login)
        requestRoot.put("password", userData.password)
        requestRoot.put("name", userData.name)
        requestRoot.put("fullName", userData.fullName)
        requestRoot.put("addInfo", userData.additional)
        val req = RequestData(Pages.SIGNUP.value, body = requestRoot.toString())
        GlobalScene.shared.request(this, initial = true, background = {
            val token = Request.signRequest(req)
            Storage.shared.setServerToken(this, token)
        }, foreground = {
            finish()
        })
    }

    fun cancel(v: View?) {
        startActivity(Intent(this, AuthActivity::class.java))
        Storage.shared.invalidateUser(this)
        finish()
    }
}