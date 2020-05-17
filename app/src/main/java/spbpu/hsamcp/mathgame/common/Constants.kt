package spbpu.hsamcp.mathgame.common

import android.graphics.Color

class Constants {
    companion object {
        const val centralExpressionDefaultSize = 28f
        const val centralExpressionMaxSize = 48f
        const val ruleDefaultSize = 23f
        const val buttonDefaultSize = 21f
        const val mathLineSpacing = 0.5f
        const val levelLineSpacing = 1.2f
        const val defaultPadding = 10
        const val drawableLeft = 0
        const val drawableRight = 2
        val primaryColor = Color.parseColor("#008577")
        val textColor = Color.parseColor("#C5C5C5")
        val lightGrey = Color.parseColor("#8B8484")
        const val storage = "MATH_GAME_PREFS"
        const val deviceId = "DEVICE_UUID"
        const val timeDeviation = 0.2f
        const val awardDeviation = 0.1f
    }
}