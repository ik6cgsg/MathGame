package mathhelper.twf.config

import mathhelper.twf.factstransformations.FactSubstitution
import mathhelper.twf.factstransformations.SubstitutionDirection

data class FactTransformationRule(
        val definitionLeftFactTree: String,
        val definitionRightFactTree: String,
        val immediate: Boolean = false,
        val weight: Double = 1.0,
        val direction: SubstitutionDirection = SubstitutionDirection.ALL_TO_ALL,
        val isOneDirection: Boolean = false
)

class FactsLogicConfiguration() {
    val signsPointersOnNotEndedExpression = listOf("+", "-", "*", "/", "^", "%", "&", "|", ",", "&#xD7;", "&#xF7;", "&#x2265;", "&#x2264;",
            "=", "&gt;", "&lt;", "&#x2A7E;", "&#x2A7D;", "&#x2261;")
    val alwaysLetTwoPartsComparisonsAsExpressionComparisons = true

    val factsTransformationRules = mutableListOf<FactTransformationRule>(
            FactTransformationRule("a+c;ec;>;ec;b+c", "a;ec;>;ec;b", isOneDirection = true),
            FactTransformationRule("a+c;ec;>=;ec;b+c", "a;ec;>=;ec;b", isOneDirection = true),
            FactTransformationRule("a+c;ec;=;ec;b+c", "a;ec;=;ec;b", isOneDirection = true),
            FactTransformationRule("AND_NODE(a*c;ec;=;ec;b*c;mn;c;ec;>;ec;0)", "AND_NODE(a;ec;=;ec;b)", isOneDirection = true),
            FactTransformationRule("AND_NODE(a/c;ec;=;ec;b/c;mn;c;ec;>;ec;0)", "AND_NODE(a;ec;=;ec;b)", isOneDirection = true),
            FactTransformationRule("AND_NODE(OR_NODE(a;mn;b);mn;c)", "OR_NODE(AND_NODE(a;mn;c);mn;AND_NODE(b;mn;c))"),
            FactTransformationRule("OR_NODE(AND_NODE(a;mn;b);mn;c)", "AND_NODE(OR_NODE(a;mn;c);mn;OR_NODE(b;mn;c))")
    )
}