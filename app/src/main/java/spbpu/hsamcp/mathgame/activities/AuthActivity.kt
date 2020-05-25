package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
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
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import org.json.JSONObject
import spbpu.hsamcp.mathgame.AuthStatus
import spbpu.hsamcp.mathgame.GlobalScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AuthInfoObjectBase
import spbpu.hsamcp.mathgame.common.Storage
import spbpu.hsamcp.mathgame.statistics.Pages
import spbpu.hsamcp.mathgame.statistics.Request
import spbpu.hsamcp.mathgame.statistics.RequestData

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
        val standartSignInButton = findViewById<SignInButton>(R.id.sign_in_button)
        standartSignInButton.setSize(SignInButton.SIZE_WIDE)
        standartSignInButton.setColorScheme(SignInButton.COLOR_LIGHT)
        standartSignInButton.setOnClickListener {
            onGoogleClicked(it)
        }
    }

    override fun onBackPressed() {
    }

    private fun checkInput() {
        signInButton.isEnabled = !loginView.text.isNullOrBlank() && !passwordView.text.isNullOrBlank()
    }

    fun continueAsGuest(v: View?) {
        Storage.shared.initUserInfo(this, AuthInfoObjectBase(
            authStatus = AuthStatus.GUEST
        ))
        val userData = Storage.shared.getUserInfoBase(this)
        val requestRoot = JSONObject()
        requestRoot.put("login", userData.login)
        requestRoot.put("password", userData.password)
        val req = RequestData(Pages.SIGNUP.value, body = requestRoot.toString())
        val token = Request.signRequest(req)
        Storage.shared.setServerToken(this, token)
        finish()
    }

    fun signIn(v: View?) {
        val login = loginView.text.toString()
        val password = passwordView.text.toString()
        val requestRoot = JSONObject()
        requestRoot.put("login", login)
        requestRoot.put("password", password)
        val req = RequestData(Pages.SIGNIN.value, body = requestRoot.toString())
        val token = Request.signRequest(req)
        Storage.shared.setServerToken(this, token)
        finish()
    }

    fun signUp(v: View?) {
        Storage.shared.initUserInfo(this, AuthInfoObjectBase(authStatus = AuthStatus.MATH_HELPER))
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
            // TODO: server request SIGN_IN with ** accountId **
            val idTokenString = account!!.idToken
            val requestRoot = JSONObject()
            requestRoot.put("idTokenString", idTokenString)
            val req = RequestData(Pages.GOOGLE_SIGN_IN.value, body = requestRoot.toString())
            val token = Request.signRequest(req)
            Storage.shared.initUserInfo(this, AuthInfoObjectBase(
                login = account.email.orEmpty().replace("@gmail.com", ""),
                name = account.givenName.orEmpty(),
                fullName = account.familyName.orEmpty() + " " + account.givenName.orEmpty(),
                authStatus = AuthStatus.GOOGLE,
                serverToken = token
            ))
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