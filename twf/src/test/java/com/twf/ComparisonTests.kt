import com.twf.baseoperations.BaseOperationsDefinitions
import com.twf.config.CompiledConfiguration
import com.twf.expressiontree.ExpressionComporator
import com.twf.expressiontree.ExpressionNodeConstructor
import com.twf.expressiontree.ExpressionSubstitution
import com.twf.org.junit.Test
import com.twf.substitutiontests.parseStringExpression
import kotlin.test.assertEquals

class ComparisonTests{
    val compiledConfiguration = CompiledConfiguration()
    val expressionNodeConstructor = ExpressionNodeConstructor()
    val baseOperationsDefinitions = BaseOperationsDefinitions()
    val expressionComporator = compiledConfiguration.factComporator.expressionComporator
    val definedFunctionNameNumberOfArgsSet = compiledConfiguration.definedFunctionNameNumberOfArgsSet

    @Test
    fun correctUnm (){
        val left = parseStringExpression("U(n,m)")
        val right = parseStringExpression("n^m")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctSimple (){
        val left = parseStringExpression("2*a + 3*b*c - a(b+c) + c(a-b)")
        val right = parseStringExpression("a + 2b(c - 1) - a*b + 2b + a")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctMultiplicationToAddition (){
        val left = parseStringExpression("2*x")
        val right = parseStringExpression("x+x")
        assertEquals(true, expressionComporator.probabilityTestComparison(left, right))
    }

    @Test
    fun uncorrectSubstruction (){
        val left = parseStringExpression("x+y+z")
        val right = parseStringExpression("(x+y+z)-z")
        assertEquals(false, expressionComporator.probabilityTestComparison(left, right))
    }

    @Test
    fun correctFactorial (){
        val left = parseStringExpression("n! + (2*n)!")
        val right = parseStringExpression("(n-1)!*n + (2*n-1)! * (2*n)")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctNotFirstRuleApplication (){
        val left = parseStringExpression("(n-1)!*n + (2*n)!")
        val right = parseStringExpression("n! + (2*n-1)! * (2*n)")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun uncorrectSimple (){
        val left = parseStringExpression("2*a + 3*b*c - a(b+c) + c(a-b)")
        val right = parseStringExpression("a + 2b(c - 1) - ab + 3b")
        assertEquals(false, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctTrigonometry (){
        val left = parseStringExpression("1 - sin(x)^2")
        val right = parseStringExpression("cos(x)^2")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctCosTrigonometry (){
        val left = parseStringExpression("cos(a)+cos(2a)+cos(6a)+cos(7a)")
        val right = parseStringExpression("4cos(a/2)cos(5a/2)cos(4a)")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun uncorrectCosTrigonometry (){
        val left = parseStringExpression("cos(a)+cos(2a)+cos(6a)+cos(7a)")
        val right = parseStringExpression("3cos(a/2)cos(5a/2)cos(4a)")
        assertEquals(false, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctTgTrigonometry (){
        val left = parseStringExpression("tg(4a) - 1/cos(4a)")
        val right = parseStringExpression("(sin(2a)-cos(2a))/(sin(2a)+cos(2a))")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun uncorrectTgTrigonometry (){
        val left = parseStringExpression("tg(4a) - 1/cos(3a)")
        val right = parseStringExpression("(sin(2a)-cos(2a))/(sin(2a)+cos(2a))")
        assertEquals(false, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctTgArcsinTrigonometry (){
        val left = parseStringExpression("tg(arcsin(12/13))")
        val right = parseStringExpression("12/5")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun uncorrectTgArcsinTrigonometry (){
        val left = parseStringExpression("tg(arcsin(13/12))")
        val right = parseStringExpression("12/5")
        assertEquals(false, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctFunctionReplacement (){
        val left = parseStringExpression("tg(4f(x)) - 1/cos(4f(x))")
        val right = parseStringExpression("(sin(2f(x))-cos(2f(x)))/(sin(2f(x))+cos(2f(x)))")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun uncorrectFunctionReplacement1 (){
        val left = parseStringExpression("tg(4f(x)) - 1/cos(4f(x))")
        val right = parseStringExpression("(sin(2f(x))-cos(2f(a)))/(sin(2f(x))+cos(2f(x)))")
        assertEquals(false, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun uncorrectFunctionReplacement2 (){
        val left = parseStringExpression("tg(4f(x)) - 1/cos(4f(x))")
        val right = parseStringExpression("(sin(2f(x))-cos(2g(x)))/(sin(2f(x))+cos(2f(x)))")
        assertEquals(false, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctSqrt (){
        val left = parseStringExpression("sqrt(-x^2)")
        val right = parseStringExpression("y")
        assertEquals(false, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun uncorrectFactorial (){
        val left = parseStringExpression("n!")
        val right = parseStringExpression("n")
        assertEquals(false, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun contextRuleCorrect (){
        val rule = ExpressionSubstitution(parseStringExpression("sin(&#x3C0;/2-z/2)"),
                parseStringExpression("cos(z/2)"), basedOnTaskContext = true)
        val left = parseStringExpression("sin(&#x3C0;/2-z/2)")
        val right = parseStringExpression("cos(z/2)")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, listOf(rule)))
    }

    @Test
    fun correctChAndLnMultiplied (){
        val left = parseStringExpression("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>s</mi><mi>h</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>+</mo><mi>c</mi><mi>h</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>c</mi><mi>t</mi><mi>g</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><msup><mi>e</mi><mrow><mo>(</mo><mi>c</mi><mi>h</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup></math>")
        val right = parseStringExpression("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>s</mi><mi>h</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>+</mo><mi>c</mi><mi>h</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>c</mi><mi>t</mi><mi>g</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><msup><mrow><mo>(</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow><mrow><mo>(</mo><mi>c</mi><mi>h</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup></math>")
        assertEquals(true, expressionComporator.compareWithoutSubstitutions(left, right))
    }

    @Test
    fun correctChAndLn (){
        val left = parseStringExpression("e^(ch(x)*ln(sin(x)))")
        val right = parseStringExpression("(sin(x))^ch(x)")
        assertEquals(true, expressionComporator.compareWithoutSubstitutions(left, right))
    }
}