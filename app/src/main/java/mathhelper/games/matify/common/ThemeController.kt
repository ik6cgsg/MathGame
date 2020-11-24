package mathhelper.games.matify.common

import android.graphics.Color
import mathhelper.games.matify.R

enum class ColorName(val str: String) {
    PRIMARY_COLOR("primaryColor"),
    TEXT_COLOR("textColor"),
    ON_TOUCH_BACKGROUND_COLOR("onTouchBackgroundColor"),
    TEXT_HIGHLIGHT_COLOR("textHighlightColor")
}

enum class ThemeName(val resId: Int){
    LIGHT(R.style.AppLightTheme) {
        override fun toString(): String {
            return "LIGHT"
        }
    },
    DARK(R.style.AppDarkTheme) {
        override fun toString(): String {
            return "DARK"
        }
    }
}

class ThemeController {
    companion object {
        val shared = ThemeController()
    }
    private val dark = mapOf(
        ColorName.PRIMARY_COLOR to "#008577",
        ColorName.TEXT_COLOR to "#C5C5C5",
        ColorName.ON_TOUCH_BACKGROUND_COLOR to "#8B8484",
        ColorName.TEXT_HIGHLIGHT_COLOR to "#00FFFF"
    )
    private val light = mapOf(
        ColorName.PRIMARY_COLOR to "#4A266A",
        ColorName.TEXT_COLOR to "#171717",
        ColorName.ON_TOUCH_BACKGROUND_COLOR to "#B9BBDF",
        ColorName.TEXT_HIGHLIGHT_COLOR to "#6639A6"
    )

    private val themes = mapOf(
        ThemeName.LIGHT to light,
        ThemeName.DARK to dark
    )

    private val alertDialog = mapOf(
        ThemeName.LIGHT to R.style.AlertDialogCustomLight,
        ThemeName.DARK to R.style.AlertDialogCustomDark
    )

    fun getColorByTheme(themeName: ThemeName, colorName: ColorName): Int {
        var themesMap = themes[themeName]
        if (themesMap == null)
            themesMap = dark
        return Color.parseColor(themesMap[colorName])
    }

    fun getAlertDialogByTheme(themeName: ThemeName): Int {
        val alertDialogInt = alertDialog[themeName]
        if (alertDialogInt == null)
            return R.style.AlertDialogCustomDark
        return alertDialogInt
    }
}
