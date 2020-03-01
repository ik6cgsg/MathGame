package com.twf.factstransformations

import com.twf.config.CompiledConfiguration
import com.twf.org.junit.Assert.*
import com.twf.org.junit.Test

class TransformationChainParserTest{
    val compiledConfiguration = CompiledConfiguration()

    @Test
    fun testMiltyPlacementFunctionsEqualProveParsing (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><munderover><mrow><mo>&#x2211;</mo><mi>i</mi><mo>!</mo></mrow><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mi>n</mi></munderover><mo>=</mo><mi>i</mi><mo>+</mo><munderover><mrow><mo>&#x2211;</mo><mi>j</mi><mo>!</mo><mo>-</mo><mi>i</mi><mo>!</mo><mo>+</mo><mn>67</mn><mo>-</mo><mstyle displaystyle=\"false\"><munderover><mo>&#x2211;</mo><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mi>k</mi></munderover></mstyle><mi>k</mi></mrow><mrow><mi>j</mi><mo>=</mo><mn>1</mn></mrow><mi>n</mi></munderover><mo>=</mo><munderover><mrow><mo>&#x220F;</mo><mi>i</mi><mo>!</mo></mrow><mrow><mi>i</mi><mo>=</mo><mi>j</mi></mrow><mi>n</mi></munderover><mo>+</mo><munderover><mrow><mo>&#x2211;</mo><mi>i</mi><mo>^</mo><mn>2</mn></mrow><mrow><mi>I</mi><mo>=</mo><mn>1</mn></mrow><mn>5</mn></munderover><mo>=</mo><mn>678</mn></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((S(i;1;n;factorial(i)))=(+(i;S(j;1;n;+(factorial(j);-(factorial(i));67;-(S(i;1;k;k))))))=(+(P(i;j;n;factorial(i));S(I;1;5;^(i;2))))=(678));)", transformationChainParser.root.toString())
    }

