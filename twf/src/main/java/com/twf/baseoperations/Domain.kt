package com.twf.baseoperations

import com.twf.expressiontree.ExpressionNode

data class Segment (
        val leftBorder: ExpressionNode? = null,
        val rightBorder: ExpressionNode? = null,
        val point: ExpressionNode? = null,
        val isLeftBorderIncluded: Boolean = true,
        val isRightBorderIncluded: Boolean = true
)

data class SegmentsUnion (
        val segmentsUnion: MutableList<Segment> = mutableListOf()
)

data class VariableProperties (
        val name: String,
        val segmentsUnionsIntersection: MutableList<SegmentsUnion> = mutableListOf(),
        val unableToCompute: Boolean = false
)

class Domain (
        val baseOperationsDefinitions: BaseOperationsDefinitions = BaseOperationsDefinitions(),
        val expressions: ArrayList<ExpressionNode> = arrayListOf(),
        val maxValueMultiplier: Double = 10.0
) {
    val border: Double
    val variablesSet = mutableSetOf<VariableProperties>()
    init {
        var variablesNamesSet = mutableSetOf<String>()
        var maxConstant = 1.0 //TODO: fix wrong work even with factorial (if generated values less than 1.0)
        for (expression in expressions){
            variablesNamesSet.addAll(expression.getVariableNames())
            maxConstant = maxOf(expression.getMaxConstant(), maxConstant)
        }
        variablesNamesSet.forEach { variablesSet.add(VariableProperties(it)) }
        border = maxConstant * maxValueMultiplier
    }

    fun generateNewPoint (): MutableMap<String, String> {
        val result = mutableMapOf<String, String>()
        for (variable in variablesSet){
            if (variable.segmentsUnionsIntersection.isEmpty()){
                result.put(variable.name, com.twf.platformdependent.random(-border, border).toString())
            } else {
                TODO()
            }
        }
        return result
    }
}