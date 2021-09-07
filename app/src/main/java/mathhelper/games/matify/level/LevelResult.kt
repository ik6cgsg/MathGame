package mathhelper.games.matify.level

import mathhelper.games.matify.common.SavableResult

class LevelResult(
    val steps: Double, val time: Long, val state: StateType, var expression: String = ""
): SavableResult {
    fun isBetter(other: LevelResult?): Boolean {
        if (other == null) {
            return true
        }
        if (other.state == StateType.PAUSED && state == StateType.DONE) {
            return true
        }
        return steps < other.steps || time < other.time
    }

    override fun toString(): String {
        val sec = "${time % 60}".padStart(2, '0')
        val stepsStr = "${steps.toInt()}"
        return "$stepsStr \uD83D\uDC63   ${time / 60}:$sec ⏰"
    }

    override fun saveString(): String {
        var str = "$steps $time ${state.name}"
        if (expression.isNotBlank()) {
            str += " $expression"
        }
        return str
    }
}