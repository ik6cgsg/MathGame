package mathhelper.twf.numbers

import mathhelper.twf.platformdependent.abs
import kotlin.math.abs
import kotlin.math.sqrt

enum class Form {ALGEBRAIC, TRIGONOMETRIC}

fun Int.toComplex() = Complex(this.toReal(), 0.toReal(), Form.ALGEBRAIC)
fun Double.toComplex() = Complex(this.toReal(), 0.toReal(), Form.ALGEBRAIC)

/* Expected format for z: x+iy, where x is real part of z and y is imaginary part
*  x or y can be omitted. So, possible formats also x or iy*/
fun String.toComplexOrNull() : Complex? {
    val realValue = this.toRealOrNull() // If number has only real part
    if (realValue != null) {
        return Complex(realValue, Real(0), Form.ALGEBRAIC)
    }

    if (this.isNotEmpty() && this[0] == 'i') { // If number has only imaginary part
        val imaginaryValue = this.substring(1).toRealOrNull()
        if (imaginaryValue != null) {
            return Complex(Real(0), imaginaryValue, Form.ALGEBRAIC)
        }
    }

    val partsOfNumber = this.split("+i")

    if (partsOfNumber.size != 2) {
        return null
    }

    val realPart = partsOfNumber[0].toRealOrNull()
    val imaginaryPart = partsOfNumber[1].toRealOrNull()

    if (realPart == null || imaginaryPart == null) {
        return null
    }

    return Complex(realPart, imaginaryPart, Form.ALGEBRAIC)
}

fun String.toComplex() : Complex{
    val result = this.toComplexOrNull()
    if (result == null) {
        throw IllegalStateException("$this cannot be converted to complex")
    } else {
        return result
    }
}

