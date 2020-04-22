package spbpu.hsamcp.mathgame.level

import android.util.Log
import com.twf.expressiontree.ExpressionNode

data class State(var expression: ExpressionNode, var depth: Int = 0)

class History {
    private val TAG = "History"
    private val index = 0
    var states = ArrayList<State>()
    var undoDepth = 0

    fun saveState(state: State) {
        Log.d(TAG, "saveState")
        states.add(state)
        undoDepth = 0
    }

    fun getPreviousStep(): State? {
        Log.d(TAG, "getPreviousStep")
        if (states.size < 1) {
            return null
        }
        val res = states[states.size - 1]
        states.removeAt(states.size - 1)
        res.depth = undoDepth
        undoDepth++
        return res
    }

    fun getNextStep(): State? {
        Log.d(TAG, "getNextStep")
        return null
    }

    fun clear() {
        Log.d(TAG, "clear")
        states.clear()
    }
}