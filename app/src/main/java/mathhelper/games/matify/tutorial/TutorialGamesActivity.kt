package mathhelper.games.matify.tutorial

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import mathhelper.games.matify.R
import mathhelper.games.matify.TutorialScene
import mathhelper.games.matify.TutorialSceneListener
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.BackgroundMode
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.common.Storage
import java.lang.ref.WeakReference

class TutorialGamesActivity : AppCompatActivity(), TutorialSceneListener {
    private val TAG = "TutorialGamesActivity"
    private lateinit var pointer: TextView

    companion object {
        val totalSteps = 2
    }

    private val steps = arrayOf(
        this::tellAboutGameLayout,
        this::waitForGameClick
    )
    private var currentStep = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt())
        setContentView(R.layout.tutorial_activity_games)
        pointer = findViewById(R.id.pointer_game)
        TutorialScene.shared.createLeaveDialog(this)
        TutorialScene.shared.createTutorialDialog(this)

        TutorialScene.shared.listenerRef = WeakReference(this)
        if (TutorialScene.shared.currentlyAdvancing) {
            currentStep = -1
            nextStep()
        } else {
            currentStep = steps.size
            prevStep()
        }
    }

    override fun onBackPressed() {
        TutorialScene.shared.leaveDialog?.let {
            AndroidUtil.showDialog(it)
        }
    }

    override fun nextStep(): Boolean {
        currentStep++
        if (currentStep == steps.size) {
            return false
        }
        TutorialScene.shared.updateDialog(resources.getString(R.string.tutorial))
        steps[currentStep]()
        return true
    }

    override fun prevStep(): Boolean {
        currentStep--
        if (currentStep == -1) {
            return false
        }
        TutorialScene.shared.updateDialog(resources.getString(R.string.tutorial))
        steps[currentStep]()
        return true
    }

    fun startTutorial(v: View?) {
        TutorialScene.shared.nextStep(this)
    }

    fun tellAboutGameLayout() {
        Logger.d(TAG, "tellAboutGameLayout")
        TutorialScene.shared.tutorialDialog?.let {
            it.setMessage(resources.getString(R.string.games_activity_tutorial))
            AndroidUtil.showDialog(it, backMode = BackgroundMode.NONE)
        }
    }

    fun waitForGameClick() {
        Logger.d(TAG, "waitForGameClick")
        pointer.visibility = View.VISIBLE
        TutorialScene.shared.animateLeftUp(pointer)
    }
}