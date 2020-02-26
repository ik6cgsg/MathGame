package com.twf.api

import com.twf.org.junit.Test

import com.twf.org.junit.Assert.*
import com.twf.org.junit.Ignore

class ExpressionAPIKtTest {
    @Test
    fun expressionToStringMinusTest() {
        val string = "-(a-b/c)"
        val expression = stringToExpression((string))
        val userString = expressionToString(expression)
        assertEquals("-(a-(b/c))", userString)
    }

    @Test
    fun expressionToStringSetTheoryTest() {
        val string = "!x \\ y"
        val expression = stringToExpression((string), "setTheory")
        val userString = expressionToString(expression, 2)
        assertEquals("!x\\\\y", userString)
    }

    @Test
    fun expressionToStringTest() {
        val string = "sin(2*x) + sin(x+sin(y))"
        val expression = stringToExpression((string))
        val userString = expressionToString(expression)
        assertEquals("sin(2*x)+sin(x+sin(y))", userString)
    }

    @Test
    fun expressionFactorialToStringTest() {
        val string = "y!!*n!"
        val expression = stringToExpression((string))
        val userString = expressionToString(expression)
        assertEquals("y!!*n!", userString)
    }

    @Test
    fun compareWithoutSubstitutionsTestCorrect (){
        val result = compareWithoutSubstitutions(
                "a + b * c + 4*a - b + sin(x)",
                "sin(x) + 5*a + b * (c - 1)"
        )
        assertEquals(true, result)
    }

    @Test
    fun compareWithoutSubstitutionsTestWrong (){
        val result = compareWithoutSubstitutions(
                "a + b * c + 4*a - b + sin(x) + a",
                "sin(x) + 5*a + b * (c - 1)",
                ""
        )
        assertEquals(false, result)
    }

    @Test
    @Ignore //TODO: investigate, why it's fails (wrong result for acos)
    fun compareTestTrigonometryAsinCorrect (){
        val result = compareWithoutSubstitutions(
                "asin(1)-acos(x)",
                "asin(x)",
                ""
        )
        assertEquals(true, result)
    }

    @Test
    fun compareTestTrigonometrySinCorrect (){
        val result = compareWithoutSubstitutions(
                "sin(pi/2)",
                "-sin(3*pi/2)",
                "", "+;-;*;/;sin"
        )
        assertEquals(true, result)
    }

    @Test
    fun compareTestTrigonometrySinUncorrectBecauseOfForbiddenSin (){
        val result = compareWithoutSubstitutions(
                "sin(pi/2)",
                "-sin(3*pi/2)",
                "", "+;-;*;/"
        )
        assertEquals(false, result)
    }

    @Test
    fun substitutionApplicationTest (){
        val expression = stringToExpression("x")
        val substitution = expressionSubstitutionFromStrings(
                "x",
                "tg(x)"
        )
        val matchedPlaces = findSubstitutionPlacesInExpression(expression, substitution)
        assertEquals(1, matchedPlaces.size)
        val applicationResult = applySubstitution(expression, substitution, matchedPlaces)
        assertEquals("tg(x)", expressionToString(applicationResult))
    }

    @Test
    fun testSubstitutionApplicationWithNodeIds (){
        val expression = stringToExpression("x+y/z")
        assertEquals("  :  [-1; 5; 0)\n" +
                "  +  :  [-1; 5; 1)\n" +
                "    x  :  [0; 1; 2)\n" +
                "    /  :  [-1; 5; 3)\n" +
                "      y  :  [2; 3; 4)\n" +
                "      z  :  [4; 5; 5)\n", expression.toStringsWithPositions())
        val substitution = expressionSubstitutionFromStrings(
                "a/b",
                "a*b/(b*b)"
        )
        val matchedPlaces = findSubstitutionPlacesInExpression(expression, substitution)
        assertEquals(1, matchedPlaces.size)
        val applicationResult = applySubstitution(expression, substitution, matchedPlaces)
        assertEquals("x+((y*z)/(z*z))", expressionToString(applicationResult))
        assertEquals("  :  [-1; 5; 0)\n" +
                "  +  :  [-1; 5; 1)\n" +
                "    x  :  [0; 1; 2)\n" +
                "    /  :  [-1; 9; 3)\n" +
                "      *  :  [-1; 3; 4)\n" +
                "        y  :  [2; 3; 5)\n" +
                "        z  :  [4; 5; 6)\n" +
                "      *  :  [4; 9; 7)\n" +
                "        z  :  [4; 5; 8)\n" +
                "        z  :  [4; 5; 9)\n", expression.toStringsWithPositions())
    }

