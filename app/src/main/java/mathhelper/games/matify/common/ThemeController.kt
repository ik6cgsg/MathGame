package mathhelper.games.matify.common

import android.content.Context
import android.graphics.Color
import mathhelper.games.matify.R

enum class ColorName(val str: String) {
    PRIMARY_COLOR("primaryColor"),
    TEXT_COLOR("textColor"),
    MULTISELECTION_COLOR("multiselectionColor"),
    ON_TOUCH_BACKGROUND_COLOR("onTouchBackgroundColor"),
    TEXT_HIGHLIGHT_COLOR("textHighlightColor"),
    TEXT_SECOND_HIGHLIGHT_COLOR("textSecondHighlightColor"),
    AWARD_TIMER_GOLD_COLOR("awardTimerGoldColor"),
    AWARD_TIMER_SILVER_COLOR("awardTimerSilverColor"),
    AWARD_TIMER_BRONZE_COLOR("awardTimerBronzeColor")
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
        ColorName.MULTISELECTION_COLOR to "#FFC300",
        ColorName.ON_TOUCH_BACKGROUND_COLOR to "#8B8484",
        ColorName.TEXT_HIGHLIGHT_COLOR to "#00FFFF",
        ColorName.TEXT_SECOND_HIGHLIGHT_COLOR to "#0088AA",
        ColorName.AWARD_TIMER_GOLD_COLOR to "#FFD700",
        ColorName.AWARD_TIMER_SILVER_COLOR to "#918E8C",
        ColorName.AWARD_TIMER_BRONZE_COLOR to "#AE6842"
    )
    private val light = mapOf(
        ColorName.PRIMARY_COLOR to "#4A266A",
        ColorName.TEXT_COLOR to "#171717",
        ColorName.MULTISELECTION_COLOR to "#FFC300",
        ColorName.ON_TOUCH_BACKGROUND_COLOR to "#B9BBDF",
        ColorName.TEXT_HIGHLIGHT_COLOR to "#6639A6",
        ColorName.TEXT_SECOND_HIGHLIGHT_COLOR to "#331853",
        ColorName.AWARD_TIMER_GOLD_COLOR to "#B8860B",
        ColorName.AWARD_TIMER_SILVER_COLOR to "#787573",
        ColorName.AWARD_TIMER_BRONZE_COLOR to "#AE6842"
    )

    private val themes = mapOf(
        ThemeName.LIGHT to light,
        ThemeName.DARK to dark
    )

    private val alertDialogMap = mapOf(
        ThemeName.LIGHT to R.style.AlertDialogCustomLight,
        ThemeName.DARK to R.style.AlertDialogCustomDark
    )

    var currentTheme = ThemeName.DARK
    val alertDialogTheme: Int
        get() = alertDialogMap[currentTheme] ?: R.style.AlertDialogCustomDark

    fun init(context: Context) {
        currentTheme = Storage.shared.theme()
    }

    fun color(name: ColorName): Int {
        return Color.parseColor(themes[currentTheme]?.get(name) ?: "#000000")
    }

    fun setTheme(context: Context, name: ThemeName) {
        currentTheme = name
        Storage.shared.setTheme(name)
    }
}
