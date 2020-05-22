package spbpu.hsamcp.mathgame.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import spbpu.hsamcp.mathgame.GlobalScene
import spbpu.hsamcp.mathgame.R
import spbpu.hsamcp.mathgame.common.AndroidUtil
import spbpu.hsamcp.mathgame.common.AuthInfo
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.statistics.Statistics
import java.util.*
import kotlin.collections.ArrayList

class GamesActivity: AppCompatActivity() {
    private val TAG = "GamesActivity"
    private lateinit var gamesViews: ArrayList<TextView>
    private lateinit var gamesList: LinearLayout
    private lateinit var searchView: EditText
    private var gameTouched: View? = null
    private lateinit var signInDialog: AlertDialog
    private lateinit var serverDivider: View
    private lateinit var serverLabel: TextView
    private lateinit var serverList: LinearLayout
    private lateinit var serverScroll: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(Constants.serverId)
            .build()
        GlobalScene.shared.googleSignInClient = GoogleSignIn.getClient(this, gso)
        val prefs = getSharedPreferences(Constants.storage, MODE_PRIVATE)
        if (!prefs.contains(Constants.deviceId)) {
            val prefEdit = prefs.edit()
            prefEdit.putString(Constants.deviceId, UUID.randomUUID().toString())
            prefEdit.commit()
        }
        if (!prefs.getBoolean(AuthInfo.AUTHORIZED.str, false)) {
            //AndroidUtil.showDialog(signInDialog)
            startActivity(Intent(this, AuthActivity::class.java))
        }
        GlobalScene.shared.gamesActivity = this
        gamesViews = ArrayList()
        gamesList = findViewById(R.id.games_list)
        searchView = findViewById(R.id.search)
        searchView.compoundDrawables[Constants.drawableRight].setVisible(false, false)
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
        signInDialog = createSignAlert()
    }

    override fun onResume() {
        super.onResume()
        serverDivider.visibility = View.GONE
        serverLabel.visibility = View.GONE
        serverList.visibility = View.GONE
        serverScroll.visibility = View.GONE
    }

    fun settings(v: View?) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSearchEngine() {
        searchView.setOnTouchListener { v, event ->
            var res = false
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = searchView.compoundDrawables[Constants.drawableRight]
                if (drawableRight != null) {
                    if (event.x >= (searchView.width - searchView.paddingRight - drawableRight.intrinsicWidth)) {
                        searchView.text.clear()
                        searchView.clearFocus()
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.hideSoftInputFromWindow(this.window.decorView.windowToken, 0)
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
                searchView.compoundDrawables[Constants.drawableRight].setVisible(true, false)
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
                if (!game.name.contains(search, ignoreCase = true)) {
                    return
                }
            }
            val gameView = AndroidUtil.createButtonView(this)
            gameView.text = game.name
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
                if (!game.name.contains(search, ignoreCase = true)) {
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
            val view = AndroidUtil.createButtonView(this)
            view.text = search
            serverList.addView(view)
        } else {
            serverDivider.visibility = View.GONE
            serverLabel.visibility = View.GONE
            serverList.removeAllViews()
            serverList.visibility = View.GONE
            serverScroll.visibility = View.GONE
            searchView.compoundDrawables[Constants.drawableRight].setVisible(false, false)
            gamesViews.map { it.visibility = View.VISIBLE }
        }
    }

    private fun createSignAlert(): AlertDialog {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        val view = layoutInflater.inflate(R.layout.dialog_sign_in, null)
        builder
            .setView(view)
            .setTitle("Please sign in")
            .setPositiveButton("Sign in") { dialog: DialogInterface, id: Int -> }
            .setNegativeButton("Exit") { dialog: DialogInterface, id: Int ->
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(homeIntent)
            }
            .setCancelable(false)
        val dialog = builder.create()
        setOnSignClick(dialog, view)
        return dialog
    }

    private fun setOnSignClick(dialog: AlertDialog, view: View) {
        val loginView = view.findViewById<EditText>(R.id.login)
        val nameView = view.findViewById<EditText>(R.id.name)
        val surnameView = view.findViewById<EditText>(R.id.surname)
        val secondNameView = view.findViewById<EditText>(R.id.secondName)
        val groupView = view.findViewById<EditText>(R.id.group)
        val institutionView = view.findViewById<EditText>(R.id.institution)
        val ageView = view.findViewById<EditText>(R.id.additional)
        val statisticsView = view.findViewById<Switch>(R.id.statistics)
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                if (loginView.text.isBlank() || nameView.text.isBlank() ||
                    surnameView.text.isBlank()) {
                    Toast.makeText(this, "Please, fill required fields!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val prefs = getSharedPreferences(Constants.storage, Context.MODE_PRIVATE)
                val prefEdit = prefs.edit()
                if (ageView.text.toString().isNotBlank()) {
                    try {
                        val age = ageView.text.toString().toInt()
                        if (age !in 10..100) {
                            Toast.makeText(this, "Appearances are deceptive...", Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        } else {
                            prefEdit.putInt(AuthInfo.AGE.str, age)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Are you sure that's your age (even not integer...)?", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                }
                prefEdit.putString(AuthInfo.LOGIN.str, loginView.text.toString())
                prefEdit.putString(AuthInfo.NAME.str, nameView.text.toString())
                prefEdit.putString(AuthInfo.SURNAME.str, surnameView.text.toString())
                prefEdit.putString(AuthInfo.SECOND_NAME.str, secondNameView.text.toString())
                prefEdit.putString(AuthInfo.GROUP.str, groupView.text.toString())
                prefEdit.putString(AuthInfo.INSTITUTION.str, institutionView.text.toString())
                //prefEdit.putBoolean(AuthInfo.STATISTICS.str, statisticsView.isChecked)
                prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, true)
                GlobalScene.shared.generateGamesMultCoeffs(prefEdit)
                prefEdit.commit()
                Statistics.logSign(this)
                dialog.dismiss()
            }
        }
    }


}