package mathhelper.twf.baseoperations

import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.twf.platformdependent.defaultRandom
import mathhelper.twf.platformdependent.random
import mathhelper.twf.platformdependent.randomInt
import kotlin.math.max
import kotlin.math.min


data class DomainSegment(
    val leftBorder: Double,
    val leftBorderInc: Boolean,
    val rightBorder: Double,
    val rightBorderInc: Boolean
) {
    fun contains(point: Double): Boolean = (leftBorder < point || (leftBorderInc && point == leftBorder)) && (point < rightBorder || (rightBorderInc && point == rightBorder))


    fun shiftBy(shift: Double) = DomainSegment(leftBorder + shift, leftBorderInc, rightBorder + shift, rightBorderInc)

    fun scaleBy(scale: Double) = DomainSegment(leftBorder * scale, leftBorderInc, rightBorder * scale, rightBorderInc)

    fun generateRandomPoint(): Double {
        var from = leftBorder
        if (!leftBorderInc) {
            from += 1e-9
        }
        var to = rightBorder
        if (rightBorderInc) {
            to += 1e-9
        }

        return random(from, to)
    }
    companion object {
        fun inclusive(left: Double, right: Double) = DomainSegment(left, true, right, true)

        fun intersect(a: DomainSegment, b: DomainSegment): DomainSegment? {
            val isIntersecting = a.leftBorder <= b.rightBorder && b.leftBorder <= a.rightBorder
            if (!isIntersecting) return null
            val newLeft = max(a.leftBorder, b.leftBorder)
            val newRight = min(a.rightBorder, b.rightBorder)
            val leftIncl = if (newLeft == a.leftBorder) a.leftBorderInc else b.leftBorderInc
            val rightIncl = if (newRight == a.rightBorder) a.rightBorderInc else b.rightBorderInc

            return DomainSegment(newLeft, leftIncl, newRight, rightIncl)
        }

        fun intersectSegments(a: Set<DomainSegment>, b: Set<DomainSegment>): Set<DomainSegment> {
            return a.union(b).sortedWith(compareBy({it.leftBorder}, {it.rightBorder})).toSet()
        }
    }
}



