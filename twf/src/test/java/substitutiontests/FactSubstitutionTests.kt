package substitutiontests

import com.twf.config.CompiledConfiguration
import com.twf.factstransformations.*
import org.junit.Test
import com.twf.logs.log
import org.junit.Ignore
import kotlin.test.assertEquals

class FactSubstitutionTests {
    @Test
    fun simpleExpressionComparison() {
        val factSubstitution = FactSubstitution(ExpressionComparison.parseFromFactIdentifier("a+c;ec;>;ec;b+c")!!,
                ExpressionComparison.parseFromFactIdentifier("a;ec;>;ec;b")!!, factComporator = factComporator)
        val root = parseFactsTransformationsString("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>a</mi><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>c</mi><mo>&#xA0;</mo><mo>&gt;</mo><mo>&#xA0;</mo><mi>b</mi><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>c</mi></math>")
        assertEquals("a;ec;>;ec;b", factSubstitution.checkAndApply(root.factTransformationChains[0].chain[0])!!.computeInIdentifier(true))
    }

    @Test
    fun simpleExpressionComparisonWithVariableReplacement() {
        val factSubstitution = FactSubstitution(ExpressionComparison.parseFromFactIdentifier("a+c;ec;>;ec;b+c")!!,
                ExpressionComparison.parseFromFactIdentifier("a;ec;>;ec;b")!!, factComporator = factComporator)
        val root = parseFactsTransformationsString("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>x</mi><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>y</mi><mo>&#xA0;</mo><mo>&gt;</mo><mo>&#xA0;</mo><mi>z</mi><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>y</mi></math>")
        assertEquals("x;ec;>;ec;z", factSubstitution.checkAndApply(root.factTransformationChains[0].chain[0])!!.computeInIdentifier(true))
    }

    @Test
    @Ignore
    fun simpleExpressionComparisonWithExpressionReplacement() {
        val factSubstitution = FactSubstitution(ExpressionComparison.parseFromFactIdentifier("a+c;ec;<=;ec;b+c")!!,
                ExpressionComparison.parseFromFactIdentifier("a;ec;<=;ec;b")!!, factComporator = factComporator)
        val root = parseFactsTransformationsString("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mn>5</mn><mo>(</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo>)</mo><mo>-</mo><mn>7</mn><mo>^</mo><mi>z</mi><mo>&lt;</mo><mo>=</mo><mn>8</mn><mi>x</mi><mo>-</mo><mn>7</mn><mo>^</mo><mi>z</mi></math>")
        assertEquals("*(+(x;y);5);ec;<=;ec;*(8;x)", factSubstitution.checkAndApply(root.factTransformationChains[0].chain[0])!!.computeInIdentifier(true))
    }

    @Test
    fun simpleExpressionComparisonWithExpressionReplacementNotCorrectChangeSign() {
        val factSubstitution = FactSubstitution(ExpressionComparison.parseFromFactIdentifier("a+c;ec;<;ec;b+c")!!,
                ExpressionComparison.parseFromFactIdentifier("a;ec;<;ec;b")!!, factComporator = factComporator)
        val root = parseFactsTransformationsString("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mn>5</mn><mo>(</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo>)</mo><mo>-</mo><mn>7</mn><mo>^</mo><mi>z</mi><mo>&lt;</mo><mo>=</mo><mn>8</mn><mi>x</mi><mo>-</mo><mn>7</mn><mo>^</mo><mi>z</mi></math>")
        assertEquals(null, factSubstitution.checkAndApply(root.factTransformationChains[0].chain[0]))
    }

    @Test
    fun simpleExpressionComparisonWithExpressionReplacementChangeSign() {
        val factSubstitution = FactSubstitution(ExpressionComparison.parseFromFactIdentifier("a+c;ec;>=;ec;b+c")!!,
                ExpressionComparison.parseFromFactIdentifier("a;ec;>=;ec;b")!!, factComporator = factComporator)
        val root = parseFactsTransformationsString("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mn>5</mn><mo>(</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo>)</mo><mo>-</mo><mn>7</mn><mo>^</mo><mi>z</mi><mo>&lt;</mo><mo>=</mo><mn>8</mn><mi>x</mi><mo>-</mo><mn>7</mn><mo>^</mo><mi>z</mi></math>")
        assertEquals("*(8;x);ec;>=;ec;*(5;+(x;y))", factSubstitution.checkAndApply(root.factTransformationChains[0].chain[0])!!.computeInIdentifier(true))
    }

