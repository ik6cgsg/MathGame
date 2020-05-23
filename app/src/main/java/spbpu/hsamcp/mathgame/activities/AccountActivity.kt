package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import spbpu.hsamcp.mathgame.GlobalScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AuthInfo
import spbpu.hsamcp.mathgame.common.AuthInfoObjectBase
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.common.Storage
import spbpu.hsamcp.mathgame.statistics.Request

class AccountActivity: AppCompatActivity() {
    private val TAG = "AccountActivity"
    private lateinit var loginView: TextView
    private lateinit var addInfoSwitch: Switch
    private lateinit var addInfoList: ScrollView
    private lateinit var nameView: TextView
    private lateinit var surnameView: TextView
    private lateinit var secondNameView: TextView
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
        surnameView = findViewById(R.id.surname)
        secondNameView = findViewById(R.id.second_name)
        additionalView = findViewById(R.id.additional)
        logButton = findViewById(R.id.log_button)
    }

    override fun onResume() {
        super.onResume()
        val info = Storage.shared.getUserInfoBase(this)
        loginView.text = info.login ?: ""
        nameView.text = info.name ?: ""
        surnameView.text = info.surname ?: ""
        secondNameView.text = info.secondName ?: ""
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
            surname = surnameView.text.toString(),
            secondName = secondNameView.text.toString(),
            additional = additionalView.text.toString()
        ))
        // TODO: normal edit request
        Request.edit(null)
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
    }

    fun logClicked(v: View?) {
        GlobalScene.shared.logout()
        finish()
    }
}