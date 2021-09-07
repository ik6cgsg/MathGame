package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.review.ReviewManagerFactory
import eightbitlab.com.blurview.BlurView
import mathhelper.games.matify.*
import mathhelper.games.matify.common.*
import java.util.*


class SettingsActivity: AppCompatActivity() {
    private val TAG = "SettingsActivity"
    private lateinit var resetDialog: AlertDialog
    private lateinit var changeLanguageDialog: AlertDialog
    private lateinit var changeThemeDialog: AlertDialog
    private lateinit var greetings: TextView
    private lateinit var changePassword: View
    lateinit var blurView: BlurView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(ThemeController.shared.currentTheme.resId)
        setContentView(R.layout.activity_settings_new)
        val backView = findViewById<TextView>(R.id.back)
        AndroidUtil.setOnTouchUpInside(this, backView, ::back)
        resetDialog = createResetAlert()
        changeThemeDialog = createChangeThemeAlert()
        changeLanguageDialog = createChangeLanguageAlert()
        changePassword = findViewById(R.id.pass_change)
        if (Build.VERSION.SDK_INT < 24) {
            val settings = findViewById<TextView>(R.id.settings)
            settings.text = resources.getString(R.string.settings)
        }
        val versionView = findViewById<TextView>(R.id.version)
        versionView.text = versionView.text.toString() + BuildConfig.VERSION_NAME
        greetings = findViewById(R.id.greetings)
        blurView = findViewById(R.id.blurView)
    }

    override fun onResume() {
        super.onResume()
        greetings.text = "\uD83D\uDC4B ${resources.getString(R.string.hi)}, ${Storage.shared.login(this)}! \uD83D\uDC4B"
        when (GlobalScene.shared.authStatus) {
            AuthStatus.MATH_HELPER, AuthStatus.GUEST -> changePassword.isEnabled = true
            else -> changePassword.isEnabled = false
        }
    }

    fun editClick(v: View?) {
        startActivity(Intent(this, AccountActivity::class.java))
    }

    fun rateClick(v: View?) {
        rateOnMarket()
    }

    fun resetClick(v: View?) {
        AndroidUtil.showDialog(resetDialog, backMode = BackgroundMode.BLUR, blurView = blurView, activity = this)
    }

    fun logoutClicked(v: View?) {
        GlobalScene.shared.logout()
        finish()
    }

    fun changePassClicked(v: View?) {
        startActivity(Intent(this, PasswordActivity::class.java))
    }

    fun changeThemeClicked(v: View?) {
        AndroidUtil.showDialog(changeThemeDialog, backMode = BackgroundMode.BLUR, blurView = blurView, activity = this)
    }

    fun changeLanguageClicked(v: View?) {
        AndroidUtil.showDialog(changeLanguageDialog, backMode = BackgroundMode.BLUR, blurView = blurView, activity = this)
    }

    fun startTutorialClick(v: View?) {
        TutorialScene.shared.start(this)
    }

    fun back(v: View?) {
        finish()
    }

    private fun rateOnMarket() {
        val uri: Uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun createResetAlert(): AlertDialog {
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.are_you_sure)
            .setMessage(R.string.this_action_will_reset_all_your_achievements)
            .setPositiveButton(R.string.yes) { dialog: DialogInterface, id: Int ->
                GlobalScene.shared.resetAll(success = {
                    finish()
                }, error = {
                    // TODO
                })
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int ->
            }
            .setCancelable(true)
        return builder.create()
    }

    private fun createChangeThemeAlert(): AlertDialog {
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.change_theme)
            .setItems(R.array.themes_array) { dialog, which ->
                val themeToChoose = when (which) {
                    0 -> ThemeName.LIGHT
                    1 -> ThemeName.DARK
                    else -> ThemeName.DARK
                }
                ThemeController.shared.setTheme(this, themeToChoose)
                finishAffinity()
                startActivity(Intent(this, GamesActivity::class.java))
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int ->
            }
            .setCancelable(true)
        return builder.create()
    }

    private fun createChangeLanguageAlert(): AlertDialog {
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.change_language)
            .setItems(R.array.languages_array) {
                    dialog, which ->
                val languageToChoose = when (which) {
                    0 -> "ru"
                    1 -> "en"
                    else -> "en"
                }
                val config = Configuration(resources.configuration)
                config.locale = Locale(languageToChoose); //locale
                resources.updateConfiguration(config, resources.displayMetrics)
                finishAffinity()
                startActivity(Intent(this, GamesActivity::class.java))
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int ->
            }
            .setCancelable(true)
        return builder.create()
    }
}