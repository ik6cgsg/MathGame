package mathhelper.games.matify.tutorial

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import mathhelper.games.matify.R
import mathhelper.games.matify.TutorialScene
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.Storage

class TutorialLevelsActivity: AppCompatActivity() {
    private val TAG = "TutorialLevelsActivity"
    private lateinit var pointer: TextView
    lateinit var dialog: AlertDialog
    lateinit var leave: AlertDialog
    lateinit var button: Button
    lateinit var progress: ProgressBar
    var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt(this))
        setContentView(R.layout.tutorial_activity_levels)
        pointer = findViewById(R.id.pointer_level)
        dialog = TutorialScene.shared.createTutorialDialog(this)
        leave = TutorialScene.shared.createLeaveDialog(this)
        button = findViewById(R.id.tutorial_level)
        button.visibility = View.GONE
        progress = findViewById(R.id.progress)
        loading = true
        TutorialScene.shared.tutorialLevelsActivity = this
    }

    override fun onBackPressed() {
        back(null)
    }

    override fun finish() {
        TutorialScene.shared.tutorialLevelsActivity = null
        TutorialScene.shared.leaveDialog = TutorialScene.shared.tutorialGamesActivity!!.leave
        super.finish()
    }

    fun back(v: View?) {
        if (!loading) {
            AndroidUtil.showDialog(leave)
        }
    }

    fun onLoad() {
        progress.visibility = View.GONE
        button.visibility = View.VISIBLE
    }

    fun startLevel(v: View?) {
        startActivity(Intent(this, TutorialPlayActivity::class.java))
    }

    fun tellAboutLevelLayout() {
        Log.d(TAG, "tellAboutLevelLayout")
        dialog.setMessage(resources.getString(R.string.level_activity_tutorial))
        AndroidUtil.showDialog(dialog, false)
    }

    fun waitForLevelClick() {
        Log.d(TAG, "waitForLevelClick")
        pointer.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointer)
    }
}