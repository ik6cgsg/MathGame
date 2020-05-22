package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AuthInfo
import spbpu.hsamcp.mathgame.common.Constants

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
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        newPassView = findViewById(R.id.new_password)
        newPassInputLayout = findViewById(R.id.newInputLayout)
        newPassInputLayout.visibility = View.GONE
        repeatPassView = findViewById(R.id.repeat_password)
        repeatPassInputLayout = findViewById(R.id.repeatInputLayout)
        repeatPassInputLayout.visibility = View.GONE
        confirmButton = findViewById(R.id.confirm)
        if (prefs.getString(AuthInfo.PASSWORD.str, "").isNullOrEmpty()) {
            oldPassInputLayout.visibility = View.GONE
            newPassInputLayout.visibility = View.VISIBLE
            repeatPassInputLayout.visibility = View.VISIBLE
        }
    }

    fun back(v: View?) {
        finish()
    }

    fun confirm(v: View?) {
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        if (oldPassInputLayout.visibility == View.VISIBLE) {
            val oldPass = prefs.getString(AuthInfo.PASSWORD.str, "")
            if (oldPassView.text.toString() == oldPass) {
                oldPassInputLayout.visibility = View.GONE
                newPassInputLayout.visibility = View.VISIBLE
                repeatPassInputLayout.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Wrong password!", Toast.LENGTH_SHORT).show()
            }
        } else if (newPassView.text.toString() == repeatPassView.text.toString()) {
            val prefEdit = prefs.edit()
            prefEdit.putString(AuthInfo.PASSWORD.str, newPassView.text.toString())
            prefEdit.commit()
            // TODO: server request: EDIT
            finish()
        } else {
            Toast.makeText(this, "Different passwords", Toast.LENGTH_SHORT).show()
        }
    }
}