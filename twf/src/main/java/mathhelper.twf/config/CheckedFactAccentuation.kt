package mathhelper.twf.config

data class CheckedFactColor(
        val checkedFactBackgroundColor: String = "7F00FF",
        val correctFactColor: String = "007F00",
        val unconfirmedFactColor: String = "FF00FF",
        val factNotHelpFactColor: String = "FF00FF",
        val factHelpFactColor: String = "007F00",
        val wrongFactColor: String = "FF0000",
        val wrongTransformationFactColor: String = "FF0000"
)

data class CheckedFactAccentuation (
        val checkedFactColor: CheckedFactColor = CheckedFactColor()
)

class CheckingKeyWords {
    companion object {
        //errors:
        val notFound = "NOT_FOUND"
        val transformationNotFound = "TRANSFORMATION_NOT_FOUND"
        val verificationFailed = "VERIFICATION_FAILED"
        val isNotExpressionRule = "CANNOT_BE_APPLIED_TO_EXPRESSIONS"
        val comparisonTypesConflict = "COMPARISON_SIGNS_CONFLICT"

        //other code words
        val expressionChainVerified = "EXPRESSION_CHAIN_VERIFIED"
        val factChainVerified = "FACT_CHAIN_VERIFIED"
        val transformationVerified = "TRANSFORMATION_VERIFIED"
        val transformationFound = "TRANSFORMATION_FOUND"
        val comparisonStart = "COMPARISON_START"
        val expressionSubstitution = "EXPRESSION_RULE"
        val factSubstitution = "FACT_RULE"
        val inTaskContext = "IN_TASK_CONTEXT"
        val notInTaskContext = "OUT_OF_TASK_CONTEXT"
        val taskContextFactUsed = "TASK_CONTEXT_FACT_USED"
        val expressionChain = "EXPRESSION_CHAIN"
        val expressionComparison = "EXPRESSION_COMPARISON"
        val factChain = "FACT_CHAIN"
        val rule = "RULE"
        val ruleReference = "RULE_REFERENCE"
        val mainLineNode = "CONTEXT"
        val ruleAddedInContext = "ADDED"
        val inFact = "KNOWN_FACT"
        val factTransformation = "FACT_TRANSFORMATION"
        val expressionTransformation = "EXPRESSION_TRANSFORMATION"
        val comparisonWithoutSubstitutions = "COMPARISON_WITHOUT_SUBSTITUTIONS"

    }
}