package mathhelper.games.matify.tutorial

import mathhelper.games.matify.common.Logger
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import mathhelper.games.matify.R
import mathhelper.games.matify.TutorialScene

class TutorialChapterSelectActivity: AppCompatActivity() {
    private val TAG = "TutorialChapterSelectActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tutorial_activity_select)
        TutorialScene.shared.loadTutorialLevels(this)
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