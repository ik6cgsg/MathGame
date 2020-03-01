package com.twf.expressionstests

import com.twf.config.FunctionConfiguration
import com.twf.expressiontree.ExpressionTreeParser
import com.twf.org.junit.Ignore
import com.twf.org.junit.Test
import kotlin.test.assertEquals

class ParserTests {
    @Test
    fun testSum() {
        val expressionTreeParser = ExpressionTreeParser("a + b")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "+" &&
                root.children[0].children.size == 2 && root.children[0].children[0].value == "a"
                && root.children[0].children[1].value == "b")
    }

    @Test
    fun testMinus() {
        val expressionTreeParser = ExpressionTreeParser("-i")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(-(i)))", root.toString())
    }

    @Test
    fun testComplicatedMinus() {
        val expressionTreeParser = ExpressionTreeParser("-i-9+8-g")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(-(i);-(9);8;-(g)))", root.toString())
    }

    @Test
    fun testUP() {
        val expressionTreeParser = ExpressionTreeParser("U(m,P(i))")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(U(m;P(i)))", root.toString())
    }

    @Test
    fun testSumSum() {
        val expressionTreeParser = ExpressionTreeParser("S(i, a, b, f(i) + g(i))", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(i;a;b;+(f(i);g(i))))", root.toString())
    }

    @Test
    fun testDivide3() {
        val expressionTreeParser = ExpressionTreeParser("a / c / d", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(/(a;c;d))", root.toString())
    }

    @Test
    fun testOr() {
        val expressionTreeParser = ExpressionTreeParser("a|d", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(or(a;d))", root.toString())
    }

    @Test
    fun testAnd() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>&amp;</mo><mi>d</mi></math>", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(and(a;d))", root.toString())
    }

    @Test
    fun testMrowQuirk() {
        val expressionTreeParser = ExpressionTreeParser("<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mrow><mo>^</mo><mn>2</mn><mo>+</mo><mn>2</mn><mo>*</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo></mrow></math>", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(^(cos(x);2);*(2;cos(x))))", root.toString())
    }

    @Test
    fun testSubstraction3() {
        val expressionTreeParser = ExpressionTreeParser("a - c - d", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(a;-(c);-(d)))", root.toString())
    }

    @Test
    fun testSubstraction() {
        val expressionTreeParser = ExpressionTreeParser("a + b - c - d", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(a;b;-(c);-(d)))", root.toString())
    }

    @Test
    fun testPSum() {
        val expressionTreeParser = ExpressionTreeParser("P(f(i) + g(i))", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(P(+(f(i);g(i))))", root.toString())
    }

    @Test
    fun testMultiArgSum() {
        val expressionTreeParser = ExpressionTreeParser("S(i, a, a, sin(i))")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(i;a;a;sin(i)))", root.toString())
    }

    @Test
    fun testMultiArgSumCoplicated() {
        val expressionTreeParser = ExpressionTreeParser("S(i, U(a,b), tg(a), i)")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(i;U(a;b);tg(a);i))", root.toString())
    }

    @Test
    fun testMultiArgSumRuleDesignation() {
        val expressionTreeParser = ExpressionTreeParser("S(i, a, a, f(i))", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(i;a;a;f(i)))", root.toString())
    }

    @Test
    fun testMultiArgSumNotRuleDesignation() {
        val expressionTreeParser = ExpressionTreeParser("S(i, a, a, f(i))")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(i;a;a;*(f;i)))", root.toString())
    }

    @Test
    fun testMathMlSum() {
        val expressionTreeParser = ExpressionTreeParser("<mi>a</mi><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>b</mi>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "+" &&
                root.children[0].children.size == 2 && root.children[0].children[0].value == "a"
                && root.children[0].children[1].value == "b")
    }

    @Test
    fun testMul() {
        val expressionTreeParser = ExpressionTreeParser("a b")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "*" &&
                root.children[0].children.size == 2 && root.children[0].children[0].value == "a"
                && root.children[0].children[1].value == "b")
    }

    @Test
    fun testSub() {
        val expressionTreeParser = ExpressionTreeParser("a - b")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "+" &&
                root.children[0].children.size == 2 && root.children[0].children[0].value == "a"
                && root.children[0].children[1].value == "-" && root.children[0].children[1].children.size == 1
                && root.children[0].children[1].children[0].value == "b")
    }

    @Test
    fun testDiv() {
        val expressionTreeParser = ExpressionTreeParser("<mi>a</mi><mo>/</mo><mi>b</mi>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "/" &&
                root.children[0].children.size == 2 && root.children[0].children[0].value == "a"
                && root.children[0].children[1].value == "b")
    }

    @Test
    fun testFrac() {
        val expressionTreeParser = ExpressionTreeParser("<mfrac><mi>a</mi><mi>b</mi></mfrac>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "/" &&
                root.children[0].children.size == 2 && root.children[0].children[0].value == "a"
                && root.children[0].children[1].value == "b")
    }

    @Test
    fun testBevelledFrac() {
        val expressionTreeParser = ExpressionTreeParser("<mfrac bevelled=\"true\"><mi>a</mi><mi>b</mi></mfrac>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "/" &&
                root.children[0].children.size == 2 && root.children[0].children[0].value == "a"
                && root.children[0].children[1].value == "b")
    }

    @Test
    fun testMulSubs() {
        val expressionTreeParser = ExpressionTreeParser("<msub><mi>a</mi><mn>1</mn></msub><msub><mi>a</mi><mn>2</mn></msub>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "*" &&
                root.children[0].children.size == 2 && root.children[0].children[0].value == "{a},{1}"
                && root.children[0].children[1].value == "{a},{2}")
    }

    @Test
    fun testSumMulPriors() {
        val expressionTreeParser = ExpressionTreeParser("a + 4b")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "+" &&
                root.children[0].children.size == 2 && root.children[0].children[0].value == "a"
                && root.children[0].children[1].value == "*" && root.children[0].children[1].children.size == 2
                && root.children[0].children[1].children[0].value == "4" && root.children[0].children[1].children[1].value == "b")
    }

    @Test
    fun testOneFactorial() {
        val expressionTreeParser = ExpressionTreeParser("a!")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "factorial" &&
                root.children[0].children.size == 1 && root.children[0].children[0].value == "a"
                && root.children[0].children[0].children.size == 0)
    }

    @Test
    fun testDoubleFactorial() {
        val expressionTreeParser = ExpressionTreeParser("abc!!")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "double_factorial" &&
                root.children[0].children.size == 1 && root.children[0].children[0].value == "abc"
                && root.children[0].children[0].children.size == 0)
    }

    @Test
    fun testDoubleFactorialMathML() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mi>b</mi><mi>c</mi><mo>!</mo><mo>!</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "double_factorial" &&
                root.children[0].children.size == 1 && root.children[0].children[0].value == "abc"
                && root.children[0].children[0].children.size == 0)
    }

    @Test
    fun testTwoFactorial() {
        val expressionTreeParser = ExpressionTreeParser("(a!)!")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "factorial" &&
                root.children[0].children.size == 1 && root.children[0].children[0].value == "factorial"
                && root.children[0].children[0].children.size == 1 && root.children[0].children[0].children[0].value == "a")
    }

    @Test
    fun testThreeFactorial() {
        val expressionTreeParser = ExpressionTreeParser("a! ! !")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "factorial" &&
                root.children[0].children.size == 1 && root.children[0].children[0].value == "factorial"
                && root.children[0].children[0].children.size == 1 && root.children[0].children[0].children[0].value == "factorial"
                && root.children[0].children[0].children[0].children.size == 1 && root.children[0].children[0].children[0].children[0].value == "a")
    }

    @Test
    fun testOneSubfactorial() {
        val expressionTreeParser = ExpressionTreeParser("!a",
                functionConfiguration = FunctionConfiguration(setOf("", "subfactorial")))
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "subfactorial" &&
                root.children[0].children.size == 1 && root.children[0].children[0].value == "a"
                && root.children[0].children[0].children.size == 0)
    }

    @Test
    fun testDivision() {
        val expressionTreeParser = ExpressionTreeParser("/a")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.toString() == "(/(a))")
    }

    @Test
    fun testSetAndNotMathML() {
        val expressionTreeParser = ExpressionTreeParser("a&b",
                functionConfiguration = FunctionConfiguration(setOf("", "setTheory")))
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(and(a;b))", root.toString())
    }

    @Test
    fun testSetSub() {
        val expressionTreeParser = ExpressionTreeParser("a\\b",
                functionConfiguration = FunctionConfiguration(setOf("", "setTheory")))
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(set-(a;b))", root.toString())
    }

    @Test
    fun testImplication() {
        val expressionTreeParser = ExpressionTreeParser("a->b",
                functionConfiguration = FunctionConfiguration(setOf("", "setTheory")))
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(implic(a;b))", root.toString())
    }

    @Test
    fun testDoubleLeftFactorial() {
        val expressionTreeParser = ExpressionTreeParser("!!a",
                functionConfiguration = FunctionConfiguration(setOf("", "setTheory")))
        val error = expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("()", root.toString())
        assertEquals("ParserError(position=0, description=Unknown operation: '!!', endPosition=-1)", error.toString())
    }

    @Test
    fun testImplicationMathML() {
        val expressionTreeParser = ExpressionTreeParser("<mi>a</mi><mo>-</mo><mo>&gt;</mo><mi>b</mi>",
                functionConfiguration = FunctionConfiguration(setOf("", "setTheory")))
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(implic(a;b))", root.toString())
    }

    @Test
    fun testSetComplete() {
        val expressionTreeParser = ExpressionTreeParser("((a\\!b)->0)\\/!(!a/\\!b/\\c)",
                functionConfiguration = FunctionConfiguration(setOf("", "setTheory")))
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(or(implic(set-(a;not(b));0);not(and(not(a);not(b);c))))", root.toString())
    }

    @Test
    fun testSetMathML() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>a</mi><mo>-</mo><mo>&gt;</mo><mi>b</mi><mo>)</mo><mo>\\</mo><mo>/</mo><mo>(</mo><mi>b</mi><mo>&#x2192;</mo><mi>a</mi><mo>)</mo></math>",
                functionConfiguration = FunctionConfiguration(setOf("", "setTheory")))
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(or(implic(a;b);implic(b;a)))", root.toString())
    }

    @Test
    fun testOneSubfactorialFactoial() {
        val expressionTreeParser = ExpressionTreeParser("!a!",
                functionConfiguration = FunctionConfiguration(setOf("", "subfactorial")))
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "subfactorial" &&
                root.children[0].children.size == 1 && root.children[0].children[0].value == "factorial"
                && root.children[0].children[0].children.size == 1 && root.children[0].children[0].children[0].value == "a")
    }

    @Test
    fun testTwoSubfactorialFactoial() {
        val expressionTreeParser = ExpressionTreeParser("! !a!",
                functionConfiguration = FunctionConfiguration(setOf("", "subfactorial")))
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "subfactorial" &&
                root.children[0].children.size == 1 && root.children[0].children[0].value == "subfactorial"
                && root.children[0].children[0].children.size == 1 && root.children[0].children[0].children[0].value == "factorial"
                && root.children[0].children[0].children[0].children.size == 1 && root.children[0].children[0].children[0].children[0].value == "a")
    }

    @Test
    fun testFractionMultiplication() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>a</mi><mo>+</mo><mi>b</mi><mo>)</mo><mfrac><mrow><mi>a</mi><mo>-</mo><mi>b</mi></mrow><mrow><mi>a</mi><mo>-</mo><mi>c</mi></mrow></mfrac><mi>c</mi></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.children.size == 1 && root.children[0].value == "*" &&
                root.children[0].children.size == 3 && root.children[0].children[0].value == "+" && root.children[0].children[1].value == "/" && root.children[0].children[2].value == "c"
                && root.children[0].children[0].children.size == 2 && root.children[0].children[0].children[0].value == "a" && root.children[0].children[0].children[1].value == "b"
                && root.children[0].children[1].children.size == 2 && root.children[0].children[1].children[0].value == "+" && root.children[0].children[1].children[1].value == "+" &&
                root.children[0].children[1].children[0].children.size == 2 && root.children[0].children[1].children[0].children[0].value == "a" && root.children[0].children[1].children[0].children[1].value == "-" && root.children[0].children[1].children[0].children[1].children.size == 1 && root.children[0].children[1].children[0].children[1].children[0].value == "b" &&
                root.children[0].children[1].children[1].children.size == 2 && root.children[0].children[1].children[1].children[0].value == "a" && root.children[0].children[1].children[1].children[1].value == "-" && root.children[0].children[1].children[1].children[1].children.size == 1 && root.children[0].children[1].children[1].children[1].children[0].value == "c")
    }

    @Test
    fun testMunderoverMathMl() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mstyle displaystyle=\"false\"><munderover><mo>&#x2211;</mo><mrow><mi>i</mi><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>1</mn></mrow><mi>n</mi></munderover><msup><mi>i</mi><mn>2</mn></msup></mstyle></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.toString() == "(S(i;1;n;^(i;2)))")
    }

    @Test
    fun testMunderover2MathMl() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><munderover><mrow><mo>&#x2211;</mo><msup><mi>i</mi><mn>2</mn></msup></mrow><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mi>n</mi></munderover></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.toString() == "(S(i;1;n;^(i;2)))")
    }

    @Test
    fun testMunderoverLongExpression2MathMl() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><munderover><mrow><mo>&#x2211;</mo><mi>u</mi><mo>+</mo><msup><mi>i</mi><mn>2</mn></msup></mrow><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mi>n</mi></munderover></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.toString() == "(S(i;1;n;+(u;^(i;2))))")
    }

    @Test
    fun testLogMathMl() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>log</mi><mfenced><mrow><munderover><mo>&#x220F;</mo><mrow><mi>i</mi><mo>=</mo><mi>n</mi><mo>/</mo><mn>2</mn></mrow><mi>n</mi></munderover><mi>i</mi><mo>,</mo><mo>&#xA0;</mo><mi>a</mi><mo>!</mo><mo>!</mo></mrow></mfenced></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.toString() == "(log(P(i;/(n;2);n;i);double_factorial(a)))")
    }

    @Test
    fun testSimpleLog() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msub><mi>log</mi><mi>b</mi></msub><mfenced><mi>a</mi></mfenced></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(log(a;b))", root.toString())
    }

    @Test
    fun testMsubLog() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>x</mi><msub><mi>log</mi><mi>u</mi></msub><mi>i</mi></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(*(x;log(i;u)))", root.toString())
    }

    @Test
    fun testCompleteLog() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msub><mi>log</mi><munderover><mrow><mo>&#x2211;</mo><msup><mi>i</mi><mi>x</mi></msup></mrow><mrow><mi>i</mi><mo>=</mo><mn>4</mn></mrow><mn>8</mn></munderover></msub><mfenced><munderover><mrow><mo>&#x2211;</mo><msup><mi>i</mi><mrow><mi>x</mi><msub><mi>log</mi><mrow><mi>u</mi><mi>i</mi></mrow></msub><mfenced><mi>i</mi></mfenced></mrow></msup></mrow><mrow><mi>i</mi><mo>=</mo><mn>4</mn></mrow><mn>8</mn></munderover></mfenced></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(log(S(i;4;8;^(i;*(x;log(i;ui))));S(i;4;8;^(i;x))))", root.toString())
    }

    @Test
    fun testSimpleLogMfenced() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>log</mi><mfenced><mrow><mi>a</mi><mo>,</mo><mi>b</mi></mrow></mfenced></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.toString() == "(log(a;b))")
    }

    @Test
    fun testSimpleLogBrackets() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>log</mi><mo>(</mo><mi>a</mi><mo>,</mo><mi>b</mi><mo>)</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.toString() == "(log(a;b))")
    }

    @Test
    fun testLogBracketsDoubleFactorial() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>log</mi><mo>(</mo><munderover><mo>&#x220F;</mo><mrow><mi>i</mi><mo>=</mo><mi>n</mi><mo>/</mo><mn>2</mn></mrow><mi>n</mi></munderover><mi>i</mi><mo>,</mo><mo>&#xA0;</mo><mi>a</mi><mo>!</mo><mo>!</mo><mo>)</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.toString() == "(log(P(i;/(n;2);n;i);double_factorial(a)))")
    }

    @Test
    fun testLogBrackets() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>log</mi><mo>(</mo><munderover><mo>&#x220F;</mo><mrow><mi>i</mi><mo>=</mo><mi>n</mi><mo>/</mo><mn>2</mn></mrow><mi>n</mi></munderover><mi>i</mi><mo>,</mo><mo>&#xA0;</mo><mi>a</mi><mo>!</mo><mo>&#xA0;</mo><mo>!</mo><mo>)</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(root.toString() == "(log(P(i;/(n;2);n;i);factorial(factorial(a))))")
    }

    @Test
    fun testSpecialSymbolAsBinaryOperation() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>&#xD7;</mo><mi>b</mi></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(*(a;b))", root.toString())
    }

    @Test
    fun testSpecialSymbolAsUnaryLeftOperation() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>&#x2202;</mo><mi>x</mi></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(partial_differential(x))", root.toString())
    }

    @Test
    fun testSimpleDerivative() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow><mo>(</mo><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mo>'</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(d(+(x;4)))", root.toString())
    }

    @Test
    @Ignore //now '\'\'' operation not implemented as double function call (because of operations like !!) todo: support?
    fun testSimpleDoubleDerivative() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow><mo>(</mo><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mo>'</mo><mo>'</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(d(d(+(x;4))))", root.toString())
    }

    @Test
    fun testSubsupDerivative() {
        val expressionTreeParser = ExpressionTreeParser("<math><msubsup><mrow><mo>(</mo><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mi>x</mi><mo>'</mo></msubsup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(d(+(x;4);x))", root.toString())
    }

    @Test
    fun testTagNamesWithoutM (){
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mrow><mn>4</mn><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>4</mn><mo>)</mo><mo>-</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>4</mn><mo>)</mo><mo>'</mo><mo>*</mo><mo>(</mo><mn>4</mn><mo>*</mo><mi>x</mi><mo>-</mo><mn>7</mn><mo>)</mo></mrow><msup><mrow><mo>(</mo><mi>x</mi><sup><mn>2</mn></sup><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mn>2</mn></msup></mfrac></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(/(+(*(4;+(^(x;2);4));-(*(d(+(^(x;2);4));+(*(4;x);-(7)))));^(+(^(x;2);4);2)))", root.toString())

        val expressionTreeParser1 = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mrow><mn>4</mn><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>4</mn><mo>)</mo><mo>-</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>4</mn><mo>)</mo><mo>'</mo><mo>*</mo><mo>(</mo><mn>4</mn><mo>*</mo><mi>x</mi><mo>-</mo><mn>7</mn><mo>)</mo></mrow><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mn>2</mn></msup></mfrac></math>")
        expressionTreeParser1.parse()
        val root1 = expressionTreeParser1.root
        assertEquals("(/(+(*(4;+(^(x;2);4));-(*(d(+(^(x;2);4));+(*(4;x);-(7)))));^(+(^(x;2);4);2)))", root1.toString())
    }

    @Test
    fun testSubsupDoubleDerivative() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msubsup><mrow><mo>(</mo><msubsup><mrow><mo>(</mo><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mi>x</mi><mo>'</mo></msubsup><mo>)</mo></mrow><mi>y</mi><mo>'</mo></msubsup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(d(d(+(x;4);x);y))", root.toString())
    }

    @Test
    fun testDoubleDerivative() {
        val expressionTreeParser = ExpressionTreeParser("d(d(x+4,x),y)")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(d(d(+(x;4);x);y))", root.toString())
    }

    @Test
    fun testCosDivDerivative() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mrow><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>'</mo><mo>*</mo><mo>(</mo><mn>1</mn><mo>-</mo><mi>x</mi><mo>)</mo><mo>+</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo></mrow><msup><mrow><mo>(</mo><mn>1</mn><mo>-</mo><mi>x</mi><mo>)</mo></mrow><mn>2</mn></msup></mfrac></math>")
                //todo: fix that it does not working without multiplication sign: "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mrow><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>'</mo><mo>(</mo><mn>1</mn><mo>-</mo><mi>x</mi><mo>)</mo><mo>+</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo></mrow><msup><mrow><mo>(</mo><mn>1</mn><mo>-</mo><mi>x</mi><mo>)</mo></mrow><mn>2</mn></msup></mfrac></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(/(+(*(d(cos(x));+(1;-(x)));cos(x));^(+(1;-(x));2)))", root.toString())
    }

    @Test
    fun testDoubleDerivativeAndSum() {
        val expressionTreeParser = ExpressionTreeParser("d(S(i,a,b,d(x+4,x)),y)")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(d(S(i;a;b;d(+(x;4);x));y))", root.toString())
    }

    @Test
    fun testDoubleDerivativeSum() {
        val expressionTreeParser = ExpressionTreeParser("d(S(i,a,d(d(x+4,x)),d(x+4,x)),y)")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(d(S(i;a;d(d(+(x;4);x));d(+(x;4);x));y))", root.toString())
    }

    @Test
    @Ignore //msubsup must contain full argument expression, not only end of it (like msup) todo: investigate and fix
    fun testSubsupApproxSelection() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow><mo>(</mo><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mo>+</mo><mo>(</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>+</mo><msubsup><mrow><mn>3</mn><mo>)</mo></mrow><mi>x</mi><mo>'</mo></msubsup><mo>+</mo><mi>x</mi></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(d(d(+(x;4);x);y))", root.toString())
    }

    @Test
    fun test2PiSinR() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mn>2</mn><mi>&#x3C0;sin</mi><mo>(</mo><mi mathvariant=\"normal\">r</mi><mo>)</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(*(2;&#x3C0;sin(r)))", root.toString())
    }

    @Test
    fun testx23y23() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msup><mi>x</mi><mrow><mn>2</mn><mo>/</mo><mn>3</mn></mrow></msup><mo>+</mo><msup><mi>y</mi><mrow><mn>2</mn><mo>/</mo><mn>3</mn></mrow></msup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(^(x;/(2;3));^(y;/(2;3))))", root.toString())
    }

    @Test
    fun testSin2x() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msup><mi>sin</mi><mn>2</mn></msup><mo>(</mo><mi>x</mi><mo>)</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(^(sin(x);2))", root.toString())
    }


    @Test
    fun mathMlDegree() {
        val expressionTreeParser = ExpressionTreeParser("<msup><mo>(</mo><mi>cos</mi><mrow><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo></mrow></msup>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(^(cos(x);sin(x)))", root.toString())
    }

    @Test
    fun testSinMfenced2x() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msup><mi>sin</mi><mn>2</mn></msup><mfenced><mi>x</mi></mfenced></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(^(sin(x);2))", root.toString())
    }

    @Test
    fun testPowMulBrackets() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msup><mi>a</mi><mrow><mn>2</mn><mo>+</mo><mi>i</mi></mrow></msup><mo>(</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo>)</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(*(^(a;+(2;i));+(x;y)))", root.toString())
    }

    @Test
    fun testPowMul() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msup><mi>a</mi><mn>2</mn></msup><msup><mi>x</mi><mi>t</mi></msup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(*(^(a;2);^(x;t)))", root.toString())
    }

    @Test
    fun testPowMulAdd() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msup><mi>a</mi><mrow><mn>2</mn><mo>+</mo><mi>i</mi></mrow></msup><msup><mi>x</mi><mrow><mi>t</mi><mo>*</mo><mi>j</mi></mrow></msup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(*(^(a;+(2;i));^(x;*(t;j))))", root.toString())
    }

    @Test
    fun testMsup() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><msup><mi>b</mi><mi>x</mi></msup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(^(ab;x))", root.toString())
    }

    @Test
    fun testFunctionWithDigitsInName() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>S</mi><mn>1</mn><mo>(</mo><mi>i</mi><mo>,</mo><mi>j</mi><mo>)</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S1(i;j))", root.toString())
    }

    @Test
    fun testFunctionWithWrongNuberOfArgumetsAsVariable() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>S</mi><mn>1</mn><mo>(</mo><mi>i</mi><mo>)</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(*(S1;i))", root.toString())
    }

    @Test
    fun testSelectedPlus() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mo>(</mo><mi>b</mi><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>c</mi><mo>)</mo></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(a;+(b;c)))", root.toString())
    }

    @Test
    fun testMsupBrackets() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>a</mi><mo>+</mo><mi>b</mi><msup><mo>)</mo><mi>x</mi></msup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(^(+(a;b);x))", root.toString())
    }

    @Test
    fun testMsupMunderover() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><munderover><mo>&#x2211;</mo><mrow><mi>x</mi><mo>=</mo><mn>1</mn></mrow><mi>n</mi></munderover><mi>a</mi><msup><mi>b</mi><mi>x</mi></msup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(x;1;n;^(ab;x)))", root.toString())
    }

    @Test
    fun testMsupMunderoverComplicatedCounter() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><munderover><mo>&#x2211;</mo><mrow><mi>x</mi><mi>y</mi><mi>z</mi><mo>=</mo><mn>1</mn></mrow><mi>n</mi></munderover><mi>a</mi><msup><mi>b</mi><mrow><mi>x</mi><mi>y</mi><mi>z</mi></mrow></msup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(xyz;1;n;^(ab;xyz)))", root.toString())
    }

    @Test
    fun testMsupMunderovers() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><munderover><mo>&#x220F;</mo><mrow><mi>u</mi><mo>=</mo><mn>0</mn></mrow><mn>9</mn></munderover><munderover><mo>&#x2211;</mo><mrow><mi>x</mi><mo>=</mo><mn>1</mn></mrow><mi>n</mi></munderover><munderover><mo>&#x2211;</mo><mrow><mi>x</mi><mo>=</mo><mn>1</mn></mrow><mi>n</mi></munderover><mi>a</mi><msup><mi>b</mi><mi>x</mi></msup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(P(u;0;9;S(x;1;n;S(x;1;n;^(ab;x)))))", root.toString())
    }

    @Test
    fun testDemidovich497() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mrow><mi>t</mi><mi>g</mi><mo>(</mo><mi>a</mi><mo>+</mo><mi>x</mi><mo>)</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>a</mi><mo>-</mo><mi>x</mi><mo>)</mo><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mi>t</mi><msup><mi>g</mi><mn>4</mn></msup><mo>(</mo><mi>a</mi><mo>)</mo></mrow><msup><mi>x</mi><mn>3</mn></msup></mfrac></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(/(+(*(tg(+(a;x));tg(+(a;-(x))));-(^(tg(a);4)));^(x;3)))", root.toString())
    }

    @Test
    fun testDemidovich506() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mfrac><mrow><mn>1</mn><mo>+</mo><mi>x</mi></mrow><mrow><mn>2</mn><mo>+</mo><mi>x</mi></mrow></mfrac><msup><mo>)</mo><mrow><mo>(</mo><mn>1</mn><mo>-</mo><msqrt><mi>x</mi></msqrt><mo>)</mo><mo>(</mo><mn>1</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mi>x</mi><mo>)</mo></mrow></msup></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(^(/(+(1;x);+(2;x));*(+(1;-(sqrt(x)));+(1;-(x)))))", root.toString())
    }

    @Test
    fun testDifficultSum() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><munderover><mo>&#x2211;</mo><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mi>t</mi></munderover><munderover><mo>&#x220F;</mo><mrow><mi>j</mi><mo>=</mo><mstyle displaystyle=\"false\"><munderover><mo>&#x2211;</mo><mrow><mi>i</mi><mo>=</mo><mn>3</mn></mrow><mn>6</mn></munderover><mi>i</mi></mstyle></mrow><mi>t</mi></munderover><mi>i</mi><mi>j</mi></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(i;1;t;P(j;S(i;3;6;i);t;ij)))", root.toString())
    }

    @Test
    fun testNewLine() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>+</mo><mspace linebreak=\"newline\"/><mi>b</mi></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(a;b))", root.toString())
    }

    @Test
    fun testNewLineBorderedByDoubleSign() { //if this construction (like '+\n+' as '+') is possible, it will be hard to resolve it with sign written be doubling sign letter (like '+\n+' as '++'). Current decision was chosen, because it makes notice shorter.
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>+</mo><mspace linebreak=\"newline\"/><mo>+</mo><mi>b</mi></math>")
        val error = expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(error != null)
        assert(error!!.description.contains('+'))
        assertEquals("()", root.toString())
    }

    @Test
    fun testNewLineErrorPosition() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mspace linebreak=\"newline\"/><mo>++++</mo><mi>b</mi></math>")
        val error = expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assert(error != null)
        val realErrorPos = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mspace linebreak=\"newline\"/><mo>".length
        assert(error!!.position == realErrorPos)
        assert(error.description.contains("++++"))
        assertEquals("()", root.toString())
    }

    @Test
    fun testAbs() {
        val expressionTreeParser = ExpressionTreeParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mn>3</mn><mi>a</mi><mo>+</mo><mfenced open=\"|\" close=\"|\"><mrow><mi>b</mi><mo>+</mo><mi>c</mi></mrow></mfenced></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(*(3;a);abs(+(b;c))))", root.toString())
    }

    @Test
    fun testDivisionMultiplicationMathML() {
        val expressionTreeParser = ExpressionTreeParser("<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>A</mi><mo>(</mo><mi>m</mi><mo>,</mo><mi>n</mi><mo>)</mo><mo>&#xA0;</mo><mo>/</mo><mo>&#xA0;</mo><mi>n</mi><mo>!</mo><mo>&#xA0;</mo><mo>/</mo><mi>n</mi><mo>*</mo><mi>n</mi></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(/(A(m;n);factorial(n);*(n;n)))", root.toString()) //special parsing feature: multiplication has bigger priority than division; may be it should be changed
    }

    @Test
    fun testDivisionMultiplication() {
        val expressionTreeParser = ExpressionTreeParser("m/n*k")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(/(m;*(n;k)))", root.toString()) //special parsing feature: multiplication has bigger priority than division; may be it should be changed
    }

    @Test
    fun testMrowInEnd() {
        val expressionTreeParser = ExpressionTreeParser("<mn>8</mn><mo>*</mo><mo>(</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mi>x</mi><mrow><mo>)</mo><msup><mo>)</mo><mn>4</mn></msup></mrow>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(*(8;^(cos(x);4)))", root.toString()) //special parsing feature: multiplication has bigger priority than division; may be it should be changed
    }

    @Test
    fun testIncorrectMinus() {
        val expressionTreeParser = ExpressionTreeParser("<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>x</mi><mo>-</mo></math>")//  "<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mrow><mn>4</mn><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>4</mn><mo>)</mo><mo>-</mo><mn>2</mn><mo>*</mo><mi>x</mi><mo>*</mo><mo>(</mo><mn>4</mn><mo>*</mo><mi>x</mi><mo>-</mo><mo>)</mo></mrow><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mn>2</mn></msup></mfrac></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("()", root.toString())
    }

    @Test
    fun testIncorrectMinus2() {
        val expressionTreeParser = ExpressionTreeParser("<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mrow><mn>4</mn><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>4</mn><mo>)</mo><mo>-</mo><mn>2</mn><mo>*</mo><mi>x</mi><mo>*</mo><mo>(</mo><mn>4</mn><mo>*</mo><mi>x</mi><mo>-</mo><mo>)</mo></mrow><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mn>2</mn></msup></mfrac></math>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("()", root.toString())
    }

    @Test
    fun testFractionDegree() {
        val expressionTreeParser = ExpressionTreeParser("<mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(/(+(^(x;4);^(x;2);1);+(^(x;2);-(x);1)))", root.toString())
    }

    @Test
    fun testFractionDegreeMrow() {
        val expressionTreeParser = ExpressionTreeParser("<mrow><mrow><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac></mrow>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(/(+(^(x;4);^(x;2);1);+(^(x;2);-(x);1)))", root.toString())
    }

    @Test
    fun testSumnInSumn() {
        val expressionTreeParser = ExpressionTreeParser("<munderover><mrow><mo>&#x2211;</mo><mi>j</mi><mo>!</mo><mo>-</mo><mi>i</mi><mo>!</mo><mo>+</mo><mn>67</mn><mo>-</mo><munderover><mo>&#x2211;</mo><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mi>k</mi></munderover><mi>k</mi></mrow><mrow><mi>j</mi><mo>=</mo><mn>1</mn></mrow><mi>n</mi></munderover>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(j;1;n;+(factorial(j);-(factorial(i));67;-(S(i;1;k;k)))))", root.toString())
    }

    @Test
    fun testSumnInSumnWithMstyle() {
        val expressionTreeParser = ExpressionTreeParser("<munderover><mrow><mo>&#x2211;</mo><mi>j</mi><mo>!</mo><mo>-</mo><mi>i</mi><mo>!</mo><mo>+</mo><mn>67</mn><mo>-</mo><mstyle displaystyle=\"false\"><munderover><mo>&#x2211;</mo><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mi>k</mi></munderover></mstyle><mi>k</mi></mrow><mrow><mi>j</mi><mo>=</mo><mn>1</mn></mrow><mi>n</mi></munderover>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(j;1;n;+(factorial(j);-(factorial(i));67;-(S(i;1;k;k)))))", root.toString())
    }

    @Test
    fun testSumnWithMstyle() {
        val expressionTreeParser = ExpressionTreeParser("<mstyle displaystyle=\"false\"><munderover><mo>&#x2211;</mo><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mi>k</mi></munderover></mstyle><mi>k</mi>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(i;1;k;k))", root.toString())
    }

    @Test
    fun testSumn() {
        val expressionTreeParser = ExpressionTreeParser("<munderover><mo>&#x2211;</mo><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mi>k</mi></munderover><mi>k</mi>")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(S(i;1;k;k))", root.toString())
    }

    @Test
    fun testWithPositions() {
        val expressionTreeParser = ExpressionTreeParser("((cos(x)*cos(y))-cos(x+y))/(cos(x-y)-(sin(x)*sin(y)))")
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(/(+(*(cos(x);cos(y));-(cos(+(x;y))));+(cos(+(x;-(y)));-(*(sin(x);sin(y))))))", root.toString())
        assertEquals("(/(+(*(cos(x{6;7}){2;8};cos(y{13;14}){9;15}){1;16};-(cos(+(x{21;22};y{23;24}){20;25}){17;25}){16;25}){0;26};+(cos(+(x{32;33};-(y{34;35}){33;35}){31;36}){28;36};-(*(sin(x{42;43}){38;44};sin(y{49;50}){45;51}){37;52}){36;52}){27;53}){0;53}){0;53}", root.computeIdentifierWithPositions())
    }
}