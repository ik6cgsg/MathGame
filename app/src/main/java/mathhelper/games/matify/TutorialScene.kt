package mathhelper.games.matify

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import android.view.View
import com.google.gson.Gson
import mathhelper.games.matify.common.*
import mathhelper.games.matify.game.Game
import mathhelper.games.matify.level.Level
import java.lang.ref.WeakReference

interface TutorialSceneListener {
    fun nextStep(): Boolean
    fun prevStep(): Boolean
    fun finish()
}

class TutorialScene {
    companion object {
        private const val TAG = "TutorialScene"
        val shared: TutorialScene = TutorialScene()
        private const val duration = 600.toLong()
        private const val translate = -30f
    }

    var listenerRef: WeakReference<TutorialSceneListener> = WeakReference(null)

    private var currentSession: Array<out Class<*>> = arrayOf()
    var totalSteps: Int = 0
    var currentStep = -1
    private var currentListener = -1
    var currentlyAdvancing = true

    lateinit var currentLevel: Level
    var currLevelIndex = 0

    var tutorialDialog: AlertDialog? = null
    var leaveDialog: AlertDialog? = null
    var restartDialog: AlertDialog? = null
    var tutorialGame: Game? = null

    private var currentAnim: AnimatorSet? = null
    private var currentAnimViewRef: WeakReference<View> = WeakReference(null)

    fun loadTutorialLevels(context: Context) {
        Logger.d(TAG, "loadTutorialLevels")
        val input = context.assets.open("tutorial.json")
        val text = input.readBytes().toString(Charsets.UTF_8)
        input.close()
        val info = Gson().fromJson(text, TasksetInfo::class.java)
        tutorialGame = Game.create(info, context)
        if (tutorialGame == null) {
            Logger.d(TAG, "failed to load tutorial")
            return
        }
        tutorialGame?.load(context)
    }

    fun startSession(context: Context, stepsInSession: Int, vararg activitiesToLaunch: Class<*>) {
        leave()
        totalSteps = stepsInSession
        currentSession = activitiesToLaunch
        currentStep = -1
        currentListener = -1
        nextStep(context)
    }

    fun nextStep(context: Context) {
        Logger.d(TAG, "nextStep")
        currentlyAdvancing = true
        currentStep++
        if (currentListener == -1) {
            currentListener = 0
            context.startActivity(Intent(context, currentSession[currentListener]))
            return
        }
        val listener = listenerRef.get() ?: return
        stopAnimation()
        if (listener.nextStep()) {
            return
        }
        currentListener++
        if (currentListener != currentSession.size) {
            listenerRef.clear()
            listener.finish()
            context.startActivity(Intent(context, currentSession[currentListener]))
        } else {
            AndroidUtil.showDialog(createFinishDialog(context), backMode = BackgroundMode.NONE)
        }
    }

    fun prevStep(context: Context) {
        currentlyAdvancing = false
        Logger.d(TAG, "prevStep")
        currentStep--
        if (currentListener == currentSession.size) {
            currentListener--
            context.startActivity(Intent(context, currentSession[currentListener]))
            return
        }
        val listener = listenerRef.get() ?: return
        stopAnimation()
        if (listener.prevStep()) {
            return
        }
        currentListener--
        if (currentListener != -1) {
            listenerRef.clear()
            context.startActivity(Intent(context, currentSession[currentListener]))
            listener.finish()
        } else {
            leave()
        }
    }

    fun leave() {
        listenerRef.get()?.finish()
        listenerRef.clear()
    }

    fun restart(context: Context) {
        listenerRef.get()?.finish()
        listenerRef.clear()
        currentStep = -1
        currentListener = -1
        nextStep(context)
    }

    fun switchLevel(num: Int) {
        currLevelIndex = num
        val game = tutorialGame?:return
        currentLevel = game.levels[num]
    }

    fun animateLeftUp(view: View) {
        val animationX = ObjectAnimator.ofFloat(view, "translationX", translate)
        animationX.repeatMode = ValueAnimator.REVERSE
        animationX.repeatCount = ValueAnimator.INFINITE
        val animationY = ObjectAnimator.ofFloat(view, "translationY", translate)
        animationY.repeatMode = ValueAnimator.REVERSE
        animationY.repeatCount = ValueAnimator.INFINITE
        val set = AnimatorSet()
        set.play(animationX)
            .with(animationY)
        set.duration = duration
        set.start()
        currentAnim = set
        currentAnimViewRef = WeakReference(view)
        view.visibility = View.VISIBLE
    }

    fun animateUp(view: View) {
        val animationY = ObjectAnimator.ofFloat(view, "translationY", translate)
        animationY.repeatMode = ValueAnimator.REVERSE
        animationY.repeatCount = ValueAnimator.INFINITE
        val set = AnimatorSet()
        set.play(animationY)
        set.duration = duration
        set.start()
        currentAnim = set
        currentAnimViewRef = WeakReference(view)
        view.visibility = View.VISIBLE
    }

    fun stopAnimation() {
        currentAnim?.let {
            it.removeAllListeners()
            it.end()
            it.cancel()
            currentAnim = null
            val currentAnimView = currentAnimViewRef.get() ?: return
            currentAnimView.translationY = 0f
            currentAnimView.translationX = 0f
            currentAnimView.visibility = View.GONE
        }
    }

    fun createTutorialDialog(context: Context) {
        val builder = AlertDialog.Builder(
            context, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle("")
            .setMessage(R.string.got_it)
            .setPositiveButton(R.string.yep) { _: DialogInterface, _: Int ->
                Handler().postDelayed({
                    nextStep(context)
                }, 100)
            }
            .setNegativeButton(R.string.step_back) { _: DialogInterface, _: Int ->
                Handler().postDelayed({
                    prevStep(context)
                }, 100)
            }
            .setNeutralButton(R.string.leave) { _: DialogInterface, _: Int ->
                if (leaveDialog != null) {
                    AndroidUtil.showDialog(leaveDialog!!)
                } else {
                    leave()
                }
            }
            .setCancelable(false)
        tutorialDialog = builder.create()
    }

    fun createLeaveDialog(context: Context) {
        Logger.d(TAG, "createLeaveDialog")
        val builder = AlertDialog.Builder(
            context, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.attention)
            .setMessage(R.string.wanna_leave)
            .setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
                Handler().postDelayed({
                    leave()
                }, 100)
            }
            .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int ->
            }
        leaveDialog = builder.create()
    }

    fun createRestartDialog(context: Context) {
        Logger.d(TAG, "createRestartDialog")
        val builder = AlertDialog.Builder(
            context, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.attention)
            .setMessage(R.string.restart_tutorial)
            .setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
                Handler().postDelayed({
                    restart(context)
                }, 100)
            }
            .setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int ->
            }
        restartDialog = builder.create()
    }

    fun createFinishDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(
            context, ThemeController.shared.alertDialogTheme
        )
        builder
            .setTitle(R.string.tutorial_chapter_finished)
            .setMessage(R.string.tutorial_on_level_seems)
            .setPositiveButton(R.string.tutorial_on_level_i_am_pro) { _: DialogInterface, _: Int ->
                leave()
            }
            .setNegativeButton(R.string.restart_info) { _: DialogInterface, _: Int ->
                restart(context)
            }
            .setCancelable(false)
        return builder.create()
    }

    fun updateDialog(tutorialStr: String) {
        tutorialDialog?.setTitle(
            "${tutorialStr}: ${currentStep + 1} / ${totalSteps}"
        )
    }
}