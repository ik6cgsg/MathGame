package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import spbpu.hsamcp.mathgame.AuthStatus
import spbpu.hsamcp.mathgame.GlobalScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.statistics.AuthInfo
import java.security.MessageDigest
import java.util.*

class AuthActivity: AppCompatActivity() {
    private val TAG = "AuthActivity"
    private lateinit var googleSignInClient: GoogleSignInClient
    private val signIn = 1

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        GlobalScene.shared.googleSignInClient = googleSignInClient
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        if (!prefs.contains(Constants.deviceId)) {
            val prefEdit = prefs.edit()
            prefEdit.putString(Constants.deviceId, UUID.randomUUID().toString())
            prefEdit.commit()
        }
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        // updateUI(account)
        if (account != null) {
            finish()
        }
    }

    override fun onBackPressed() {
    }

    fun continueAsGuest(v: View?) {
        val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
        if (prefs.getString(AuthInfo.AUTH_STATUS.str, "") != AuthStatus.GUEST.str) {
            val prefEdit = prefs.edit()
            prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, false)
            prefEdit.putString(AuthInfo.AUTH_STATUS.str, AuthStatus.GUEST.str)
            val uuid = prefs.getString(Constants.deviceId, "")
            prefEdit.putString(AuthInfo.LOGIN.str, "user" + uuid!!.hashCode())
            prefEdit.putString(AuthInfo.NAME.str, "")
            prefEdit.putString(AuthInfo.SURNAME.str, "")
            prefEdit.putString(AuthInfo.SECOND_NAME.str, "")
            prefEdit.putString(AuthInfo.ADDITIONAL.str, "")
            prefEdit.commit()
            GlobalScene.shared.generateGamesMultCoeffs(prefEdit)
            GlobalScene.shared.authStatus = AuthStatus.GUEST
        }
        finish()
    }

    fun onGoogleClicked(v: View?) {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, signIn)
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
            prefEdit.putString(AuthInfo.LOGIN.str, account!!.givenName)
            /*
            prefEdit.putString(AuthInfo.NAME.str, nameView.text.toString())
            prefEdit.putString(AuthInfo.SURNAME.str, surnameView.text.toString())
            prefEdit.putString(AuthInfo.SECOND_NAME.str, secondNameView.text.toString())
            prefEdit.putString(AuthInfo.GROUP.str, groupView.text.toString())
            prefEdit.putString(AuthInfo.INSTITUTION.str, institutionView.text.toString())
             */
            prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, true)
            prefEdit.putString(AuthInfo.AUTH_STATUS.str, AuthStatus.GOOGLE.str)
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