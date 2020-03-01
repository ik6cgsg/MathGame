package com.twf.numbers

import com.twf.org.junit.Test
import com.twf.org.junit.jupiter.api.Assertions
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComplexTest {
    @Test
    fun testDifferentFormsEquality() {
        assertTrue(Complex(1.toReal(), 1.toReal(), Form.ALGEBRAIC).equals(
                     Complex(2.0.pow(0.5).toReal(), (PI / 4).toReal(), Form.TRIGONOMETRIC)))
        assertTrue(Complex(0.toReal(), 1.toReal(), Form.ALGEBRAIC).equals(
                Complex(1.toReal(), ((5 * PI) / 2).toReal(), Form.TRIGONOMETRIC)))
        assertFalse(Complex(0.toReal(), 1.toReal(), Form.ALGEBRAIC).equals(
                Complex(1.toReal(), ((3 * PI) / 2).toReal(), Form.TRIGONOMETRIC)))
    }

    @Test
    fun testFromString() {
        assertTrue(Complex(Real(1), Real(0), Form.ALGEBRAIC).equals("1".toComplex()))
        assertTrue(Complex(Real(0), Real(1), Form.ALGEBRAIC).equals("i1".toComplex()))
        assertTrue(Complex(Real(2.0), Real(4), Form.ALGEBRAIC).equals("2+i4".toComplex()))
    }

    @Test
    fun testPlus() {
        val res1 = Complex(1.toReal(), 1.toReal(), Form.ALGEBRAIC) + Complex(2.toReal(), 4.toReal(), Form.ALGEBRAIC)
        val res2 = Complex((-1.1).toReal(), 107.7.toReal(), Form.ALGEBRAIC) +
                   Complex(8.9.toReal(), 56.toReal(), Form.ALGEBRAIC)

        assertTrue(Complex(3.toReal(), 5.toReal(), Form.ALGEBRAIC).equals(res1))
        assertTrue(Complex(7.8.toReal(), 163.7.toReal(), Form.ALGEBRAIC).equals(res2))
        assertFalse(Complex(7.9.toReal(), 163.6.toReal(), Form.ALGEBRAIC).equals(res2))
    }

    @Test
    fun minus() {
        val res1 = Complex(1.toReal(), 1.toReal(), Form.ALGEBRAIC) - Complex(2.toReal(), 4.toReal(), Form.ALGEBRAIC)
        val res2 = Complex((-1.1).toReal(), 107.7.toReal(), Form.ALGEBRAIC) -
                   Complex(8.9.toReal(), 56.toReal(), Form.ALGEBRAIC)

        assertTrue(Complex((-1).toReal(), (-3).toReal(), Form.ALGEBRAIC).equals(res1))
        assertTrue(Complex((-10).toReal(), 51.70.toReal(), Form.ALGEBRAIC).equals(res2))
        assertFalse(Complex(7.9.toReal(), 163.6.toReal(), Form.ALGEBRAIC).equals(res2))
    }

    @Test
    fun testTimes() {
        val res1 = Complex(7.toReal(), 1.toReal(), Form.TRIGONOMETRIC) * Complex(2.toReal(), 4.toReal(), Form.TRIGONOMETRIC)
        val res2 = Complex(1.1.toReal(), 107.7.toReal(), Form.TRIGONOMETRIC) *
                   Complex(8.9.toReal(), 56.toReal(), Form.TRIGONOMETRIC)

        assertTrue(Complex(14.toReal(), 5.toReal(), Form.TRIGONOMETRIC).equals(res1))
        assertFalse(Complex(14.toReal(), 5.toReal(), Form.ALGEBRAIC).equals(res1))
        assertTrue(Complex((8.9 * 1.1).toReal(), (163.7 + 2 * PI).toReal(), Form.TRIGONOMETRIC).equals(res2))
    }

    @Test
    fun testDiv() {
        val res1 = Complex(7.toReal(), 1.toReal(), Form.TRIGONOMETRIC) / Complex(2.toReal(), 4.toReal(), Form.TRIGONOMETRIC)
        val res2 = Complex(1.1.toReal(), 107.7.toReal(), Form.TRIGONOMETRIC) /
                   Complex(8.9.toReal(), 56.toReal(), Form.TRIGONOMETRIC)

        assertTrue(Complex(3.5.toReal(), (-3).toReal(), Form.TRIGONOMETRIC).equals(res1))
        assertFalse(Complex((1.1 / 8.9).toReal(), 51.7.toReal(), Form.ALGEBRAIC).equals(res1))
        assertTrue(Complex((1.1 / 8.9).toReal(), (51.7 + 2 * PI).toReal(), Form.TRIGONOMETRIC).equals(res2))
    }

    @Test
    fun testUnaryMinus() {
        assertTrue((-Complex(1.toReal(), (-3.9).toReal(), Form.ALGEBRAIC)).equals(
                     Complex((-1).toReal(), 3.9.toReal(), Form.ALGEBRAIC)))
    }

    @Test
    fun testPlusAssign() {
        val res = Complex(1.toReal(), 5.1.toReal(), Form.ALGEBRAIC)
        res += Complex(5.6.toReal(), 7.8.toReal(), Form.ALGEBRAIC)

        assertTrue(Complex(6.6.toReal(), 12.9.toReal(), Form.ALGEBRAIC).equals(res))
    }

    @Test
    fun testMinusAssign() {
        val res = Complex(1.toReal(), 5.1.toReal(), Form.ALGEBRAIC)
        res -= Complex(5.6.toReal(), 7.8.toReal(), Form.ALGEBRAIC)

        assertTrue(Complex((-4.6).toReal(), (-2.7).toReal(), Form.ALGEBRAIC).equals(res))
    }

    @Test
    fun testTimesAssign() {
        val res = Complex(1.toReal(), 5.1.toReal(), Form.TRIGONOMETRIC)
        res *= Complex(5.6.toReal(), 7.8.toReal(), Form.TRIGONOMETRIC)

        assertTrue(Complex(5.6.toReal(), 12.9.toReal(), Form.TRIGONOMETRIC).equals(res))
    }

    @Test
    fun testDivAssign() {
        val res = Complex(1.toReal(), 5.1.toReal(), Form.TRIGONOMETRIC)
        res /= Complex(5.6.toReal(), 7.8.toReal(), Form.TRIGONOMETRIC)

        assertTrue(Complex((1 / 5.6).toReal(), (-2.7).toReal(), Form.TRIGONOMETRIC).equals(res))
    }

    @Test
    fun testPow() {
        val resComplexInComplex = Complex(0.toReal(), 1.toReal(), Form.ALGEBRAIC).pow(
                                  Complex(0.toReal(), 1.toReal(), Form.ALGEBRAIC))
        val resComplexInReal = Complex(2.3.toReal(), 5.toReal(), Form.TRIGONOMETRIC).pow(1.3.toReal())

        assertTrue(Complex(exp(-PI / 2).toReal(), 0.toReal(), Form.TRIGONOMETRIC).equals(resComplexInComplex))
        assertTrue(Complex((2.3.pow(1.3)).toReal(), ((5 - 2 * PI) * 1.3).toReal(), Form.TRIGONOMETRIC).equals(resComplexInReal))
    }

    @Test
    fun testTimesReal() {
        val res = Complex(0.5.toReal(), 12.toReal(), Form.TRIGONOMETRIC) * Real(5.1)
        assertTrue(Complex(2.55.toReal(), 12.toReal(), Form.TRIGONOMETRIC).equals(res))
    }

    @Test
    fun testDivReal() {
        val res = Complex(0.5.toReal(), 12.toReal(), Form.TRIGONOMETRIC) / Real(5.1)
        assertTrue(Complex((0.5 / 5.1).toReal(), 12.toReal(), Form.TRIGONOMETRIC).equals(res))
    }

    @Test
    fun testExp() {
        val res = Complex.exp(Complex(0.3.toReal(), 2.7.toReal(), Form.ALGEBRAIC))
        assertTrue(Complex(exp(0.3).toReal(), 2.7.toReal(), Form.TRIGONOMETRIC).equals(res))
    }

    @Test
    fun testLn() {
        val res = Complex.ln(Complex(2.3.toReal(), (-9.4).toReal(), Form.TRIGONOMETRIC))

        assertTrue(Complex(ln(2.3).toReal(), (-9.4 + 2 * PI).toReal(), Form.ALGEBRAIC).equals(res))
        assertTrue(Complex.ln(Complex.i).equals(Complex(0.toReal(), (PI / 2).toReal(), Form.ALGEBRAIC)))
        assertTrue(Complex.ln(exp(2.0).toComplex()).equals(2.toComplex()))
    }

    @Test
    fun testSin() {
        val arg = Complex((-13).toReal(), 5.6.toReal(), Form.ALGEBRAIC)
        val res = (Complex.exp(Complex.i * arg) - Complex.exp(-Complex.i * arg)) / (Complex.i * 2.toReal())
        assertTrue(Complex.sin(arg).equals(res))
    }

    @Test
    fun testCos() {
        val arg = Complex((-13).toReal(), 5.6.toReal(), Form.ALGEBRAIC)
        val res = (Complex.exp(Complex.i * arg) + Complex.exp(-Complex.i * arg)) / 2.toReal()
        assertTrue(Complex.cos(arg).equals(res))
    }

    @Test
    fun testTan() {
        val arg = Complex((-13).toReal(), 5.6.toReal(), Form.ALGEBRAIC)
        val res = Complex.sin(arg) / Complex.cos(arg)

        assertTrue(Complex.tan(arg).equals(res))
        assertTrue(Complex.tan((PI / 4).toComplex()).equals(1.toComplex()))
    }

    @Test
    fun testSinh() {
        val arg = Complex((-13).toReal(), 5.6.toReal(), Form.ALGEBRAIC)
        val res = (Complex.exp(arg) - Complex.exp(-arg)) / 2.toReal()

        assertTrue(Complex.sinh(arg).equals(res))
        assertTrue(Complex.sinh(1.toComplex()).equals(((exp(1.0) - exp(-1.0)) / 2).toComplex()))
    }

    @Test
    fun testCosh() {
        val arg = Complex((-13).toReal(), 5.6.toReal(), Form.ALGEBRAIC)
        val res = (Complex.exp(arg) + Complex.exp(-arg)) / 2.toReal()

        assertTrue(Complex.cosh(arg).equals(res))
        assertTrue(Complex.cosh(1.toComplex()).equals(((exp(1.0) + exp(-1.0)) / 2).toComplex()))
    }

    @Test
    fun testTanh() {
        val arg = Complex((-13).toReal(), 5.6.toReal(), Form.ALGEBRAIC)
        val res = Complex.sinh(arg) / Complex.cosh(arg)

        assertTrue(Complex.tanh(arg).equals(res))
        assertTrue(Complex.tanh(1.toComplex()).equals(((exp(2.0) - 1) / (exp(2.0) + 1)).toComplex()))
    }

    @Test
    fun testAbs() {
        val arg = Complex((-12).toReal(), 5.toReal(), Form.ALGEBRAIC)

        assertTrue(Complex.abs(arg).additivelyEqualTo(13.toReal()))
        assertTrue(Complex.abs((-1).toComplex()).additivelyEqualTo(1.toReal()))
    }

    @Test
    fun testSqrt() {
        val arg = Complex(16.toReal(), 3.toReal(), Form.TRIGONOMETRIC)

        assertTrue(Complex.sqrt(arg).equals(Complex(4.toReal(), 1.5.toReal(), Form.TRIGONOMETRIC)))
        assertTrue(Complex.sqrt(12.7.toComplex()).equals(12.7.pow(0.5).toComplex()))
    }

    @Test
    fun testPow2() {
        val arg = Complex(4.toReal(), 3.toReal(), Form.TRIGONOMETRIC)

        assertTrue(Complex.pow2(arg).equals(Complex(16.toReal(), 6.toReal(), Form.TRIGONOMETRIC)))
        assertTrue(Complex.pow2(12.7.toComplex()).equals(12.7.pow(2).toComplex()))
    }

    @Test
    fun testAsin() {
        val arg = Complex((-2).toReal(), 0.4.toReal(), Form.ALGEBRAIC)
        val res = -Complex.i * Complex.ln(Complex.i * arg + Complex.sqrt(-Complex.pow2(arg) + 1.toComplex()))

        assertTrue(Complex.asin(arg).equals(res))
        assertTrue(Complex.asin(0.5.toComplex()).equals((PI / 6).toComplex()))
    }

    @Test
    fun testAcos() {
        val arg = Complex((-2).toReal(), 0.4.toReal(), Form.ALGEBRAIC)
        val res = -Complex.i * Complex.ln(arg + Complex.sqrt(Complex.pow2(arg) - 1.toComplex()))

        assertTrue(Complex.acos(arg).equals(res))
        assertTrue(Complex.acos(0.5.toComplex()).equals((PI / 3).toComplex()))

    }

    @Test
    fun testAtan() {
        val arg = Complex((-2).toReal(), 0.4.toReal(), Form.ALGEBRAIC)
        val res = -(Complex.i / 2.toReal()) * Complex.ln((1.toComplex() + Complex.i * arg) / (1.toComplex() - Complex.i * arg))

        assertTrue(Complex.atan(arg).equals(res))
        assertTrue(Complex.atan(1.toComplex()).equals((PI / 4).toComplex()))
    }

    @Test
    fun testAdd_library() {
        val x = Complex(Real(3.0), Real(4.0), Form.ALGEBRAIC)
        val y = Complex(Real(5.0), Real(6.0), Form.ALGEBRAIC)
        val z = x + y
        assertTrue(Real(8.0).additivelyEqualTo(z.getReal()))
        assertTrue(Real(10).additivelyEqualTo(z.getImaginary()))
    }

    @Test
    fun testDivide_library() {
        val x = Complex(Real(3.0), Real(4.0), Form.ALGEBRAIC)
        val y = Complex(Real(5.0), Real(6.0), Form.ALGEBRAIC)
        val z = x / y
        assertTrue(Real(39.0 / 61.0).additivelyEqualTo(z.getReal()))
        assertTrue(Real(2.0 / 61.0).additivelyEqualTo(z.getImaginary()))
    }

    @Test
    fun testDivideReal_library() {
        val x = Complex(Real(2), Real(3), Form.ALGEBRAIC)
        val y = Complex(Real(2), Real(0), Form.ALGEBRAIC)
        assertTrue(Complex(Real(1), Real(1.5), Form.ALGEBRAIC).equals(x / y))
    }

    @Test
    fun testDivideImaginary_library() {
        val x = Complex(Real(2), Real(3), Form.ALGEBRAIC)
        val y = Complex(Real(0), Real(2), Form.ALGEBRAIC)
        assertTrue(Complex(Real(1.5), Real(-1), Form.ALGEBRAIC).equals(x / y))
    }

    @Test
    fun testScalarDivide_library() {
        val x = Complex(Real(3.0), Real(4.0), Form.ALGEBRAIC)
        val yDouble = 2.0;
        val yComplex = yDouble.toComplex()
        assertTrue((x / yComplex).equals(x / Real(yDouble)))
    }

    @Test
    fun testMultiply_library() {
        val x = Complex(Real(3.0), Real(4.0), Form.ALGEBRAIC)
        val y = Complex(Real(5.0), Real(6.0), Form.ALGEBRAIC)
        val z = x * y
        assertTrue(Real(-9.0).additivelyEqualTo(z.getReal()))
        assertTrue(Real(38.0).additivelyEqualTo(z.getImaginary()))
    }

    @Test
    fun testScalarMultiply_library() {
        val x = Complex(Real(3.0), Real(4.0), Form.ALGEBRAIC)
        val yDouble = 2.0;
        val yComplex = yDouble.toComplex()
        assertTrue((x * yDouble.toReal()).equals(yComplex * x))
    }

    @Test
    fun testNegate_library() {
        val x = Complex(Real(3.0), Real(4.0), Form.ALGEBRAIC)
        val z = -x
        assertTrue(Real(-3.0).additivelyEqualTo(z.getReal()))
        assertTrue(Real(-4.0).additivelyEqualTo(z.getImaginary()))
    }

    @Test
    fun testEqualsSame() {
        val x = Complex(Real(3.0), Real(4.0), Form.ALGEBRAIC)
        assertTrue(x.equals(x));
    }

    @Test
    fun testEqualsTrue_library() {
        val x = Complex(Real(3.0), Real(4.0), Form.ALGEBRAIC)
        val y = Complex(Real(3.0), Real(4.0), Form.ALGEBRAIC)
        assertTrue(x.equals(y));
    }

    @Test
    fun testAcos_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(0.936812), Real(-2.30551), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.acos(z)))
    }

    @Test
    fun testAsin_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(0.633984), Real(2.30551), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.asin(z)))
    }


    @Test
    fun testAtan_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(1.448306995), Real(0.1589971917), Form.ALGEBRAIC)
        val answer = Complex.atan(z)
        assertTrue(expected.equals(Complex.atan(z)))
    }

    @Test
    fun testCos_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(-27.03494560), Real(-3.851153335), Form.ALGEBRAIC)
        val tmp = Complex.cos(z)
        assertTrue(expected.equals(Complex.cos(z)))
    }

    @Test
    fun testCosh_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(-6.58066304), Real(-7.58155274), Form.ALGEBRAIC)
        val tmp = Complex.cosh(z)
        assertTrue(expected.equals(Complex.cosh(z)))
    }

    @Test
    fun testExp_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(-13.12878308), Real(-15.20078446), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.exp(z)))
    }

    @Test
    fun testLog_library() {
        val z = Complex(Real(3), Real(4),Form.ALGEBRAIC)
        val expected = Complex(Real(1.609437912434), Real(0.927295218), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.ln(z)))
    }

    @Test
    fun testPow_library() {
        val x = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val y = Complex(Real(5), Real(6), Form.ALGEBRAIC)
        val expected = Complex(Real(-1.860893306), Real(11.8367671067), Form.ALGEBRAIC)
        assertTrue(expected.equals(x.pow(y)))
    }

    @Test
    fun testScalarPow_library() {
        val x = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val yDouble = 5.0
        val yComplex =  yDouble.toComplex()
        assertTrue(x.pow(yComplex).equals(x.pow(Real(yDouble))))
    }

    @Test
    fun testSin_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(3.8537380379), Real(-27.016813258), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.sin(z)))
    }

    @Test
    fun testSinh_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(-6.5481200409), Real(-7.6192317203), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.sinh(z)))
    }

    @Test
    fun testSqrtRealPositive_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(2), Real(1), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.sqrt(z)))
    }

    @Test
    fun testSqrtRealZero_library() {
        val z = Complex(Real(0.0), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(1.41421356), Real(1.41421356), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.sqrt(z)))
    }

    @Test
    fun testSqrtRealNegative_library() {
        val z = Complex(Real(-3.0), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(1), Real(2), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.sqrt(z)))
    }

    @Test
    fun testSqrtImaginaryZero_library() {
        val z = Complex(Real(-3.0), Real(0.0), Form.ALGEBRAIC)
        val expected = Complex(Real(0.0), Real(1.73205), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.sqrt(z)))
    }

    @Test
    fun testSqrtImaginaryNegative_library() {
        val z = Complex(Real(-3.0), Real(-4.0), Form.ALGEBRAIC)
        val expected = Complex(Real(1.0), Real(-2.0), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.sqrt(z)))
    }

    @Test
    fun testSqrtTrigonometric_library() {
        var r = 1.0
        for (i in 0..5) {
            r += i;
            var theta = 0.0
            for (j in 0..10) {
                theta += PI /12;
                val z = Complex(Real(r), Real(theta), Form.TRIGONOMETRIC);
                val sqrtZ = Complex(Real(r.pow(0.5)), Real(theta / 2), Form.TRIGONOMETRIC)
                assertTrue(sqrtZ.equals(Complex.sqrt(z)))
            }
        }
    }

    @Test
    fun testTan_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(-0.0001873462046), Real(0.999355987),Form.ALGEBRAIC)
        val answer = Complex.tan(z)
        assertTrue(expected.equals(Complex.tan(z)))
    }

    @Test
    fun testTanh_library() {
        val z = Complex(Real(3), Real(4), Form.ALGEBRAIC)
        val expected = Complex(Real(1.00071), Real(0.00490826), Form.ALGEBRAIC)
        assertTrue(expected.equals(Complex.tanh(z)))
    }

    @Test
    fun testGetAngle_library() {
        var z =  Complex(Real(1), Real(0), Form.ALGEBRAIC)
        assertTrue(z.getAngle().additivelyEqualTo(Real(0)))

        z = Complex(Real(1), Real(1),Form.ALGEBRAIC)
        assertTrue(z.getAngle().additivelyEqualTo(Real(PI / 4)))

        z = Complex(Real(0), Real(1), Form.ALGEBRAIC)
        assertTrue(z.getAngle().additivelyEqualTo(Real(PI / 2)))

        z = Complex(Real(-1), Real(1), Form.ALGEBRAIC)
        assertTrue(z.getAngle().additivelyEqualTo(Real(3 * PI / 4)))

        z = Complex(Real(-1), Real(0), Form.ALGEBRAIC)
        assertTrue(z.getAngle().additivelyEqualTo(Real(PI)))

        z = Complex(Real(-1), Real(-1), Form.ALGEBRAIC)
        assertTrue(z.getAngle().additivelyEqualTo(Real(-3 * PI / 4)))

        z = Complex(Real(0), Real(-1), Form.ALGEBRAIC);
        assertTrue(z.getAngle().additivelyEqualTo(Real(-PI / 2)))

        z = Complex(Real(1), Real(-1), Form.ALGEBRAIC)
        assertTrue(z.getAngle().additivelyEqualTo(Real(-PI / 4)))

    }

    @Test
    fun testEqualityOfAlgebraicAndTrigonometricForms_library() {
        assertTrue(Complex(Real(1), Real(0), Form.ALGEBRAIC).equals(Complex(Real(1), Real(0), Form.TRIGONOMETRIC)))
        assertTrue(Complex(Real(1), Real(1), Form.ALGEBRAIC).equals(Complex(Real(2.0.pow(0.5)), Real(PI / 4), Form.TRIGONOMETRIC)))
    }

    @Test
    fun testTrigonometricFormWithSubzeroModule_library() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            Complex(Real(-1), Real(0), Form.TRIGONOMETRIC)
        }

        assertEquals("error: radius < 0", exception.message)
    }
}