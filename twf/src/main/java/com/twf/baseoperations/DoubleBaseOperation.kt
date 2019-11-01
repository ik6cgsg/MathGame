package com.twf.baseoperations

import kotlin.math.*


class DoubleBaseOperation {
    companion object {
        fun plus(listOfArgs : List<Double>) : Double {
            var result = 0.0
            for (arg in listOfArgs) {
                result += arg
            }
            return result
        }

        fun minus(listOfArgs: List<Double>) : Double {
            if (listOfArgs.isEmpty()) {
                throw IllegalArgumentException("Given list is empty")
            }

            var result = 0.0
            for (arg in listOfArgs) {
                result -= arg
            }

            return result
        }

        fun mul(listOfArgs: List<Double>) : Double {
            var result = 1.0
            for (arg in listOfArgs) {
                result *= arg
            }
            return result
        }

        fun div(listOfArgs: List<Double>) : Double {
            if (listOfArgs.isEmpty()) {
                throw IllegalArgumentException("Given list is empty")
            }

            var result = listOfArgs[0]
            var isFirstElement = true

            for (arg in listOfArgs) {
                if (isFirstElement) {
                    isFirstElement = false
                } else {
                    result /= arg
                }
            }

            return result
        }

        fun mod(listOfArgs : List<Double>) : Double {
            if (listOfArgs.size != 2) {
                throw IllegalArgumentException("list size not equals 2")
            } else {
                return listOfArgs[0].rem(listOfArgs[1])
            }
        }

        fun pow(listOfArgs : List<Double>) : Double {
            var index = listOfArgs.size - 1
            var currentPow = 1.0

            while (index >= 0) {
                currentPow = listOfArgs[index].pow(currentPow)
                index--
            }

            return currentPow
        }

        fun and(listOfArgs: List<Double>) : Double {
            for (arg in listOfArgs) {
                if (arg == 0.0) {
                    return 0.0
                }
            }

            return 1.0
        }

        fun not(listOfArgs: List<Double>) : Double {
            return if (listOfArgs.size != 1) {
                throw IllegalArgumentException("List size is not equals to 1")
            } else {
                if (BaseOperationsComputation.additivelyEqual(0.0, listOfArgs[0])) {
                    1.0
                } else {
                    0.0
                }
            }
        }

        fun or(listOfArgs: List<Double>) : Double {
            for (arg in listOfArgs) {
                if (arg != 0.0) {
                    return 1.0
                }
            }

            return 0.0
        }

        fun xor(listOfArgs: List<Double>) : Double {
            var counter = 0
            for (arg in listOfArgs) {
                if (arg != 0.0) {
                    counter++
                }
            }

            return (counter % 2).toDouble()
        }

        fun alleq(listOfArgs: List<Double>) : Double {
            if (listOfArgs.isEmpty()) {
                return 1.0
            }

            val firstArg = listOfArgs[0]
            for (arg in listOfArgs) {
                if (arg != firstArg) {
                    return 0.0
                }
            }

            return 1.0
        }

        fun sin(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return sin(listOfArgs[0])
        }

        fun cos(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return cos(listOfArgs[0])
        }

        fun sinh(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return sinh(listOfArgs[0])
        }


        fun cosh(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return cosh(listOfArgs[0])
        }

        fun tan(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return tan(listOfArgs[0])
        }

        fun tanh(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return tanh(listOfArgs[0])
        }

        fun asin(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return asin(listOfArgs[0])
        }

        fun acos(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return acos(listOfArgs[0])
        }

        fun atan(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return atan(listOfArgs[0])
        }

        fun actan(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return atan(1.0 /listOfArgs[0])
        }

        fun exp(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return exp(listOfArgs[0])
        }

        fun ln(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return ln(listOfArgs[0])
        }

        fun abs(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return abs(listOfArgs[0])
        }

        fun sqrt(listOfArgs: List<Double>) : Double {
            if (listOfArgs.size != 1) {
                throw IllegalArgumentException("Given list contains not one argument")
            }

            return sqrt(listOfArgs[0])
        }
    }
}