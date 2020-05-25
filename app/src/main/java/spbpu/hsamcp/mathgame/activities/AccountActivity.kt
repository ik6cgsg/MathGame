package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import spbpu.hsamcp.mathgame.GlobalScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AuthInfoObjectBase
import spbpu.hsamcp.mathgame.common.Storage
import spbpu.hsamcp.mathgame.statistics.Pages
import spbpu.hsamcp.mathgame.statistics.Request
import spbpu.hsamcp.mathgame.statistics.RequestData

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
        Storage.shared.setUserInfo(this, AuthInfoObjectBase(
            login = loginView.text.toString(),
            name = nameView.text.toString(),
            fullName = fullNameView.text.toString(),
            additional = additionalView.text.toString()
        ))
        val userData = Storage.shared.getUserInfoBase(this)
        val requestRoot = JSONObject()
        requestRoot.put("login", userData.login)
        requestRoot.put("name", userData.name)
        requestRoot.put("fullName", userData.fullName)
        requestRoot.put("addInfo", userData.additional)
        val req = RequestData(Pages.EDIT.value, Storage.shared.serverToken(this), body = requestRoot.toString())
        Request.editRequest(req)
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
    }

    fun logClicked(v: View?) {
        GlobalScene.shared.logout()
        finish()
    }
}