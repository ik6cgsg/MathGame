package mathhelper.games.matify.tutorial

import android.os.Bundle
import android.view.View
import android.widget.Button
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

class TutorialLevelsActivity : AppCompatActivity(), TutorialSceneListener {
    private val TAG = "TutorialLevelsActivity"
    private lateinit var pointer: TextView
    lateinit var button: Button

    companion object {
        const val totalSteps = 2
    }
    val steps = arrayOf(
        this::tellAboutLevelLayout,
        this::waitForLevelClick
    )
    private var currentStep = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setTheme(Storage.shared.themeInt())
        setContentView(R.layout.tutorial_activity_levels)
        pointer = findViewById(R.id.pointer_level)
        TutorialScene.shared.createTutorialDialog(this)
        TutorialScene.shared.createLeaveDialog(this)
        button = findViewById(R.id.tutorial_level)

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
        back(null)
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

    fun back(v: View?) {
        TutorialScene.shared.leaveDialog?.let {
            AndroidUtil.showDialog(it)
        }
    }

    fun startLevel(v: View?) {
        TutorialScene.shared.nextStep(this)
    }

    private fun tellAboutLevelLayout() {
        Logger.d(TAG, "tellAboutLevelLayout")
        TutorialScene.shared.tutorialDialog?.let {
            it.setMessage(resources.getString(R.string.level_activity_tutorial))
            AndroidUtil.showDialog(it, backMode = BackgroundMode.NONE)
        }
    }

    private fun waitForLevelClick() {
        Logger.d(TAG, "waitForLevelClick")
        TutorialScene.shared.animateLeftUp(pointer)
    }
}