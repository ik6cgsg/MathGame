package mathhelper.games.matify.level

import android.util.Log
import mathhelper.twf.api.expressionToStructureString
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.games.matify.LevelScene
import mathhelper.games.matify.common.Logger

data class State(var expression: ExpressionNode, var result: LevelResult)

class History {
    private val TAG = "History"
    var states = ArrayList<State>()

    val empty
        get() = states.isEmpty()

    fun saveState(steps: Double, time: Long, expression: ExpressionNode) {
        Logger.d(TAG, "saveState")
        val state = State(expression, LevelResult(steps, time, StateType.PAUSED, expressionToStructureString(expression)))
        states.add(state)
        LevelScene.shared.updateResult(state.result)
    }

    fun getPreviousStep(): State? {
        Logger.d(TAG, "getPreviousStep")
        if (states.size < 1) {
            LevelScene.shared.updateResult(null)
            return null
        }
        Logger.d(TAG, "$states")
        states.removeAt(states.size - 1)
        val res = states[states.size - 1]
        LevelScene.shared.updateResult(res.result)
        return res
    }

    fun clear() {
        Logger.d(TAG, "clear")
        states.clear()
    }

    fun isUndoable(): Boolean {
        return states.size > 1
    }
}