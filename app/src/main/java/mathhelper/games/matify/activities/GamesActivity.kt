package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import eightbitlab.com.blurview.BlurView
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.LevelScene
import mathhelper.games.matify.R
import mathhelper.games.matify.TutorialScene
import mathhelper.games.matify.common.*
import mathhelper.games.matify.game.Game
import mathhelper.games.matify.game.GameResult
import mathhelper.games.matify.level.StateType
import java.util.*
import kotlin.collections.ArrayList

class GamesActivity: AppCompatActivity() {
    private val TAG = "GamesActivity"
    private lateinit var gamesViews: ArrayList<TextView>
    private lateinit var gamesList: LinearLayout
    private lateinit var searchView: EditText
    private lateinit var serverDivider: View
    private lateinit var serverLabel: TextView
    private lateinit var serverList: LinearLayout
    private lateinit var serverScroll: ScrollView
    private var askForTutorial = false
    lateinit var blurView: BlurView

    private fun setLanguage() {
        val locale = Locale("en")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        setLanguage()
        setTheme(Storage.shared.themeInt(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(Constants.serverId)
            .build()
        GlobalScene.shared.googleSignInClient = GoogleSignIn.getClient(this, gso)
        Storage.shared.checkDeviceId(this)
        if (!Storage.shared.isUserAuthorized(this)) {
            startActivity(Intent(this, AuthActivity::class.java))
            askForTutorial = true
        }
        GlobalScene.shared.gamesActivity = this
        gamesViews = ArrayList()
        gamesList = findViewById(R.id.games_list)
        searchView = findViewById(R.id.search)
        searchView.compoundDrawables[Constants.drawableEnd].setVisible(false, true)
        serverDivider = findViewById(R.id.server_divider)
        serverLabel = findViewById(R.id.server_games_label)
        serverList = findViewById(R.id.server_games_list)
        serverScroll = findViewById(R.id.server_scroll)
        blurView = findViewById(R.id.blurView)
        setSearchEngine()
        generateList()
        if (Build.VERSION.SDK_INT < 24) {
            val settings = findViewById<TextView>(R.id.settings)
            settings.text = "\uD83D\uDD27"
        }
    }

    override fun onResume() {
        super.onResume()
        serverDivider.visibility = View.GONE
        serverLabel.visibility = View.GONE
        serverList.visibility = View.GONE
        serverScroll.visibility = View.GONE
        clearSearch()
        if (askForTutorial) {
            askForTutorialDialog()
            askForTutorial = false
        }
    }

    fun settings(v: View?) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun clearSearch() {
        searchView.text.clear()
        searchView.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(this.window.decorView.windowToken, 0)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSearchEngine() {
        searchView.setOnTouchListener { v, event ->
            var res = false
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = searchView.compoundDrawables[Constants.drawableEnd]
                if (drawableEnd != null) {
                    if (event.x >= (searchView.width - searchView.paddingRight - drawableEnd.intrinsicWidth)) {
                        clearSearch()
                        filterList()
                        res = true
                    }
                }
            }
            res
        }
        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
                searchView.compoundDrawables[Constants.drawableEnd].setVisible(true, true)
            }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                filterList(search = s)
            }
        })
    }

    fun updateResult(newRes: GameResult?) {
        val i = GlobalScene.shared.currentGameIndex
        gamesViews[i].text = "${GlobalScene.shared.currentGame?.getNameByLanguage(resources.configuration.locale.language)}" +
            if (newRes != null) {
                "\n${newRes.toString().format(GlobalScene.shared.currentGame?.tasks?.size)}"
            } else {
                ""
            }
        GlobalScene.shared.currentGame?.lastResult = newRes
        GlobalScene.shared.currentGame?.save(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun generateList(search: CharSequence? = null) {
        GlobalScene.shared.games.forEachIndexed { i, game ->
            if (search != null) {
                if (!game.getNameByLanguage(resources.configuration.locale.language)!!.contains(search, ignoreCase = true)) {
                    return
                }
            }
            val gameView = AndroidUtil.createButtonView(this)
            gameView.text = game.getNameByLanguage(resources.configuration.locale.language)
            if (game.recommendedByCommunity) {
                val d = getDrawable(R.drawable.tick)
                d!!.setBounds(0, 0, 70, 70)
                gameView.setCompoundDrawables(null, null, d, null)
            }
            val themeName = Storage.shared.theme(this)
            gameView.setTextColor(ThemeController.shared.getColorByTheme(themeName, ColorName.TEXT_COLOR))
            if (game.lastResult != null) {
                gameView.text = "${gameView.text}\n${game.lastResult!!.toString().format(game.tasks.size)}"
            }
            gameView.background = getDrawable(R.drawable.button_rect)
            gameView.setOnClickListener {
                GlobalScene.shared.currentGameIndex = i
                GlobalScene.shared.currentGame = game
            }
            gameView.isLongClickable = true
            gameView.setOnLongClickListener { showInfo(game) }
            gamesList.addView(gameView)
            gamesViews.add(gameView)
        }
    }

    private fun showInfo(game: Game): Boolean {
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.getAlertDialogByTheme(Storage.shared.theme(this))
        )
        builder
            .setTitle("Game Info")
            .setMessage("Name: ${game.nameEn}\nRecommended by society: ${if (game.recommendedByCommunity) "yes" else "no"}")
            .setCancelable(true)
        val alert = builder.create()
        AndroidUtil.showDialog(alert, backMode = BackgroundMode.BLUR, blurView = blurView, activity = this)
        return true
    }

    private fun filterList(search: CharSequence? = null) {
        if (search != null && search.isNotBlank()) {
            GlobalScene.shared.games.forEachIndexed { i, game ->
                if (!game.getNameByLanguage(resources.configuration.locale.language)!!.contains(search, ignoreCase = true)) {
                    gamesViews[i].visibility = View.GONE
                } else {
                    gamesViews[i].visibility = View.VISIBLE
                }
            }
            serverDivider.visibility = View.VISIBLE
            serverLabel.visibility = View.VISIBLE
            serverScroll.visibility = View.VISIBLE
            serverList.visibility = View.VISIBLE
            // TODO: games from server
            // val view = AndroidUtil.createButtonView(this)
            // view.text = search
            // serverList.addView(view)
        } else {
            serverDivider.visibility = View.GONE
            serverLabel.visibility = View.GONE
            serverList.removeAllViews()
            serverList.visibility = View.GONE
            serverScroll.visibility = View.GONE
            searchView.compoundDrawables[Constants.drawableEnd].setVisible(false, true)
            gamesViews.map { it.visibility = View.VISIBLE }
        }
    }

    private fun askForTutorialDialog() {
        val builder = AlertDialog.Builder(
            this, ThemeController.shared.getAlertDialogByTheme(Storage.shared.theme(this))
        )
        builder
            .setTitle(R.string.welcome)
            .setMessage(R.string.wanna_see_tutorial)
            .setPositiveButton(R.string.yep) { dialog: DialogInterface, id: Int ->
                TutorialScene.shared.start(this)
            }
            .setNegativeButton(R.string.no_i_am_pro) { dialog: DialogInterface, id: Int ->
            }
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog)
    }
}