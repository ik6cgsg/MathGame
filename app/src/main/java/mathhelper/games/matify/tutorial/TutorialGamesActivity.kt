package mathhelper.games.matify.tutorial

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import mathhelper.games.matify.R
import mathhelper.games.matify.TutorialScene
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.Storage

class TutorialGamesActivity: AppCompatActivity() {
    private val TAG = "TutorialGamesActivity"
    private lateinit var pointer: TextView
    lateinit var dialog: AlertDialog
    lateinit var leave: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt(this))
        setContentView(R.layout.tutorial_activity_games)
        pointer = findViewById(R.id.pointer_game)
        dialog = TutorialScene.shared.createTutorialDialog(this)
        leave = TutorialScene.shared.createLeaveDialog(this)
        TutorialScene.shared.tutorialGamesActivity = this
    }

    override fun onBackPressed() {
        AndroidUtil.showDialog(leave)
    }

    override fun finish() {
        TutorialScene.shared.tutorialGamesActivity = null
        super.finish()
    }

    fun startTutorial(v: View?) {
        startActivity(Intent(this, TutorialLevelsActivity::class.java))
    }

    fun tellAboutGameLayout() {
        Log.d(TAG, "tellAboutGameLayout")
        dialog.setMessage(resources.getString(R.string.games_activity_tutorial))
        AndroidUtil.showDialog(dialog, false)
    }

    fun waitForGameClick() {
        Log.d(TAG, "waitForGameClick")
        pointer.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointer)
    }
}