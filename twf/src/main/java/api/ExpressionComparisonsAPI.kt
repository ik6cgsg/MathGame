package api

import config.CompiledConfiguration
import config.FunctionConfiguration
import expressiontree.ExpressionNode
import expressiontree.ExpressionStructureConditionNode
import expressiontree.checkExpressionStructure


//compare expressions without substitutions
fun compareWithoutSubstitutions(
        left: ExpressionNode,
        right: ExpressionNode,
        scope: Set<String> = setOf(""),
        notChangesOnVariablesFunction: Set<String> = setOf("+", "-", "*", "/", "^"),
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(scope, notChangesOnVariablesFunction),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
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
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet(),
                notChangesOnVariablesInComparisonFunctionFilter = notChangesOnVariablesFunction.split(";")
                        .filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
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
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = compareWithoutSubstitutions(
        structureStringToExpression(leftStructureString),
        structureStringToExpression(rightStructureString),
        compiledConfiguration = compiledConfiguration
)