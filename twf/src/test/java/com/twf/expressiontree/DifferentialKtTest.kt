package com.twf.expressiontree

import com.twf.org.junit.Test

import com.twf.org.junit.Assert.*

class DifferentialKtTest {

    @Test
    fun diffTest() {
        val expressionTreeParser = ExpressionTreeParser("<math><msubsup><mrow><mo>(</mo><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mi>x</mi><mo>'</mo></msubsup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        kotlin.test.assertEquals("(d(+(x;4);x))", root.toString())

        val res = root.diff()
        kotlin.test.assertEquals("(+(1;0))", res.toString())
    }

    @Test
    fun diffTestMul() {
        val expressionTreeParser = ExpressionTreeParser("d(d(4*x-a*x+7*y,x),y)")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        kotlin.test.assertEquals("(d(d(+(*(4;x);-(*(a;x));*(7;y));x);y))", root.toString())

        val res = root.diff()
        kotlin.test.assertEquals("(d(+(+(*(0;x);*(4;1));-(+(*(0;x);*(a;1)));0);y))", res.toString())
    }

    @Test
    fun diffTestDiv() {
        val expressionTreeParser = ExpressionTreeParser("d(d((4*x*(a*x+b)-a/x/(x+x)/x^2+7*y+1/x+(x^3+x)/(x^3+5)),x),y)")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        kotlin.test.assertEquals("(d(d(+(*(4;x;+(*(a;x);b));-(/(a;x;+(x;x);^(x;2)));*(7;y);/(1;x);/(+(^(x;3);x);+(^(x;3);5)));x);y))", root.toString())

        val res = root.diff()
        kotlin.test.assertEquals("(d(+(+(*(0;x;+(*(a;x);b));*(4;1;+(*(a;x);b));*(4;x;+(+(*(0;x);*(a;1));0)));-(/(+(-(*(a;+(*(1;+(x;x);^(x;2));*(x;+(1;1);^(x;2));*(x;+(x;x);*(2;^(x;+(2;-(1));1)))))));^(*(x;+(x;x);^(x;2));2)));0;/(+(-(*(1;1)));^(x;2));/(+(*(+(^(x;3);5);+(*(3;^(x;+(3;-(1));1));1));-(*(+(^(x;3);x);+(*(3;^(x;+(3;-(1));1));0))));^(+(^(x;3);5);2)));y))",
                res.toString())
    }

    @Test
    fun diffTestDivWeight1() {
        val expressionTreeParser = ExpressionTreeParser("d(d((4*x*(a*x+b)-a/x/(x+x)/x^2+7*y+1/x+(x^3+x)/(x^3+5)),x),y)")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        kotlin.test.assertEquals("(d(d(+(*(4;x;+(*(a;x);b));-(/(a;x;+(x;x);^(x;2)));*(7;y);/(1;x);/(+(^(x;3);x);+(^(x;3);5)));x);y))", root.toString())

        val weight = mutableListOf(0.0)
        val res = root.diff(weight)
        kotlin.test.assertEquals(2.05, weight[0])
        kotlin.test.assertEquals("(d(+(+(*(0;x;+(*(a;x);b));*(4;1;+(*(a;x);b));*(4;x;+(+(*(0;x);*(a;1));0)));-(/(+(-(*(a;+(*(1;+(x;x);^(x;2));*(x;+(1;1);^(x;2));*(x;+(x;x);*(2;^(x;+(2;-(1));1)))))));^(*(x;+(x;x);^(x;2));2)));0;/(+(-(*(1;1)));^(x;2));/(+(*(+(^(x;3);5);+(*(3;^(x;+(3;-(1));1));1));-(*(+(^(x;3);x);+(*(3;^(x;+(3;-(1));1));0))));^(+(^(x;3);5);2)));y))",
                res.toString())
//        kotlin.test.assertEquals("(d(+(+(*(0;x;+(*(a;x);b));*(4;1;+(*(a;x);b));*(4;x;+(+(*(0;x);*(a;1));0)));-(/(+(-(*(a;d(*(x;+(x;x);^(x;2));x))));^(*(x;+(x;x);^(x;2));2)));0;/(+(-(*(1;1)));^(x;2));/(+(*(+(^(x;3);5);+(d(^(x;3);x);1));-(*(+(^(x;3);x);+(d(^(x;3);x);0))));^(+(^(x;3);5);2)));y))",
//                res.toString())
    }
}