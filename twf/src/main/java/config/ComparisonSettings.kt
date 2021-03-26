package config

import standartlibextensions.SplittingString

enum class ComparisonType(val string: String) { LEFT_MORE_OR_EQUAL(">="), LEFT_LESS_OR_EQUAL("<="), EQUAL("="), LEFT_MORE(">"), LEFT_LESS("<") } //order is important for parser

fun ComparisonType.reverse() = when (this) {
    ComparisonType.LEFT_MORE_OR_EQUAL -> ComparisonType.LEFT_LESS_OR_EQUAL
    ComparisonType.LEFT_LESS_OR_EQUAL -> ComparisonType.LEFT_MORE_OR_EQUAL
    ComparisonType.LEFT_MORE -> ComparisonType.LEFT_LESS
    ComparisonType.LEFT_LESS -> ComparisonType.LEFT_MORE
    else -> this
}

fun strictComparison(comp: ComparisonType): Boolean {
    return comp == ComparisonType.LEFT_LESS || comp == ComparisonType.LEFT_MORE
}

fun valueOfComparisonType(value: String) = when (value) {
    ">=" -> ComparisonType.LEFT_MORE_OR_EQUAL
    "<=" -> ComparisonType.LEFT_LESS_OR_EQUAL
    "=" -> ComparisonType.EQUAL
    ">" -> ComparisonType.LEFT_MORE
    "<" -> ComparisonType.LEFT_LESS
    else -> ComparisonType.EQUAL
}

fun valueFromSignString(value: String) = when (value) {
    "<mo>=</mo>" -> ComparisonType.EQUAL
    "<=" -> ComparisonType.LEFT_LESS_OR_EQUAL
    ">=" -> ComparisonType.LEFT_MORE_OR_EQUAL
    "<" -> ComparisonType.LEFT_LESS
    ">" -> ComparisonType.LEFT_MORE
    "\\lt" -> ComparisonType.LEFT_LESS
    "\\le" -> ComparisonType.LEFT_LESS_OR_EQUAL
    "\\gt" -> ComparisonType.LEFT_MORE
    "\\ge" -> ComparisonType.LEFT_MORE_OR_EQUAL
    "<mo>&#x2265;</mo>" -> ComparisonType.LEFT_MORE_OR_EQUAL
    "<mo>&#x2A7E;</mo>" -> ComparisonType.LEFT_MORE_OR_EQUAL
    "<mo>&#x2264;</mo>" -> ComparisonType.LEFT_LESS_OR_EQUAL
    "<mo>&#x2A7D;</mo>" -> ComparisonType.LEFT_LESS_OR_EQUAL
    "<mo>&gt;</mo>" -> ComparisonType.LEFT_MORE
    "<mo>&lt;</mo>" -> ComparisonType.LEFT_LESS
    "<mo>&gt;</mo><mo>=</mo>" -> ComparisonType.LEFT_MORE_OR_EQUAL
    "<mo>&lt;</mo><mo>=</mo>" -> ComparisonType.LEFT_LESS_OR_EQUAL
    else -> ComparisonType.EQUAL
}

fun getAllComparisonTypeSignStrings(isMathML: Boolean) = if (isMathML) {
    listOf(
            SplittingString("="),
            SplittingString("<mo>=</mo>"),
            SplittingString("<mo>&gt;</mo><mo>=</mo>"),
            SplittingString("<mo>&lt;</mo><mo>=</mo>"),
            SplittingString("<mo>&gt;</mo>"),
            SplittingString("<mo>&lt;</mo>"),
            SplittingString("<mo>&#x2265;</mo>"),
            SplittingString("<mo>&#x2A7E;</mo>"),
            SplittingString("<mo>&#x2264;</mo>"),
            SplittingString("<mo>&#x2A7D;</mo>")
    )
} else {
    listOf(
            SplittingString("="),
            SplittingString("<="),
            SplittingString(">="),
            SplittingString("<"),
            SplittingString(">"),
            SplittingString("\\lt"),
            SplittingString("\\le", listOf("\\left")),
            SplittingString("\\gt"),
            SplittingString("\\ge")
    )
}


data class ComparisonSettings(
        val maxTransformationWeight: Double = 1.0,
        val maxBustCount: Int = 4096,
        var maxExpressionTransformationWeight: Double = 1.0,
        val maxExpressionBustCount: Int = 4096,
        val isComparisonWithRules: Boolean = true,
        var compareExpressionsWithProbabilityRulesWhenComparingExpressions: Boolean = true,
        var compareExpressionsWithProbabilityRulesWhenComparingFacts: Boolean = true,
        var compareExpressionsAndFactsWithProbabilityRules: Boolean = false,
        var useTestingToCompareFunctionArgumentsInProbabilityComparison: Boolean = true,
        var useOldSimpleProbabilityTesting: Boolean = true,

        var defaultComparisonType: ComparisonType = ComparisonType.EQUAL,
        var minNumberOfPointsForEquality: Int = 4,
        val justInDomainsIntersection: Boolean = false,
        var allowedPartOfErrorTests: Double = 0.7,
        var testWithUndefinedResultIncreasingCoef: Double = 0.67,

        var maxDistBetweenDiffSteps: Double = 1.0
)

data class GradientDescentComparisonConfiguration(
        val startPointsCount: Int = 1,
        val iterationCount: Int = 20,

        val ternarySearchLeftBorder: Double = 1e-9,
        val ternarySearchRightBorder: Double = 1e9,
        val ternarySearchIterationCount: Int = 100,

        // coefs for ternary search borders formulas:
        //      m1 = (alpha * l + beta * r) / (alpha + beta)
        //      m2 = (beta * l + alpha * r) / (alpha + beta)
        //
        // only alpha / beta matters,
        // the bigger this ratio the better algorithm works for expressions with bounded domain,
        // but more ternary search iterations is needed
        val ternarySearchAlpha: Double = 1.57,
        val ternarySearchBeta: Double = 1.0

)