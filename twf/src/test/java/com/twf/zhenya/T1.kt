package com.twf.zhenya

import com.twf.api.*
import com.twf.config.ComparisonType
import com.twf.config.CompiledConfiguration
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import com.twf.org.junit.Assert
import com.twf.org.junit.Ignore
import com.twf.org.junit.Test
import com.twf.java.math.BigDecimal
import com.twf.java.math.RoundingMode
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextInt

class T1 {
    @Test
    fun compareWithoutSubstitutionsTestWrong() {
        val result = compareWithoutSubstitutions(
                "a + b * c + 4*a - b + sin(x) + a",
                "sin(x) + 5*a + b * (c - 1)"
        )
        Assert.assertEquals(false, result)
    }

    //One parameter functions
    @Test
    fun compareTestAbsWrong() {
        val result = compareWithoutSubstitutions(
                "abs(x)",
                "x"
        )
        Assert.assertEquals(false, result)
    }

    @Test
    fun compareTestTrigonometrySinCorrect() {
        val result = compareWithoutSubstitutions(
                "sin(pi/2)",
                "-sin(3*pi/2)",
                "", ""
        )
        Assert.assertEquals(true, result)
    }

    @Test
    fun compareTestTrigonometryPolynomialWrong() {
        var f = "1"
        for (i in -10..10) {
            f += "*(x+($i*20*pi))"
        }
        val result = compareWithoutSubstitutions(
                "sin(x)",
                f
        )
        Assert.assertEquals(false, result)
    }

    @Test
    fun compareTestPolynomialsCorrect() {
        var f = "1"
        for (i in -1..1) {
            f += when {
                i < 0 -> "*(x$i)"
                i == 0 -> "*x"
                else -> "*(x+$i)"
            }
        }
        val result = compareWithoutSubstitutions(
                "1*(x-1)*x*(x+1)",
                f
        )
        Assert.assertEquals(true, result)
    }


    @Test
    fun compareTestLinePointWrong() {
        val result = compareWithoutSubstitutions(
                "sqrt(1-x)*sqrt(x-1)",
                "x-1"
        )
        Assert.assertEquals(false, result)
    }

    //Не всегда проходит тест
    @Test
    @Ignore
    fun compareTestOverlappingWrong() { // Expressions are not equal only because of a domain difference in two points
        val result = compareWithoutSubstitutions(
                "sqrt(x)*sqrt(0.2-x)*ln(x)/sqrt(0.2-x)",
                "sqrt(x)*sqrt(0.1-x)*ln(x)/sqrt(0.1-x)"
        )
        Assert.assertEquals(false, result)
    }

    @Test
    fun substitutionApplicationTest() {
        val expression = stringToExpression("tg(x)")
        val substitution = expressionSubstitutionFromStrings(
                "x",
                "y"
        )
        val matchedPlaces = findSubstitutionPlacesInExpression(expression, substitution)
        val nMatchedPlaces = mutableListOf(matchedPlaces[0])
        val applicationResult = applySubstitution(expression, substitution, nMatchedPlaces)
        Assert.assertEquals("tg(y)", expressionToString(applicationResult))
    }

    //Тесты на сравнение
    @Test
    fun compareTestPointCorrect() {
        val compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.LEFT_MORE_OR_EQUAL
        val result = compareWithoutSubstitutions(
                "-sqrt(1-(x-1)^2)*sqrt(-1+(-x+1)^2)*ln(x)",
                "sqrt(x-2)*sqrt(2-x)",
                compiledConfiguration = compiledConfiguration
        )
        Assert.assertEquals(true, result)
    }

    private val names = arrayOf("asin(x)", "acos(x)", "tg(x)", "ctg(x)", "ln(x)", "x^0.5")

