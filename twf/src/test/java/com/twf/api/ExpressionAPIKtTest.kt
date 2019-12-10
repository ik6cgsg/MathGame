package com.twf.api

import org.junit.Test

import org.junit.Assert.*
import org.junit.Ignore

class ExpressionAPIKtTest {
    @Test
    fun expressionToStringMinusTest() {
        val string = "-(a-b/c)"
        val expression = stringToExpression((string))
        val userString = expressionToString(expression)
        assertEquals("(-(a-(b/c)))", userString)
    }

    @Test
    fun expressionToStringTest() {
        val string = "sin(2*x) + sin(x+sin(y))"
        val expression = stringToExpression((string))
        val userString = expressionToString(expression)
        assertEquals("(sin(2*x)+sin(x+sin(y)))", userString)
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
                "sin(x) + 5*a + b * (c - 1)"
        )
        assertEquals(false, result)
    }

    @Test
    @Ignore //TODO: investigate, why it's fails (wrong result for acos)
    fun compareTestTrigonometryAsinCorrect (){
        val result = compareWithoutSubstitutions(
                "asin(1)-acos(x)",
                "asin(x)"
        )
        assertEquals(true, result)
    }

    @Test
    fun compareTestTrigonometrySinCorrect (){
        val result = compareWithoutSubstitutions(
                "sin(pi/2)",
                "-sin(3*pi/2)"
        )
        assertEquals(true, result)
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
        assertEquals("{\"substitutionPlaces\":[{\"parentStartPosition\":\"0\",\"parentEndPosition\":\"24\",\"startPosition\":\"0\",\"endPosition\":\"7\"},{\"parentStartPosition\":\"14\",\"parentEndPosition\":\"23\",\"startPosition\":\"17\",\"endPosition\":\"22\"},{\"parentStartPosition\":\"0\",\"parentEndPosition\":\"24\",\"startPosition\":\"11\",\"endPosition\":\"23\"}]}", result)
    }

    @Test
    fun applySubstitutionTest(){
        val result = applyExpressionBySubstitutionPlaceCoordinates(
                "sin(2*x) + sin(x+sin(y))",
                "sin(x)",
                "sqrt(1 - (cos(x))^2)",
                14,
                23,
                17,
                22
        )
        assertEquals("(sin(2*x)+sin(x+sqrt(1-(cos(y)^2))))", result)
    }

}