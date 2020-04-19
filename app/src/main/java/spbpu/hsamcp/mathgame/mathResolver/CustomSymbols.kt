package spbpu.hsamcp.mathgame.mathResolver

import com.twf.expressiontree.ExpressionNode

class CustomSymbolsHandler {
    companion object {
        private val setCustomSymbols = hashMapOf(
            "0" to "∅",
            "1" to "U"
        )

        private val otherCustomSymbols = hashMapOf(
            "cherry" to "\uD83C\uDF52"
        )

        fun getPrettyValue(origin: ExpressionNode): Pair<String, Boolean> {
            if (origin.parent == null) {
                return Pair(origin.value, false)
            }
            return if (Operation.isSetOperation(origin.parent!!.value) &&
                    setCustomSymbols.containsKey(origin.value)) {
                Pair(setCustomSymbols[origin.value]!!, true)
            } else if (otherCustomSymbols.containsKey(origin.value)) {
                Pair(otherCustomSymbols[origin.value]!!, true)
            } else {
                Pair(origin.value, false)
            }
        }
    }
}