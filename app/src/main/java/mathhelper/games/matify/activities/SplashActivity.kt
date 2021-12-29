package mathhelper.games.matify.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import mathhelper.games.matify.R
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.common.ThemeController
import java.util.*
import kotlinx.coroutines.launch
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.common.Constants

class SplashActivity: AppCompatActivity() {
    private val TAG = "SplashActivity"
    private val minSplashTime = 1000

    private fun setLanguage() {
        val locale = Locale(Storage.shared.language())
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.locale = locale;
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setLanguage()
        ThemeController.shared.init(this)
        setTheme(ThemeController.shared.currentTheme.resId)
        setContentView(R.layout.activity_splash)
        GlobalScope.launch {
            initialWork()
        }
    }

    private suspend fun initialWork() {
        Logger.d(TAG, "initialWork")
        GlobalScene.shared.init()
        val minFinishTime = System.currentTimeMillis() + minSplashTime
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(Constants.serverId)
            .build()
        GlobalScene.shared.googleSignInClient = GoogleSignIn.getClient(this, gso)
        Storage.shared.checkDeviceId()
        val authed = Storage.shared.isUserAuthorized()
        val dif = minFinishTime - System.currentTimeMillis()
        if (dif > 0) {
            delay(dif)
        }
        if (authed) {
            startActivity(Intent(this, GamesActivity::class.java))
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
        }
        finish()
    }
}