class DefinitionDomain(
        val points: Set<Double> = mutableSetOf(),
        val excludedPoints: Set<Double> = mutableSetOf(),
        val segments: Set<DomainSegment> = mutableSetOf()
) {
    fun isInDomain(point: Double): Boolean = !excludedPoints.contains(point) && (point in points || segments.any { segment -> segment.contains(point) })

    fun generateNewPoint(): Double {
        if (points.isEmpty() && segments.isEmpty()) {
            throw EmptyDomainException()
        }

        var generated = unsafeGenerateNewPoint()
        while (excludedPoints.contains(generated)) generated = unsafeGenerateNewPoint()
        return generated
    }

    private fun unsafeGenerateNewPoint(): Double =
        if (points.isEmpty()) {
            generateFromSegments()
        } else if (segments.isEmpty()) {
            generateFromPoints()
        } else {
            if (defaultRandom() < 0.6) generateFromSegments() else generateFromPoints()
        }

    private fun generateFromPoints(): Double {
        val randnum = randomInt(0, points.size)
        var got = 0
        while (got < randnum) {
            for (p in points) {
                got += 1
                if (got > randnum) return p
            }
        }
        throw IllegalStateException()
    }

    private fun generateFromSegments(): Double {
        val randnum = randomInt(0, segments.size)
        var got = 0
        while (got < randnum) {
            for (p in segments) {
                got += 1
                if (got > randnum) return p.generateRandomPoint()
            }
        }
        throw IllegalStateException()
    }

    fun intersectWith(other: DefinitionDomain) = DefinitionDomain(
            points.intersect(other.points),
            excludedPoints.union(other.excludedPoints),
            DomainSegment.intersectSegments(this.segments, other.segments)
    )

    fun shiftBy(shift: Double): DefinitionDomain = DefinitionDomain(
            points.map { it + shift }.toSet(),
            excludedPoints.map { it + shift }.toSet(),
            segments.map { it.shiftBy(shift) }.toSet())

    fun scaleBy(scale: Double): DefinitionDomain = DefinitionDomain(
            points.map { it * scale }.toSet(),
            excludedPoints.map { it * scale }.toSet(),
            segments.map { it.scaleBy(scale) }.toSet())

    fun except(vararg newPoints: Double): DefinitionDomain = DefinitionDomain(
            points,
            newPoints.toMutableSet().plus(excludedPoints),
            segments
    )

    fun rightMostElement(): Double {
        return funcOrNull({a: Double, b: Double -> max(a, b)}, rightMostPoint(), rightMostFromSequence())
    }

    fun leftMostElement(): Double {
        return funcOrNull({a: Double, b: Double -> min(a, b)}, leftMostPoint(), leftMostFromSequence())
    }

    private fun funcOrNull(func: (l: Double, r: Double) -> Double, p: Double?, r: Double?): Double {
        return if (p == null && r == null) {
            throw EmptyDomainException()
        } else if (p == null && r != null) {
            r
        } else if (r == null && p != null) {
            p
        } else if (r != null && p != null) {
            func(p, r)
        } else {
            throw IllegalStateException()
        }
    }

    private fun leftMostPoint(): Double? =
            points.sorted().asSequence().filter { !excludedPoints.contains(it) }.firstOrNull()

    private fun rightMostPoint(): Double? =
            points.sortedDescending().asSequence().filter { !excludedPoints.contains(it) }.firstOrNull()

    private fun leftMostFromSequence(): Double? =
            segments.asSequence().sortedWith(compareBy({it.leftBorder}, {it.rightBorder})).firstOrNull()?.leftBorder

    private fun rightMostFromSequence(): Double? =
            segments.asSequence().sortedWith(compareBy({-it.rightBorder}, {-it.leftBorder})).firstOrNull()?.rightBorder

    override fun toString(): String
        = StringBuilder()
            .append("DefinitionDomain(points=")
            .append(points)
            .append(", excludedPoints=")
            .append(excludedPoints)
            .append(", segments=")
            .append(segments)
            .append(")").toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
//        if (javaClass != other?.javaClass) return false

        other as DefinitionDomain

        if (points != other.points) return false
        if (excludedPoints != other.excludedPoints) return false
        if (segments != other.segments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = points.hashCode()
        result = 31 * result + excludedPoints.hashCode()
        result = 31 * result + segments.hashCode()
        return result
    }

    companion object {
        fun inclusive(left: Double, right: Double) = DefinitionDomain(segments = mutableSetOf(DomainSegment.inclusive(left, right)))
    }
}

class EmptyDomainException : Throwable() {

}

class AnalyticallyDefinedDomain(
        val predicates: Collection<(Double) -> Boolean>
) {
    fun isInDomain(point: Double): Boolean = predicates.all { it.invoke(point) }

    fun generateNewPoint(): Double = TODO()

    fun intersectWith(other: AnalyticallyDefinedDomain) = AnalyticallyDefinedDomain(predicates.plus(other.predicates))
}


val DomainAll = DefinitionDomain(points = emptySet(), segments = setOf(DomainSegment.inclusive(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)))
val DomainNil = DefinitionDomain(points = emptySet(), segments = emptySet())

class MultivariateDefinitionDomain(
        val expression: ExpressionNode
) {
    val variables: MutableMap<String, DefinitionDomain> = expression.getVariableNames().associateWith { DomainAll }.toMutableMap()

    fun set(varname: String, vardomain: DefinitionDomain) {
        if (variables.containsKey(varname)) { variables[varname] = vardomain }
    }

    fun get(varname: String) = variables[varname]

    fun generateNewPoint(): Map<String, Double> = variables.mapValues { entry -> entry.value.generateNewPoint() }

    fun intersect(other: MultivariateDefinitionDomain): Map<String, DefinitionDomain> =
            this.variables.keys.union(other.variables.keys).associateWith {
                variables.get(it)!!.intersectWith(other.variables.get(it)!!)
            }
}