    @Test
    fun testSubstitutionApplicationWithNodeIdsForDeepAppliedSubstitution (){
        val expression = stringToExpression("(!(!(!(a|b))))", scope = "setTheory")
        assertEquals("  :  [0; 14; 0)\n" +
                "  not  :  [1; 11; 1)\n" +
                "    not  :  [3; 11; 2)\n" +
                "      not  :  [5; 11; 3)\n" +
                "        or  :  [6; 11; 4)\n" +
                "          a  :  [7; 8; 5)\n" +
                "          b  :  [9; 10; 6)\n", expression.toStringsWithPositions())
        val substitution = expressionSubstitutionFromStrings(
                "a",
                "a&a"
        )
        val matchedPlaces = findSubstitutionPlacesInExpression(expression, substitution)
        assertEquals(6, matchedPlaces.size)
        val applicationResult = applySubstitution(expression, substitution, listOf(matchedPlaces[1]))
        assertEquals("!!!(a|(b&b))", expressionToString(applicationResult))
        assertEquals("  :  [0; 14; 0)\n" +
                "  not  :  [1; 11; 1)\n" +
                "    not  :  [3; 11; 2)\n" +
                "      not  :  [5; 11; 3)\n" +
                "        or  :  [6; 11; 4)\n" +
                "          a  :  [7; 8; 5)\n" +
                "          and  :  [0; 3; 6)\n" +
                "            b  :  [9; 10; 7)\n" +
                "            b  :  [9; 10; 8)\n", expression.toStringsWithPositions())
    }

    @Test
    fun findSubstitutionPlacesInExpressionJSONTest(){
        val result = findSubstitutionPlacesCoordinatesInExpressionJSON(
                "sin(2*x) + sin(x+sin(y))",
                "sin(x)",
                "sqrt(1 - (cos(x))^2)"
        )
        assertEquals("{\"substitutionPlaces\":[{\"parentStartPosition\":\"0\",\"parentEndPosition\":\"24\",\"startPosition\":\"0\",\"endPosition\":\"8\"},{\"parentStartPosition\":\"14\",\"parentEndPosition\":\"24\",\"startPosition\":\"17\",\"endPosition\":\"23\"},{\"parentStartPosition\":\"0\",\"parentEndPosition\":\"24\",\"startPosition\":\"11\",\"endPosition\":\"24\"}]}", result)
    }

    @Test
    fun applySubstitutionTest(){
        val result = applyExpressionBySubstitutionPlaceCoordinates(
                "sin(2*x) + sin(x+sin(y))",
                "sin(x)",
                "sqrt(1 - (cos(x))^2)",
                14,
                24,
                17,
                23
        )
        assertEquals("sin(2*x)+sin(x+sqrt(1-(cos(y)^2)))", result)
    }

    @Test
    fun findSubstitutionPlacesInExpressionJSONTest_BugFromIlya(){
        val result = findSubstitutionPlacesCoordinatesInExpressionJSON(
                "((cos(x)*cos(y))-cos(x+y))/(cos(x-y)-(sin(x)*sin(y)))",
                "cos(x+y)",
                "cos(x)*cos(y)-sin(x)*sin(y)"
        )
        assertEquals("{\"substitutionPlaces\":[{\"parentStartPosition\":\"16\",\"parentEndPosition\":\"25\",\"startPosition\":\"17\",\"endPosition\":\"25\"},{\"parentStartPosition\":\"27\",\"parentEndPosition\":\"53\",\"startPosition\":\"28\",\"endPosition\":\"36\"}]}", result)
    }

    @Test
    fun findSubstitutionPlacesInExpressionJSONTest_BugFromAlex(){
        val result = findSubstitutionPlacesCoordinatesInExpressionJSON(
                "(A|B)&(C->0)",
                "!A&!B",
                "!(A|B)",
                scope = "setTheory"
        )
        assertEquals("{\"substitutionPlaces\":[]}", result)
    }

    @Test
    fun applySubstitutionTest_BugFromIlya(){
        val result = applyExpressionBySubstitutionPlaceCoordinates(
                "((cos(x)*cos(y))-cos(x+y))/(cos(x-y)-(sin(x)*sin(y)))",
                "cos(x+y)",
                "cos(x)*cos(y)-sin(x)*sin(y)",
                27,
                53,
                28,
                36
        )
        assertEquals("((cos(x)*cos(y))-cos(x+y))/(((cos(x)*cos(-y))-(sin(x)*sin(-y)))-(sin(x)*sin(y)))", result)
    }

