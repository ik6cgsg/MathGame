package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.R
import mathhelper.games.matify.common.AuthInfoObjectBase
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.common.ThemeController
import mathhelper.games.matify.statistics.Pages
import mathhelper.games.matify.statistics.Request
import mathhelper.games.matify.statistics.RequestData

class AccountActivity: AppCompatActivity() {
    private val TAG = "AccountActivity"
    private lateinit var loginView: TextView
    private lateinit var additionalSwitch: Switch
    private lateinit var additionalList: ScrollView
    private lateinit var nameView: TextView
    private lateinit var fullNameView: TextView
    private lateinit var additionalView: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(ThemeController.shared.currentTheme.resId)
        setContentView(R.layout.activity_account)
        loginView = findViewById(R.id.login)
        additionalSwitch = findViewById(R.id.show_add)
        additionalList = findViewById(R.id.additional_info_list)
        nameView = findViewById(R.id.name)
        fullNameView = findViewById(R.id.full_name)
        additionalView = findViewById(R.id.additional)
        GlobalScene.shared.loadingElement = findViewById(R.id.progress)
    }

    override fun onResume() {
        super.onResume()
        val info = Storage.shared.getUserInfoBase(this)
        loginView.text = info.login ?: ""
        nameView.text = info.name ?: ""
        fullNameView.text = info.fullName ?: ""
        additionalView.text = info.additional ?: ""
    }

    override fun onBackPressed() {
        back(null)
    }

    fun back(v: View?) {
        finish()
    }

    fun toggleAdditionalInfo(v: View?) {
        if (additionalSwitch.isChecked) {
            additionalList.visibility = View.VISIBLE
        } else {
            additionalList.visibility = View.GONE
        }
    }

    fun save(v: View?) {
        val passwordData = Storage.shared.getUserInfoBase(this).password
        val userData = AuthInfoObjectBase(
            login = loginView.text.toString(),
            name = nameView.text.toString(),
            fullName = fullNameView.text.toString(),
            additional = additionalView.text.toString(),
            password = if (passwordData.isNullOrBlank()) loginView.text.toString() else passwordData
        )
        if (Storage.shared.serverToken(this).isNullOrBlank()){
            GlobalScene.shared.signUp(this, userData)
        } else {
            val requestRoot = JSONObject()
            requestRoot.put("login", loginView.text.toString())
            requestRoot.put("name", nameView.text.toString())
            requestRoot.put("fullName", fullNameView.text.toString())
            requestRoot.put("additional", additionalView.text.toString())
            val req = RequestData(Pages.EDIT.value, Storage.shared.serverToken(this), body = requestRoot.toString())
            GlobalScene.shared.asyncTask(this, background = {
                Request.editRequest(req)
                Storage.shared.setUserInfo(this, userData)
            }, foreground = {
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            }, errorground = {})
        }
    }
}