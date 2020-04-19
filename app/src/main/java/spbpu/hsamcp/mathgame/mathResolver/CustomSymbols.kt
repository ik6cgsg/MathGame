package spbpu.hsamcp.mathgame.mathResolver

import com.twf.expressiontree.ExpressionNode
import spbpu.hsamcp.mathgame.mathResolver.VariableStyle.GREEK

class CustomSymbolsHandler {
    companion object {
        private val setCustomSymbols = hashMapOf(
            "0" to "∅",
            "1" to "U"
        )

        private val greekSymbols = hashMapOf(
            "A" to "α",
            "B" to "β",
            "C" to "c",
            "D" to "δ",
            "E" to "ε",
            "F" to "φ",
            "G" to "γ",
            "H" to "η",
            "I" to "ι",
            "J" to "j",
            "K" to "κ",
            "L" to "λ",
            "M" to "μ",
            "N" to "ν",
            "O" to "ω",
            "P" to "π",
            "Q" to "q",
            "R" to "ρ",
            "S" to "ς",
            "T" to "τ",
            "U" to "υ",
            "V" to "v",
            "W" to "w",
            "X" to "ξ",
            "Y" to "y",
            "Z" to "ζ"
        )

        private val otherCustomSymbols = hashMapOf(
            "cherry" to "\uD83C\uDF52"
        )

        fun getPrettyValue(origin: ExpressionNode, style: VariableStyle): Pair<String, Boolean> {
            if (origin.parent == null) {
                return Pair(origin.value, false)
            }
            return when {
                style == GREEK && greekSymbols.containsKey(origin.value.toUpperCase()) ->
                    Pair(greekSymbols[origin.value.toUpperCase()]!!, true)
                Operation.isSetOperation(origin.parent!!.value) && setCustomSymbols.containsKey(origin.value) ->
                    Pair(setCustomSymbols[origin.value]!!, true)
                otherCustomSymbols.containsKey(origin.value) -> Pair(otherCustomSymbols[origin.value]!!, true)
                else -> Pair(origin.value, false)
            }
        }
    }
}