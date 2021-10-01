package mathhelper.twf.expressiontree

import mathhelper.twf.numbers.CageHolder
import mathhelper.twf.numbers.LineSegmentHolder
import kotlin.math.PI

class ExpressionTreeAnalyzer(private val analyzingExpression: ExpressionNode) {
    private val expressionDimension : Int = analyzingExpression.getVariableNames().size

    private val functionByName = mapOf<String, (ExpressionNode, List<LineSegmentHolder>) -> CageHolder>(
            "" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                    bracketsDomain(expression, possibleValues) },
            "+" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                plusDomain(expression, possibleValues) },
            "*" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                mulDomain(expression, possibleValues) },
            "-" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                minusDomain(expression, possibleValues) },
            "/" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                divDomain(expression, possibleValues) },
            "^" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                powDomain(expression, possibleValues) },
            "mod" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                modDomain(expression, possibleValues) },
            "S" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                sumNDomain(expression, possibleValues) },
            "P" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                prodNDomain(expression, possibleValues) },

            "and" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                andDomain(expression, possibleValues) },
            "or" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                orDomain(expression, possibleValues) },
            "xor" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                xorDomain(expression, possibleValues) },
            "alleq" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                alleqDomain(expression, possibleValues) },
            "not" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                notDomain(expression, possibleValues) },

            "sin" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                sinDomain(expression, possibleValues) },
            "cos" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                cosDomain(expression, possibleValues) },
            "sh" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                sinhDomain(expression, possibleValues) },
            "ch" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                coshDomain(expression, possibleValues) },
            "tg" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                tanDomain(expression, possibleValues) },
            "th" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                tanhDomain(expression, possibleValues) },
            "asin" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                asinDomain(expression, possibleValues) },
            "acos" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                acosDomain(expression, possibleValues) },
            "atg" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                atanDomain(expression, possibleValues) },
            "exp" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                expDomain(expression, possibleValues) },
            "ln" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                lnDomain(expression, possibleValues) },
            "abs" to { expression : ExpressionNode, possibleValues : List<LineSegmentHolder> ->
                absDomain(expression, possibleValues) }

        )

    fun findDomain() : CageHolder {
        return findDomain(analyzingExpression, LineSegmentHolder.fullSegment)
    }

    private fun findDomain(expression: ExpressionNode, possibleValues: LineSegmentHolder) : CageHolder {
        return findDomain(expression, listOf(possibleValues))
    }

    private fun findDomain(expression: ExpressionNode, possibleValues : List<LineSegmentHolder>) : CageHolder {
        return functionByName[expression.value]!!.invoke(expression, possibleValues)
    }

    private fun plusDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        val childrenDomains = calculateChildrenDomains(expression, LineSegmentHolder.fullSegment)
        for (domain in childrenDomains) {
            if (domain.isEmpty()) {
                return domain
            }
        }

        // Should be right domain choosing from list of domains
        return CageHolder.domainIntersection(childrenDomains, expressionDimension)
    }

    private fun minusDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        val childrenDomains = calculateChildrenDomains(expression, LineSegmentHolder.fullSegment)
        for (domain in childrenDomains) {
            if (domain.isEmpty()) {
                return domain
            }
        }

        // Should be right domain choosing from list of domains
        return CageHolder.domainIntersection(childrenDomains, expressionDimension)
    }

    private fun mulDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        val childrenDomains = calculateChildrenDomains(expression, LineSegmentHolder.fullSegment)
        for (domain in childrenDomains) {
            if (domain.isEmpty()) {
                return domain
            }
        }

        // Should be right domain choosing from list of domains
        return CageHolder.domainIntersection(childrenDomains, expressionDimension)
    }

    private fun divDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        val childrenDomains = calculateChildrenDomains(expression, LineSegmentHolder.fullSegment)
        for (domain in childrenDomains) {
            if (domain.isEmpty()) {
                return domain
            }
        }

        // Should be right domain choosing from list of domains
        return CageHolder.domainIntersection(childrenDomains, expressionDimension)
    }

    private fun bracketsDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>)
            = findDomain(expression.children[0], possibleValues)

    private fun modDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        val childrenDomains = calculateChildrenDomains(expression, LineSegmentHolder.fullSegment)
        for (domain in childrenDomains) {
            if (domain.isEmpty()) {
                return domain
            }
        }

        // Should be right domain choosing from list of domains
        return CageHolder.domainIntersection(childrenDomains, expressionDimension)
    }

    private fun powDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (allSegmentsSubzero(possibleValues)) {
            return CageHolder.emptyCageHolder(expressionDimension)
        }

        val childrenDomains = ArrayList<CageHolder>()
        for (i in 0 until childrenDomains.size - 1) {
            val domain = findDomain(expression.children[i], LineSegmentHolder.positiveSegment)
            if (domain.isEmpty()) {
                return domain
            }
        }
        val lastChildDomain = findDomain(expression.children.last(), LineSegmentHolder.fullSegment)
        childrenDomains.add(lastChildDomain)

        // Should be right domain choosing from list of domains
        return CageHolder.domainIntersection(childrenDomains, expressionDimension)
    }

    private fun sumNDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        TODO("should be unfolded first")
    }

    private fun prodNDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        TODO("should be unfolded first")
    }

    private fun andDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (pointInSegments(possibleValues, 1.0)) {
            TODO("One of children should be equal to 0")
        }

        val childrenDomains = calculateChildrenDomains(expression, LineSegmentHolder.fullSegment)
        for (domain in childrenDomains) {
            if (domain.isEmpty()) {
                return domain
            }
        }

        // Should be right domain choosing from list of domains
        return CageHolder.domainIntersection(childrenDomains, expressionDimension)
    }

    private fun orDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (pointInSegments(possibleValues, 1.0)) {
            TODO("All children should be equal to 0")
        }

        val childrenDomains = calculateChildrenDomains(expression, LineSegmentHolder.fullSegment)
        for (domain in childrenDomains) {
            if (domain.isEmpty()) {
                return domain
            }
        }

        // Should be right domain choosing from list of domains
        return CageHolder.domainIntersection(childrenDomains, expressionDimension)
    }

    private fun xorDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (pointInSegments(possibleValues,0.0)) {
            TODO("All children should be equal to 0")
        }

        val childrenDomains = calculateChildrenDomains(expression, LineSegmentHolder.fullSegment)
        for (domain in childrenDomains) {
            if (domain.isEmpty()) {
                return domain
            }
        }

        // Should be right domain choosing from list of domains
        return CageHolder.domainIntersection(childrenDomains, expressionDimension)
    }

    private fun alleqDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        TODO("No ideas, infinitely small domain")
    }

    private fun notDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (pointInSegments(possibleValues,0.0)) {
            TODO("Child should be equal to 0")
        }

        return findDomain(expression.children[0], LineSegmentHolder.fullSegment)
    }

    private fun sinDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (fullyContains(LineSegmentHolder(0.0, 1.0), possibleValues)) {
            return findDomain(expression.children[0], LineSegmentHolder.fullSegment)
        }

        TODO("Problems with periodicity")
    }

    private fun cosDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (fullyContains(LineSegmentHolder(0.0, 1.0), possibleValues)) {
            return findDomain(expression.children[0], LineSegmentHolder.fullSegment)
        }

        TODO("Problems with periodicity")
    }

    private fun tanDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        TODO("Problems with periodicity")
    }

    private fun sinhDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        TODO("Unclear how to do")
    }

    private fun coshDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (allSegmentsSubzero(possibleValues)) {
            return CageHolder.emptyCageHolder(expressionDimension)
        }

        TODO("Unclear how to do")
    }

    private fun tanhDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        TODO("Absolutely unclear how to do")
    }

    private fun asinDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (emptyIntersection(LineSegmentHolder(-PI / 2, PI / 2), possibleValues)) {
            return CageHolder.emptyCageHolder(expressionDimension)
        }

        if (fullyContains(LineSegmentHolder(-PI / 2, PI / 2), possibleValues)) {
            return findDomain(expression.children[0], LineSegmentHolder(0.0, 1.0))
        }

        TODO("Not so hard")
    }

    private fun acosDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (emptyIntersection(LineSegmentHolder(0.0, PI), possibleValues)) {
            return CageHolder.emptyCageHolder(expressionDimension)
        }

        if (fullyContains(LineSegmentHolder(0.0, PI), possibleValues)) {
            return findDomain(expression.children[0], LineSegmentHolder(0.0, 1.0))
        }

        TODO("Not so hard")
    }

    private fun atanDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (emptyIntersection(LineSegmentHolder(-PI / 2, PI / 2), possibleValues)) {
            return CageHolder.emptyCageHolder(expressionDimension)
        }

        if (fullyContains(LineSegmentHolder(-PI / 2, PI / 2), possibleValues)) {
            return findDomain(expression.children[0], LineSegmentHolder(0.0, 1.0))
        }

        TODO("Not so hard")
    }

    private fun expDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (allSegmentsSubzero(possibleValues)) {
            return CageHolder.emptyCageHolder(expressionDimension);
        }

        TODO("Not so hard")
    }

    private fun lnDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        TODO("Not so hard")
    }

    private fun absDomain(expression: ExpressionNode, possibleValues: List<LineSegmentHolder>) : CageHolder {
        if (allSegmentsSubzero(possibleValues)) {
            return CageHolder.emptyCageHolder(expressionDimension);
        }

        TODO("Not so hard")
    }

    private fun calculateChildrenDomains(expression: ExpressionNode,
                                         segment: LineSegmentHolder) : List<CageHolder> {
        val childrenDomains = ArrayList<CageHolder>()
        for (child in expression.children) {
            childrenDomains.add(findDomain(child, segment))
        }

        return childrenDomains
    }

    private fun pointInSegments(possibleValues: List<LineSegmentHolder>, point : Double) : Boolean {
        for (segment in possibleValues) {
            if (segment.pointInSegment(point)) {
                return true
            }
        }

        return false
    }

    private fun allSegmentsSubzero(segments: List<LineSegmentHolder>) : Boolean {
        for (segment in segments) {
            if (!segment.isSubzero()) {
                return false
            }
        }

        return true
    }

    // Supposed that domains in list does not intersect
    private fun fullyContains(segment: LineSegmentHolder, possibleValues: List<LineSegmentHolder>) : Boolean {
        for (possibleSegment in possibleValues) {
            if (segment.isSubSegment(possibleSegment)) {
                return true
            }
        }

        return false
    }

    private fun emptyIntersection(segment: LineSegmentHolder, possibleValues: List<LineSegmentHolder>) : Boolean {
        for (possibleSegment in possibleValues) {
            if (segment.hasIntersection(possibleSegment)) {
                return false
            }
        }

        return true
    }
}

// All TODO("Not so hard") are similar, just iterate through list and apply inverse function and some optimize domains