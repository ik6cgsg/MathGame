package mathhelper.twf.api

import mathhelper.twf.config.ComparisonSettings
import mathhelper.twf.config.CompiledConfiguration
import mathhelper.twf.config.DebugOutputMessages
import mathhelper.twf.config.FunctionConfiguration
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.twf.expressiontree.ExpressionStructureConditionNode
import mathhelper.twf.expressiontree.checkExpressionStructure


//compare expressions without substitutions
fun compareWithoutSubstitutions(
        left: ExpressionNode,
        right: ExpressionNode,
        scope: Set<String> = setOf(""),
        notChangesOnVariablesFunction: Set<String> = setOf("+", "-", "*", "/", "^"),
        maxExpressionBustCount: Int = 4096,
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(scope, notChangesOnVariablesFunction),
        comparisonSettings: ComparisonSettings = ComparisonSettings().apply { this.maxExpressionBustCount = maxExpressionBustCount},
        debugOutputMessages: DebugOutputMessages = DebugOutputMessages(),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration, comparisonSettings = comparisonSettings, debugOutputMessages = debugOutputMessages)
) = compiledConfiguration.factComporator.expressionComporator.compareWithoutSubstitutions(left, right)


//compare expression by pattern
fun compareByPattern(
        expression: ExpressionNode,
        pattern: ExpressionStructureConditionNode
) = checkExpressionStructure(expression, pattern)






//string API
fun compareWithoutSubstitutions(
        left: String,
        right: String,
        scope: String = "",
        notChangesOnVariablesFunction: String = "+;-;*;/;^",
        maxExpressionBustCount: Int = 4096,
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet(),
                notChangesOnVariablesInComparisonFunctionFilter = notChangesOnVariablesFunction.split(";")
                        .filter { it.isNotEmpty() }.toSet()
        ),
        comparisonSettings: ComparisonSettings = ComparisonSettings().apply { this.maxExpressionBustCount = maxExpressionBustCount},
        debugOutputMessages: DebugOutputMessages = DebugOutputMessages(),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration, comparisonSettings = comparisonSettings, debugOutputMessages = debugOutputMessages)
) = compareWithoutSubstitutions(
        stringToExpression(left, compiledConfiguration = compiledConfiguration),
        stringToExpression(right, compiledConfiguration = compiledConfiguration),
        compiledConfiguration = compiledConfiguration
)

fun compareByPattern(
        expression: String,
        pattern: String,
        scope: String = "",
        notChangesOnVariablesFunction: String = "+;-;*;/;^",
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet(),
                notChangesOnVariablesInComparisonFunctionFilter = notChangesOnVariablesFunction.split(";")
                        .filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = compareByPattern(
        stringToExpression(expression, compiledConfiguration = compiledConfiguration),
        stringToExpressionStructurePattern(pattern, functionConfiguration = functionConfiguration)
)

fun compareWithoutSubstitutionsStructureStrings(
        leftStructureString: String,
        rightStructureString: String,
        scope: String = "",
        notChangesOnVariablesFunction: String = "+;-;*;/;^",
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet(),
                notChangesOnVariablesInComparisonFunctionFilter = notChangesOnVariablesFunction.split(";")
                        .filter { it.isNotEmpty() }.toSet()
        ),
        debugOutputMessages: DebugOutputMessages = DebugOutputMessages(),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration, debugOutputMessages = debugOutputMessages)
) = compareWithoutSubstitutions(
        structureStringToExpression(leftStructureString),
        structureStringToExpression(rightStructureString),
        compiledConfiguration = compiledConfiguration
)