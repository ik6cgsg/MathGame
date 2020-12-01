package api

import config.CompiledConfiguration
import config.FunctionConfiguration
import expressiontree.*
import platformdependent.escapeCharacters

/**
 * Parses the string as a [ExpressionNode] tree and returns the result
 * or `ExpressionNode(nodeType = NodeType.ERROR, value = <description>, startPosition, endPosition)` if the string is not a valid representation of an expression.
 *
 * Input expression samples: "a+b*c", "S(i, a, b, f(i) + g(i))", "<mfrac><mi>a</mi><mi>b</mi></mfrac>"
 */
fun stringToExpression(
        string: String,
        scope: String = "",
        isMathMl: Boolean = false,
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
): ExpressionNode {
    val expressionTreeParser = ExpressionTreeParser(
            string,
            functionConfiguration = compiledConfiguration.functionConfiguration,
            compiledImmediateVariableReplacements = compiledConfiguration.compiledImmediateVariableReplacements,
            isMathML = isMathMl
    )
    expressionTreeParser.parse()
    return expressionTreeParser.root
}

fun expressionToString(
        expressionNode: ExpressionNode,
        characterEscapingDepth: Int = 1
) = escapeCharacters(expressionNode.toPlainTextView(), characterEscapingDepth)


fun expressionToTexString(
        expressionNode: ExpressionNode,
        characterEscapingDepth: Int = 1
) = escapeCharacters(expressionNode.toTexView(), characterEscapingDepth)


/**
 * Parses the expression tree structure string as a [ExpressionNode] tree and returns the result
 * or `ExpressionNode(nodeType = NodeType.ERROR, value = <description>, startPosition, endPosition)` if the string is not a valid representation of an expression.
 *
 * Input structure string samples: "+(a;*(b;c))", "(S(i;a;b;+(f(i);g(i))))"
 */
fun structureStringToExpression(
        structureString: String,
        scope: String = "",
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
        )
): ExpressionNode {
    val expressionNodeConstructor = ExpressionNodeConstructor(functionConfiguration)
    val result = expressionNodeConstructor.construct(structureString)
    result.computeNodeIdsAsNumbersInDirectTraversalAndDistancesToRoot()
    result.computeIdentifier()
    return result
}

fun expressionToStructureString(
        expressionNode: ExpressionNode
) = expressionNode.toString()



fun stringToStructureString(
        string: String,
        scope: String = "",
        isMathMl: Boolean = false,
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
        ),
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
) = expressionToStructureString(
        stringToExpression(
                string,
                scope,
                isMathMl,
                functionConfiguration,
                compiledConfiguration
        )
)


/**
 * Parses the string as a [ExpressionStructureConditionNode] tree and returns the result.
 * TODO: support error handling
 *
 * Input structure string samples: "and : (or : 3)" - 3CNF, "?:0:?:?N" - Z number
 */
fun stringToExpressionStructurePattern(
        string: String,
        scope: String = "",
        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
        )
): ExpressionStructureConditionNode {
    val expressionStructureConditionConstructor = ExpressionStructureConditionConstructor()
    return expressionStructureConditionConstructor.parse(string)
}


fun investigateIfExpressionFormIsStructureString (expression: String): Boolean {
    if (!expression.endsWith(')')) {
        return false
    }
    return true
}