package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import org.json.JSONObject
import mathhelper.games.matify.AuthStatus
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.R
import mathhelper.games.matify.common.AuthInfoObjectBase
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.common.ThemeController
import mathhelper.games.matify.statistics.RequestPage
import mathhelper.games.matify.statistics.Request
import mathhelper.games.matify.statistics.RequestData
import mathhelper.games.matify.statistics.Statistics

class AuthActivity: AppCompatActivity() {
    private val TAG = "AuthActivity"
    private val signIn = 1
    private lateinit var loginView: TextView
    private lateinit var passwordView: TextView
    private lateinit var signInButton: Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(ThemeController.shared.currentTheme.resId)
        setContentView(R.layout.activity_auth)
        loginView = findViewById(R.id.loginText)
        loginView.doAfterTextChanged { checkInput() }
        passwordView = findViewById(R.id.passwordText)
        passwordView.doAfterTextChanged { checkInput() }
        signInButton = findViewById(R.id.sign_in)
        signInButton.isEnabled = false
        GlobalScene.shared.loadingElement = findViewById(R.id.progress)
    }

    override fun onBackPressed() {
    }

    override fun finish() {
        GlobalScene.shared.loadingElement = null
        super.finish()
    }

    private fun checkInput() {
        signInButton.isEnabled = !loginView.text.isNullOrBlank() && !passwordView.text.isNullOrBlank()
    }

    fun continueAsGuest(v: View?) {
        Storage.shared.initUserInfo(AuthInfoObjectBase(
            authStatus = AuthStatus.GUEST
        ))
        val userData = Storage.shared.getUserInfoBase()
        val requestRoot = JSONObject()
        requestRoot.put("login", userData.login)
        requestRoot.put("password", userData.password)
        val req = RequestData(RequestPage.SIGNUP, body = requestRoot.toString())
        GlobalScene.shared.asyncTask(this, background = {
            val response = Request.signRequest(req)
            val token = response.getString("token")
            Storage.shared.setServerToken(response.getString("token"))
            getUserHistory(token)
        }, foreground = {
            finishSign()
        }, errorground = {
            this.runOnUiThread {
                Toast.makeText(this, R.string.self_phone_mode, Toast.LENGTH_LONG).show()
                finish()
                startActivity(Intent(this, GamesActivity::class.java))
            }
        })
    }

    fun signIn(v: View?) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(this.window.decorView.windowToken, 0)
        val login = loginView.text.toString()
        val password = passwordView.text.toString()
        val requestRoot = JSONObject()
        requestRoot.put("loginOrEmail", login)
        requestRoot.put("password", password)
        val req = RequestData(RequestPage.SIGNIN, body = requestRoot.toString())
        GlobalScene.shared.asyncTask(this, background = {
            val response = Request.signRequest(req)
            val token = response.getString("token")
            Storage.shared.initUserInfo(AuthInfoObjectBase(
                login = login,
                name = response.getString("name"),
                fullName = response.getString("fullName"),
                additional = response.getString("additional"),
                authStatus = AuthStatus.MATH_HELPER,
                serverToken = token
            ))
            getUserHistory(token)
        }, foreground = {
            finishSign()
        }, errorground = {
            Storage.shared.invalidateUser()
        })
    }

    fun signUp(v: View?) {
        Storage.shared.initUserInfo(AuthInfoObjectBase(authStatus = AuthStatus.MATH_HELPER))
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
            Logger.d("GoogleToken", account!!.idToken ?: "")
            // Signed in successfully, show authenticated UI.
            val idTokenString = account.idToken
            val requestRoot = JSONObject()
            requestRoot.put("idTokenString", idTokenString)
            val req = RequestData(RequestPage.GOOGLE_SIGN_IN, body = requestRoot.toString())
            GlobalScene.shared.asyncTask(this, background = {
                val response = Request.signRequest(req)
                val token = response.getString("token")
                Storage.shared.initUserInfo(AuthInfoObjectBase(
                    login = response.getString("login"),
                    name = response.getString("name"),
                    fullName = response.getString("fullName"),
                    additional = response.getString("additional"),
                    authStatus = AuthStatus.GOOGLE,
                    serverToken = token
                ))
                getUserHistory(token)
            }, foreground = {
                finishSign()
            }, errorground = {
                Storage.shared.invalidateUser()
            })
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, R.string.google_sign_in_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun finishSign() {
        Statistics.logSign(this)
        finish()
        startActivity(Intent(this, GamesActivity::class.java))
        //GlobalScene.shared.parseLoadedOrRequestDefaultGames()
    }

    fun onGitHubClicked(v: View?) {
        Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
    }

    private fun getUserHistory(token: String) {
        val req = RequestData(RequestPage.USER_HISTORY, securityToken = token)
        val res = Request.historyRequest(req)
        Storage.shared.saveResultFromServer(res)
    }
}