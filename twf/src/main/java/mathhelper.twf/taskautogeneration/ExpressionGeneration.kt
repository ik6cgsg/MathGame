package mathhelper.twf.taskautogeneration

import mathhelper.twf.config.CompiledConfiguration
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.twf.expressiontree.NodeType
import mathhelper.twf.platformdependent.randomInt

fun trigonometryShortMultiplicationStartExpressionGeneration(compiledConfiguration: CompiledConfiguration): GeneratedExpression {
    val result = GeneratedExpression(ExpressionNode(NodeType.FUNCTION, ""),
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "standard_math",
            mutableSetOf("trigonometry", "short_multiplication")
    )
    val trigonometryFunctions = listOf("Sin", "Cos", "Tg", "Ctg")
    val capitalizedFunctionA = trigonometryFunctions[randomInt(0,trigonometryFunctions.size)]
    val capitalizedFunctionB = trigonometryFunctions[randomInt(0,trigonometryFunctions.size)]
    val functionA = capitalizedFunctionA.toLowerCase()
    val functionB = capitalizedFunctionB.toLowerCase()
    result.tags!!.add(functionA)
    result.tags!!.add(functionB)
    val variableA = "x"
    val variableB = if (functionA == functionB) "y" else ('x' + randomInt(0, 2)).toString()
    val nodeA = compiledConfiguration.createExpressionFunctionNode(functionA, 1).apply { addChild(compiledConfiguration.createExpressionVariableNode(variableA)) }
    val nodeB = compiledConfiguration.createExpressionFunctionNode(functionB, 1).apply { addChild(compiledConfiguration.createExpressionVariableNode(variableB)) }
    val pow = randomInt(2,4)

    if (randomInt(0,2) == 1) {
        //sum or diff of pows
        val powWord = if (pow == 2) "Squares" else "Cubes"
        val powWordRu = if (pow == 2) "квадратов" else "кубов"
        val powedA = compiledConfiguration.createExpressionFunctionNode("^", -1).apply { addChild(nodeA)
            addChild(compiledConfiguration.createExpressionVariableNode(pow.toString())) }
        val powedB = compiledConfiguration.createExpressionFunctionNode("^", -1).apply { addChild(nodeB)
            addChild(compiledConfiguration.createExpressionVariableNode(pow.toString())) }
        result.expressionNode.addChild(compiledConfiguration.createExpressionFunctionNode("+", -1).apply {
            addChild(powedA)
        })
        if (randomInt(0,2) == 1) {
            result.code += "Sum$powWord$capitalizedFunctionA$variableA$capitalizedFunctionB$variableB"
            result.nameEn += "Sum of $powWord of $capitalizedFunctionA and $capitalizedFunctionB"
            result.nameRu += "Сумма $powWordRu $functionA и $functionB"
            result.expressionNode.children.first().addChild(powedB)
        } else {
            result.code += "Diff$powWord$capitalizedFunctionA$variableA$capitalizedFunctionB$variableB"
            result.nameEn += "Difference of $powWord of $capitalizedFunctionA and $capitalizedFunctionB"
            result.nameRu += "Разность $powWordRu $functionA и $functionB"
            result.expressionNode.children.first().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
            result.expressionNode.children.first().children.last().addChild(powedB)
        }
    } else {
        //pow of sum or diff
        val powWord = if (pow == 2) "Square" else "Cube"
        val powWordRu = if (pow == 2) "Квадрат" else "Куб"
        result.expressionNode.addChild(compiledConfiguration.createExpressionFunctionNode("^", -1).apply {
            addChild(compiledConfiguration.createExpressionFunctionNode("+", -1))
            addChild(compiledConfiguration.createExpressionVariableNode(pow.toString()))
        })
        result.expressionNode.children.first().children.first().addChild(nodeA)
        if (randomInt(0,2) == 1) {
            result.code += "${powWord}Sum$capitalizedFunctionA$variableA$capitalizedFunctionB$variableB"
            result.nameEn += "$powWord of Sum of $capitalizedFunctionA and $capitalizedFunctionB"
            result.nameRu += "$powWordRu суммы $functionA и $functionB"
            result.expressionNode.children.first().children.first().addChild(nodeB)
        } else {
            result.code += "${powWord}Diff$capitalizedFunctionA$variableA$capitalizedFunctionB$variableB"
            result.nameEn += "$powWord of Difference of $capitalizedFunctionA and $capitalizedFunctionB"
            result.nameRu += "$powWordRu разности $functionA и $functionB"
            result.expressionNode.children.first().children.first().addChild(compiledConfiguration.createExpressionFunctionNode("-", -1))
            result.expressionNode.children.first().children.first().children.last().addChild(nodeB)
        }
    }

    return result
}