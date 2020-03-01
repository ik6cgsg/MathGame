package com.twf.substitutiontests

import com.twf.api.expressionSubstitutionFromStrings
import com.twf.api.findSubstitutionPlacesInExpression
import com.twf.api.stringToExpression
import com.twf.expressiontree.*
import com.twf.org.junit.Test
import kotlin.test.assertEquals

class SubstitutionCheckAndApplyTests {
    @Test
    fun varNamesTimeStorage() {
        val varNamesTimeStorage = VarNamesTimeStorage()
        val time1 = varNamesTimeStorage.addVarName("var1", SubstitutionInstanceVarType.EXPR_VAR)
        assertEquals(0, time1)
        val time2 = varNamesTimeStorage.addVarName("var2", SubstitutionInstanceVarType.EXPR_VAR)
        assertEquals(1, time2)
        val time3 = varNamesTimeStorage.addVarName("var3", SubstitutionInstanceVarType.EXPR_VAR)
        val time4 = varNamesTimeStorage.addVarName("var4", SubstitutionInstanceVarType.EXPR_VAR)
        val pop1 = varNamesTimeStorage.popVarsAfter(2)
        assertEquals(listOf(SubstitutionInstanceVar("var3", SubstitutionInstanceVarType.EXPR_VAR, 2), SubstitutionInstanceVar("var4", SubstitutionInstanceVarType.EXPR_VAR, 3)), pop1)
        assertEquals(listOf(SubstitutionInstanceVar("var1", SubstitutionInstanceVarType.EXPR_VAR, 0), SubstitutionInstanceVar("var2", SubstitutionInstanceVarType.EXPR_VAR, 1)), varNamesTimeStorage.varsList)
        val time5 = varNamesTimeStorage.addVarName("var5", SubstitutionInstanceVarType.EXPR_VAR)
        val time6 = varNamesTimeStorage.addVarName("var6", SubstitutionInstanceVarType.EXPR_VAR)
        val pop2 = varNamesTimeStorage.popVarsAfter(5)
        assertEquals(listOf(SubstitutionInstanceVar("var6", SubstitutionInstanceVarType.EXPR_VAR, 5)), pop2)
        val pop3 = varNamesTimeStorage.popVarsAfter(1)
        assertEquals(listOf(SubstitutionInstanceVar("var2", SubstitutionInstanceVarType.EXPR_VAR, 1), SubstitutionInstanceVar("var5", SubstitutionInstanceVarType.EXPR_VAR, 4)), pop3)
    }