//class Complex private constructor() {
//    private var real : Real = Real(0)
//    private var imaginary : Real = Real(0)
//    private var radius : Real = Real(0)
//    private var angle : Real = Real(0)
//
//    companion object {
//        val i = Complex(Real(0), Real(1), Form.ALGEBRAIC)
//
//        fun exp(z : Complex) = Complex(Real.exp(z.real), z.imaginary, Form.TRIGONOMETRIC)
//        fun ln(z : Complex) = Complex(Real.ln(z.radius), z.angle, Form.ALGEBRAIC)
//        fun sin(z : Complex) = (exp(i * z) - exp(- i * z)) / (i * 2.toReal())
//        fun cos(z : Complex) = (exp(i * z) + exp(- i * z)) /  2.toReal()
//        fun tan(z : Complex) = sin(z) / cos(z)
//        fun sinh(z : Complex) = - i * sin(i * z)
//        fun cosh(z : Complex) =  cos(i * z)
//        fun tanh(z : Complex) =  -i * tan(i * z)
//        fun abs(z : Complex) = z.radius
//        fun sqrt(z : Complex) = Complex(z.radius, z.angle / 2.toReal(), Form.TRIGONOMETRIC)
//        fun pow2(z : Complex) = z.pow(Real(2))
//        fun asin(z : Complex) = -i * ln(i * z + sqrt(-pow2(z) + 1.toComplex()))
//        fun acos(z : Complex) = -i * ln(z + sqrt(pow2(z) - 1.toComplex()))
//        fun atan(z : Complex) = - (i / 2.toReal()) * ln((1.toComplex() + i * z) / (1.toComplex() - i * z))
//    }
//
//    constructor(realOrRadius : Real, imaginaryOrAngle : Real, form : Form) : this() {
//        when (form) {
//            Form.ALGEBRAIC -> {
//                real = realOrRadius
//                imaginary = imaginaryOrAngle
//
//                updateTrigonometricForm()
//            }
//
//            Form.TRIGONOMETRIC -> {
//                if (realOrRadius < 0.toReal()) {
//                    throw IllegalArgumentException("error: radius < 0")
//                }
//
//                radius = realOrRadius
//                angle = imaginaryOrAngle
//
//                angleToNormalForm()
//                updateAlgebraicForm()
//            }
//        }
//    }
//
//    /** Transforms to angle with same trigonometric functions, which belongs [0, 2pi) */
//    private fun angleToNormalForm() {
//        val angleSin = Real.sin(angle)
//        val angleCos = Real.cos(angle)
//
//        angle = if (angleSin >= 0.toReal()) {
//            Real.acos(angleCos)
//        } else {
//             -Real.acos(angleCos)
//        }
//    }
//
//    private fun updateAlgebraicForm() {
//        real = radius * Real.cos(angle)
//        imaginary = radius * Real.sin(angle)
//    }
//
//    private fun updateTrigonometricForm() {
//        radius = Real.sqrt(Real.pow2(real) + Real.pow2(imaginary))
//        angle = if (radius.additivelyEqualToZero()) {
//            Real(0)
//        } else {
//            if (imaginary >= 0.toReal()) {
//                Real.acos(real / radius)
//            } else {
//                -Real.acos(real / radius)
//            }
//        }
//    }
//
//    operator fun plus(other : Complex) = Complex(real + other.real,
//            imaginary + other.imaginary, Form.ALGEBRAIC)
//
//    operator fun minus(other: Complex) = Complex(real - other.real,
//            imaginary - other.imaginary, Form.ALGEBRAIC)
//
//    operator fun times(other: Complex) = Complex(radius * other.radius,
//            angle + other.angle, Form.TRIGONOMETRIC)
//
//    operator fun div(other: Complex) = Complex(radius / other.radius,
//            angle - other.angle, Form.TRIGONOMETRIC)
//
//    operator fun times(other: Real) = Complex(radius * other, angle, Form.TRIGONOMETRIC)
//
//    operator fun div(other: Real) = Complex(radius / other, angle, Form.TRIGONOMETRIC)
//
//    operator fun unaryMinus() = Complex(-real, -imaginary, Form.ALGEBRAIC)
//
//    operator fun plusAssign(other: Complex) {
//        real += other.real
//        imaginary += other.imaginary
//
//        updateTrigonometricForm()
//    }
//
//    operator fun minusAssign(other: Complex) {
//        real -= other.real
//        imaginary -= other.imaginary
//
//        updateTrigonometricForm()
//    }
//
//    operator fun timesAssign(other: Complex) {
//        radius *= other.radius
//        angle += other.angle
//
//        angleToNormalForm()
//        updateAlgebraicForm()
//    }
//
//    operator fun divAssign(other: Complex) {
//        radius /= other.radius
//        angle -= other.angle
//
//        angleToNormalForm()
//        updateAlgebraicForm()
//    }
//
//    fun equals(other: Complex) = real.additivelyEqualTo(other.real) && imaginary.additivelyEqualTo(other.imaginary) //TODO: discuss: test 'compareTestTrigonometryAsinCorrect'
//
//    fun pow(arg : Complex) : Complex {
//        return if (additivelyEqualToZero()) {
//            0.toComplex()
//        } else {
//            exp(arg * ln(this))
//        }
//    }
//
//    private fun additivelyEqualToZero(): Boolean {
//        return radius.additivelyEqualToZero()
//    }
//
//    fun pow(arg : Real) = pow(Complex(arg, Real(0), Form.ALGEBRAIC))
//
//    override fun toString() = "$real+i$imaginary"
//    fun toDouble() = real.toDouble()
//
//    fun getRadius() = radius
//    fun getAngle() = angle
//    fun getReal() = real
//    fun getImaginary() = imaginary
//}


class Complex private constructor() {
    private var real : Real = Real(0)
    private var imaginary : Real = Real(0)

    companion object {
        val i = Complex(Real(0), Real(1), Form.ALGEBRAIC)

        fun exp(z : Complex) = Complex(Real.exp(z.real), z.imaginary, Form.TRIGONOMETRIC)
        fun ln(z : Complex) = Complex(Real.ln(z.getRadius()), z.getAngle(), Form.ALGEBRAIC)
        fun sin(z : Complex) = (exp(i * z) - exp(- i * z)) / (i * 2.toReal())
        fun cos(z : Complex) = (exp(i * z) + exp(- i * z)) /  2.toReal()
        fun tan(z : Complex) = sin(z) / cos(z)
        fun sinh(z : Complex) = - i * sin(i * z)
        fun cosh(z : Complex) =  cos(i * z)
        fun tanh(z : Complex) =  -i * tan(i * z)
        fun abs(z : Complex) = z.getRadius()
        fun sqrt(z : Complex) : Complex {
            if (z.imaginary.additivelyEqualToZero()){
                if (z.real.additivelyEqualToZero())
                    return 0.toComplex()
                if (z.real > 0.toReal())
                    return Complex(Real.sqrt(z.real), 0.toReal())
                if (z.imaginary.value < -Real.EPSILON)
                    return Complex(0.toReal(), -Real.sqrt(-z.real))
                return Complex(0.toReal(), Real.sqrt(-z.real))
            } else {
                val r = z.getRadius()
                val zz = z + (1.toComplex() - z) * r / (r + 1.toReal())
                val rz = zz.getRadius()
                zz *= Real.sqrt(r) / rz
                return zz
            }
        }

        fun pow2(z : Complex) = z.pow(Real(2))
        fun asin(z : Complex) = -i * ln(i * z + sqrt(-pow2(z) + 1.toComplex()))
        fun acos(z : Complex) = -i * ln(z + sqrt(pow2(z) - 1.toComplex()))
        fun atan(z : Complex) = - (i / 2.toReal()) * ln((1.toComplex() + i * z) / (1.toComplex() - i * z))
    }

