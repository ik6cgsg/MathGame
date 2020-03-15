package spbpu.hsamcp.mathgame.level

class Result(val steps: Float, val time: Long, val award: Award) {
    fun isBetter(other: Result?): Boolean {
        if (other == null) {
            return true
        }
        return award.value.ordinal - other.award.value.ordinal < 0
    }
}