    @Test
    fun allowedNoSubstitution() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("S(i, a, a, f(i))", true),
                parseStringExpression("f(a)", true)
        )
        val root = parseStringExpression("S(i, a, b, i^2)")
        assertEquals(null, substitution.checkAndApply(root.children[0]))
    }

    @Test
    fun allowedSubstitution() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("S(i, a, a, f(i))", true),
                parseStringExpression("f(a)", true)
        )
        val root = parseStringExpression("S(i, b, b, i^2)")
        assertEquals("^(b;2)", substitution.checkAndApply(root.children[0]).toString())
    }

    @Test
    fun allowedNumberSubstitution() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("S(i, a, a, f(i))", true),
                parseStringExpression("f(a)", true)
        )
        val root = parseStringExpression("S(i, 945.8, 945.8, i^2)")
        assertEquals("^(945.8;2)", substitution.checkAndApply(root.children[0]).toString())
    }

    @Test
    fun allowedSumSubstitution() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("S(i, a, b, f(i)) + S(j, b+1, c, f(j))", true),
                parseStringExpression("S(i, a, c, f(i))", true)
        )
        val root = parseStringExpression("S(i, a, b, i^i) + S(j, b+1, c, j^j)")
        assertEquals("S(i;a;c;^(i;i))", substitution.checkAndApply(root.children[0]).toString())
    }

    @Test
    fun allowedSumSumSubstitution() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("S(i, a, b, f(i)) + S(j, a, b, g(j))", true),
                parseStringExpression("S(i, a, b, f(i) + g(i))", true)
        )
        val root = parseStringExpression("S(i, 1, n+1, i^i) + S(j, 1, n+1, j^j)")
        assertEquals("S(i;1;+(n;1);+(^(i;i);^(i;i)))", substitution.checkAndApply(root.children[0]).toString())
    }

    @Test
    fun allowedNotBasedOnTaskContextSubstitution() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("x + y", true),
                parseStringExpression("z + b", true)
        )
        val root = parseStringExpression("a+y")
        assertEquals("+(z;b)", substitution.checkAndApply(root.children[0]).toString())
    }

    @Test
    fun allowedBasedOnTaskContextSubstitution() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("x + y", true),
                parseStringExpression("z + b", true),
                basedOnTaskContext = true
        )
        val root = parseStringExpression("x+y")
        assertEquals("+(z;b)", substitution.checkAndApply(root.children[0]).toString())
    }

    @Test
    fun notAllowedBasedOnTaskContextSubstitution() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("x + y", true),
                parseStringExpression("z + b", true),
                basedOnTaskContext = true
        )
        val root = parseStringExpression("a+y")
        assertEquals(null, substitution.checkAndApply(root.children[0]))
    }

    @Test
    fun notAllowedBasedOnTaskContextSubstitutionFunctionDesignation() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("S(i, a, a, f(i))", true),
                parseStringExpression("f(a)", true),
                basedOnTaskContext = true
        )
        val root = parseStringExpression("S(i, a, a, i^2)")
        assertEquals(null, substitution.checkAndApply(root.children[0]))
    }

    @Test
    fun allowedBasedOnTaskContextSubstitutionFunctionDesignation() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("S(i, c, b, i^2)", true),
                parseStringExpression("a^2", true),
                basedOnTaskContext = true
        )
        val root = parseStringExpression("S(i, c, b, i^2)")
        assertEquals("^(a;2)", substitution.checkAndApply(root.children[0]).toString())
    }

    @Test
    fun allowedSumSumSubstitutionP() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("S(i, a, b, f(i)) + S(j, a, b, g(j))", true),
                parseStringExpression("S(i, a, b, f(i) + g(i))", true)
        )
        val root = parseStringExpression("S(i, 1, n+1, i^i) + P(j, 1, n+1, j^j)")
        assertEquals(null, substitution.checkAndApply(root.children[0]))
    }

    @Test
    fun minusSubstitution_BugFromIlya() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("cos(x+y)", true),
                parseStringExpression("cos(x)*cos(y)-sin(x)*sin(y)", true)
        )
        val root = parseStringExpression("cos(x-y)")
        assertEquals("+(*(cos(x);cos(-(y)));-(*(sin(x);sin(-(y)))))", substitution.checkAndApply(root.children[0]).toString())
    }

    @Test
    fun minusSubstitutionFullExpression_BugFromIlya() {
        val substitution = ExpressionSubstitution(
                parseStringExpression("cos(x+y)", true),
                parseStringExpression("cos(x)*cos(y)-sin(x)*sin(y)", true)
        )
        val root = parseStringExpression("((cos(x)*cos(y))-cos(x+y))/(cos(x-y)-(sin(x)*sin(y)))")
        val substitutionPlaces = findSubstitutionPlacesInExpression(root, substitution)
        assertEquals(2, substitutionPlaces.size)
        substitution.applySubstitution(listOf(substitutionPlaces[1]))
        assertEquals("(/(+(*(cos(x);cos(y));-(cos(+(x;y))));+(+(*(cos(x);cos(-(y)));-(*(sin(x);sin(-(y)))));-(*(sin(x);sin(y))))))", root.toString())
        assertEquals("((cos(x)*cos(y))-cos(x+y))/(((cos(x)*cos(-y))-(sin(x)*sin(-y)))-(sin(x)*sin(y)))", root.toUserView())
    }
}

fun parseStringExpression(expression: String, nameForRuleDesignationsPossible: Boolean = false): ExpressionNode {
    val expressionTreeParser = ExpressionTreeParser(expression, nameForRuleDesignationsPossible)
    expressionTreeParser.parse()
    return expressionTreeParser.root
}
