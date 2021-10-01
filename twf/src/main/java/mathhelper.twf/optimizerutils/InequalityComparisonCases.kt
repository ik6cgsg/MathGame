package mathhelper.twf.optimizerutils

import mathhelper.twf.baseoperations.BaseOperationsComputation
import mathhelper.twf.baseoperations.ComputationType
import mathhelper.twf.config.ComparisonType
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.twf.expressiontree.NodeType
import mathhelper.twf.standartlibextensions.abs
import kotlin.math.*

enum class Case(val string: String) { ONE_DIMENSIONAL_POLYNOMIAL("one dimensional polynomial"), NONE("none") }

class SpecialCaseSolver(
        val left: ExpressionNode,
        val right: ExpressionNode
) {
    var case = Case.NONE
    private val eps: Double = 1E-7

    init {
        if (isPolynomial(left) && isPolynomial(right)) {
            val leftVariables = left.getContainedVariables()
            val rightVariables = right.getContainedVariables()
            if (leftVariables.size <= 1 && rightVariables.size <= 1 && leftVariables.union(rightVariables).size <= 1) {
                case = Case.ONE_DIMENSIONAL_POLYNOMIAL
            }
        }
    }

    fun currentCase() = case

    fun solve(comparisonType: ComparisonType, leftBorder: Double = Double.NEGATIVE_INFINITY, rightBorder: Double = Double.POSITIVE_INFINITY): Boolean {
        when (case) {
            Case.ONE_DIMENSIONAL_POLYNOMIAL -> return solveForPolynomials(comparisonType, leftBorder, rightBorder)
            else -> throw Exception("I can't solve this type of tasks")  //TODO: change on check by usual random points generation
        }
    }

    private val preciseSolutionDegree = 10
    private val approximatePolynomialSolutionDegree = 100

    private fun polynomialCanBeNegative(
            coefs: ArrayList<Double>,
            leftBorder: Double,
            rightBorder: Double
    ): Boolean {
        val deg = getPolynomialDegree(coefs)
        if (deg < 0)
            return false
        if (deg == 0)
            return coefs[0] < -eps
        if (valueAt(coefs, leftBorder) < -eps || valueAt(coefs, rightBorder) < -eps)
            return true
        if (deg < preciseSolutionDegree){
            return when (deg) {
                1 -> solutionForPolynomialWithDeg1(coefs, leftBorder, rightBorder)
                2 -> solutionForPolynomialWithDeg2(coefs, leftBorder, rightBorder)
                3 -> solutionForPolynomialWithDeg3(coefs, leftBorder, rightBorder)
                4 -> solutionForPolynomialWithDeg4(coefs, leftBorder, rightBorder)
                else -> solutionForPolynomialWithMediumDeg(coefs, leftBorder, rightBorder)
            }
        } else if (deg < approximatePolynomialSolutionDegree)
            return solutionForPolynomialWithLargeDeg(coefs, leftBorder, rightBorder)
        else throw Exception("I can't solve this type of tasks")   //TODO: change on check by usual random points generation
    }

    private fun getPolynomialRoots(
            coefs: ArrayList<Double>,
            leftBorder: Double,
            rightBorder: Double,
            iterCount: Int = 20,
            newtonIterationCount: Int = 5
    ): ArrayList<Double> {
        val deg = getPolynomialDegree(coefs)
        if (deg <= 0)
            return arrayListOf()
        if (deg == 1) {
            val root = -coefs[0] / coefs[1]
            if (root < leftBorder - eps || root > rightBorder + eps)
                return arrayListOf()
            return arrayListOf(root)
        }
        val derivative = derivativeForPolynomial(coefs)
        val derivativeRoots = getPolynomialRoots(derivative, leftBorder, rightBorder, iterCount, newtonIterationCount)
        val values = ArrayList<Double>()
        if (derivativeRoots.isEmpty() || derivativeRoots[0] - leftBorder > eps) {
            derivativeRoots.add(0, leftBorder)
            values.add(valueAt(coefs, leftBorder))
        }
        for (i in 1 until derivativeRoots.size)
            values.add(valueAt(coefs, derivativeRoots[i]))
        if (derivativeRoots.isEmpty() || rightBorder - derivativeRoots[derivativeRoots.size - 1] > eps) {
            derivativeRoots.add(rightBorder)
            values.add(valueAt(coefs, rightBorder))
        }
        val roots = ArrayList<Double>()
        for (i in 1 until values.size) {
            if (sign(values[i - 1]) * sign(values[i]) == 1)
                continue
            if (sign(values[i - 1]) == 0)
                roots.add(derivativeRoots[i - 1])
            else {
                if (sign(values[i]) == 0 && i + 1 < values.size)
                    continue
                var l = derivativeRoots[i - 1]
                var r = derivativeRoots[i]
                for (iter in 0 until iterCount) {
                    var m = (l + r) / 2
                    if (l > 0 || r < 0)
                        m = sqrt(l * r) * sign(l)
                    else {
                        if (l * l < r && r > 1)
                            m = min(sqrt(r), m)
                        if (r * r < -l && l < -1)
                            m = max(-sqrt(-l), m)
                    }
                    if (sign(valueAt(coefs, m)) != sign(values[i]))
                        l = m
                    else
                        r = m
                }
                for (iter in 0 until newtonIterationCount)
                    l += -valueAt(coefs, l) / valueAt(derivative, l)
                if (sign(valueAt(coefs, l)) == 0)
                    roots.add(l)
            }
        }
        return roots
    }

    private fun isPolynomial(node: ExpressionNode): Boolean {
        for (child in node.children) {
            if (!isPolynomial(child))
                return false
        }
        if (node.nodeType == NodeType.FUNCTION) {
            if (node.value == "+" || node.value == "*" || node.value == "-" || node.value == "")
                return true
            if (node.value == "^") {
                if (node.children.size != 2)
                    return false
                val n = node.children[1].value.toIntOrNull()
                return n != null && n >= 0
            }
            return false
        }
        return true
    }

    private fun solveForPolynomials(comparisonType: ComparisonType, leftBorder: Double, rightBorder: Double): Boolean {
        val leftPoly = toPolynomial(left)
        val rightPoly = toPolynomial(right)
        for (i in 0 until max(leftPoly.size, rightPoly.size)) {
            if (i >= leftPoly.size)
                leftPoly.add(0.0)
            if (i < rightPoly.size)
                leftPoly[i] -= rightPoly[i]
        }
        if (comparisonType == ComparisonType.LEFT_MORE_OR_EQUAL)
            return !polynomialCanBeNegative(leftPoly, leftBorder, rightBorder)
        if (comparisonType == ComparisonType.LEFT_MORE) {
            leftPoly[0] -= 5 * eps
            return !polynomialCanBeNegative(leftPoly, leftBorder, rightBorder)
        }
        for (i in leftPoly.indices)
            leftPoly.set(i, -leftPoly[i])
        if (comparisonType == ComparisonType.LEFT_LESS_OR_EQUAL)
            return !polynomialCanBeNegative(leftPoly, leftBorder, rightBorder)
        if (comparisonType == ComparisonType.LEFT_LESS) {
            leftPoly[0] -= 5 * eps
            return !polynomialCanBeNegative(leftPoly, leftBorder, rightBorder)
        }
        if (comparisonType == ComparisonType.EQUAL) {
            if (leftBorder != Double.NEGATIVE_INFINITY && rightBorder != Double.POSITIVE_INFINITY && leftBorder == rightBorder)
                return sign(valueAt(leftPoly, leftBorder)) == 0
            return getPolynomialDegree(leftPoly) == -1
        }
        return false
    }

    fun toPolynomial(node: ExpressionNode): ArrayList<Double> {
        if (node.nodeType == NodeType.VARIABLE) {
            if (node.isNumberValue())
                return arrayListOf(node.value.toDouble())
            return arrayListOf(0.0, 1.0)
        }
        if (node.nodeType == NodeType.FUNCTION) {
            if (node.value == "+") {
                val res = arrayListOf<Double>()
                for (child in node.children) {
                    val childPoly = toPolynomial(child)
                    for (i in 0 until childPoly.size) {
                        if (i >= res.size)
                            res.add(0.0)
                        res[i] += childPoly[i]
                    }
                }
                return res
            }
            if (node.value == "-") {
                val res = toPolynomial(node.children[0])
                for (i in res.indices)
                    res.set(i, -res[i])
                return res
            }
            if (node.value == "*") {
                var res = arrayListOf(1.0)
                for (child in node.children)
                    res = multiplyPolynomials(res, toPolynomial(child))
                return res
            }
            if (node.value == "^") {
                var res = toPolynomial(node.children[0])
                res = power(res, node.children[1].value.toInt())
                return res
            }
            if (node.value == "")
                return toPolynomial(node.children[0])
        }
        //assert(false)  ///TODO: fix
        return arrayListOf()
    }

    private fun multiplyPolynomials(a: ArrayList<Double>, b: ArrayList<Double>): ArrayList<Double> {
        val res = ArrayList<Double>()
        for (i in 0 until a.size + b.size - 1)
            res.add(0.0)
        for (i in a.indices)
            for (j in b.indices)
                res[i + j] += a[i] * b[j]
        return res
    }

    private fun power(poly: ArrayList<Double>, n: Int): ArrayList<Double> {
        var t = n
        var cur = poly
        var res = arrayListOf(1.0)
        while (t > 0) {
            if (t % 2 == 1)
                res = multiplyPolynomials(res, cur)
            cur = multiplyPolynomials(cur, cur)
            t /= 2
        }
        return res
    }

    private fun sign(x: Double): Int {
        if (x < -eps)
            return -1
        if (x > eps)
            return 1
        return 0
    }

    private fun valueAt(coef: ArrayList<Double>, point: Double): Double {
        if (point > Double.NEGATIVE_INFINITY && point < Double.POSITIVE_INFINITY) {
            var cur = 1.0
            var res = 0.0
            for (c in coef) {
                res += c * cur
                cur *= point
            }
            return res
        }
        val deg = getPolynomialDegree(coef)
        if (deg % 2 == 0 || point == Double.POSITIVE_INFINITY)
            return if (coef[deg] > eps) Double.POSITIVE_INFINITY else Double.NEGATIVE_INFINITY
        return if (coef[deg] > eps) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
    }

    fun derivativeForPolynomial(coefs: ArrayList<Double>): ArrayList<Double> {
        val deg = getPolynomialDegree(coefs)
        if (deg <= 0)
            return arrayListOf(0.0)
        val derivative = ArrayList<Double>()
        for (i in 0 until deg)
            derivative.add(coefs[i + 1] * (i + 1))
        return derivative
    }

    private fun getPolynomialDegree(coefs: ArrayList<Double>): Int {
        var deg = coefs.size - 1
        while (deg >= 0 && coefs[deg].abs() < eps)
            deg--
        return deg
    }

    private fun solutionForPolynomialWithDeg1(coefs: ArrayList<Double>, leftBorder: Double, rightBorder: Double): Boolean {
        //assert(coefs.size == 2 && sign(coefs[1]) != 0)   //TODO: fix asserts
        val root = -coefs[0] / coefs[1]
        val pointsToCheck = arrayListOf(leftBorder, rightBorder)
        if (leftBorder - eps < root && root < rightBorder + eps)
            pointsToCheck.add(root)
        return checkCertainPoints(coefs, pointsToCheck)
    }

    private fun solutionForPolynomialWithDeg2(coefs: ArrayList<Double>, leftBorder: Double, rightBorder: Double): Boolean {
        //assert(coefs.size == 3 && sign(coefs[2]) != 0)   //TODO: fix asserts
        val pointsToCheck = arrayListOf(leftBorder, rightBorder)
        val vert = -coefs[1] / 2 / coefs[2]
        if (leftBorder - eps < vert && vert < rightBorder + eps)
            pointsToCheck.add(vert)
        return checkCertainPoints(coefs, pointsToCheck)
    }

    private fun solutionForPolynomialWithDeg3(coefs: ArrayList<Double>, leftBorder: Double, rightBorder: Double): Boolean {
        //assert(coefs.size == 4 && sign(coefs[3]) != 0)   //TODO: fix asserts
        val pointsToCheck = arrayListOf(leftBorder, rightBorder)
        val semiDiscriminant = coefs[2] * coefs[2] - 3 * coefs[3] - coefs[1]
        if (semiDiscriminant > eps) {
            val r1 = (-coefs[2] + sqrt(semiDiscriminant)) / 3 / coefs[3]
            if (r1 > leftBorder - eps && r1 < rightBorder + eps)
                pointsToCheck.add(r1)
            val r2 = (-coefs[2] - sqrt(semiDiscriminant)) / 3 / coefs[3]
            if (r2 > leftBorder - eps && r2 < rightBorder + eps)
                pointsToCheck.add(r2)
        }
        return checkCertainPoints(coefs, pointsToCheck)
    }

    private fun solutionForPolynomialWithDeg4(coefs: ArrayList<Double>, leftBorder: Double, rightBorder: Double): Boolean { //may be rewrite with direct formulas
        //assert(coefs.size == 5 && sign(coefs[4]) != 0)   //TODO: fix asserts
        val derivative = derivativeForPolynomial(coefs)
        val pointsToCheck = arrayListOf(leftBorder, rightBorder)
        val roots = rootsOfCubicPolynomial(derivative)
        for (root in roots)
            if (root > leftBorder - eps && root < rightBorder + eps)
                pointsToCheck.add(root)
        return checkCertainPoints(coefs, pointsToCheck)
    }

    private fun solutionForPolynomialWithMediumDeg(
            coefs: ArrayList<Double>,
            leftBorder: Double,
            rightBorder: Double
    ): Boolean {
        val derivative = derivativeForPolynomial(coefs)
        val left = max(leftBorder, -1e9)
        val right = min(rightBorder, 1e9)
        val derivativeRoots = getPolynomialRoots(derivative, left, right)
        derivativeRoots.add(left)
        derivativeRoots.add(right)
        for (root in derivativeRoots) {
            if (valueAt(coefs, root) < -eps)
                return true
        }
        return false
    }

    private fun solutionForPolynomialWithLargeDeg(
            coefs: ArrayList<Double>,
            leftBorder: Double,
            rightBorder: Double,
            iterCount: Int = 10
    ): Boolean {
        if (solutionForPolynomialWithMediumDeg(coefs, leftBorder, rightBorder))
            return true
        val pointsToCheck = arrayListOf(leftBorder, rightBorder)
        val derivative = derivativeForPolynomial(coefs)
        val deg = getPolynomialDegree(derivative)
        val q = arrayListOf<Double>()
        for (v in derivative)
            q.add(v / derivative.last())
        val m = if (deg % 2 == 1) -1 else 1
        for (iterNumber in 1..iterCount) {
            for (i in 0..deg) {
                var res = 0.0
                for (j in 0..i) {
                    val c = if (j % 2 == 1) -1 else 1
                    res += q[j] * q[i - j] * c
                }
                q[i] = res * m
            }
            val root1 = q[0].pow(1 / 2.0.pow(iterNumber))
            if (leftBorder - eps < root1 && root1 < rightBorder + eps)
                pointsToCheck.add(root1)
            val root2 = q[0].pow(1 / 2.0.pow(iterNumber))
            if (leftBorder - eps < root2 && root2 < rightBorder + eps)
                pointsToCheck.add(root2)
        }
        return checkCertainPoints(coefs, pointsToCheck)
    }

    private fun checkCertainPoints(coefs: ArrayList<Double>, points: ArrayList<Double>): Boolean {
        for (point in points)
            if (valueAt(coefs, point) < -eps)
                return true
        return false
    }

    fun rootsOfCubicPolynomial(poly: ArrayList<Double>): ArrayList<Double> {
        // https://ru.wikipedia.org/wiki/%D0%A4%D0%BE%D1%80%D0%BC%D1%83%D0%BB%D0%B0_%D0%9A%D0%B0%D1%80%D0%B4%D0%B0%D0%BD%D0%BE - formula Cardano
        val p = poly[1] / poly[3] - poly[2].pow(2) / 3 / poly[3].pow(2)
        val q = poly[0] / poly[3] + 2 * poly[2].pow(3) / 27 / poly[3].pow(3) - poly[2] * poly[1] / 3 / poly[3].pow(2)
        val Q = (p / 3).pow(3) + (q / 2).pow(2)
        val r1: Double
        var r2: Double? = null
        var r3: Double? = null
        when {
            Q > eps -> {
                val alpha = (-q / 2 + sqrt(Q)).pow(1.0 / 3)
                val beta = (-q / 2 - sqrt(Q)).pow(1.0 / 3)
                r1 = alpha + beta
            }
            Q > -eps -> {
                val alpha = (-q / 2).pow(1.0 / 3)
                r1 = 2 * alpha
                r2 = -alpha
            }
            else -> {
                val r = ((q / 2).pow(2) + -Q).pow(1.0 / 6)
                val phi = atan2(sqrt(-Q), -q / 2) / 3
                val re = r * cos(phi)
                val im = r * sin(phi)
                r1 = 2 * re
                r2 = -re + sqrt(3.0) * im
                r3 = -re - sqrt(3.0) * im
            }
        }
        val roots = arrayListOf(r1 - poly[2] / 3 / poly[3])
        if (r2 != null)
            roots.add(r2 - poly[2] / 3 / poly[3])
        if (r3 != null)
            roots.add(r3 - poly[2] / 3 / poly[3])
        return roots
    }
}