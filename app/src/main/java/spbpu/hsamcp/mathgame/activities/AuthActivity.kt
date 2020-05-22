package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import spbpu.hsamcp.mathgame.AuthStatus
import spbpu.hsamcp.mathgame.GlobalScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AuthInfo
import spbpu.hsamcp.mathgame.common.Constants
import java.util.*
import kotlin.math.absoluteValue

class AuthActivity: AppCompatActivity() {
    private val TAG = "AuthActivity"
    private val signIn = 1
    private lateinit var loginView: TextView
    private lateinit var passwordView: TextView
    private lateinit var signInButton: Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        loginView = findViewById(R.id.loginText)
        loginView.doAfterTextChanged { checkInput() }
        passwordView = findViewById(R.id.passwordText)
        passwordView.doAfterTextChanged { checkInput() }
        signInButton = findViewById(R.id.sign_in)
        signInButton.isEnabled = false
    }

    override fun onBackPressed() {
    }

    private fun checkInput() {
        if (!loginView.text.isNullOrBlank() && !passwordView.text.isNullOrBlank()) {
            signInButton.isEnabled = true
        } else {
            signInButton.isEnabled = false
        }
    }

    fun continueAsGuest(v: View?) {
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        //if (prefs.getString(AuthInfo.AUTH_STATUS.str, "") != AuthStatus.GUEST.str) {
        val prefEdit = prefs.edit()
        val uuid = UUID.randomUUID()
        prefEdit.putString(AuthInfo.UUID.str, uuid.toString())
        prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, true)
        prefEdit.putString(AuthInfo.AUTH_STATUS.str, AuthStatus.GUEST.str)
        prefEdit.putString(AuthInfo.LOGIN.str, "guest" + uuid.hashCode().absoluteValue)
        prefEdit.putString(AuthInfo.NAME.str, "")
        prefEdit.putString(AuthInfo.SURNAME.str, "")
        prefEdit.putString(AuthInfo.SECOND_NAME.str, "")
        prefEdit.putString(AuthInfo.ADDITIONAL.str, "")
        // TODO: server request SIGN_UP
        // TODO: prefEdit.putString(AuthInfo.SERVER_ID.str, "")
        prefEdit.commit()
        GlobalScene.shared.generateGamesMultCoeffs(prefEdit)
        GlobalScene.shared.authStatus = AuthStatus.GUEST
        //}
        finish()
    }

    fun signIn(v: View?) {
        val login = loginView.text.toString()
        val password = passwordView.text.toString()
        // TODO: server request SIGN_IN
        // TODO: prefEdit.putString(AuthInfo.SERVER_ID.str, "")

    }

    fun signUp(v: View?) {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    fun onGoogleClicked(v: View?) {
        val signInIntent = GlobalScene.shared.googleSignInClient?.signInIntent
        if (signInIntent != null) {
            startActivityForResult(signInIntent, signIn)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == signIn) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
            val prefEdit = prefs.edit()
            prefEdit.putString(AuthInfo.LOGIN.str, account!!.email.orEmpty().replace("@gmail.com", ""))
            prefEdit.putString(AuthInfo.NAME.str, account.givenName.orEmpty())
            prefEdit.putString(AuthInfo.SURNAME.str, account.familyName.orEmpty())
            prefEdit.putString(AuthInfo.SECOND_NAME.str, "")
            prefEdit.putString(AuthInfo.ADDITIONAL.str, "")
            prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, true)
            prefEdit.putString(AuthInfo.AUTH_STATUS.str, AuthStatus.GOOGLE.str)
            // TODO: server request SIGN_IN with ** account.tokenId **
            // TODO: prefEdit.putString(AuthInfo.SERVER_ID.str, "")
            GlobalScene.shared.authStatus = AuthStatus.GOOGLE
            GlobalScene.shared.generateGamesMultCoeffs(prefEdit)
            prefEdit.commit()
            //Statistics.logSign(this)
            finish()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Error while sign with Google...", Toast.LENGTH_LONG).show()
        }
    }

    fun onGitHubClicked(v: View?) {
        Toast.makeText(this, "Coming soon...", Toast.LENGTH_SHORT).show()
    }
}