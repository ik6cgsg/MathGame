package standartlibextensions

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