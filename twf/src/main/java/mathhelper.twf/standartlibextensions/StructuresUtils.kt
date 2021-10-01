package mathhelper.twf.standartlibextensions

interface TransformationsPart {
    val startPosition: Int //included
    val endPosition: Int //excluded
}

class StringPart(
        override val startPosition: Int,
        override val endPosition: Int,
        val splittingSubstring: String? = null
): TransformationsPart{
}

fun Set<Int>.containsAny (o: Collection<Int>): Boolean {
    for (e in o) {
        if (this.contains(e)) {
            return true
        }
    }
    return false
}

fun Set<Int>.containsAny (o: Array<Int>): Boolean {
    for (e in o) {
        if (this.contains(e)) {
            return true
        }
    }
    return false
}