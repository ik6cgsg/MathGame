package com.twf.numbers

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.math.*

class RealTest {
    @Test
    fun testPlus() {
        assertTrue(Real(8.8).additivelyEqualTo(Real(3.6) + Real(5.2)))
        assertTrue(Real(9.8).additivelyEqualTo(Real(3.6) + Real(5.2) + Real(1)))
        assertTrue(Real(-1000.1).additivelyEqualTo(Real(-1000) + Real(-0.1)))
        assertTrue(Real(0).additivelyEqualTo(Real(50.67) + Real(-50.67)))
        assertFalse(Real(160).additivelyEqualTo(Real(50) + Real(100)))
        assertTrue(Real(0.00008).additivelyEqualTo(Real(0.00006) + Real(0.00002)))
    }

    @Test
    fun testMinus() {
        assertTrue(Real(-1.6).additivelyEqualTo(Real(3.6) - Real(5.2)))
        assertTrue(Real(-2.6).additivelyEqualTo(Real(3.6) - Real(5.2) - Real(1)))
        assertTrue(Real(-999.9).additivelyEqualTo(Real(-1000) - Real(-0.1)))
        assertTrue(Real(0).additivelyEqualTo(Real(50.67) - Real(50.67)))
        assertFalse(Real(0).additivelyEqualTo(Real(5.5) - Real(5.4)))
        assertTrue(Real(0.00004).additivelyEqualTo(Real(0.00006) - Real(0.00002)))
    }

    @Test
    fun testTimes() {
        assertTrue(Real(3.6 * 5.2).additivelyEqualTo(Real(3.6) * Real(5.2)))
        assertTrue(Real(-2.6 * 1.5).additivelyEqualTo(Real(-2.6) * Real(1.5)))
        assertFalse(Real(-1).additivelyEqualTo(Real(1) * Real(1)))
    }

    @Test
    fun testDiv() {
        assertTrue(Real(3.6 / 5.2).additivelyEqualTo(Real(3.6) / Real(5.2)))
        assertTrue(Real(-2.6 / 1.5).additivelyEqualTo(Real(-2.6) / Real(1.5)))
        assertFalse(Real(-1).additivelyEqualTo(Real(1) / Real(1)))
    }

    @Test
    fun testPow() {
        assertTrue(Real(3.6.pow(5.2)).additivelyEqualTo(Real(3.6).pow(Real(5.2))))
        assertTrue(Real(2.6.pow(-1.5)).additivelyEqualTo(Real(2.6).pow(Real(-1.5))))
        assertFalse(Real(1.1).additivelyEqualTo(Real(1.1).pow(Real(0.9))))
    }

    @Test
    fun testSin() {
        assertTrue(Real(sin(3.6)).additivelyEqualTo(Real.sin(Real(3.6))))
        assertTrue(Real(sin(-2.6)).additivelyEqualTo(Real.sin(Real(-2.6))))
        assertFalse(Real(sin(-1.0)).additivelyEqualTo(Real.sin(Real(1))))
    }

    @Test
    fun testCos() {
        assertTrue(Real(cos(3.6)).additivelyEqualTo(Real.cos(Real(3.6))))
        assertTrue(Real(cos(-2.6)).additivelyEqualTo(Real.cos(Real(-2.6))))
        assertFalse(Real(cos(-1.1)).additivelyEqualTo(Real.cos(Real(1))))
    }

    @Test
    fun testTan() {
        assertTrue(Real(tan(3.6)).additivelyEqualTo(Real.tan(Real(3.6))))
        assertTrue(Real(tan(-2.6)).additivelyEqualTo(Real.tan(Real(-2.6))))
        assertFalse(Real(tan(-1.0)).additivelyEqualTo(Real.tan(Real(1))))
    }

    @Test
    fun testExp() {
        assertTrue(Real(exp(3.6)).additivelyEqualTo(Real.exp(Real(3.6))))
        assertTrue(Real(exp(-2.6)).additivelyEqualTo(Real.exp(Real(-2.6))))
        assertFalse(Real(exp(-1.0)).additivelyEqualTo(Real.exp(Real(1))))
    }

