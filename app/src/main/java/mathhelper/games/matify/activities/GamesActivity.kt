package mathhelper.games.matify.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import eightbitlab.com.blurview.BlurView
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.R
import mathhelper.games.matify.TutorialScene
import mathhelper.games.matify.common.*
import mathhelper.games.matify.game.GameResult
import java.util.*

class GamesActivity: AppCompatActivity(), ConnectionListener, LogStateListener {
    private val TAG = "GamesActivity"
    lateinit var gamesList: LinearLayout
    private lateinit var searchView: EditText
    private lateinit var gameDivider: View
    private lateinit var serverDivider: View
    private lateinit var serverLabel: TextView
    private lateinit var serverList: LinearLayout
    private lateinit var serverNotFound: TextView
    private lateinit var progress: ProgressBar
    private lateinit var offline: TextView
    private lateinit var unsavedData: TextView
    //private var askForTutorial = false
    lateinit var blurView: BlurView
    var currentLongClicked: String = ""
    var alertInfo: AlertDialog? = null
    val isLoading: Boolean
        get() = progress.visibility == View.VISIBLE

    fun setLoading(flag: Boolean) {
        progress.visibility = if (flag) View.VISIBLE else View.INVISIBLE
        gameDivider.visibility = if (flag) View.INVISIBLE else View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        AndroidUtil.setLanguage(this)
        setTheme(ThemeController.shared.currentTheme.resId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)
        progress = findViewById(R.id.progress)
        gameDivider = findViewById(R.id.divider_bottom)
        setLoading(true)
        GlobalScene.shared.gamesActivity = this
        gamesList = findViewById(R.id.games_list)
        gameDivider = findViewById(R.id.divider_bottom)
        searchView = findViewById(R.id.search)
        searchView.compoundDrawables[Constants.drawableEnd].setVisible(false, true)
        serverDivider = findViewById(R.id.server_divider)
        serverLabel = findViewById(R.id.server_games_label)
        serverList = findViewById(R.id.server_games_list)
        serverNotFound = findViewById(R.id.server_not_found)
        blurView = findViewById(R.id.blurView)
        offline = findViewById(R.id.offline)
        offline.visibility = View.GONE
        unsavedData = findViewById(R.id.unsaved_data)
        unsavedData.visibility = View.GONE
        initSwipeRefresher()
        if (Build.VERSION.SDK_INT < 24) {
            val settings = findViewById<TextView>(R.id.settings)
            settings.text = "\uD83D\uDD27"
        }
        GlobalScene.shared.parseDefaultAndLoadedGames()
        ConnectionChecker.shared.subscribe(this)
        Request.subscribe(this)
    }

    override fun onResume() {
        super.onResume()
        serverDivider.visibility = View.GONE
        serverLabel.visibility = View.GONE
        serverList.visibility = View.GONE
        serverNotFound.visibility = View.GONE
        /* TODO: decide what to do with tutorial
        if (askForTutorial) {
            askForTutorialDialog()
            askForTutorial = false
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        ConnectionChecker.shared.unsubscribe(this)
        Request.unsubscribe()
    }

    override fun onLogStateChange(haveUnsavedData: Boolean) {
        runOnUiThread {
            unsavedData.visibility = if (haveUnsavedData) View.VISIBLE else View.GONE
        }
    }

    override fun onConnectionChange(type: ConnectionChangeType) {
        runOnUiThread {
            if (type == ConnectionChangeType.ESTABLISHED) {
                offline.visibility = View.GONE
            } else {
                offline.visibility = View.VISIBLE
            }
            generateList()
        }
    }

    override fun connectionBannerClicked(v: View?) {
        ConnectionChecker.shared.connectionBannerClicked(this, blurView, ActivityType.GAMES)
    }

    override fun connectionButtonClick(v: View) {
        ConnectionChecker.shared.connectionButtonClick(this, v)
    }

    fun search(v: View?) {
        if (!isLoading) {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    fun settings(v: View?) {
        if (!isLoading) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun initSwipeRefresher() {
        val refresher = findViewById<SwipeRefreshLayout>(R.id.refresher)
        refresher.setOnRefreshListener {
            refresher.isRefreshing = false
            if (!isLoading && ConnectionChecker.shared.isConnected) {
                setLoading(true)
                Toast.makeText(this, R.string.refresh_message, Toast.LENGTH_LONG).show()
                GlobalScene.shared.refreshGames()
                // TODO: refresh stats??
            }
        }
    }

    fun updateResult(newRes: GameResult?) {
        val i = GlobalScene.shared.currentGameIndex
        val view = gamesList[i] as TextView
        val pin = if (GlobalScene.shared.currentGame?.isPinned == true) "📌 " else ""
        view.text = "$pin${GlobalScene.shared.currentGame?.getNameByLanguage(resources.configuration.locale.language)}" +
            if (newRes != null) {
                "\n${newRes.toString().format(GlobalScene.shared.currentGame?.tasks?.size)}"
            } else {
                ""
            }
        GlobalScene.shared.currentGame?.lastResult = newRes
        GlobalScene.shared.currentGame?.save(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun generateList() {
        if (GlobalScene.shared.gameOrder.isEmpty()) {
            setLoading(false)
            return
        }
        gamesList.removeAllViews()
        GlobalScene.shared.gameOrder.forEachIndexed { i, code ->
            val gameView = GlobalScene.shared.gameMap[code]?.let { game ->
                AndroidUtil.generateGameView(this, game,
                    onClick = {
                        if (!isLoading) {
                            GlobalScene.shared.currentGameIndex = i
                        }
                    }, onLongClick = {
                        currentLongClicked = game.code
                        alertInfo = AndroidUtil.showGameInfo(this, game, blurView)
                        true
                    })
            }
            gamesList.addView(gameView ?: return)
        }
        Storage.shared.saveOrder(GlobalScene.shared.gameOrder)
        setLoading(false)
    }

    fun removeGame(v: View) {
        GlobalScene.shared.removeGame(currentLongClicked)
        alertInfo?.dismiss()
    }

    fun pinGame(v: View) {
        GlobalScene.shared.pinGame(currentLongClicked)
        alertInfo?.dismiss()
    }

//    private fun askForTutorialDialog() {
//        val builder = AlertDialog.Builder(
//            this, ThemeController.shared.alertDialogTheme
//        )
//        builder
//            .setTitle(R.string.welcome)
//            .setMessage(R.string.wanna_see_tutorial)
//            .setPositiveButton(R.string.yep) { dialog: DialogInterface, id: Int ->
//                TutorialScene.shared.start(this)
//            }
//            .setNegativeButton(R.string.no_i_am_pro) { dialog: DialogInterface, id: Int ->
//            }
//        val dialog = builder.create()
//        AndroidUtil.showDialog(dialog)
//    }
}