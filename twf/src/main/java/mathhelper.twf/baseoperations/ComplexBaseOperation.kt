package mathhelper.twf.baseoperations

import mathhelper.twf.numbers.*

class ComplexBaseOperation {
    companion object {
        fun plus(listOfArgs : List<Complex>) : Complex {
            val result = 0.toComplex()
            for (arg in listOfArgs) {
                result += arg
            }
            return result
        }

        fun minus(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.isEmpty()) {
                throw IllegalArgumentException("Given list is empty")
            }

            val result = 0.toComplex()
            for (arg in listOfArgs) {
                result -= arg
            }

            return result
        }

        fun mul(listOfArgs: List<Complex>) : Complex {
            val result = 1.toComplex()
            for (arg in listOfArgs) {
                result *= arg
            }
            return result
        }

        fun div(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.isEmpty()) {
                throw IllegalArgumentException("Given list is empty")
            }

            val result = listOfArgs[0]
            var isFirstElement = true // Unlike minus, multiplying on first value may lead to accuracy losing

            for (arg in listOfArgs) {
                if (isFirstElement) {
                    isFirstElement = false
                } else {
                    result /= arg
                }
            }

            return result
        }

        fun mod(listOfArgs : List<Complex>) : Complex {
            // I don't know what to do
            return 0.toComplex()
        }

        fun pow(listOfArgs : List<Complex>) : Complex {
            var index = listOfArgs.size - 1
            var currentPow = 1.toComplex()

            while (index >= 0) {
                currentPow = listOfArgs[index].pow(currentPow)
                index--
            }

            return currentPow
        }

        fun and(listOfArgs: List<Complex>) : Complex {
            for (arg in listOfArgs) {
                if (arg.equals(0.toComplex())) {
                    return 0.toComplex()
                }
            }

            return 1.toComplex()
        }

        fun or(listOfArgs: List<Complex>) : Complex {
            for (arg in listOfArgs) {
                if (!arg.equals(0.toComplex())) {
                    return 1.toComplex()
                }
            }

            return 0.toComplex()
        }

        fun xor(listOfArgs: List<Complex>) : Complex {
            var counter = 0
            for (arg in listOfArgs) {
                if (!arg.equals(0.toComplex())) {
                    counter++
                }
            }

            return (counter % 2).toComplex()
        }

        fun alleq(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.isEmpty()) {
                return 1.toComplex()
            }

            val firstArg = listOfArgs[0]
            for (arg in listOfArgs) {
                if (!arg.equals(firstArg)) {
                    return 0.toComplex()
                }
            }

            return 1.toComplex()
        }

        fun not(listOfArgs: List<Complex>) : Complex {
            return if (listOfArgs.size != 1) {
                throw IllegalArgumentException("List size is not equals to 1")
            } else {
                if (listOfArgs[0].equals(0.toComplex())) {
                    1.toComplex()
                } else {
                    0.toComplex()
                }
            }
        }

        fun sin(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.sin(listOfArgs[0])
        }

        fun cos(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.cos(listOfArgs[0])
        }

        fun sinh(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.sinh(listOfArgs[0])
        }


        fun cosh(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.cosh(listOfArgs[0])
        }

        fun tan(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.tan(listOfArgs[0])
        }

        fun tanh(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.tanh(listOfArgs[0])
        }

        fun asin(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.asin(listOfArgs[0])
        }

        fun acos(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.acos(listOfArgs[0])
        }

        fun atan(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.atan(listOfArgs[0])
        }

        fun actan(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            val args = listOf(Complex(Real(1.0), Real(0.0), Form.ALGEBRAIC), listOfArgs[0])
            return Complex.atan(div(args))
        }

        fun exp(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.exp(listOfArgs[0])
        }

        fun ln(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex.ln(listOfArgs[0])
        }

        fun abs(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return Complex(Complex.abs(listOfArgs[0]), Real(0), Form.ALGEBRAIC)
        }

        fun sqrt(listOfArgs: List<Complex>) : Complex {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }
            return Complex.sqrt(listOfArgs[0])
        }
    }
}