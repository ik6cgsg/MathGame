package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.R
import mathhelper.games.matify.common.AuthInfoObjectBase
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.statistics.Pages
import mathhelper.games.matify.statistics.Request
import mathhelper.games.matify.statistics.RequestData

class AccountActivity: AppCompatActivity() {
    private val TAG = "AccountActivity"
    private lateinit var loginView: TextView
    private lateinit var addInfoSwitch: Switch
    private lateinit var addInfoList: ScrollView
    private lateinit var nameView: TextView
    private lateinit var fullNameView: TextView
    private lateinit var additionalView: TextView
    private lateinit var logButton: Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        loginView = findViewById(R.id.login)
        addInfoSwitch = findViewById(R.id.show_add)
        addInfoList = findViewById(R.id.additional_info_list)
        nameView = findViewById(R.id.name)
        fullNameView = findViewById(R.id.full_name)
        additionalView = findViewById(R.id.additional)
        logButton = findViewById(R.id.log_button)
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
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }

    fun toggleAdditionalInfo(v: View?) {
        if (addInfoSwitch.isChecked) {
            addInfoList.visibility = View.VISIBLE
        } else {
            addInfoList.visibility = View.GONE
        }
    }

    fun save(v: View?) {
        val requestRoot = JSONObject()
        requestRoot.put("login", loginView.text.toString())
        requestRoot.put("name", nameView.text.toString())
        requestRoot.put("fullName", fullNameView.text.toString())
        requestRoot.put("addInfo", additionalView.text.toString())
        val req = RequestData(Pages.EDIT.value, Storage.shared.serverToken(this), body = requestRoot.toString())
        GlobalScene.shared.request(this, background = {
            Request.editRequest(req)
            Storage.shared.setUserInfo(this, AuthInfoObjectBase(
                login = loginView.text.toString(),
                name = nameView.text.toString(),
                fullName = fullNameView.text.toString(),
                additional = additionalView.text.toString()
            ))
        }, foreground = {
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
        })
    }

    fun logClicked(v: View?) {
        GlobalScene.shared.logout()
        finish()
    }
}