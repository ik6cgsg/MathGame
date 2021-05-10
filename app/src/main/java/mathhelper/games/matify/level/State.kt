package mathhelper.games.matify.level

import android.content.Context
import android.os.Build
import mathhelper.games.matify.common.ColorName
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.common.ThemeController

enum class StateType(val str: String) {
    DONE("✅"),
    PAUSED("\u23F8"),
    NOT_STARTED("")
}