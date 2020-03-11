package spbpu.hsamcp.mathgame.level

class Result(val steps: Int, val time: Long, val award: Award) {
    fun isBetter(other: Result?): Boolean {
        if (other == null) {
            return true
        }
        return award.value.ordinal - other.award.value.ordinal < 0
    }
}