    private fun generateExpression(expression: ExpressionNode, varName: String): ExpressionNode {
        var localExpression = expression
        val doInverse = nextBoolean()
        if (doInverse) {
            localExpression = stringToExpression("-${expressionToString(localExpression)}")
        }
        //Do a shift to current expression or not
        val doShift = nextBoolean()
        if (doShift) {
            var shift = nextDouble(-1.0, 1.0)
            shift = BigDecimal(shift).setScale(2, RoundingMode.HALF_EVEN).toDouble()
            if (shift > 0)
                localExpression = stringToExpression(
                        expressionToString(localExpression) + "+$shift"
                )
            else if (shift < 0)
                localExpression = stringToExpression(
                        expressionToString(localExpression) + "$shift"
                )
        }

        //ExpressionSubstitution
        val i = nextInt(0, names.size)

        val rightPart = stringToExpression(names[i])

        val substitutionRightPart = expressionSubstitutionFromStrings(
                "x",
                varName)

        val substitutionPlacesRightPart = findSubstitutionPlacesInExpression(
                rightPart,
                substitutionRightPart)

        val desiredSubstitutionPlacesRightPart = mutableListOf(
                substitutionPlacesRightPart[0])

        val desiredSubstitutionRightPart = applySubstitution(
                rightPart,
                substitutionRightPart,
                desiredSubstitutionPlacesRightPart)

        val currentSubstitution = expressionSubstitutionFromStrings(
                varName,
                expressionToString(desiredSubstitutionRightPart))

        val placesSubstitution = findSubstitutionPlacesInExpression(
                localExpression,
                currentSubstitution
        )

        return if (placesSubstitution.isNotEmpty()) {
            val r = nextInt(0, placesSubstitution.size)
            val chosenPlacesSubstitutions = mutableListOf(placesSubstitution[r]) //List<>placesSubstitution[i]
            applySubstitution(localExpression, currentSubstitution, chosenPlacesSubstitutions)
        } else
            localExpression
    }

    @Test
    @Ignore
    fun compareTestRandomSearchWrong() {
        var m = 0
        val all = 20
        for (j in 1..all) {
            var expressionA = stringToExpression("x")
            var expressionB = stringToExpression("x")

            for (i in 1..3) {
                expressionA = generateExpression(expressionA, "x")
                expressionB = generateExpression(expressionB, "x")
            }
            val result = compareWithoutSubstitutions(
                    expressionA,
                    expressionB
            )//expected result-false
            if (!result)
                m++
            else {
                println("@Test\n" +
                        "    fun test$j() {\n" +
                        "        val result = compareWithoutSubstitutions(\n" +
                        "                \"${expressionToString(expressionA)}\",\n" +
                        "                \"${expressionToString(expressionB)}\"\n" +
                        "        )\n" +
                        "        Assert.assertEquals(false, result)\n" +
                        "    }")
                println()
            }
        }
        println(m)
        Assert.assertEquals(1, m / all)
    }

    private val vars = arrayOf("x", "y", "z", "u")

    private fun generateExpressionTwoVar(): ExpressionNode {
        val base = nextInt(0, vars.size)
        var expressionString = vars[base]
        val timesToAdd = nextInt(1, 5)
        for (j in 1..timesToAdd) {
            val plus = nextBoolean()
            expressionString += if (plus)
                "+"
            else
                "-"
            val varToAdd = nextInt(0, vars.size)
            expressionString += vars[varToAdd]
        }
        return stringToExpression(expressionString)
    }

    private fun modifyExpressionTwoVar(expression: ExpressionNode): ExpressionNode {
        var localExpression = expression
        for (i in 0 until vars.size)
            localExpression = generateExpression(localExpression, vars[i])
        return localExpression
    }

    @Test
    @Ignore
    fun compareTestTwoVarRandomSearchWrong() {
        var m = 0
        val all = 20
        for (k in 1..all) {
            var expressionA = generateExpressionTwoVar()
            var expressionB = generateExpressionTwoVar()

            for (j in 1..3) {
                expressionA = modifyExpressionTwoVar(expressionA)
                expressionB = modifyExpressionTwoVar(expressionB)
            }
            val result = compareWithoutSubstitutions(expressionA, expressionB)
            if (!result)
                m++
            else {
                println("@Test\n" +
                        "    fun test$k() {\n" +
                        "        val result = compareWithoutSubstitutions(\n" +
                        "                \"${expressionToString(expressionA)}\",\n" +
                        "                \"${expressionToString(expressionB)}\"\n" +
                        "        )\n" +
                        "        Assert.assertEquals(false, result)\n" +
                        "    }")
                println()
            }
        }
        println(m)
        Assert.assertEquals(1, m / all)
    }
}