    constructor(realOrRadius : Real, imaginaryOrAngle : Real, form : Form = Form.ALGEBRAIC) : this() {
        when (form) {
            Form.ALGEBRAIC -> {
                real = realOrRadius
                imaginary = imaginaryOrAngle
            }

            Form.TRIGONOMETRIC -> {
                if (realOrRadius < 0.toReal()) {
                    throw IllegalArgumentException("error: radius < 0")
                }

                real = realOrRadius * Real.cos(imaginaryOrAngle)
                imaginary = realOrRadius * Real.sin(imaginaryOrAngle)
            }
        }
    }

    operator fun plus(other : Complex) = Complex(real + other.real,
            imaginary + other.imaginary)

    operator fun minus(other: Complex) = Complex(real - other.real,
            imaginary - other.imaginary)

    operator fun times(other: Complex) : Complex {
        val r = real * other.real - imaginary * other.imaginary
        val i = real * other.imaginary + other.real * imaginary

        return Complex(r, i)
    }

    operator fun div(other: Complex) : Complex {
        var res = times(Complex(other.real, -other.imaginary))
        val radius2 =     Real.pow2(other.real) + Real.pow2(other.imaginary)
        res.real /= radius2
        res.imaginary /= radius2

        return res
    }

    operator fun times(other: Real) = Complex(real * other, imaginary * other)

    operator fun div(other: Real) = Complex(real / other, imaginary / other)

    operator fun unaryMinus() = Complex(-real, -imaginary)

    operator fun plusAssign(other: Complex) {
        real += other.real
        imaginary += other.imaginary
    }

    operator fun minusAssign(other: Complex) {
        real -= other.real
        imaginary -= other.imaginary
    }

    operator fun timesAssign(other: Complex) {
        val r = real * other.real - imaginary * other.imaginary
        val i = real * other.imaginary + other.real * imaginary

        real = r
        imaginary = i
    }

    operator fun divAssign(other: Complex) {
        timesAssign(Complex(other.real, -other.imaginary))
        val radius2 = Real.pow2(other.real) + Real.pow2(other.imaginary)

        real /= radius2
        imaginary /= radius2
    }

    operator fun timesAssign(other: Real) {
        real *= other
        imaginary *= other
    }

    operator fun divAssign(other: Real) {
        real /= other
        imaginary /= other
    }


    fun equals(other: Complex) = real.additivelyEqualTo(other.real) && imaginary.additivelyEqualTo(other.imaginary) //TODO: discuss: test 'compareTestTrigonometryAsinCorrect'

    fun pow(arg : Complex) : Complex {
        return if (additivelyEqualToZero()) {
            0.toComplex()
        } else {
            exp(arg * ln(this))
        }
    }

    private fun additivelyEqualToZero(): Boolean {
        return getRadius().additivelyEqualToZero()
    }

    private fun getAngle(r: Real) : Real {
        if (r.additivelyEqualToZero())
            return 0.toReal()
        return if (imaginary >= 0.toReal()) Real.acos(real / r)
        else -Real.acos(real / r)
    }

    fun pow(arg : Real) = pow(Complex(arg, Real(0)))

    override fun toString() = "$real+i$imaginary"
    fun toDouble() = real.toDouble()

    fun getRadius() : Real {
        return Real.sqrt(Real.pow2(real) + Real.pow2(imaginary))
    }

    fun getAngle() : Real {
        val r = getRadius()
        return getAngle(r)
    }

    fun getReal() = real
    fun getImaginary() = imaginary
}