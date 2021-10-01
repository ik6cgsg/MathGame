package mathhelper.twf.api

import mathhelper.twf.config.CompiledConfiguration
import mathhelper.twf.config.FunctionConfiguration
import mathhelper.twf.config.PI_STRING
import mathhelper.twf.expressiontree.*
import mathhelper.twf.numbers.toReal
import mathhelper.twf.platformdependent.escapeCharacters
import mathhelper.twf.platformdependent.toShortString
import kotlin.math.sqrt


//fun generateTask(
//        expressionSubstitutions: List<ExpressionSubstitution>, //if substitution can be applied in both directions, it has to be specified twice
//        stepsCount: Int,
//        originalExpressions: List<ExpressionNode> //it's better to set original expression manually, to choose correct number of variables
//) = generateExpressionTask(expressionSubstitutions, stepsCount, originalExpressions)
//
//
//fun generateTaskInJSON(
//        expressionSubstitutions: String, //';' separated equalities
//        stepsCount: Int,
//        originalExpressions: String, //';' separated
//        scope: String = "", //';' separated
//        characterEscapingDepth: Int = 1,
//        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
//                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
//        ),
//        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
//): String {
//    val expressionTask = generateTask(
//            expressionSubstitutions.split(";").map {
//                val parts = it.split("=")
//                expressionSubstitutionFromStrings(
//                        parts.first(),
//                        parts.last(),
//                        compiledConfiguration = compiledConfiguration
//                )
//            },
//            stepsCount,
//            originalExpressions.split(";").map { stringToExpression(it, compiledConfiguration = compiledConfiguration) }
//    )
//    return escapeCharacters("{" +
//            "\"originalExpression\":\"${expressionToString(expressionTask.originalExpression)}\"," +
//            "\"finalExpression\":\"${expressionToString(expressionTask.finalExpression)}\"," +
//            "\"requiredSubstitutions\":[${
//            expressionTask.requiredSubstitutions.joinToString(separator = ",") {
//                "{\"left\":\"${expressionToString(it.left)}\",\"right\":\"${expressionToString(
//                        it.right
//                )}\"}"
//            }
//            }]," +
//            "\"allSubstitutions\":[${
//            expressionTask.allSubstitutions.joinToString(separator = ",") {
//                "{\"left\":\"${expressionToString(it.left)}\",\"right\":\"${expressionToString(
//                        it.right
//                )}\"}"
//            }
//            }]" +
//            "}",
//            characterEscapingDepth)
//}
//
//fun generateTaskByStructureStringsSubstitutionsInJSON(
//        expressionSubstitutions: String, //';' separated equalities
//        stepsCount: Int,
//        originalExpressions: String, //';' separated
//        scope: String = "", //';' separated
//        characterEscapingDepth: Int = 1,
//        functionConfiguration: FunctionConfiguration = FunctionConfiguration(
//                scopeFilter = scope.split(";").filter { it.isNotEmpty() }.toSet()
//        ),
//        compiledConfiguration: CompiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration)
//): String {
//    val expressionTask = generateTask(
//            expressionSubstitutions.split(";").map {
//                val parts = it.split("=")
//                expressionSubstitutionFromStructureStrings(parts.first(), parts.last())
//            },
//            stepsCount,
//            originalExpressions.split(";").map { stringToExpression(it, compiledConfiguration = compiledConfiguration) }
//    )
//    return escapeCharacters("{" +
//            "\"originalExpression\":\"${expressionToString(expressionTask.originalExpression)}\"," +
//            "\"finalExpression\":\"${expressionToString(expressionTask.finalExpression)}\"," +
//            "\"requiredSubstitutions\":[${
//            expressionTask.requiredSubstitutions.joinToString(separator = ",") {
//                "{\"left\":\"${expressionToString(it.left)}\",\"right\":\"${expressionToString(
//                        it.right
//                )}\"}"
//            }
//            }]," +
//            "\"allSubstitutions\":[${
//            expressionTask.allSubstitutions.joinToString(separator = ",") {
//                "{\"left\":\"${expressionToString(it.left)}\",\"right\":\"${expressionToString(
//                        it.right
//                )}\"}"
//            }
//            }]" +
//            "}",
//            characterEscapingDepth)
//}


fun optGenerateSimpleComputationRule(
        expressionPartOriginal: ExpressionNode,
        simpleComputationRuleParams: SimpleComputationRuleParams
): MutableList<ExpressionSubstitution> {
    val result = mutableListOf<ExpressionSubstitution>()
    if (!simpleComputationRuleParams.isIncluded || expressionPartOriginal.calcComplexity() > 4 || expressionPartOriginal.getContainedFunctions().subtract(simpleComputationRuleParams.operationsMap.keys).isNotEmpty()){
        return result
    }
    val expressionPart = expressionPartOriginal.clone()
    expressionPart.variableReplacement(mapOf("π" to PI_STRING))
    if (expressionPart.getContainedVariables().isEmpty()) {
        if (expressionPart.children.isNotEmpty()) {
            val computed = expressionPart.computeNodeIfSimple(simpleComputationRuleParams) ?: return result
            result.add(ExpressionSubstitution(addRootNodeToExpression(expressionPart.clone()), addRootNodeToExpression(ExpressionNode(NodeType.VARIABLE, computed.toShortString()))))
        } else if (expressionPart.value.toDoubleOrNull() != null) { //add plus node
            val currentValue = expressionPart.value.toDoubleOrNull() ?: return result

            //x ~> (x-1) + 1
            val plusTreeNode = ExpressionNode(NodeType.FUNCTION, "+")
            plusTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, (currentValue - 1).toShortString()))
            plusTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, "1"))
            result.add(ExpressionSubstitution(addRootNodeToExpression(expressionPart.clone()), addRootNodeToExpression(plusTreeNode)))

            //x ~> (x+1) - 1
            val minusTreeNode = ExpressionNode(NodeType.FUNCTION, "+")
            minusTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, (currentValue + 1).toShortString()))
            minusTreeNode.addChild(ExpressionNode(NodeType.FUNCTION, "-"))
            minusTreeNode.children.last().addChild(ExpressionNode(NodeType.VARIABLE, "1"))
            result.add(ExpressionSubstitution(addRootNodeToExpression(expressionPart.clone()), addRootNodeToExpression(minusTreeNode)))

            val intCurrentValue = currentValue.toInt()
            if (1 < intCurrentValue && intCurrentValue < 145 && (intCurrentValue - currentValue).toReal().additivelyEqualToZero()
            ) {
                val sqrtValue = sqrt(currentValue).toInt()
                for (i in 2..sqrtValue) {
                    val div = intCurrentValue / i
                    if (intCurrentValue == i * div) {
                        val mulTreeNode = ExpressionNode(NodeType.FUNCTION, "*")
                        mulTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, i.toString()))
                        mulTreeNode.addChild(ExpressionNode(NodeType.VARIABLE, div.toString()))
                        result.add(ExpressionSubstitution(addRootNodeToExpression(expressionPart.clone()), addRootNodeToExpression(mulTreeNode)))
                    }
                }
            }
        }
    }
    return result
}