    @Test
    fun testSimpleEqualProveParsing (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>a</mi><mo>+</mo><mi>b</mi><msup><mo>)</mo><mn>2</mn></msup><mo>=</mo><msup><mi>a</mi><mn>2</mn></msup><mo>+</mo><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><msup><mi>b</mi><mn>2</mn></msup><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><msup><mi>a</mi><mn>2</mn></msup><mo>+</mo><mn>2</mn><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><msup><mi>b</mi><mn>2</mn></msup></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((^(+(a;b);2))=(+(^(a;2);*(a;b);*(a;b);^(b;2)))=(+(^(a;2);*(2;a;b);^(b;2))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testSimpleEqualWithDoubleEqualProveParsing (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>a</mi><mo>+</mo><mi>b</mi><msup><mo>)</mo><mn>2</mn></msup><mo>=</mo><msup><mi>a</mi><mn>2</mn></msup><mo>+</mo><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><msup><mi>b</mi><mn>2</mn></msup><mo>&#xA0;</mo><mo>=</mo><mspace linebreak=\"newline\"/><mspace linebreak=\"newline\"/><mo>=</mo><mo>&#xA0;</mo><msup><mi>a</mi><mn>2</mn></msup><mo>+</mo><mn>2</mn><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><msup><mi>b</mi><mn>2</mn></msup></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((^(+(a;b);2))=(+(^(a;2);*(a;b);*(a;b);^(b;2)))=(+(^(a;2);*(2;a;b);^(b;2))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testSimpleEqualWithDoubleEqualProveParsingWithComment (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>a</mi><mo>+</mo><mi>b</mi><msup><mo>)</mo><mn>2</mn></msup><mo>=</mo><msup><mi>a</mi><mn>2</mn></msup><mo>+</mo><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><msup><mi>b</mi><mn>2</mn></msup><mo>&#xA0;</mo><mo>=</mo><mspace linebreak=\"newline\"/><mo>/</mo><mo>*</mo><mi>c</mi><mi>o</mi><mi>m</mi><mi>m</mi><mi>e</mi><mi>n</mi><mi>t</mi><mo>*</mo><mo>/</mo><mspace linebreak=\"newline\"/><mo>=</mo><mo>&#xA0;</mo><msup><mi>a</mi><mn>2</mn></msup><mo>+</mo><mn>2</mn><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><msup><mi>b</mi><mn>2</mn></msup></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((^(+(a;b);2))=(+(^(a;2);*(a;b);*(a;b);^(b;2)))=(+(^(a;2);*(2;a;b);^(b;2))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testLessOrEqualComparison (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mn>5</mn><mo>(</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo>)</mo><mo>-</mo><mn>7</mn><mo>^</mo><mi>z</mi><mo>&lt;</mo><mo>=</mo><mn>8</mn><mi>x</mi><mo>-</mo><mn>7</mn><mo>^</mo><mi>z</mi></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(facts chains:(((+(*(5;+(x;y));-(^(7;z))));<=;(+(*(8;x);-(^(7;z)))))))", transformationChainParser.root.toString())
    }

    @Test
    fun testSimpleLeftMoreProveParsing (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>a</mi><mo>+</mo><mi>b</mi><mo>)</mo><mo>+</mo><mn>1</mn><mo>&gt;</mo><msup><mi>a</mi><mn>2</mn></msup><mo>+</mo><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><msup><mi>b</mi><mn>2</mn></msup><mo>&#xA0;</mo><mo>&gt;</mo><mo>&#xA0;</mo><msup><mi>a</mi><mn>2</mn></msup><mo>+</mo><mn>2</mn><mi>a</mi><mo>*</mo><mi>b</mi><mo>+</mo><msup><mi>b</mi><mn>2</mn></msup><mo>-</mo><mn>1</mn></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((+(+(a;b);1))>(+(^(a;2);*(a;b);*(a;b);^(b;2)))>(+(^(a;2);*(2;a;b);^(b;2);-(1))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testSimpleLeftLessAndEqualUnionProveParsing (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>+</mo><mi>a</mi><mo>=</mo><mn>2</mn><mi>a</mi><mo>&lt;</mo><mn>3</mn><mi>a</mi><mo>+</mo><mfenced open=\"|\" close=\"|\"><mi>b</mi></mfenced></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((+(a;a))<=(*(2;a))<=(+(*(3;a);abs(b))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testSimpleLeftLessAndEqualUnionProveParsing_x2264 (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>+</mo><mi>a</mi><mo>=</mo><mn>2</mn><mi>a</mi><mo>&#x2264;</mo><mn>3</mn><mi>a</mi><mo>+</mo><mfenced open=\"|\" close=\"|\"><mi>b</mi></mfenced></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((+(a;a))<=(*(2;a))<=(+(*(3;a);abs(b))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testExpressionChainsProveParsing (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>&lt;</mo><mi>f</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>&lt;</mo><mo>=</mo><mi>f</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>&#x2264;</mo><mn>3</mn><mi>a</mi><mo>&#x2264;</mo><mn>4</mn><mi>a</mi><mo>&#x2A7D;</mo><mn>5</mn><mi>a</mi><mspace linebreak=\"newline\"/><mi>b</mi><mo>&gt;</mo><mi>f</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>&gt;</mo><mo>=</mo><mi>f</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>&#x2A7E;</mo><mn>3</mn><mi>a</mi><mo>&gt;</mo><mn>4</mn><mi>a</mi><mo>&#x2265;</mo><mn>5</mn><mi>a</mi><mspace linebreak=\"newline\"/><mi>c</mi><mo>&lt;</mo><mn>2</mn><mi>c</mi><mo>&lt;</mo><mn>3</mn><mi>c</mi></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((a)<=(*(f;a))<=(*(f;*(2;a)))<=(*(3;a))<=(*(4;a))<=(*(5;a));(b)>=(*(f;b))>=(*(f;*(2;b)))>=(*(3;a))>=(*(4;a))>=(*(5;a));(c)<(*(2;c))<(*(3;c)));)",
                transformationChainParser.root.toString())
    }

    @Test
    fun testSimpleSignsUnionErrorProveParsing (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>&#x2265;</mo><mn>2</mn><mi>a</mi><mo>&#x2264;</mo><mi>a</mi></math>")
        val error = transformationChainParser.parse()
        assert(error!= null)
        assertEquals(49, error!!.position)
        assertEquals(123,error.endPosition)
        assert(error.description.contains('<'))
        assert(error.description.contains('>'))
    }

    @Test
    fun testSimpleSignsUnionErrorProveParsingWithNextLine (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>&#x2265;</mo><mn>2</mn><mi>a</mi><mo>&#x2264;</mo><mi>a</mi><mspace linebreak=\"newline\"/><mi>a</mi><mo>&#xA0;</mo><mo>&gt;</mo><mn>3</mn></math>")
        val error = transformationChainParser.parse()
        assert(error!= null)
        assertEquals(49, error!!.position)
        assertEquals(123,error.endPosition)
        assert(error.description.contains('<'))
        assert(error.description.contains('>'))
    }

    @Test
    fun testExpressionChainsProveWithCustomRulesParsing (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>)</mo><mo>^</mo><mn>2</mn><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mo>(</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><msup><mo>)</mo><mn>2</mn></msup><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>4</mn><mi>sin</mi><mo>(</mo><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mi>cos</mi><mo>(</mo><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>4</mn><mi>sin</mi><mo>(</mo><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>(</mo><mn>1</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mi>sin</mi><mo>(</mo><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>)</mo><mspace linebreak=\"newline\"/><mi>t</mi><mi>g</mi><mo>(</mo><mn>4</mn><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mfrac><mrow><mo>(</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mo>*</mo><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>)</mo><mo>)</mo><mo>^</mo><mn>2</mn></mrow><mrow><mo>(</mo><mi>cos</mi><mo>(</mo><mn>2</mn><mo>*</mo><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>)</mo><mo>)</mo><mo>^</mo><mn>2</mn></mrow></mfrac><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mfrac><mrow><mn>4</mn><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>(</mo><mn>1</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>)</mo></mrow><mrow><mo>(</mo><mi>cos</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>^</mo><mn>2</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>^</mo><mn>2</mn><mo>)</mo><mo>^</mo><mn>2</mn></mrow></mfrac><mo>=</mo><mo>&#xA0;</mo><mfrac><mrow><mn>4</mn><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>(</mo><mn>1</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>)</mo></mrow><mrow><mo>(</mo><mn>1</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mn>2</mn><mo>*</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>^</mo><mn>2</mn><mo>)</mo><mo>^</mo><mn>2</mn></mrow></mfrac><mspace linebreak=\"newline\"/></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((^(sin(*(2;a));2))=(^(+(*(sin(a);cos(a));*(cos(a);sin(a)));2))=(*(4;sin;^(a;2);cos;^(a;2)))=(*(4;sin;^(a;2);+(1;-(*(sin;^(a;2))))));(*(tg;^(*(4;a);2)))=(/(^(sin(*(2;*(2;a)));2);^(cos(*(2;*(2;a)));2)))=(/(*(4;sin;^(*(2;a);2);+(1;-(*(sin;^(*(2;a);2)))));^(+(^(cos(*(2;a));2);-(^(sin(*(2;a));2)));2)))=(/(*(4;sin;^(*(2;a);2);+(1;-(*(sin;^(*(2;a);2)))));^(+(1;-(*(2;^(sin(*(2;a));2))));2))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testExpressionChainsWithNewLinesParsing (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>+</mo><mi>b</mi><mo>=</mo><mi>b</mi><mo>+</mo><mi>a</mi><mo>=</mo><mspace linebreak=\"newline\"/><mo>=</mo><mi>a</mi><mo>+</mo><mi>b</mi></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((+(a;b))=(+(b;a))=(+(a;b)));)", transformationChainParser.root.toString())
    }

    @Test
    fun testExpressionChainsProveWithCustomRulesWithNewLinesParsing (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>)</mo><mo>^</mo><mn>2</mn><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mo>(</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><msup><mo>)</mo><mn>2</mn></msup><mo>&#xA0;</mo><mo>=</mo><mspace linebreak=\"newline\"/><mo>=</mo><mo>&#xA0;</mo><mn>4</mn><mi>sin</mi><mo>(</mo><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mi>cos</mi><mo>(</mo><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>4</mn><mi>sin</mi><mo>(</mo><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>(</mo><mn>1</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mi>sin</mi><mo>(</mo><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>)</mo><mspace linebreak=\"newline\"/><mi>t</mi><mi>g</mi><mo>(</mo><mn>4</mn><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mfrac><mrow><mo>(</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mo>*</mo><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>)</mo><mo>)</mo><mo>^</mo><mn>2</mn></mrow><mrow><mo>(</mo><mi>cos</mi><mo>(</mo><mn>2</mn><mo>*</mo><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>)</mo><mo>)</mo><mo>^</mo><mn>2</mn></mrow></mfrac><mo>&#xA0;</mo><mo>=</mo><mspace linebreak=\"newline\"/><mo>=</mo><mo>&#xA0;</mo><mfrac><mrow><mn>4</mn><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>(</mo><mn>1</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>)</mo></mrow><mrow><mo>(</mo><mi>cos</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>^</mo><mn>2</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>^</mo><mn>2</mn><mo>)</mo><mo>^</mo><mn>2</mn></mrow></mfrac><mo>=</mo><mo>&#xA0;</mo><mspace linebreak=\"newline\"/><mo>=</mo><mfrac><mrow><mn>4</mn><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>(</mo><mn>1</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><msup><mo>)</mo><mn>2</mn></msup><mo>)</mo></mrow><mrow><mo>(</mo><mn>1</mn><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mn>2</mn><mo>*</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>^</mo><mn>2</mn><mo>)</mo><mo>^</mo><mn>2</mn></mrow></mfrac></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(transformation chains:((^(sin(*(2;a));2))=(^(+(*(sin(a);cos(a));*(cos(a);sin(a)));2))=(*(4;sin;^(a;2);cos;^(a;2)))=(*(4;sin;^(a;2);+(1;-(*(sin;^(a;2))))));(*(tg;^(*(4;a);2)))=(/(^(sin(*(2;*(2;a)));2);^(cos(*(2;*(2;a)));2)))=(/(*(4;sin;^(*(2;a);2);+(1;-(*(sin;^(*(2;a);2)))));^(+(^(cos(*(2;a));2);-(^(sin(*(2;a));2)));2)))=(/(*(4;sin;^(*(2;a);2);+(1;-(*(sin;^(*(2;a);2)))));^(+(1;-(*(2;^(sin(*(2;a));2))));2))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testEquationWithoutRules (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>x</mi><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>4</mn><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mn>2</mn><mspace linebreak=\"newline\"/><mi>x</mi><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>6</mn></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(facts chains:(((x);=;(+(4;2)));((x);=;(6))))", transformationChainParser.root.toString())
    }

    @Test
    fun testEquationWithoutRulesWithTableStructure (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mtable><mtr><mtd><mi>x</mi><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mn>4</mn><mo>*</mo><mn>2</mn><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mn>8</mn><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>9</mn><mo>&#xA0;</mo><mo>*</mo><mo>&#xA0;</mo><mn>6</mn></mtd></mtr><mtr><mtd><mi>x</mi><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mn>8</mn><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mn>8</mn><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>54</mn><mo>&#xA0;</mo></mtd></mtr><mtr><mtd><mi>x</mi><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>54</mn></mtd></mtr></mtable></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(facts chains:(((+(x;-(*(4;2));8));=;(*(9;6)));((+(x;-(8);8));=;(54));((x);=;(54))))", transformationChainParser.root.toString())
    }

    @Test
    fun testEquationWithoutRulesWithTableStructureColored (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mtable mathcolor=\"#007F7F\"><mtr><mtd><mi>x</mi><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mn>4</mn><mo>*</mo><mn>2</mn><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mn>8</mn><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>9</mn><mo>&#xA0;</mo><mo>*</mo><mo>&#xA0;</mo><mn>6</mn></mtd></mtr><mtr><mtd><mi>x</mi><mo>&#xA0;</mo><mo>-</mo><mo>&#xA0;</mo><mn>8</mn><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mn>8</mn><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>54</mn><mo>&#xA0;</mo></mtd></mtr><mtr><mtd><mi>x</mi><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>54</mn></mtd></mtr></mtable></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(facts chains:(((+(x;-(*(4;2));8));=;(*(9;6)));((+(x;-(8);8));=;(54));((x);=;(54))))", transformationChainParser.root.toString())
    }

    @Test
    fun testEquation (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>x</mi><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mn>5</mn><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>y</mi><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>8</mn><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>y</mi><mspace linebreak=\"newline\"/><mi>x</mi><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mn>3</mn></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(facts chains:(((+(x;5;y));=;(+(8;y)));((x);=;(3))))", transformationChainParser.root.toString())
    }

    @Test
    fun testEquationSystem (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfenced open=\"{\" close=\"\"><mtable columnalign=\"left\"><mtr><mtd><mn>2</mn><mi>x</mi><mo>+</mo><mn>3</mn><mi>y</mi><mo>=</mo><mn>7</mn></mtd></mtr><mtr><mtd><mn>2</mn><mi>x</mi><mo>-</mo><mn>3</mn><mi>y</mi><mo>=</mo><mn>1</mn></mtd></mtr></mtable></mfenced><mspace linebreak=\"newline\"/><mfenced open=\"{\" close=\"\"><mtable columnalign=\"left\"><mtr><mtd><mo>(</mo><mn>2</mn><mi>x</mi><mo>+</mo><mn>3</mn><mi>y</mi><mo>)</mo><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mo>(</mo><mn>2</mn><mi>x</mi><mo>-</mo><mn>3</mn><mi>y</mi><mo>)</mo><mo>&#xA0;</mo><mo>=</mo><mn>7</mn><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mn>1</mn></mtd></mtr><mtr><mtd><mn>2</mn><mi>x</mi><mo>-</mo><mn>3</mn><mi>y</mi><mo>=</mo><mn>1</mn></mtd></mtr></mtable></mfenced><mspace linebreak=\"newline\"/><mfenced open=\"{\" close=\"\"><mtable columnalign=\"left\"><mtr><mtd><mn>4</mn><mi>x</mi><mo>=</mo><mn>8</mn></mtd></mtr><mtr><mtd><mn>2</mn><mi>x</mi><mo>-</mo><mn>3</mn><mi>y</mi><mo>=</mo><mn>1</mn></mtd></mtr></mtable></mfenced><mspace linebreak=\"newline\"/><mfenced open=\"{\" close=\"\"><mtable columnalign=\"left\"><mtr><mtd><mi>x</mi><mo>=</mo><mn>2</mn></mtd></mtr><mtr><mtd><mn>2</mn><mi>x</mi><mo>-</mo><mn>3</mn><mi>y</mi><mo>=</mo><mn>1</mn><mo>=</mo><mo>&gt;</mo><mn>2</mn><mo>*</mo><mn>2</mn><mo>-</mo><mn>3</mn><mi>y</mi><mo>=</mo><mn>1</mn><mo>&#xA0;</mo><mo>=</mo><mo>&gt;</mo><mn>3</mn><mo>-</mo><mn>3</mn><mi>y</mi><mo>=</mo><mn>0</mn><mo>=</mo><mo>&gt;</mo><mn>3</mn><mo>=</mo><mn>3</mn><mi>y</mi><mo>=</mo><mo>&gt;</mo><mn>1</mn><mo>=</mo><mi>y</mi><mo>=</mo><mo>&gt;</mo><mi>y</mi><mo>=</mo><mn>1</mn></mtd></mtr></mtable></mfenced><mspace linebreak=\"newline\"/><mfenced open=\"{\" close=\"\"><mtable columnalign=\"left\"><mtr><mtd><mi>x</mi><mo>=</mo><mn>2</mn></mtd></mtr><mtr><mtd><mi>y</mi><mo>=</mo><mn>1</mn></mtd></mtr></mtable></mfenced></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(facts chains:((AND_NODE(facts chains:(((+(*(2;x);*(3;y)));=;(7));((+(*(2;x);-(*(3;y))));=;(1)))));(AND_NODE(facts chains:(((+(+(*(2;x);*(3;y));+(*(2;x);-(*(3;y)))));=;(+(7;1)));((+(*(2;x);-(*(3;y))));=;(1)))));(AND_NODE(facts chains:(((*(4;x));=;(8));((+(*(2;x);-(*(3;y))));=;(1)))));(AND_NODE(facts chains:(((x);=;(2));((+(*(2;x);-(*(3;y))));=;(1));((+(*(2;2);-(*(3;y))));=;(1));((+(3;-(*(3;y))));=;(0));((3);=;(*(3;y)));((1);=;(y));((y);=;(1)))));(AND_NODE(facts chains:(((x);=;(2));((y);=;(1)))))))", transformationChainParser.root.toString())
    }

    @Test
    fun testNamedRule1 (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfenced open=\"[\" close=\"]\"><mrow><mtext>rule_sin2a:</mtext><mi>s</mi><mi>i</mi><mi>n</mi><mo>(2</mo><mi>a</mi><mo>)</mo><mo>=</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>+</mo><mi>a</mi><mo>)</mo><mo>=</mo><mi>s</mi><mi>i</mi><mi>n</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mi>a</mi><mo>)+</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo></mrow></mfenced><mspace linebreak=\"newline\"/><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>=</mo><mfenced open=\"[\" close=\"]\"><mtext>rule_sin2a:</mtext></mfenced><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>^</mo><mn>2</mn></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(rules:([rule_sin2a:AND_NODE(transformation chains:((sin(*(2;a)))=(sin(+(a;a)))=(+(*(sin(a);cos(a));*(sin(a);cos(a))))=(*(2;sin(a);cos(a))));)]);transformation chains:((*(sin(*(2;b));tg(b)))=[rule_sin2a:]=(*(2;sin(b);cos(b);tg(b)))=(*(2;^(sin(b);2))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testNamedRule2 (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mtext>rule_sin2a:</mtext><mi>s</mi><mi>i</mi><mi>n</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>=</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>+</mo><mi>a</mi><mo>)</mo><mo>=</mo><mi>s</mi><mi>i</mi><mi>n</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mi>a</mi><mo>)+</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>]</mo><mspace linebreak=\"newline\"/><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>=</mo><mo>[</mo><mtext>rule_sin2a:</mtext><mo>]</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>^</mo><mn>2</mn></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(rules:([rule_sin2a:AND_NODE(transformation chains:((sin(*(2;a)))=(sin(+(a;a)))=(+(*(sin(a);cos(a));*(sin(a);cos(a))))=(*(2;sin(a);cos(a))));)]);transformation chains:((*(sin(*(2;b));tg(b)))=[rule_sin2a:]=(*(2;sin(b);cos(b);tg(b)))=(*(2;^(sin(b);2))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testNamedRule3 (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mtext>rule_sin2a:</mtext><mi>s</mi><mi>i</mi><mi>n</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>=</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>+</mo><mi>a</mi><mo>)</mo><mo>=</mo><mi>s</mi><mi>i</mi><mi>n</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mi>a</mi><mo>)+</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>]</mo><mspace linebreak=\"newline\"/><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>=</mo><mfenced open=\"[\" close=\"]\"><mtext>rule_sin2a:</mtext></mfenced><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>^</mo><mn>2</mn></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(rules:([rule_sin2a:AND_NODE(transformation chains:((sin(*(2;a)))=(sin(+(a;a)))=(+(*(sin(a);cos(a));*(sin(a);cos(a))))=(*(2;sin(a);cos(a))));)]);transformation chains:((*(sin(*(2;b));tg(b)))=[rule_sin2a:]=(*(2;sin(b);cos(b);tg(b)))=(*(2;^(sin(b);2))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testNamedRule4 (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mtext>rule_sin2a:</mtext><mi>s</mi><mi>i</mi><mi>n</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>=</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>+</mo><mi>a</mi><mo>)</mo><mo>=</mo><mi>s</mi><mi>i</mi><mi>n</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mi>a</mi><mo>)+</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>]</mo><mspace linebreak=\"newline\"/><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>=</mo><mtext>rule_sin2a:</mtext><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>^</mo><mn>2</mn></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(rules:([rule_sin2a:AND_NODE(transformation chains:((sin(*(2;a)))=(sin(+(a;a)))=(+(*(sin(a);cos(a));*(sin(a);cos(a))))=(*(2;sin(a);cos(a))));)]);transformation chains:((*(sin(*(2;b));tg(b)))=[rule_sin2a:]=(*(2;sin(b);cos(b);tg(b)))=(*(2;^(sin(b);2))));)", transformationChainParser.root.toString())
    }

    @Test
    fun testRuleInComparison (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>5</mn><mo>&#xA0;</mo><mo>=</mo><mo>&gt;</mo><mo>[</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>=</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>+</mo><mi>a</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>]</mo><mo>&#x21D2;</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>5</mn></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(facts chains:(((sin(*(2;b)));>;(5));([:AND_NODE(transformation chains:((sin(*(2;a)))=(sin(+(a;a)))=(*(2;sin(a);cos(a))));)]);((*(2;sin(b);cos(b)));>;(5))))", transformationChainParser.root.toString())
    }

    @Test
    fun testRuleInComparisonWithName (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mtext>sin2:</mtext><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>=</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>+</mo><mi>a</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>]</mo><mspace linebreak=\"newline\"/><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>5</mn><mo>&#xA0;</mo><mo>=</mo><mo>&gt;</mo><mtext>sin2:</mtext><mo>&#x21D2;</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>5</mn></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(rules:([sin2:AND_NODE(transformation chains:((sin(*(2;a)))=(sin(+(a;a)))=(*(2;sin(a);cos(a))));)]);facts chains:(((sin(*(2;b)));>;(5));([sin2:]);((*(2;sin(b);cos(b)));>;(5))))",
                transformationChainParser.root.toString())
    }

    @Test
    fun testEquationRule (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfenced open=\"{\" close=\"\"><mtable columnalign=\"left\"><mtr><mtd><mn>2</mn><mi>a</mi><mo>+</mo><mn>8</mn><mo>&lt;</mo><mn>9</mn><mo>+</mo><mi>a</mi></mtd></mtr><mtr><mtd><mi>a</mi><mo>+</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>6</mn></mtd></mtr></mtable></mfenced><mspace linebreak=\"newline\"/><mo>[</mo><mi>a</mi><mo>+</mo><mn>8</mn><mo>&lt;</mo><mn>9</mn><mo>=</mo><mo>&gt;</mo><mi>a</mi><mo>+</mo><mi>a</mi><mo>+</mo><mn>8</mn><mo>&lt;</mo><mn>9</mn><mo>+</mo><mi>a</mi><mo>=</mo><mo>&gt;</mo><mi>a</mi><mo>&lt;</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mtext>sin2:</mtext><mi>sin</mi><mo>(</mo><mn>2</mn><mi>a</mi><mo>)</mo><mo>=</mo><mi>sin</mi><mo>(</mo><mi>a</mi><mo>+</mo><mi>a</mi><mo>)</mo><mo>=</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>a</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>a</mi><mo>)</mo><mo>]</mo><mspace linebreak=\"newline\"/><mfenced open=\"{\" close=\"\"><mtable columnalign=\"left\"><mtr><mtd><mi>a</mi><mo>&lt;</mo><mn>1</mn></mtd></mtr><mtr><mtd><mi>a</mi><mo>+</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>6</mn><mo>=</mo><mo>&gt;</mo><mi>a</mi><mo>+</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>-</mo><mi>a</mi><mo>&gt;</mo><mn>6</mn><mo>-</mo><mi>a</mi><mo>=</mo><mo>&gt;</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>6</mn><mo>-</mo><mi>a</mi></mtd></mtr></mtable></mfenced><mspace linebreak=\"newline\"/><mfenced open=\"{\" close=\"\"><mtable columnalign=\"left\"><mtr><mtd><mi>a</mi><mo>&lt;</mo><mn>1</mn></mtd></mtr><mtr><mtd><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>6</mn><mo>-</mo><mi>a</mi><mo>=</mo><mo>&gt;</mo><mo>[</mo><mi>a</mi><mo>&lt;</mo><mn>1</mn><mo>=</mo><mo>&gt;</mo><mo>-</mo><mi>a</mi><mo>&gt;</mo><mo>-</mo><mn>1</mn><mo>]</mo><mo>=</mo><mo>&gt;</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>6</mn><mo>-</mo><mo>(</mo><mo>-</mo><mn>1</mn><mo>)</mo><mo>=</mo><mo>&gt;</mo><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>5</mn></mtd></mtr></mtable></mfenced><mspace linebreak=\"newline\"/><mfenced open=\"{\" close=\"\"><mtable columnalign=\"left\"><mtr><mtd><mi>a</mi><mo>&lt;</mo><mn>1</mn></mtd></mtr><mtr><mtd><mi>sin</mi><mo>(</mo><mn>2</mn><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>5</mn><mo>&#xA0;</mo><mo>=</mo><mo>&gt;</mo><mtext>sin2:</mtext><mo>&#x21D2;</mo><mn>2</mn><mi>sin</mi><mo>(</mo><mi>b</mi><mo>)</mo><mi>cos</mi><mo>(</mo><mi>b</mi><mo>)</mo><mo>&gt;</mo><mn>5</mn></mtd></mtr></mtable></mfenced></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(rules:([:AND_NODE(facts chains:(((+(a;8));<;(9));((+(a;a;8));<;(+(9;a)));((a);<;(1))))];[sin2:AND_NODE(transformation chains:((sin(*(2;a)))=(sin(+(a;a)))=(*(2;sin(a);cos(a))));)]);facts chains:((AND_NODE(facts chains:(((+(*(2;a);8));<;(+(9;a)));((+(a;sin(*(2;b))));>;(6)))));(AND_NODE(facts chains:(((a);<;(1));((+(a;sin(*(2;b))));>;(6));((+(a;sin(*(2;b));-(a)));>;(+(6;-(a))));((sin(*(2;b)));>;(+(6;-(a)))))));(AND_NODE(facts chains:(((a);<;(1));((sin(*(2;b)));>;(+(6;-(a))));([:AND_NODE(facts chains:(((a);<;(1));((+(-(a)));>;(+(-(1))))))]);((sin(*(2;b)));>;(+(6;-(+(-(1))))));((sin(*(2;b)));>;(5)))));(AND_NODE(facts chains:(((a);<;(1));((sin(*(2;b)));>;(5));([sin2:]);((*(2;sin(b);cos(b)));>;(5)))))))",
                transformationChainParser.root.toString())
    }

    @Test
    fun testMath10comTrigonometricheskieZadachiDifficultTask1CorrectSolution (){
        val transformationChainParser = TransformationChainParser("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo>+</mo><mi>z</mi><mo>=</mo><mi mathvariant=\"normal\">&#x3C0;</mi><mo>=</mo><mo>&gt;</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mi mathvariant=\"normal\">y</mi><mo>=</mo><mi mathvariant=\"normal\">&#x3C0;</mi><mo>-</mo><mi mathvariant=\"normal\">z</mi><mo>=</mo><mo>&gt;</mo><mspace linebreak=\"newline\"/><mo>=</mo><mo>&gt;</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>+</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>=</mo><mfrac><mi mathvariant=\"normal\">&#x3C0;</mi><mn>2</mn></mfrac><mo>-</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>=</mo><mo>&gt;</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>=</mo><mfrac><mi mathvariant=\"normal\">&#x3C0;</mi><mn>2</mn></mfrac><mo>-</mo><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>+</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo><mo>]</mo><mspace linebreak=\"newline\"/><mi>ctg</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mo>+</mo><mi>ctg</mi><mfenced><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac></mfenced><mo>+</mo><mi>ctg</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo><mo>=</mo><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>+</mo><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>+</mo><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>=</mo><mspace linebreak=\"newline\"/><mo>=</mo><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo><mo>+</mo><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>+</mo><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>=</mo><mfrac><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>+</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>+</mo><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>=</mo><mspace linebreak=\"newline\"/><mo>=</mo><mfrac><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">&#x3C0;</mi><mn>2</mn></mfrac><mo>-</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>+</mo><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>=</mo><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>+</mo><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac><mo>=</mo><mspace linebreak=\"newline\"/><mo>=</mo><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo><mfenced><mfrac><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo><mo>+</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac></mfenced><mo>=</mo><mspace linebreak=\"newline\"/><mo>=</mo><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo><mfenced><mfrac><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">&#x3C0;</mi><mn>2</mn></mfrac><mo>-</mo><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>+</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo><mo>)</mo><mo>+</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac></mfenced><mo>=</mo><mspace linebreak=\"newline\"/><mo>=</mo><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo><mfenced><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>+</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo><mo>+</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac></mfenced><mo>=</mo><mspace linebreak=\"newline\"/><mo>=</mo><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo><mfenced><mfrac><mrow><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>cos</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac><mo>)</mo><mi>sin</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></mrow></mfrac></mfenced><mo>=</mo><mi>ctg</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">x</mi><mn>2</mn></mfrac><mo>)</mo><mi>ctg</mi><mfenced><mfrac><mi mathvariant=\"normal\">y</mi><mn>2</mn></mfrac></mfenced><mi>ctg</mi><mo>(</mo><mfrac><mi mathvariant=\"normal\">z</mi><mn>2</mn></mfrac><mo>)</mo></math>")
        val error = transformationChainParser.parse()
        assertEquals(null, error)
        assertEquals("AND_NODE(rules:([:AND_NODE(facts chains:(((+(x;y;z));=;(&#x3C0));((+(x;y));=;(+(&#x3C0;-(z))));((+(/(x;2);/(y;2)));=;(+(/(&#x3C0;2);-(/(z;2)))));((/(z;2));=;(+(/(&#x3C0;2);-(+(/(x;2);/(y;2))))))))]);transformation chains:((+(ctg(/(x;2));ctg(/(y;2));ctg(/(z;2))))=(+(/(cos(/(x;2));sin(/(x;2)));/(cos(/(y;2));sin(/(y;2)));/(cos(/(z;2));sin(/(z;2)))))=(+(/(+(*(cos(/(x;2));sin(/(y;2)));*(cos(/(y;2));sin(/(x;2))));*(sin(/(x;2));sin(/(y;2))));/(cos(/(z;2));sin(/(z;2)))))=(+(/(sin(+(/(x;2);/(y;2)));*(sin(/(x;2));sin(/(y;2))));/(cos(/(z;2));sin(/(z;2)))))=(+(/(sin(+(/(&#x3C0;2);-(/(z;2))));*(sin(/(x;2));sin(/(y;2))));/(cos(/(z;2));sin(/(z;2)))))=(+(/(cos(/(z;2));*(sin(/(x;2));sin(/(y;2))));/(cos(/(z;2));sin(/(z;2)))))=(*(cos(/(z;2));/(+(sin(/(z;2));*(sin(/(x;2));sin(/(y;2))));*(sin(/(x;2));sin(/(y;2));sin(/(z;2))))))=(*(cos(/(z;2));/(+(sin(+(/(&#x3C0;2);-(+(/(x;2);/(y;2)))));*(sin(/(x;2));sin(/(y;2))));*(sin(/(x;2));sin(/(y;2));sin(/(z;2))))))=(*(cos(/(z;2));/(+(cos(+(/(x;2);/(y;2)));*(sin(/(x;2));sin(/(y;2))));*(sin(/(x;2));sin(/(y;2));sin(/(z;2))))))=(*(cos(/(z;2));/(*(cos(/(x;2));cos(/(y;2)));*(sin(/(x;2));sin(/(y;2));sin(/(z;2))))))=(*(ctg(/(x;2));ctg(/(y;2));ctg(/(z;2)))));)",
                transformationChainParser.root.toString())
    }
}