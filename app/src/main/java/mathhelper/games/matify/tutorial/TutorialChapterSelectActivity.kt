package mathhelper.games.matify.tutorial

import mathhelper.games.matify.common.Logger
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mathhelper.games.matify.R
import mathhelper.games.matify.TutorialScene
import mathhelper.games.matify.common.Storage

class TutorialChapterSelectActivity: AppCompatActivity() {
    private val TAG = "TutorialChapterSelectActivity"
    private lateinit var overviewButton: TableRow
    private lateinit var playButton: TableRow
    private lateinit var multiselectButton: TableRow
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt())
        setContentView(R.layout.tutorial_activity_select)
        setViews()

        overviewButton.visibility = View.GONE
        playButton.visibility = View.GONE
        multiselectButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        val chapterSelectActivity = this
        lifecycleScope.launch {
            TutorialScene.shared.loadTutorialLevels(chapterSelectActivity)
            overviewButton.visibility = View.VISIBLE
            playButton.visibility = View.VISIBLE
            multiselectButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

    private fun setViews() {
        overviewButton = findViewById(R.id.overview)
        playButton = findViewById(R.id.playable)
        multiselectButton = findViewById(R.id.multiselect)
        progressBar = findViewById(R.id.progress)
    }

    fun startOverviewTutorial(v: View?) {
        TutorialScene.shared.startSession(
            this,
            TutorialGamesActivity.totalSteps + TutorialLevelsActivity.totalSteps,
            TutorialGamesActivity::class.java,
            TutorialLevelsActivity::class.java
        )
    }

    fun startPlayableActivityTutorial(v: View?) {
        TutorialScene.shared.startSession(
            this,
            TutorialPlayActivity.totalSteps,
            TutorialPlayActivity::class.java
        )
    }

    fun startMultiselectTutorial(v: View?) {
        TutorialScene.shared.startSession(
            this,
            TutorialMultiselectActivity.totalSteps,
            TutorialMultiselectActivity::class.java
        )
    }

    fun back(v: View?) {
        finish()
    }
}