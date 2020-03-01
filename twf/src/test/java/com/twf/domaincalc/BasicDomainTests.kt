package com.twf.domaincalc

import com.twf.api.stringToExpression
import com.twf.baseoperations.DomainAll
import com.twf.expressiontree.DomainCalculator
import com.twf.org.junit.Test
import kotlin.math.exp
import kotlin.test.assertEquals

class BasicDomainTests {

    @Test
    fun testSumDomainBasic() {
        val expression = stringToExpression("x+1")
        val dom = DomainCalculator(expression).calculate().varDomain
        assertEquals(DomainAll, dom.variables.get("x"))
    }

    @Test
    fun testSumDomainTwoVarsBasic() {
        val expression = stringToExpression("x + y")
        val dom = DomainCalculator(expression).calculate().varDomain
        assertEquals(2, dom.variables.size)
        assertEquals(DomainAll, dom.variables["x"])
        assertEquals(DomainAll, dom.variables["y"])
    }

    @Test
    fun testMinusBasic() {
        val expression = stringToExpression("(x + 1) - 10")
        val dom = DomainCalculator(expression).calculate().varDomain
        assertEquals(DomainAll, dom.variables["x"])
    }

    @Test
    fun divisionForReal() {
        val expression = stringToExpression("1 / x")
        val dom = DomainCalculator(expression).calculate().varDomain
        assertEquals(DomainAll.except(0.0), dom.variables["x"])
    }

    @Test
    fun somethingMoreComplex() {
        val expression = stringToExpression("(x + 1)/((x + 1)*(x+2))")
        val dom = DomainCalculator(expression).calculate().varDomain
        assertEquals(DomainAll.except(-1.0, -2.0), dom.variables["x"])
    }

    @Test
    fun somethingMoreComplexWithStuff() {
        val expression = stringToExpression("(x + 1)/((x - 1) * (x - 2))")
        val dom = DomainCalculator(expression).calculate().varDomain
        assertEquals(DomainAll.except(1.0, 2.0), dom.variables["x"])

    }

    @Test
    fun somethingMoreComplexWIthOtherStuff() {
        val expression = stringToExpression("(x + 1)/ ((x - 1) / (x - 2))")
        val dom = DomainCalculator(expression).calculate().varDomain
        assertEquals(DomainAll.except(1.0, 2.0), dom.variables["x"])
    }
}