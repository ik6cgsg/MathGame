package com.twf.baseoperations

import com.twf.numbers.Complex
import com.twf.numbers.toComplex
import com.twf.org.junit.Assert.assertTrue
import com.twf.org.junit.Test
import com.twf.substitutiontests.parseStringExpression
import kotlin.math.*

class BaseOperationsComputationTest {
    private val doubleComputation = BaseOperationsComputation(ComputationType.DOUBLE)
    private val complexComputation = BaseOperationsComputation(ComputationType.COMPLEX)

    @Test
    fun testPrimitiveArithmetical() {
        val exprPlus = parseStringExpression("1 + 3")
        val exprMinus = parseStringExpression("1 - 3")
        val exprMul = parseStringExpression("2 * 3")
        val exprDiv = parseStringExpression("6 / 3")

        assertTrue(BaseOperationsComputation.additivelyEqual(4.0, doubleComputation.compute(exprPlus) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(-2.0, doubleComputation.compute(exprMinus) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(6.0, doubleComputation.compute(exprMul) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(2.0, doubleComputation.compute(exprDiv) as Double))

        assertTrue((4.toComplex().equals(complexComputation.compute(exprPlus) as Complex)))
        assertTrue(((-2).toComplex().equals(complexComputation.compute(exprMinus) as Complex)))
        assertTrue((6.toComplex().equals(complexComputation.compute(exprMul) as Complex)))
        assertTrue((2.toComplex().equals(complexComputation.compute(exprDiv) as Complex)))
    }

    @Test
    fun testArithmetical() {
        val expression1 = parseStringExpression("(1 + 6.7 * 4.1 - 1.32 * 43 / 5.4) * 4 - 2.3")
        val result1 = (1 + 6.7 * 4.1 - 1.32 * 43 / 5.4) * 4 - 2.3
        
        assertTrue(BaseOperationsComputation.additivelyEqual(result1, doubleComputation.compute(expression1) as Double))
        assertTrue(result1.toComplex().equals(complexComputation.compute(expression1) as Complex))

        val expression2 = parseStringExpression("(12.12 * 32.5 + 1 / 8.9) - 32")
        val result2 = (12.12 * 32.5 + 1 / 8.9) - 32

        assertTrue(BaseOperationsComputation.additivelyEqual(result2, doubleComputation.compute(expression2) as Double))
        assertTrue(result2.toComplex().equals(complexComputation.compute(expression2) as Complex))
    }

    @Test
    fun testPrimitiveLogical() {
        val exprAnd = parseStringExpression("and(1, 0)")
        val exprOr = parseStringExpression("or(1, 0)")
        val exprXor = parseStringExpression("xor(1, 0)")
        val exprNot = parseStringExpression("not(0)")
        val exprAlleq = parseStringExpression("alleq(1, 1, 1)")

        assertTrue(BaseOperationsComputation.additivelyEqual(0.0, doubleComputation.compute(exprAnd) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(1.0, doubleComputation.compute(exprOr) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(1.0, doubleComputation.compute(exprXor) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(1.0, doubleComputation.compute(exprNot) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(1.0, doubleComputation.compute(exprAlleq) as Double))

        assertTrue(0.toComplex().equals(complexComputation.compute(exprAnd) as Complex))
        assertTrue(1.toComplex().equals(complexComputation.compute(exprOr) as Complex))
        assertTrue(1.toComplex().equals(complexComputation.compute(exprXor) as Complex))
        assertTrue(1.toComplex().equals(complexComputation.compute(exprNot) as Complex))
        assertTrue(1.toComplex().equals(complexComputation.compute(exprAlleq) as Complex))
    }

    @Test
    fun testTrigonometric() {
        val exprSin = parseStringExpression("sin(32.5)");
        val exprCos = parseStringExpression("cos(12)");
        val exprTg = parseStringExpression("tg(-121.87)");

        val resultSin = sin(32.5)
        val resultCos = cos(12.0)
        val resultTg = tan(-121.87)

        assertTrue(BaseOperationsComputation.additivelyEqual(resultSin, doubleComputation.compute(exprSin) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(resultCos, doubleComputation.compute(exprCos) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(resultTg, doubleComputation.compute(exprTg) as Double))

        assertTrue(resultSin.toComplex().equals(complexComputation.compute(exprSin) as Complex))
        assertTrue(resultCos.toComplex().equals(complexComputation.compute(exprCos) as Complex))
        assertTrue(resultTg.toComplex().equals(complexComputation.compute(exprTg) as Complex))
    }

    @Test
    fun testInverseTrigonometric() {
        val exprAsin = parseStringExpression("asin(0.87)");
        val exprAcos = parseStringExpression("acos(-0.13)");
        val exprAtan = parseStringExpression("atan(-121.87)");

        val resultAsin = asin(0.87)
        val resultAcos = acos(-0.13)
        val resultAtan = atan(-121.87)

        assertTrue(BaseOperationsComputation.additivelyEqual(resultAsin, doubleComputation.compute(exprAsin)as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(resultAcos, doubleComputation.compute(exprAcos) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(resultAtan, doubleComputation.compute(exprAtan) as Double))

        assertTrue(resultAsin.toComplex().equals(complexComputation.compute(exprAsin) as Complex))
        assertTrue(resultAcos.toComplex().equals(complexComputation.compute(exprAcos) as Complex))
        assertTrue(resultAtan.toComplex().equals(complexComputation.compute(exprAtan) as Complex))
    }

    @Test
    fun testHyperbolic() {
        val exprSinh = parseStringExpression("sh(0.87)");
        val exprCosh = parseStringExpression("ch(-0.13)");
        val exprTanh = parseStringExpression("th(-121.87)");

        val resultSinh = sinh(0.87)
        val resultCosh = cosh(-0.13)
        val resultTanh = tanh(-121.87)

        assertTrue(BaseOperationsComputation.additivelyEqual(resultSinh, doubleComputation.compute(exprSinh) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(resultCosh, doubleComputation.compute(exprCosh) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(resultTanh, doubleComputation.compute(exprTanh) as Double))

        assertTrue(resultSinh.toComplex().equals(complexComputation.compute(exprSinh) as Complex))
        assertTrue(resultCosh.toComplex().equals(complexComputation.compute(exprCosh) as Complex))
        assertTrue(resultTanh.toComplex().equals(complexComputation.compute(exprTanh) as Complex))
    }

    @Test
    fun testLn() {
        val exprLn = parseStringExpression("ln(0.87)");
        val resultLn = ln(0.87)

        assertTrue(BaseOperationsComputation.additivelyEqual(resultLn, doubleComputation.compute(exprLn) as Double))
        assertTrue(resultLn.toComplex().equals(complexComputation.compute(exprLn) as Complex))
    }

    @Test
    fun testExp() {
        val exprExp = parseStringExpression("exp(0.87)");
        val resultExp = exp(0.87)

        assertTrue(BaseOperationsComputation.additivelyEqual(resultExp, doubleComputation.compute(exprExp) as Double))
        assertTrue(resultExp.toComplex().equals(complexComputation.compute(exprExp) as Complex))
    }

    @Test
    fun testPow() {
        val exprPow = parseStringExpression("0.85^12");
        val resultPow = 0.85.pow(12)

        assertTrue(BaseOperationsComputation.additivelyEqual(resultPow, doubleComputation.compute(exprPow) as Double))
        assertTrue(resultPow.toComplex().equals(complexComputation.compute(exprPow) as Complex))
    }

    @Test
    fun testFunctionValuesNotInDomain() {
        val exprLn = parseStringExpression("ln(-1)")
        val exprPow = parseStringExpression("(-4)^(0.5)")

        val resultLn = Complex.ln((-1).toComplex())
        val resultPow = Complex.sqrt((-4).toComplex())

        assertTrue(resultLn.equals(complexComputation.compute(exprLn) as Complex))
        assertTrue(resultPow.equals(complexComputation.compute(exprPow) as Complex))
    }

    @Test
    fun testComplexExpressions() {
        val expr1 = parseStringExpression("ln(sin(3) + cos(8) + 7) - 3.2^0.4 * acos(0.12)")
        val expr2 = parseStringExpression("th(2^(sin(7))) + ln(2) - atan(4)")

        val res1 = ln(sin(3.0) + cos(8.0) + 7) - 3.2.pow(0.4) * acos(0.12)
        val res2 = tanh(2.0.pow(sin(7.0))) + ln(2.0) - atan(4.0)

        assertTrue(BaseOperationsComputation.additivelyEqual(res1, doubleComputation.compute(expr1) as Double))
        assertTrue(BaseOperationsComputation.additivelyEqual(res2, doubleComputation.compute(expr2) as Double))
        assertTrue(res2.toComplex().equals(complexComputation.compute(expr2) as Complex))
        assertTrue(res1.toComplex().equals(complexComputation.compute(expr1) as Complex))
    }

    @Test
    fun testSumNSquare() {
        val expr = parseStringExpression("S(i, 0, 3, i^2)")

        assertTrue(BaseOperationsComputation.additivelyEqual(14.0, doubleComputation.compute(expr) as Double))
        assertTrue(14.toComplex().equals(complexComputation.compute(expr) as Complex))
    }

    @Test
    fun testProdNSquare() {
        val expr = parseStringExpression("P(j, 1, 2, 1 + j)")

        assertTrue(BaseOperationsComputation.additivelyEqual(6.0, doubleComputation.compute(expr) as Double))
        assertTrue(6.toComplex().equals(complexComputation.compute(expr) as Complex))
    }

    @Test
    fun testSumNLn() {
        val expr = parseStringExpression("S(i, 1, 4, ln(i))")

        assertTrue(BaseOperationsComputation.additivelyEqual(ln(24.0), doubleComputation.compute(expr) as Double))
        assertTrue(ln(24.0).toComplex().equals(complexComputation.compute(expr) as Complex))

    }

    @Test
    fun testProdCos() {
        val expr = parseStringExpression("P(i, 4, 6, cos(i))")
        val res = cos(4.0) * cos(5.0) * cos(6.0)

        assertTrue(BaseOperationsComputation.additivelyEqual(res, doubleComputation.compute(expr) as Double))
        assertTrue(res.toComplex().equals(complexComputation.compute(expr) as Complex))
    }

    @Test
    fun testInnerSum() {
        val expr = parseStringExpression("S(i, 0, 10, S(j, 0, i, 1))")

        assertTrue(BaseOperationsComputation.additivelyEqual(66.0, doubleComputation.compute(expr) as Double))
        assertTrue(66.0.toComplex().equals(complexComputation.compute(expr) as Complex))
    }

    @Test
    fun testInnerProd() {
        val expr = parseStringExpression("P(i, 2, 5, P(j, 2, i, i))")
        val res = 2 * 9 * 64 * 625

        assertTrue(BaseOperationsComputation.additivelyEqual(res.toDouble(), doubleComputation.compute(expr) as Double))
        assertTrue(res.toComplex().equals(complexComputation.compute(expr) as Complex))
    }

    @Test
    fun testSumWithLowerIndexLess() {
        val expr = parseStringExpression("S(i, 3, 1, 12)")

        assertTrue(BaseOperationsComputation.additivelyEqual(0.toDouble(), doubleComputation.compute(expr) as Double))
        assertTrue(0.toComplex().equals(complexComputation.compute(expr) as Complex))
    }

    @Test
    fun testProdWithLowerIndexLess() {
        val expr = parseStringExpression("P(i, 3, 1, 12)")

        assertTrue(BaseOperationsComputation.additivelyEqual(1.toDouble(), doubleComputation.compute(expr) as Double))
        assertTrue(1.toComplex().equals(complexComputation.compute(expr) as Complex))
    }

    @Test
    fun testSumWithNegativeIndexes() {
        val expr = parseStringExpression("S(i, -3, -1, 12)")

        assertTrue(BaseOperationsComputation.additivelyEqual(36.toDouble(), doubleComputation.compute(expr) as Double))
        assertTrue(36.toComplex().equals(complexComputation.compute(expr) as Complex))
    }

    @Test
    fun testProdWithNegativeIndexes() {
        val expr = parseStringExpression("P(i, -3, -1, 2)")

        assertTrue(BaseOperationsComputation.additivelyEqual(8.toDouble(), doubleComputation.compute(expr) as Double))
        assertTrue(8.toComplex().equals(complexComputation.compute(expr) as Complex))
    }
}