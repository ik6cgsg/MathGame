package spbpu.hsamcp.mathgame.level

import android.graphics.Color
import spbpu.hsamcp.mathgame.common.Constants

enum class AwardType(val str: String) {
    PLATINUM("\uD83C\uDF96"),
    GOLD("\uD83E\uDD47"),
    SILVER("\uD83E\uDD48"),
    BRONZE("\uD83E\uDD49"),
    PAUSED("\u23F8"),
    NONE("\uD83D\uDE2D")
}

data class Award(val value: AwardType, val coeff: Double) {
    var color: Int = when (value) {
        AwardType.GOLD -> Color.rgb(255, 215, 0)
        AwardType.SILVER -> Color.rgb(145, 142, 140)
        AwardType.BRONZE -> Color.rgb(174, 104, 66)
        else -> Constants.textColor
    }

    companion object {
        fun getPaused(): Award {
            return Award(AwardType.PAUSED, -1.0)
        }
    }
}