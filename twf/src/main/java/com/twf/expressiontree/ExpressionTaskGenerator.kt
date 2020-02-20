package com.twf.expressiontree

import com.twf.baseoperations.BaseOperationsComputation.Companion.epsilon
import com.twf.config.CompiledConfiguration
import com.twf.platformdependent.random
import com.twf.platformdependent.randomInt

class ExpressionTask(
        val originalExpression: ExpressionNode,
        val finalExpression: ExpressionNode,
        val requiredSubstitutions: List<ExpressionSubstitution>, //applied in task generation in order of application
        val allSubstitutions: List<ExpressionSubstitution>
)

fun generateExpressionTask (
        expressionSubstitutions: List<ExpressionSubstitution>, //if substitution can be applied in both directions, it has to be specified twice
        stepsCount: Int,
        originalExpressions: List<ExpressionNode> //it's better to set original expression manually, to choose correct number of variables
): ExpressionTask {
    val originalExpression = if (originalExpressions.size == 1)
        originalExpressions.first()
    else
        originalExpressions[randomInt(0,originalExpressions.lastIndex)]
    var currentExpression: ExpressionNode = originalExpression.clone()
    val requiredSubstitutions = mutableListOf<ExpressionSubstitution>()

    for (i in 1..stepsCount){
        val functionsInExpression = currentExpression.getContainedFunctions()
        val appropriateSubstitutions = mutableListOf<ExpressionSubstitution>()
        for (expressionSubstitution in expressionSubstitutions){
            if (expressionSubstitution.leftFunctions.intersect(functionsInExpression).isNotEmpty() &&
                    expressionSubstitution.findAllPossibleSubstitutionPlaces(currentExpression).isNotEmpty()){
                appropriateSubstitutions.add(expressionSubstitution)
            }
        }

        val appropriateSubstitutionWeight = appropriateSubstitutions.sumByDouble { it.weight }
        if (appropriateSubstitutionWeight <= epsilon){
            break //no allowed substitutions found
        }

        var selector = random(0.0, appropriateSubstitutionWeight)
        var currentSubstitutionIndex = 0
        while (selector > appropriateSubstitutions[currentSubstitutionIndex].weight){
            selector -= appropriateSubstitutions[currentSubstitutionIndex].weight
            currentSubstitutionIndex++
        }

        val selectedSubstitution = appropriateSubstitutions[currentSubstitutionIndex]
        requiredSubstitutions.add(ExpressionSubstitution(selectedSubstitution.left, selectedSubstitution.right))
        val places = selectedSubstitution.findAllPossibleSubstitutionPlaces(currentExpression)
        if (places.size == 1){
            selectedSubstitution.applySubstitution(places)
        } else {
            val partSize = selectedSubstitution.weight / places.size
            val index = (selector / partSize).toInt()
            selectedSubstitution.applySubstitution(places.subList(index, index+1))
        }
    }

    return ExpressionTask(originalExpression, currentExpression, requiredSubstitutions, expressionSubstitutions)
}