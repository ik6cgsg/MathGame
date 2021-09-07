package mathhelper.games.matify.level

import android.util.Log
import api.expressionToStructureString
import expressiontree.ExpressionNode
import mathhelper.games.matify.LevelScene

data class State(var expression: ExpressionNode, var result: LevelResult)

class History {
    private val TAG = "History"
    var states = ArrayList<State>()

    val empty
        get() = states.isEmpty()

    fun saveState(steps: Double, time: Long, expression: ExpressionNode) {
        Log.d(TAG, "saveState")
        val state = State(expression, LevelResult(steps, time, StateType.PAUSED, expressionToStructureString(expression)))
        states.add(state)
        LevelScene.shared.levelsActivity?.updateResult(state.result)
    }

    fun getPreviousStep(): State? {
        Log.d(TAG, "getPreviousStep")
        if (states.size < 1) {
            LevelScene.shared.levelsActivity?.updateResult(null)
            return null
        }
        val res = states[states.size - 1]
        states.removeAt(states.size - 1)
        LevelScene.shared.levelsActivity?.updateResult(res.result)
        return res
    }

    fun clear() {
        Log.d(TAG, "clear")
        states.clear()
    }
}