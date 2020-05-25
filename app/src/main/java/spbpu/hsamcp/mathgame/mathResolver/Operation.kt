package spbpu.hsamcp.mathgame.mathResolver

enum class OperationType(val names: Array<String>) {
    POW(arrayOf("^")),
    DIV(arrayOf("/")),
    MINUS(arrayOf("-")),
    PLUS(arrayOf("+")),
    MULT(arrayOf("*")),
    FUNCTION(arrayOf("")),
    RIGHT_UNARY(arrayOf("")),
    SET_AND(arrayOf("&", "and")),
    SET_OR(arrayOf("|", "or")),
    SET_MINUS(arrayOf("\\", "set-")),
    SET_NOT(arrayOf("!", "not")),
    SET_IMPLIC(arrayOf("->", "implic"))
}

class Operation(var name: String) {
    val priority: Int
    val type: OperationType

    init {
        if (name == "factorial") {
            name = "!"
            type = OperationType.RIGHT_UNARY
            priority = 5
        } else {
            priority = getPriority(name)
            val types = OperationType.values().filter {
                name in it.names
            }
            type = if (types.isEmpty()) {
                OperationType.FUNCTION
            } else {
                types[0]
            }
        }
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
    }
}