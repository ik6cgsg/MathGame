package spbpu.hsamcp.mathgame.mathResolver

import com.twf.expressiontree.ExpressionNode

enum class OperationType(val names: Array<String>) {
    POW(arrayOf("^")),
    DIV(arrayOf("/")),
    MINUS(arrayOf("-")),
    PLUS(arrayOf("+")),
    MULT(arrayOf("*")),
    FUNCTION(arrayOf("cos", "sin", "tg", "ctg")),
    SET_AND(arrayOf("&"))
}

class Operation(val name: String) {
    val priority: Int
    val type: OperationType

    init {
        priority = getPriority(name)
        type = OperationType.values().filter {
            name in it.names
        }[0]
    }

    companion object {
        fun getPriority(name: String): Int {
            return when (name) {
                "^" -> 3
                "*", "/" -> 2
                "-" -> 1
                "+" -> 0
                // set
                "&" -> 1
                else -> -1
            }
        }
    }
}