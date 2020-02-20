package spbpu.hsamcp.mathgame.mathResolver

import com.twf.expressiontree.ExpressionNode

enum class OperationType(val names: Array<String>) {
    POW(arrayOf("^")),
    DIV(arrayOf("/")),
    MINUS(arrayOf("-")),
    PLUS(arrayOf("+")),
    MULT(arrayOf("*")),
    FUNCTION(arrayOf("cos", "sin", "tg", "ctg")),
    SET_AND(arrayOf("&", "and"))
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
                in OperationType.POW.names -> 3
                in OperationType.MULT.names, in OperationType.DIV.names -> 2
                in OperationType.MINUS.names -> 1
                in OperationType.PLUS.names -> 0
                // set
                in OperationType.SET_AND.names -> 1
                else -> -1
            }
        }
    }
}