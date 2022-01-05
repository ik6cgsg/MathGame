package mathhelper.games.matify.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.JsonWriter
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.core.view.size
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import eightbitlab.com.blurview.BlurView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import mathhelper.games.matify.R
import java.util.*
import kotlinx.coroutines.launch
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.SearchType
import mathhelper.games.matify.common.*
import mathhelper.games.matify.game.Game

class SearchActivity: AppCompatActivity(), SearchView.OnQueryTextListener, ConnectionListener {
    private val TAG = "SearchActivity"
    private lateinit var searchView: SearchView
    private lateinit var divider: View
    private lateinit var progress: ProgressBar
    private lateinit var localGamesList: LinearLayout
    private lateinit var serverGamesList: LinearLayout
    private lateinit var offline: TextView
    private lateinit var localNotFound: TextView
    private lateinit var serverNotFound: TextView
    private var serverGamesCodes = arrayListOf<String>()
    lateinit var blurView: BlurView
    private var currentQuery: String? = null
    private var searchType = SearchType.BY_NAME
    private val isLoading: Boolean
        get() = progress.visibility == View.VISIBLE
    private var alertInfo: AlertDialog? = null
    private var currentLongClickedGame: Game? = null
    private var currentLongClickedView: View? = null

    private fun setLoading(flag: Boolean) {
        progress.visibility = if (flag) View.VISIBLE else View.INVISIBLE
        divider.visibility = if (flag) View.INVISIBLE else View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        AndroidUtil.setLanguage(this)
        setTheme(ThemeController.shared.currentTheme.resId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        divider = findViewById(R.id.divider)
        progress = findViewById(R.id.progress)
        localGamesList = findViewById(R.id.games_list)
        serverGamesList = findViewById(R.id.server_games_list)
        searchView = findViewById(R.id.search)
        searchView.setOnQueryTextListener(this)
        val id = searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val query = searchView.findViewById<TextView>(id)
        query.typeface = Typeface.MONOSPACE
        query.setTextColor(ThemeController.shared.color(ColorName.TEXT_COLOR))
        query.setHintTextColor(ThemeController.shared.color(ColorName.TEXT_COLOR))
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                searchType = when (tab?.position) {
                    0 -> SearchType.BY_NAME
                    1 -> SearchType.BY_CODE
                    2 -> SearchType.BY_SPACE
                    else -> SearchType.BY_NAME
                }
                filter(currentQuery)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        offline = findViewById(R.id.offline)
        offline.visibility = View.GONE
        blurView = findViewById(R.id.blurView)
        localNotFound = findViewById(R.id.local_not_found)
        localNotFound.visibility = View.GONE
        serverNotFound = findViewById(R.id.server_not_found)
        serverNotFound.visibility = View.GONE
        ConnectionChecker.shared.subscribe(this)
        filter()
    }

    fun back(v: View?) {
        filter()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        //Storage.shared.clearSpecifiedGames(serverGamesCodes)
        ConnectionChecker.shared.unsubscribe(this)
    }

    override fun onConnectionChange(type: ConnectionChangeType) {
        runOnUiThread {
            if (type == ConnectionChangeType.ESTABLISHED) {
                offline.visibility = View.GONE
            } else {
                offline.visibility = View.VISIBLE
            }
            filter(currentQuery)
        }
    }

    override fun connectionBannerClicked(v: View?) {
        ConnectionChecker.shared.connectionBannerClicked(this, blurView, ActivityType.SEARCH)
    }

    override fun connectionButtonClick(v: View) {
        ConnectionChecker.shared.connectionButtonClick(this, v)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        currentQuery = query
        searchView.clearFocus()
        filter(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        currentQuery = newText
        filter(currentQuery)
        return false
    }

    private fun filter(search: String? = null) {
        setLoading(true)
        if (!search.isNullOrBlank()) {
            filterLocal(search)
            filterServer(search)
        } else {
            GlobalScene.shared.cancelActiveJobs()
            //Storage.shared.clearSpecifiedGames(serverGamesCodes)
            localGamesList.removeAllViews()
            serverGamesList.removeAllViews()
            localNotFound.visibility = View.VISIBLE
            serverNotFound.visibility = View.VISIBLE
            setLoading(false)
        }
    }

    private fun filterLocal(search: String? = null) {
        localGamesList.removeAllViews()
        val q = search ?: return
        val isOk: (Game) -> Boolean = when (searchType) {
            SearchType.BY_NAME -> { g: Game -> g.nameEn.contains(q, ignoreCase = true) || g.nameRu.contains(q, ignoreCase = true) }
            SearchType.BY_CODE -> { g: Game -> g.code.contains(q, ignoreCase = true) }
            SearchType.BY_SPACE -> { g: Game -> g.namespaceCode.contains(q, ignoreCase = true) }
        }
        GlobalScene.shared.gameOrder.forEachIndexed { i, code ->
            val game = GlobalScene.shared.gameMap[code] ?: return
            if (isOk(game)) {
                val gameView = AndroidUtil.generateGameView(this, game, onClick = {
                    if (!isLoading) {
                        finish()
                        GlobalScene.shared.currentGameIndex = i
                    }
                }, onLongClick = { false })
                localGamesList.addView(gameView)
            }
        }
        localNotFound.visibility = if (localGamesList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun filterServer(search: String) {
        serverGamesList.removeAllViews()
        GlobalScene.shared.cancelActiveJobs()
        val currentGameCodes = GlobalScene.shared.gameOrder
        var serverGames = arrayListOf<Game>()
        GlobalScene.shared.requestGamesByParams(serverGames, searchType, search, success = {
            serverGames = ArrayList(serverGames.filter { it.code !in currentGameCodes })
            serverGamesCodes = ArrayList(serverGames.map { it.code })
            serverNotFound.visibility = if (serverGames.isEmpty()) View.VISIBLE else View.GONE
            serverGames.forEach { game ->
                val view = AndroidUtil.generateGameView(this, game, onClick = {
                    if (!isLoading) {
                        GlobalScene.shared.currentGameIndex = GlobalScene.shared.addGame(game)
                        finish()
                    }
                }, onLongClick = {
                    currentLongClickedGame = game
                    currentLongClickedView = it
                    alertInfo = AndroidUtil.showGameInfo(this, game, blurView, true)
                    true
                })
                serverGamesList.addView(view)
            }
            setLoading(false)
        }, error = {
            setLoading(false)
            serverNotFound.visibility = View.VISIBLE
        }, toastError = false)
    }

    fun addGame(v: View) {
        alertInfo?.dismiss()
        alertInfo = null
        GlobalScene.shared.addGame(currentLongClickedGame ?: return)
        filterLocal(currentQuery)
        serverGamesList.removeView(currentLongClickedView ?: return)
    }
}