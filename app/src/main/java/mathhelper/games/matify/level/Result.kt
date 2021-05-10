package mathhelper.games.matify.level

class Result(val steps: Double, val time: Long, val state: StateType, var expression: String = "") {
    fun isBetter(other: Result?): Boolean {
        if (other == null) {
            return true
        }
        return steps < other.steps || time < other.time
    }

    override fun toString(): String {
        val sec = "${time % 60}".padStart(2, '0')
        val stepsStr = if (steps.equals(steps.toInt().toFloat())) {
            "${steps.toInt()}"
        } else {
            "%.1f".format(steps)
        }

        return "${state.str} \uD83D\uDC63: $stepsStr ⏰: ${time / 60}:$sec"
    }

    fun saveString(): String {
        var str = "$steps $time ${state.name}"
        if (expression.isNotBlank()) {
            str += " $expression"
        }
        return str
    }
}