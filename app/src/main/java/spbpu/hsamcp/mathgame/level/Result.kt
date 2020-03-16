package spbpu.hsamcp.mathgame.level

class Result(val steps: Float, val time: Long, val award: Award) {
    fun isBetter(other: Result?): Boolean {
        if (other == null) {
            return true
        }
        return award.coeff - other.award.coeff > 0
    }

    override fun toString(): String {
        val sec = "${time % 60}".padStart(2, '0')
        return "${award.value.str} \uD83D\uDC63: $steps ⏰: ${time / 60}:$sec"
    }
}