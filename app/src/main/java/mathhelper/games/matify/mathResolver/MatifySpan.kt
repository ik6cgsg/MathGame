package mathhelper.games.matify.mathResolver
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.style.ReplacementSpan
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.ColorName
import mathhelper.games.matify.common.ThemeController
import kotlin.math.roundToInt

enum class ExpressionType {
    GLOBAL, RULE, SIMPLE
}

class MatifySymbols {
    companion object {
        const val div = "⏤"
        const val plus = "+ "
        const val minusUnary = "− "
        const val minus = "− "
        const val mult = "∙ "
        // set
        const val setAnd = "∧ "
        const val setOr = "∨ "
        const val setImplic = "⇒ "
        const val setMinus = "∖ "
        const val setNot = "⏤"

        private val symbolsToMove = arrayOf(plus, minus, mult, setAnd, setOr, setImplic, setMinus)
            .joinToString(separator = "").replace(" ", "")

        private val symbolsToMagnify= arrayOf(plus, minus, mult)
            .joinToString(separator = "").replace(" ", "")

        fun needMove(s: Char, n: Char?) = symbolsToMove.contains(s) && n == ' '
        fun needMagnify(s: Char) = symbolsToMagnify.contains(s)
    }
}

data class SymbolInfo(
    var color: Int = 0,
    var typeface: Typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL),
    var sizeMult: Float = 1.0f,
    var removable: Boolean = true
)

class MatifySpan(val type: ExpressionType): ReplacementSpan() {
    companion object {
        var widthGlobal = 0
        var heightGlobal = 0
        private val usualType = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        private val boldType = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        private var customedSymbols = hashMapOf<Int, SymbolInfo>()

        fun select(start: Int, end: Int, color: Int? = null, multiselection: Boolean = false) {
            val s = if (start < 0) 0 else start
            val e = if (end < 0) 0 else end
            for (i in s until e) {
                var oldInfo = customedSymbols[i]
                if (multiselection) {
                    val defColor = ThemeController.shared.color(ColorName.MULTISELECTION_COLOR)
                    when {
                        oldInfo == null -> {
                            oldInfo = SymbolInfo(color = defColor)
                        }
                        oldInfo.color == 0 -> {
                            oldInfo.color = defColor
                        }
                        else -> {
                            oldInfo.color = AndroidUtil.darkenColor(oldInfo.color)
                        }
                    }
                } else if (oldInfo == null) {
                    oldInfo = SymbolInfo()
                    if (color != null) {
                        oldInfo.color = color
                    }
                } else if (color != null) {
                    oldInfo.color = color
                }
                oldInfo.typeface = boldType
                customedSymbols[i] = oldInfo
            }
        }

        fun unselect(start: Int, end: Int, multiselection: Boolean = false) {
            for (i in start until end) {
                if (multiselection) {
                    val defColor = ThemeController.shared.color(ColorName.MULTISELECTION_COLOR)
                    val color = AndroidUtil.lighterColor(customedSymbols[i]!!.color, defColor)
                    if (color == null) {
                        customedSymbols[i]!!.color = 0
                        customedSymbols[i]!!.typeface = usualType
                        if (customedSymbols[i]!!.removable) {
                            customedSymbols.remove(i)
                        }
                    } else {
                        customedSymbols[i]?.color = color
                    }
                } else if (customedSymbols[i]!!.removable) {
                    customedSymbols.remove(i)
                } else {
                    customedSymbols[i]!!.color = 0
                    customedSymbols[i]!!.typeface = usualType
                }
            }
        }

        fun recolor(color: Int) {
            for (pair in customedSymbols) {
                pair.value.color = color
            }
        }

        fun clearSelected() {
            val keys = customedSymbols.keys.map { it }
            for (i in keys) {
                if (customedSymbols[i]?.removable == true) {
                    customedSymbols.remove(i)
                }
            }
        }
    }

    private var mainColor: Int = 0
    private var defSize: Float = 0f
    private var multiplierMap = hashMapOf<Int, Float>()
    private var height = 0

    fun setSizeMultiplier(start: Int, end: Int, mult: Float) {
        for (i in start until end) {
            multiplierMap[i] = mult
        }
    }

    private fun getMaxCharWidth(paint: Paint, text: CharSequence, start: Int, end: Int, widths: FloatArray?): Int {
        var widths = widths
        if (widths == null) {
            widths = FloatArray(end - start)
        }
        paint.getTextWidths(text, start, end, widths)
        val m = (paint.textSize * 0.59).roundToInt()
        if (type == ExpressionType.GLOBAL) {
            widthGlobal = m
        }
        return m
    }

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        if (fm != null) {
            paint.getFontMetricsInt(fm)
        }
        val pfm = Paint.FontMetrics()
        paint.getFontMetrics(pfm)
        height = (pfm.bottom - pfm.top + pfm.leading).roundToInt()
        if (type == ExpressionType.GLOBAL) {
            heightGlobal = height
        }
        var count = end - start
        if (count < 0) {
            count = 0
        }
        return getMaxCharWidth(paint, text, 0, text.length, null) * count
    }

    private fun setPaint(s: Int, paint: Paint, mult: Float) {
        if (type == ExpressionType.GLOBAL && s in customedSymbols.keys) {
            paint.color = customedSymbols[s]!!.color
            if (paint.color == 0) {
                paint.color = mainColor
            }
            paint.typeface = customedSymbols[s]!!.typeface
        }
        paint.textSize = defSize * mult
    }

    private fun clearPaint(paint: Paint) {
        paint.color = mainColor
        paint.typeface = usualType
        paint.textSize = defSize
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        mainColor = paint.color
        defSize = paint.textSize
        val widths = FloatArray(end - start)
        val max = getMaxCharWidth(paint, text, start, end, widths)
        var i = 0
        val n = end - start
        while (i < n) {
            val s = start + i
            var of = 1
            var mult = multiplierMap[s] ?: 1f
            var yOf = 0f
            if (MatifySymbols.needMagnify(text[s])) {
                mult *= 1.5f
                yOf = height.toFloat() * mult * 0.1f
            }
            setPaint(s, paint, mult)
            val w = widths[i] * mult
            var p = (max - w) / 2
            if (MatifySymbols.needMove(text[s], if (i < n - 1) text[s + 1] else null)) {
                p += max / 2f
                of = 2
            }
            canvas.drawText(text, s, s + 1, x + max * i + p, y.toFloat() + yOf, paint)
            i += of
            clearPaint(paint)
        }
    }
}