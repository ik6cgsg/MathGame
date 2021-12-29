package mathhelper.twf.testpointgeneration

import mathhelper.twf.config.ComparisonType
import mathhelper.twf.config.CompiledConfiguration
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.twf.factstransformations.ExpressionComparison
import mathhelper.twf.factstransformations.ExpressionComparisonExpanse
import mathhelper.twf.platformdependent.random


data class VariableLink (
        val targetVariable: String,
        val computingExpression: ExpressionNode,
        val sourceVariables: Set<String>,
        val comparisonType: ComparisonType // targetVariable <comparisonType> computingExpression
)

data class VariableData (
        val name: String,
        val fromVariableLinks: MutableMap<String,VariableLink> = mutableMapOf(), //variable as source
        val toVariableLinks: MutableMap<String,VariableLink> = mutableMapOf(), //variable as target, it can bu computed by each link
        val mandatorySources: Set<String> = mutableSetOf() //variables, that must be computed before this variable (from connections like 'a = n!' where it's easier to compute n than a)
)

class TestPointGenerator (val compiledConfiguration: CompiledConfiguration,
                          val expressionComparisonExpanse: ExpressionComparisonExpanse = ExpressionComparisonExpanse(compiledConfiguration)) {
    val variablesData = mutableMapOf<String, VariableData>()
    val possibleStartVariables = mutableSetOf<String>()

    fun addVariableLinkToVariable (name: String, variableLink: VariableLink){
        val variableLinks = variablesData[name] ?: VariableData(name)
        if (variableLink.targetVariable == name) {
        }
    }

    fun addCondition(expressionComparison: ExpressionComparison) {
        // Function adds applicable connections to the graph. For Example, if condition is "n!=m*k", relations "n!/m -> k" and "n!/k -> m" are added, but no relation for n!
        // if comparison is inequality - assume that all variables are positive
        val variableRelations = mutableListOf<ExpressionComparison>()
        expressionComparisonExpanse.expanseGenerator(expressionComparison, variableRelations, true)
        val targetVariables = mutableSetOf<String>()
        val allVariables = expressionComparison.leftExpression.data.getContainedVariables() + expressionComparison.rightExpression.data.getContainedVariables()
        for (relation in variableRelations) {
            val sourceVariables = relation.rightExpression.data.getContainedVariables()


            val variableLink = VariableLink(relation.leftExpression.data.value, relation.rightExpression.data, sourceVariables, relation.comparisonType)

        }
    }

    fun generateNewPoint (): MutableMap<String, String> {
        val variableUninitializedSourcesCount = mutableMapOf<String, Int>()


        val result = mutableMapOf<String, String>()
//        for (variable in variablesSet){
//            if (variable.segmentsUnionsIntersection.isEmpty()){
//                result.put(variable.name, random(-border, border).toString())
//            } else {
//                TODO()
//            }
//        }
        return result
    }
}