    @Test
    fun testAsin() {
        assertTrue(Real(asin(0.6)).additivelyEqualTo(Real.asin(Real(0.6))))
        assertTrue(Real(asin(-0.4)).additivelyEqualTo(Real.asin(Real(-0.4))))
        assertFalse(Real(asin(-1.0)).additivelyEqualTo(Real.asin(Real(1))))
    }

    @Test
    fun testAcos() {
        assertTrue(Real(acos(0.6)).additivelyEqualTo(Real.acos(Real(0.6))))
        assertTrue(Real(acos(-0.4)).additivelyEqualTo(Real.acos(Real(-0.4))))
        assertFalse(Real(acos(-1.0)).additivelyEqualTo(Real.acos(Real(1))))
    }

    @Test
    fun testAtan() {
        assertTrue(Real(atan(3.6)).additivelyEqualTo(Real.atan(Real(3.6))))
        assertTrue(Real(atan(-2.6)).additivelyEqualTo(Real.atan(Real(-2.6))))
        assertFalse(Real(atan(-1.0)).additivelyEqualTo(Real.atan(Real(1))))
    }

    @Test
    fun testSinh() {
        assertTrue(Real(sinh(3.6)).additivelyEqualTo(Real.sinh(Real(3.6))))
        assertTrue(Real(sinh(-2.6)).additivelyEqualTo(Real.sinh(Real(-2.6))))
        assertFalse(Real(sinh(-1.0)).additivelyEqualTo(Real.sinh(Real(1))))
    }

    @Test
    fun testCosh() {
        assertTrue(Real(cosh(3.6)).additivelyEqualTo(Real.cosh(Real(3.6))))
        assertTrue(Real(cosh(-2.6)).additivelyEqualTo(Real.cosh(Real(-2.6))))
        assertFalse(Real(cosh(-1.1)).additivelyEqualTo(Real.cosh(Real(1))))
    }

    @Test
    fun testTanh() {
        assertTrue(Real(tanh(3.6)).additivelyEqualTo(Real.tanh(Real(3.6))))
        assertTrue(Real(tanh(-2.6)).additivelyEqualTo(Real.tanh(Real(-2.6))))
        assertFalse(Real(tanh(-1.0)).additivelyEqualTo(Real.tanh(Real(1))))
    }

    @Test
    fun testLn() {
        assertTrue(Real(ln(3.6)).additivelyEqualTo(Real.ln(Real(3.6))))
        assertTrue(Real(ln(0.06)).additivelyEqualTo(Real.ln(Real(0.06))))
        assertFalse(Real(ln(1.1)).additivelyEqualTo(Real.ln(Real(1))))
    }

    @Test
    fun testAbs() {
        assertTrue(Real(3.6).additivelyEqualTo(Real.abs(Real(3.6))))
        assertTrue(Real(2.6).additivelyEqualTo(Real.abs(Real(-2.6))))
        assertFalse(Real(-1.0).additivelyEqualTo(Real.abs(Real(-1))))
    }

    @Test
    fun testPow2() {
        assertTrue(Real(3.6.pow(2)).additivelyEqualTo(Real.pow2(Real(3.6))))
        assertTrue(Real(2.6.pow(2)).additivelyEqualTo(Real.pow2(Real(-2.6))))
        assertFalse(Real(-1.0).additivelyEqualTo(Real.pow2(Real(-1.0))))
    }

    @Test
    fun testSqrt() {
        assertTrue(Real(3.6.pow(0.5)).additivelyEqualTo(Real.sqrt(Real(3.6))))
        assertTrue(Real(0.0006.pow(0.5)).additivelyEqualTo(Real.sqrt(Real(0.0006))))
        assertFalse(Real(-1.0).additivelyEqualTo(Real.sqrt(Real(1.12))))
    }
}