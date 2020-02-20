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