package spbpu.hsamcp.mathgame

class Result(val steps: Int, val time: Long, val award: Award) {
    fun isBetter(other: Result?): Boolean {
        if (other == null) {
            return true
        }
        return award.ordinal - other.award.ordinal < 0
    }
}