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
import mathhelper.games.matify.tutorial.TutorialPlayActivity
import mathhelper.games.matify.game.Game
import mathhelper.games.matify.level.Level
import mathhelper.games.matify.tutorial.TutorialGamesActivity
import mathhelper.games.matify.tutorial.TutorialLevelsActivity
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

    val listenerClasses = arrayOf(
        TutorialGamesActivity::class.java,
        TutorialLevelsActivity::class.java,
        TutorialPlayActivity::class.java
    )
    var currentStep = -1
    private var currentListener = -1

    lateinit var currentLevel: Level
    var currLevelIndex = 0

    var tutorialDialog: AlertDialog? = null
    var leaveDialog: AlertDialog? = null
    var tutorialGame: Game? = null

    private var currentAnim: AnimatorSet? = null
    private var currentAnimViewRef: WeakReference<View> = WeakReference(null)

    fun start(context: Context) {
        GlobalScene.shared.tutorialProcessing = true
        val input = context.assets.open("tutorial.json")
        val text = input.readBytes().toString(Charsets.UTF_8)
        input.close()
        val info = Gson().fromJson(text, TasksetInfo::class.java)
        tutorialGame = Game.create(info, context)
        if (tutorialGame == null) {
            Logger.d(TAG, "failed to load tutorial")
            return
        }
        currentListener = -1
        nextStep(context)
    }

    fun nextStep(context: Context) {
        Logger.d(TAG, "nextStep")
        currentStep++
        if (currentListener == -1) {
            currentListener = 0
            context.startActivity(Intent(context, listenerClasses[currentListener]))
            // activity must call its nextStep() upon creation itself
            // and write itself into listenerRef
            return
        }
        val listener = listenerRef.get() ?: return
        stopAnimation()
        if (listener.nextStep()) {
            return
        }
        listenerRef.clear()
        currentListener++
        if (currentListener != listenerClasses.size) {
            context.startActivity(Intent(context, listenerClasses[currentListener]))
            // activity must call its nextStep() upon creation itself
            // and write itself into listenerRef
        }
        listener.finish()
    }

    fun prevStep(context: Context) {
        Logger.d(TAG, "prevStep")
        currentStep--
        if (currentListener == listenerClasses.size) {
            currentListener--
            context.startActivity(Intent(context, listenerClasses[currentListener]))
            return
        }
        val listener = listenerRef.get() ?: return
        stopAnimation()
        if (listener.prevStep()) {
            return
        }
        listenerRef.clear()
        currentListener--
        if (currentListener != -1) {
            context.startActivity(Intent(context, listenerClasses[currentListener]))
        }
        listener.finish()
    }

    fun totalSteps(): Int {
        return TutorialPlayActivity.totalSteps + TutorialGamesActivity.totalSteps + TutorialLevelsActivity.totalSteps
    }

    fun leave() {
        GlobalScene.shared.tutorialProcessing = false

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
            .setPositiveButton(R.string.yep) { dialog: DialogInterface, id: Int ->
                Handler().postDelayed({
                    nextStep(context)
                }, 100)
            }
            .setNegativeButton(R.string.step_back) { dialog: DialogInterface, id: Int ->
                Handler().postDelayed({
                    prevStep(context)
                }, 100)
            }
            .setNeutralButton(R.string.leave) { dialog: DialogInterface, id: Int ->
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
            .setPositiveButton(R.string.yes) { dialog: DialogInterface, id: Int ->
                leave()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, id: Int ->
            }
        leaveDialog = builder.create()
    }

    fun updateDialog(tutorialStr: String) {
        tutorialDialog?.setTitle(
            "${tutorialStr}: ${currentStep + 1} / ${totalSteps()}"
        )
    }
}