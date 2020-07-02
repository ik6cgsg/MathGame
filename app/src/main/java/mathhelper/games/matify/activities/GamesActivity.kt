package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
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
import mathhelper.games.matify.*
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.Constants
import mathhelper.games.matify.common.Storage
import java.util.*
import kotlin.collections.ArrayList

class GamesActivity: AppCompatActivity() {
    private val TAG = "GamesActivity"
    private lateinit var gamesViews: ArrayList<TextView>
    private lateinit var gamesList: LinearLayout
    private lateinit var searchView: EditText
    private var gameTouched: View? = null
    private lateinit var serverDivider: View
    private lateinit var serverLabel: TextView
    private lateinit var serverList: LinearLayout
    private lateinit var serverScroll: ScrollView
    private var askForTutorial = false

    fun setLanguage() {
        val locale = Locale("en")
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        setLanguage()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun generateList(search: CharSequence? = null) {
        GlobalScene.shared.games.forEachIndexed { i, game ->
            if (search != null) {
                if (!game.getNameByLanguage(resources.configuration.locale.language).contains(search, ignoreCase = true)) {
                    return
                }
            }
            val gameView = AndroidUtil.createButtonView(this)
            gameView.text = game.getNameByLanguage(resources.configuration.locale.language)
            /*
            if (game.lastResult != null) {
                gameView.text = "${game.name}\n${game.lastResult!!}"
            }
            */
            gameView.background = getDrawable(R.drawable.rect_shape)
            gameView.setOnTouchListener { v, event ->
                super.onTouchEvent(event)
                when {
                    event.action == MotionEvent.ACTION_DOWN && gameTouched == null -> {
                        gameTouched = v
                        v.background = getDrawable(R.drawable.rect_shape_clicked)
                    }
                    event.action == MotionEvent.ACTION_UP && gameTouched == v -> {
                        v.background = getDrawable(R.drawable.rect_shape)
                        if (AndroidUtil.touchUpInsideView(v, event)) {
                            GlobalScene.shared.currentGame = game
                        }
                        gameTouched = null
                    }
                    event.action == MotionEvent.ACTION_CANCEL && gameTouched == v -> {
                        v.background = getDrawable(R.drawable.rect_shape)
                        gameTouched = null
                    }
                }
                true
            }
            gamesList.addView(gameView)
            gamesViews.add(gameView)
        }
    }

    private fun filterList(search: CharSequence? = null) {
        if (search != null && search.isNotBlank()) {
            GlobalScene.shared.games.forEachIndexed { i, game ->
                if (!game.getNameByLanguage(resources.configuration.locale.language).contains(search, ignoreCase = true)) {
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
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder
            .setTitle("Welcome!")
            .setMessage("Wanna see tutorial?")
            .setPositiveButton("Yep \uD83D\uDE0D") { dialog: DialogInterface, id: Int ->
                TutorialScene.shared.start(this)
            }
            .setNegativeButton("Nope, I'm pro \uD83D\uDE0E") { dialog: DialogInterface, id: Int ->
            }
        val dialog = builder.create()
        AndroidUtil.showDialog(dialog)
    }
}