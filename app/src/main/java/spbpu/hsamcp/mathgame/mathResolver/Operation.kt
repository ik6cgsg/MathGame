package spbpu.hsamcp.mathgame.mathResolver

enum class OperationType(val names: Array<String>) {
    POW(arrayOf("^")),
    DIV(arrayOf("/")),
    MINUS(arrayOf("-")),
    PLUS(arrayOf("+")),
    MULT(arrayOf("*")),
    FUNCTION(arrayOf("cos", "sin", "tg", "ctg")),
    SET_AND(arrayOf("&", "and")),
    SET_OR(arrayOf("|", "or")),
    SET_MINUS(arrayOf("\\", "set-")),
    SET_NOT(arrayOf("!", "not")),
    SET_IMPLIC(arrayOf("->", "implic"))
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
                in OperationType.POW.names -> 4
                in OperationType.DIV.names -> 3
                in OperationType.MULT.names -> 2
                in OperationType.MINUS.names -> 1
                in OperationType.PLUS.names -> 0
                // set
                in OperationType.SET_AND.names -> 3
                in OperationType.SET_OR.names -> 2
                in OperationType.SET_MINUS.names -> 1
                in OperationType.SET_IMPLIC.names -> 0
                else -> -1
            }
        }

        fun isSetOperation(name: String): Boolean {
            return when (name) {
                in OperationType.SET_AND.names, in OperationType.SET_OR.names,
                    in OperationType.SET_MINUS.names, in OperationType.SET_IMPLIC.names -> true
                else -> false
            }
        }
    }
}