    @Test
    fun simpleMainLineNodes() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(OR_NODE(a;mn;b);mn;c)")!!,
                MainLineOrNode.parseFromFactIdentifier("OR_NODE(AND_NODE(a;mn;c);mn;AND_NODE(b;mn;c))")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(OR_NODE(a;ec;=;ec;ee;mn;b;ec;=;ec;r);mn;c;ec;<;ec;g)")!!
        assertEquals("OR_NODE(AND_NODE((a);ec;=;ec;(ee);mn;(c);ec;<;ec;(g));mn;AND_NODE((b);ec;=;ec;(r);mn;(c);ec;<;ec;(g)))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    @Test
    fun simpleMainLineNodesWithLostExtraFacts() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(OR_NODE(a;mn;b);mn;c)")!!,
                MainLineOrNode.parseFromFactIdentifier("OR_NODE(AND_NODE(a;mn;c);mn;AND_NODE(b;mn;c))")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(OR_NODE(a;ec;=;ec;ee;mn;b;ec;=;ec;r);mn;c;ec;<;ec;g;mn;k;ec;<;ec;gl)")!!
        assertEquals("OR_NODE(AND_NODE((a);ec;=;ec;(ee);mn;(c);ec;<;ec;(g));mn;AND_NODE((b);ec;=;ec;(r);mn;(c);ec;<;ec;(g)))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    @Test
    fun simpleMainLineNodesWithLostExtraFactsAndChangedOrder() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(OR_NODE(a;mn;b);mn;c)")!!,
                MainLineOrNode.parseFromFactIdentifier("OR_NODE(AND_NODE(a;mn;c);mn;AND_NODE(b;mn;c))")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c;ec;<;ec;g;mn;OR_NODE(a;ec;=;ec;ee;mn;b;ec;=;ec;r);mn;k;ec;<;ec;gl)")!!
        assertEquals("OR_NODE(AND_NODE((a);ec;=;ec;(ee);mn;(c);ec;<;ec;(g));mn;AND_NODE((b);ec;=;ec;(r);mn;(c);ec;<;ec;(g)))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    @Test
    fun expressionsInMainLineNodesWithLostExtraFactsAndChangedOrder() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(OR_NODE(a;mn;b);mn;c)")!!,
                MainLineOrNode.parseFromFactIdentifier("OR_NODE(AND_NODE(a;mn;c);mn;AND_NODE(b;mn;c))")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5);mn;k-4;ec;<;ec;gl)")!!
        assertEquals("OR_NODE(AND_NODE((a);ec;=;ec;(8);mn;(^(c;r));ec;<;ec;(+(g;-(6))));mn;AND_NODE((+(b;-(7)));ec;=;ec;(^(r;5));mn;(^(c;r));ec;<;ec;(+(g;-(6)))))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    @Test
    fun expressionsInMainLineNodesWithLostExtraFactsAndChangedOrderCheckOutMainLineNodePart() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(OR_NODE(a;mn;b);mn;c)")!!,
                MainLineOrNode.parseFromFactIdentifier("OR_NODE(AND_NODE(a;mn;c);mn;AND_NODE(b;mn;c))")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5);mn;k-4;ec;<;ec;gl)")!!
        assertEquals("OR_NODE(AND_NODE((a);ec;=;ec;(8);mn;(^(c;r));ec;<;ec;(+(g;-(6))));mn;AND_NODE((+(b;-(7)));ec;=;ec;(^(r;5));mn;(^(c;r));ec;<;ec;(+(g;-(6)))))",
                factSubstitution.checkAndApply(root, checkOutMainLineNodePart = false)!!.computeOutIdentifier(true))
    }

    @Test
    fun expressionsInMainLineNodesWithLostExtraFactsAndChangedOrderCheckOutMainLineNodePartNotCorrectUsing() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(OR_NODE(a;mn;b);mn;c)")!!,
                MainLineOrNode.parseFromFactIdentifier("OR_NODE(AND_NODE(a;mn;c);mn;AND_NODE(b;mn;c))")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5);mn;k-4;ec;<;ec;gl)")!!
        assertEquals("OR_NODE()",
                factSubstitution.checkAndApply(root, checkOutMainLineNodePart = false)!!.computeInIdentifier(true))
    }

    @Test
    fun simpleMainLineNodesNotCorrect() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(OR_NODE(a;mn;b);mn;c)")!!,
                MainLineOrNode.parseFromFactIdentifier("OR_NODE(AND_NODE(a;mn;c);mn;AND_NODE(b;mn;c))")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(AND_NODE(a;ec;=;ec;e;mn;b;ec;=;ec;r);mn;c;ec;<;ec;g)")!!
        assertEquals(null, factSubstitution.checkAndApply(root))
    }

    @Test
    fun addInequalities() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x-y;ec;>=;ec;8)")!!
        assertEquals("AND_NODE((+(x;y));ec;=;ec;(6);mn;+((+(x;y));(+(x;-(y))));ec;>=;ec;+((6);(8)))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    @Test
    fun addInequalitiesNotCorrect() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(x+y;ec;>=;ec;6;mn;x-y;ec;>=;ec;8)")!!
        assertEquals(null, factSubstitution.checkAndApply(root))
    }

    @Test
    fun addInequalitiesChangedOrder() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;8;mn;x+y;ec;=;ec;6)")!!
        assertEquals("AND_NODE((+(x;y));ec;=;ec;(6);mn;+((+(x;y));(+(x;-(y))));ec;>=;ec;+((6);(8)))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    @Test
    fun addInequalitiesChangedOrderWithExtraFacts() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;8;mn;x+y;ec;=;ec;6;mn;x+y;ec;=;ec;z)")!!
        assertEquals("AND_NODE((+(x;y));ec;=;ec;(6);mn;+((+(x;y));(+(x;-(y))));ec;>=;ec;+((6);(8));mn;(+(x;y));ec;=;ec;(z))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    @Test
    fun addInequalitiesChangedOrderWithTwoExtraFacts() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;8;mn;x-y;ec;>=;ec;z;mn;x+y;ec;=;ec;6;mn;x+y;ec;=;ec;z)")!!
        assertEquals("AND_NODE((+(x;y));ec;=;ec;(6);mn;+((+(x;y));(+(x;-(y))));ec;>=;ec;+((6);(8));mn;(+(x;-(y)));ec;>=;ec;(z);mn;(+(x;y));ec;=;ec;(z))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    @Test
    fun unionSameInequalities() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;<=;ec;b;mn;a;ec;<=;ec;b)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;<=;ec;b)")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(x^r+6;ec;>=;ec;r^8;mn;r^8;ec;<=;ec;x^r+6)")!!
        assertEquals("AND_NODE((^(r;8));ec;<=;ec;(+(^(x;r);6)))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    @Test
    fun unionSameInequalitiesNotCorrect() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;<=;ec;b;mn;a;ec;<=;ec;b)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;<=;ec;b)")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(x^r+6;ec;>=;ec;r^8;mn;r^8;ec;>=;ec;x^r+6)")!!
        assertEquals(null, factSubstitution.checkAndApply(root))
    }

    @Test
    fun unionSameInequalitiesExtraFacts() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;<=;ec;b;mn;a;ec;<=;ec;b)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;<=;ec;b)")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(x^r+6;ec;>=;ec;r^8;mn;x+t;ec;>=;ec;r^8;mn;r^8;ec;<=;ec;x^r+6)")!!
        assertEquals("AND_NODE((^(r;8));ec;<=;ec;(+(^(x;r);6));mn;(+(x;t));ec;>=;ec;(^(r;8)))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    @Test
    fun useFactFromAdditionalFacts() {
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator)
        val root = MainLineAndNode.parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6)")!!
        assertEquals("AND_NODE((+(x;y));ec;=;ec;(6);mn;+((+(x;y));(+(x;-(y))));ec;>=;ec;+((6);(8)))",
                factSubstitution.checkAndApply(root, additionalFacts = listOf(ExpressionComparison.parseFromFactIdentifier("x-y;ec;>=;ec;8")!!))!!.computeInIdentifier(true))
    }

    @Test
    @Ignore
    fun comparisonToOrNode() {
        val factSubstitution = FactSubstitution(ExpressionComparison.parseFromFactIdentifier("abs(x);ec;>=;ec;b")!!,
                MainLineOrNode.parseFromFactIdentifier("OR_NODE(x;ec;<=;ec;-b;mn;x;ec;>=;ec;b)")!!, factComporator = factComporator)
        val root = ExpressionComparison.parseFromFactIdentifier("abs(x+8);ec;>=;ec;b+k")!!
        assertEquals("OR_NODE(+(8;x);ec;<=;ec;+(-((+(b;k))));mn;+(8;x);ec;>=;ec;(+(b;k)))", factSubstitution.checkAndApply(root)!!.computeInIdentifier(true))
    }

    val compiledConfiguration = CompiledConfiguration()
    val factComporator = compiledConfiguration.factComporator

    @Test
    fun factsCompareAsIsOrNodeOrderChanged() {
        val left = MainLineOrNode.parseFromFactIdentifier("OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5)")!!
        val right = MainLineOrNode.parseFromFactIdentifier("OR_NODE(b-7;ec;=;ec;r^5;mn;a;ec;=;ec;8)")!!
        assert(factComporator.compareAsIs(left, right))
    }

    @Test
    fun factsCompareAsIsExternalOrderChanged() {
        val left = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5);mn;k-4;ec;<;ec;gl)")!!
        val right = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;OR_NODE(b-7;ec;=;ec;r^5;mn;a;ec;=;ec;8);mn;k-4;ec;<;ec;gl)")!!
        assert(factComporator.compareAsIs(left, right),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun factsCompareAsIsInternalOrderChanged() {
        val left = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5);mn;k-4;ec;<;ec;gl)")!!
        val right = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;k-4;ec;<;ec;gl;mn;OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5))")!!
        assert(factComporator.compareAsIs(left, right))
    }

    @Test
    @Ignore
    fun factsCompareAsIsNotCorrectTwoOrdersChanged() { //order can be changed only in one MainLineNode
        val left = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5);mn;k-4;ec;<;ec;gl)")!!
        val right = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;k-4;ec;<;ec;gl;mn;OR_NODE(b-7;ec;=;ec;r^5;mn;a;ec;=;ec;8))")!!
        assert(!factComporator.compareAsIs(left, right))
    }

    @Test
    fun factsCompareAsIsNotCorrect() {
        val left = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5);mn;k-4;ec;<;ec;gl)")!!
        val right = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;k-4;ec;<;ec;gl;mn;OR_NODE(a;ec;=;ec;7;mn;b-7;ec;=;ec;r^5))")!!
        assert(!factComporator.compareAsIs(left, right))
    }

    @Test
    fun factsCompareAsIsUseAdditionalFacts() {
        val left = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;k-4;ec;<;ec;gl)")!!
        val right = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5))")!!
        val additionalFacts = listOf(
                ExpressionComparison.parseFromFactIdentifier("k-4;ec;<;ec;gl")!!,
                MainLineOrNode.parseFromFactIdentifier("OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5)")!!
        )
        val additionalFactsSortedIdentifiers = additionalFacts.map { it.computeSortedOutIdentifier(true) }.sorted()
        assert(factComporator.compareAsIs(left, right, additionalFactsSortedIdentifiers))
    }

    @Test
    fun fullFactsCompareOrderChanged() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        compiledConfiguration.comparisonSettings.compareExpressionsWithProbabilityRulesWhenComparingFacts = false
        val left = parseFromFactIdentifier("AND_NODE(OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5))")!!
        val right = parseFromFactIdentifier("AND_NODE(OR_NODE(b-7;ec;=;ec;r^5;mn;a;ec;=;ec;8))")!!
        assert(factComporator.fullFactsCompare(left, right, listOf(),
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithNotCorrectSubstitution() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        compiledConfiguration.comparisonSettings.compareExpressionsWithProbabilityRulesWhenComparingFacts = false
        val left = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;8;mn;x-y;ec;>=;ec;z;mn;x+y;ec;=;ec;6;mn;x+y;ec;=;ec;z)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x-y;ec;>=;ec;8;mn;x-y;ec;>=;ec;z)")!!
        assert(!factComporator.fullFactsCompare(left, right, listOf(),
                additionalFactUsed = mutableListOf()))
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevel() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator)
        val left = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;8;mn;x+y;ec;=;ec;6)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;(x-y)+(x+y);ec;>=;ec;(6)+(8))")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), listOf(factSubstitution),
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelComplicated() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator)
        val left = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;8;mn;x-y;ec;>=;ec;z;mn;x+y;ec;=;ec;6;mn;x+y;ec;=;ec;z)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14;mn;x-y;ec;>=;ec;z;mn;x+y;ec;=;ec;z)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), listOf(factSubstitution),
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelComplicatedBackDirection() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitution = FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator)
        val right = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;8;mn;x+y;ec;=;ec;6)")!!
        val left = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), listOf(factSubstitution),
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelSimpleWithAdditionalFact() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;8)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;(x-y)+(x+y);ec;>=;ec;(6)+(8))")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x+y;ec;=;ec;6")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    @Ignore
    fun fullFactsCompareOnTopLevelComplicatedWithAdditionalFact() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val left = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;k-4;ec;<;ec;gl)")!!
        val right = MainLineAndNode.parseFromFactIdentifier("AND_NODE(c^r;ec;<;ec;g-6;mn;OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5))")!!
        val additionalFacts = listOf(
                ExpressionComparison.parseFromFactIdentifier("k-4;ec;<;ec;gl")!!,
                MainLineOrNode.parseFromFactIdentifier("OR_NODE(a;ec;=;ec;8;mn;b-7;ec;=;ec;r^5)")!!
        )
        assert(factComporator.compareWithTreeTransformationRules(left, right,
                additionalFacts,
                listOf(),
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelComplicatedWithAdditionalFact() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;8;mn;x-y;ec;>=;ec;z)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14;mn;x-y;ec;>=;ec;z)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x+y;ec;=;ec;6")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelComplicatedWithAdditionalFact2OnePossibleSubstitution() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x-y;ec;>=;ec;8")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelComplicatedWithAdditionalFact2() { //now not correct, too much substitutions on one level can be applied - now its division are not supported - it's NPC task
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x-y;ec;>=;ec;z)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14;mn;x-y;ec;>=;ec;z)")!!
        assert(!factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x-y;ec;>=;ec;8")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionBaseContextRule() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14)")!!, basedOnTaskContext = true, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x-y;ec;>=;ec;8")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionBaseContextRuleWrong() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14)")!!, basedOnTaskContext = true, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x+z;ec;=;ec;6)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+z;ec;=;ec;6;mn;x+x;ec;>=;ec;14)")!!
        assert(!factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x-y;ec;>=;ec;8")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelComplicatedWithAdditionalFactBaseContextRule() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14)")!!, basedOnTaskContext = true, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x-y;ec;>=;ec;z)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14;mn;x-y;ec;>=;ec;z)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x-y;ec;>=;ec;8")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    @Ignore
    fun fullFactsCompareWithOneSubstitutionOnTopLevelComplicatedWithTwoAdditionalFacts() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;>=;ec;b;mn;b;ec;>=;ec;c;mn;c;ec;>;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;>;ec;d)")!!, direction = SubstitutionDirection.LEFT_TO_RIGHT, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;z)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;>;ec;z-9)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x+y;ec;>=;ec;x-y")!!, parseFromFactIdentifier("z;ec;>;ec;z-9")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelComplicatedWithTwoAdditionalFactsWrongDirection() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;>=;ec;b;mn;b;ec;>=;ec;c;mn;c;ec;>;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;>;ec;d)")!!, direction = SubstitutionDirection.LEFT_TO_RIGHT, factComporator = factComporator))
        val right = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;z)")!!
        val left = parseFromFactIdentifier("AND_NODE(x+y;ec;>;ec;z-9)")!!
        assert(!factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x+y;ec;>=;ec;x-y")!!, parseFromFactIdentifier("z;ec;>;ec;z-9")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelNotCorrect() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;z)")!!
        val right = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;z;mn;x+y;ec;=;ec;z)")!!
        assert(!factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x+y;ec;=;ec;6")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelNotCorrectSimilarFact() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;z)")!!
        val right = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;z;mn;x+y;ec;=;ec;z)")!!
        assert(!factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x-y;ec;>=;ec;8")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithOneSubstitutionOnTopLevelComplicatedWithAdditionalFactNotCorrect() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator))
        val left = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;z)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;2*x;ec;>=;ec;14;mn;x-y;ec;>=;ec;z;mn;x+y;ec;=;ec;z)")!!
        assert(!factComporator.compareWithTreeTransformationRules(left, right,
                listOf(parseFromFactIdentifier("x+y;ec;=;ec;6")!!, parseFromFactIdentifier("x-y;ec;>=;ec;8")!!),
                factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsCompareWithSubstitutionsOnTopLevelComplicated() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;<=;ec;b;mn;a;ec;<=;ec;b)")!!,
                        MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;<=;ec;b)")!!, factComporator = factComporator),
                FactSubstitution(MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;c;ec;>=;ec;d)")!!,
                        MainLineAndNode.parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b;mn;a+c;ec;>=;ec;b+d)")!!, factComporator = factComporator),
                FactSubstitution(ExpressionComparison.parseFromFactIdentifier("a+c;ec;>;ec;b+c")!!,
                        ExpressionComparison.parseFromFactIdentifier("a;ec;>;ec;b")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(x-y;ec;>=;ec;8;mn;x-y;ec;>=;ec;z;mn;x+y;ec;=;ec;6;mn;x+y;ec;=;ec;z)")!!
        val right = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;6;mn;x+x;ec;>=;ec;14;mn;x-y;ec;>=;ec;z;mn;x+y;ec;=;ec;z)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsWithConditionCompareWithSubstitutionsCorrect() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*c;ec;>;ec;b*c;mn;c;ec;>;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(a;ec;>;ec;b)")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(x;ec;>;ec;y)")!!
        val right = parseFromFactIdentifier("AND_NODE(x*5;ec;>;ec;y*5)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsWithConditionCompareWithSubstitutionsWrongCondition() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*c;ec;>;ec;b*c;mn;c;ec;>;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(a;ec;>;ec;b;mn;c;ec;>;ec;0)")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(x;ec;>;ec;y)")!!
        val right = parseFromFactIdentifier("AND_NODE(x*0;ec;>;ec;y*0)")!!
        assert(!factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsWithConditionCompareWithSubstitutionsWrong() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*c;ec;>;ec;b*c;mn;c;ec;>;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(a;ec;>;ec;b;mn;c;ec;>;ec;0)")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(x;ec;>;ec;y)")!!
        val right = parseFromFactIdentifier("AND_NODE(x*y;ec;>;ec;y*y)")!!
        assert(!factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsWithConditionCompareWithSubstitutionsFull() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*c;ec;>;ec;b*c;mn;c;ec;>;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(a;ec;>;ec;b;mn;c;ec;>;ec;0)")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(x;ec;>;ec;y;mn;y;ec;>;ec;0)")!!
        val right = parseFromFactIdentifier("AND_NODE(x*y;ec;>;ec;y*y;mn;y;ec;>;ec;0)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsWithAutoMatchedConditionCompareWithSubstitutionsFull() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*c;ec;=;ec;b*c;mn;c;ec;>;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b)")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(x;ec;=;ec;y)")!!
        val right = parseFromFactIdentifier("AND_NODE(x*5;ec;=;ec;y*5)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsWithAutoMatchedConditionCompareWithSubstitutionsFullWrong() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*c;ec;=;ec;b*c;mn;c;ec;<;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b)")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(x;ec;=;ec;y)")!!
        val right = parseFromFactIdentifier("AND_NODE(x*5;ec;=;ec;y*5)")!!
        assert(!factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsWithAutoDoubleMatchedConditionCompareWithSubstitutionsFull() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*c;ec;=;ec;b*c;mn;c;ec;=;ec;5)")!!,
                        parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b)")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(x;ec;=;ec;y)")!!
        val right = parseFromFactIdentifier("AND_NODE(x*5;ec;=;ec;y*5)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun fullFactsWithAutoDoubleMatchedConditionCompareWithSubstitutionsFullDivision() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a/c;ec;=;ec;b/c;mn;c;ec;>;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(a;ec;=;ec;b)")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(x+y;ec;=;ec;z-u)")!!
        val right = parseFromFactIdentifier("AND_NODE((x+y)/2;ec;=;ec;(z-u)/2)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun squaredEquationD0() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*x^2+b*x+c;ec;=;ec;0;mn;b^2-4*a*c;ec;=;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(x;ec;=;ec;-b/(2*a))")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(1*x^2+(-2)*x+1;ec;=;ec;0)")!!
        val right = parseFromFactIdentifier("AND_NODE(x;ec;=;ec;1)")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun squaredEquationD() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*x^2+b*x+c;ec;=;ec;0;mn;b^2-4*a*c;ec;>;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(OR_NODE(x;ec;=;ec;(-b+(b^2-4*a*c)^0.5)/(2*a);mn;x;ec;=;ec;(-b-(b^2-4*a*c)^0.5)/(2*a)))")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(1*x^2+5*x+6;ec;=;ec;0)")!!
        val right = parseFromFactIdentifier("AND_NODE(OR_NODE(x;ec;=;ec;-2;mn;x;ec;=;ec;-3))")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    @Ignore
    fun squaredEquationD1OrderChanged() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*x^2+b*x+c;ec;=;ec;0;mn;b^2-4*a*c;ec;>;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(OR_NODE(x;ec;=;ec;(-b+(b^2-4*a*c)^0.5)/(2*a);mn;x;ec;=;ec;(-b-(b^2-4*a*c)^0.5)/(2*a)))")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(1*x^2+5*x+6;ec;=;ec;0)")!!
        val right = parseFromFactIdentifier("AND_NODE(OR_NODE(x;ec;=;ec;-3;mn;x;ec;=;ec;-2))")!!
        assert(factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }

    @Test
    fun squaredEquationNotSelectedB() {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        val factSubstitutions = listOf(
                FactSubstitution(parseFromFactIdentifier("AND_NODE(a*x^2+b*x+c;ec;=;ec;0;mn;b^2-4*a*c;ec;=;ec;0)")!!,
                        parseFromFactIdentifier("AND_NODE(x;ec;=;ec;-b/(2*a))")!!, factComporator = factComporator)
        )
        val left = parseFromFactIdentifier("AND_NODE(1*x^2-2*x+1;ec;=;ec;0)")!!
        val right = parseFromFactIdentifier("AND_NODE(x;ec;=;ec;1)")!!
        assert(!factComporator.compareWithTreeTransformationRules(left, right, listOf(), factSubstitutions,
                additionalFactUsed = mutableListOf()),
                { print(log.getLogInPlainText()) })
    }
}

fun parseFactsTransformationsString(facts: String, nameForRuleDesignationsPossible: Boolean = false): MainLineAndNode {
    val transformationChainParser = TransformationChainParser(facts)
    transformationChainParser.parse()
    return transformationChainParser.root
}