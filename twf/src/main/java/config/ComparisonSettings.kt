package config

enum class ComparisonType(val string: String) { LEFT_MORE_OR_EQUAL(">="), LEFT_LESS_OR_EQUAL("<="), EQUAL("="), LEFT_MORE(">"), LEFT_LESS("<") } //order is important for parser

fun strictComparison(comp: ComparisonType) : Boolean {
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

fun valueFromMathMLString(value: String) = when (value) {
    "<mo>=</mo>" -> ComparisonType.EQUAL
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

fun getAllComparisonTypeMathML() = listOf(
        "=",
        "<mo>=</mo>",
        "<mo>&gt;</mo><mo>=</mo>",
        "<mo>&lt;</mo><mo>=</mo>",
        "<mo>&gt;</mo>",
        "<mo>&lt;</mo>",
        "<mo>&#x2265;</mo>",
        "<mo>&#x2A7E;</mo>",
        "<mo>&#x2264;</mo>",
        "<mo>&#x2A7D;</mo>"
)


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