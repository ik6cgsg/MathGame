package mathhelper.twf.numbers

import kotlin.math.*

fun Int.toReal() = Real(this)
fun Double.toReal() = Real(this)

// Expected format for x: x
fun String.toRealOrNull() : Real? {
    val resultDouble = this.toDoubleOrNull()

    return if (resultDouble === null) {
        null
    } else {
        Real(resultDouble)
    }
}

class Real(val value : Double) : Comparable<Real> {
    companion object {
        val PI = Real(kotlin.math.PI)
        const val EPSILON = 11.9e-7
        private val POSITIVE_INFINITY = Real(Double.POSITIVE_INFINITY)
        private val NEGATIVE_INFINITY = Real(Double.NEGATIVE_INFINITY)

        fun sin(arg : Real) = Real(sin(arg.value))
        fun cos(arg : Real) = Real(cos(arg.value))
        fun tan(arg: Real) = Real(tan(arg.value))
        fun exp(arg: Real) : Real {
            return when {
                arg === POSITIVE_INFINITY -> POSITIVE_INFINITY
                arg === NEGATIVE_INFINITY -> Real(0)
                else -> Real(exp(arg.value))
            }
        }

        fun asin(arg: Real) = Real(asin(arg.value))
        fun acos(arg: Real) = Real(acos(arg.value))
        fun atan(arg: Real) : Real {
            return when {
                arg === POSITIVE_INFINITY -> Real(kotlin.math.PI / 2)
                arg === NEGATIVE_INFINITY -> Real(-kotlin.math.PI / 2)
                else -> Real(atan(arg.value))
            }
        }
        fun sinh(arg: Real) = Real(sinh(arg.value))
        fun cosh(arg: Real) = Real(cosh(arg.value))
        fun tanh(arg: Real) = Real(tanh(arg.value))
        fun ln(arg: Real) : Real {
            return when {
                arg === POSITIVE_INFINITY -> POSITIVE_INFINITY
                ln(arg.value) == Double.NEGATIVE_INFINITY -> NEGATIVE_INFINITY
                else -> Real(ln(arg.value))
            }
        }

        fun abs(arg: Real) : Real {
            return if (arg === POSITIVE_INFINITY || arg === NEGATIVE_INFINITY) {
                POSITIVE_INFINITY
            } else {
                Real(abs(arg.value))
            }
        }
        fun pow2(arg : Real) : Real {
            return if (arg === POSITIVE_INFINITY || arg === NEGATIVE_INFINITY) {
                POSITIVE_INFINITY
            } else {
                Real(arg.value * arg.value)
            }
        }

        fun sqrt(arg : Real) : Real {
            return if (arg === POSITIVE_INFINITY) {
                POSITIVE_INFINITY
            } else {
                Real(sqrt(arg.value))  //TODO benchmark
            }
        }
    }

    constructor(value : Int) : this(value.toDouble())

    override fun compareTo(other: Real): Int {
        return value.compareTo(other.value)
    }

    operator fun plus(other : Real) = Real(value + other.value)
    operator fun minus(other : Real) = Real(value - other.value)
    operator fun times(other : Real) = Real(value * other.value)
    operator fun div(other : Real) = Real(value / other.value)
    operator fun unaryMinus() = Real(-value)

    fun additivelyEqualTo(number: Real) = ((value - number.value) in (-EPSILON)..EPSILON)
    fun additivelyEqualToZero() = (value in (-EPSILON)..EPSILON)

    fun pow(arg : Real) : Real {
        return if (additivelyEqualToZero()) {
            Real(0)
        } else {
            Real(value.pow(arg.value))
        }
    }
    fun toDouble() = value

    override fun toString() = value.toString()
}