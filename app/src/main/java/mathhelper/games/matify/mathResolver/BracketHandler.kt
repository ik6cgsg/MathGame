package mathhelper.games.matify.mathResolver

class BracketHandler {
    companion object {
        private const val ltSymbol = "⎧"
        private const val lbSymbol = "⎩"
        private const val rtSymbol = "⎫"
        private const val rbSymbol = "⎭"
        private const val midSymbol = "⎪"
        private const val lSingle = "("
        private const val rSingle = ")"

        fun setBrackets(stringMatrix: ArrayList<String>, spannableArray: ArrayList<SpanInfo>, lt: Point, rb: Point) {
            when (rb.y - lt.y + 1) {
                0 -> return
                1 -> {
                    stringMatrix[lt.y] = stringMatrix[lt.y].replaceByIndex(lt.x, lSingle)
                    stringMatrix[lt.y] = stringMatrix[lt.y].replaceByIndex(rb.x, rSingle)
                }
                else -> {
                    var curStr = lt.y
                    stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(lt.x, ltSymbol)
                    stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(rb.x, rtSymbol)
                    curStr++
                    while (curStr < rb.y) {
                        stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(lt.x, midSymbol)
                        stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(rb.x, midSymbol)
                        curStr++
                    }
                    stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(lt.x, lbSymbol)
                    stringMatrix[curStr] = stringMatrix[curStr].replaceByIndex(rb.x, rbSymbol)
                }
            }
        }
    }
}