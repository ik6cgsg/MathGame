package spbpu.hsamcp.mathgame.level

enum class AwardType(val str: String) {
    PLATINUM("\uD83C\uDF96"),
    GOLD("\uD83E\uDD47"),
    SILVER("\uD83E\uDD48"),
    BRONZE("\uD83E\uDD49"),
    NONE("\uD83D\uDE2D")
}

data class Award(val value: AwardType, val coeff: Double)