    @Test
    fun setTaskGeneration(){
        val task = generateTaskInJSON(
                "A\\/(B\\/C)=A\\/B\\/C;A/\\(B/\\C)=A/\\B/\\C;A\\/(B/\\C)=(A\\/B)/\\(A\\/C);A/\\(B\\/C)=(A/\\B)\\/(A/\\C)",
                3,
                "A/\\(B/\\C)",
                "setTheory"
        )
        assertEquals("{\"originalExpression\":\"A&(B&C)\",\"finalExpression\":\"A&B&C\",\"requiredSubstitutions\":[{\"left\":\"A&(B&C)\",\"right\":\"A&B&C\"}],\"allSubstitutions\":[{\"left\":\"A|(B|C)\",\"right\":\"A|B|C\"},{\"left\":\"A&(B&C)\",\"right\":\"A&B&C\"},{\"left\":\"A|(B&C)\",\"right\":\"(A|B)&(A|C)\"},{\"left\":\"A&(B|C)\",\"right\":\"(A&B)|(A&C)\"}]}", task)
    }

    @Test
    fun setTaskTwoStepsGeneration(){
        val task = generateTaskInJSON(
                "!(A/\\B)=!A\\/!B;A\\/(B/\\C)=(A\\/B)/\\(A\\/C)",
                2,
                "!(A\\/(B/\\C))",
                "setTheory"
        )
        assertEquals("{\"originalExpression\":\"!(A|(B&C))\",\"finalExpression\":\"!(A|B)|!(A|C)\",\"requiredSubstitutions\":[{\"left\":\"A|(B&C)\",\"right\":\"(A|B)&(A|C)\"},{\"left\":\"!(A&B)\",\"right\":\"!A|!B\"}],\"allSubstitutions\":[{\"left\":\"!(A&B)\",\"right\":\"!A|!B\"},{\"left\":\"A|(B&C)\",\"right\":\"(A|B)&(A|C)\"}]}", task)
    }

    @Test
    fun setTaskOneStepGeneration(){
        val task = generateTaskInJSON(
                "!(A&B)=!A|!B;A|(B&C)=(A|B)&(A|C)",
                1,
                "!(A|(B&C))",
                "setTheory"
        )
        assertEquals("{\"originalExpression\":\"!(A|(B&C))\",\"finalExpression\":\"!((A|B)&(A|C))\",\"requiredSubstitutions\":[{\"left\":\"A|(B&C)\",\"right\":\"(A|B)&(A|C)\"}],\"allSubstitutions\":[{\"left\":\"!(A&B)\",\"right\":\"!A|!B\"},{\"left\":\"A|(B&C)\",\"right\":\"(A|B)&(A|C)\"}]}", task)
    }

    @Test
    @Ignore
    fun taskGenerationWorker(){
        for (i in 1..5) {
            val task = generateTaskInJSON(
                    "A\\/(B\\/C)=A\\/B\\/C;" +
                            "A\\/B\\/C=A\\/(B\\/C);" +
                            "A/\\(B/\\C)=A/\\B/\\C;" +
                            "A/\\B/\\C=A/\\(B/\\C);" +
                            "(A\\/B)\\/C=A\\/B\\/C;" +
                            "A\\/B\\/C=(A\\/B)\\/C;" +
                            "(A/\\B)/\\C=A/\\B/\\C;" +
                            "A/\\B/\\C=(A/\\B)/\\C;" +
                            "A\\/(B/\\C)=(A\\/B)/\\(A\\/C);" +
                            "(A\\/B)/\\(A\\/C)=A\\/(B/\\C);" +
                            "A/\\(B\\/C)=(A/\\B)\\/(A/\\C);" +
                            "(A/\\B)\\/(A/\\C)=A/\\(B\\/C);" +
                            "!(A/\\B)=!A\\/!B;" +
                            "!A\\/!B=!(A/\\B);" +
                            "!(A\\/B)=!A/\\!B;" +
                            "!A/\\!B=!(A\\/B);" +
                            "!A\\/B=A->B;" +
                            "A->B=!A\\/B;" +
                            "A/\\!B=A\\B;" +
                            "A\\B=A/\\!B;" +
                            "!(!(A))=A;" +
                            "A=!(!(A));" +
                            "A\\/(A/\\B)=A;" +
                            "A=A\\/(A/\\B);" +
                            "A/\\(A\\/B)=A;" +
                            "A=A/\\(A\\/B);" +
                            "A\\/A=A;" +
                            "A=A\\/A;" +
                            "A/\\A=A;" +
                            "A=A/\\A",
                    (i+1)/2+1,
                    "(C->B)->A",
                    "setTheory"
            )
            println(task)
        }
    }
}