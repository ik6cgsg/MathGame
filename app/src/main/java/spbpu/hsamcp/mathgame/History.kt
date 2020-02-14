package spbpu.hsamcp.mathgame

import android.util.Log
import com.twf.expressiontree.ExpressionNode

data class State(var formula: ExpressionNode)

class History {
    private val TAG = "History"
    private val index: Int = 0
    var states = ArrayList<State>()

    fun saveState(state: State) {
        Log.d(TAG, "saveState")
        states.add(state)
    }

    fun getPreviousStep(): State? {
        Log.d(TAG, "getPreviousStep")
        if (states.size < 1) {
            return null
        }
        val res = states[states.size - 1]
        states.removeAt(states.size - 1)
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