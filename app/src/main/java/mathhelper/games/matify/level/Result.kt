package mathhelper.games.matify.level

class Result(val steps: Float, val time: Long, val award: Award, var expression: String = "") {
    fun isBetter(other: Result?): Boolean {
        if (other == null) {
            return true
        }
        return award.coeff - other.award.coeff > 0
    }

    override fun toString(): String {
        val sec = "${time % 60}".padStart(2, '0')
        val stepsStr = if (steps.equals(steps.toInt().toFloat())) {
            "${steps.toInt()}"
        } else {
            "%.1f".format(steps)
        }

        return "$award \uD83D\uDC63: $stepsStr ⏰: ${time / 60}:$sec"
    }

    fun saveString(): String {
        var str = "$steps $time ${award.coeff}"
        if (expression.isNotBlank()) {
            str += " $expression"
        }
        return str
    }
}