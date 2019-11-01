import com.twf.baseoperations.BaseOperationsDefinitions
import org.junit.Test
import substitutiontests.parseStringExpression
import kotlin.test.assertEquals

class BaseOperationsTests {
    val baseOperationsDefinitions = BaseOperationsDefinitions()

    @Test
    fun testPlus (){
        val root = parseStringExpression("1+2+3")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(6.0)", root.toString())
    }

    @Test
    fun testPlusVariable (){
        val root = parseStringExpression("<msub><mi>a</mi><mn>1</mn></msub>+0")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("({a},{1})", root.toString())
    }

    @Test
    fun testPlus2Variable (){
        val root = parseStringExpression("n+y")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(+(n;y))", root.toString())
    }

    @Test
    fun testPlus2VariableDigit (){
        val root = parseStringExpression("2+n+2.5+y")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(+(n;y;4.5))", root.toString())
    }

    @Test
    fun testMul (){
        val root = parseStringExpression("1*2*3")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(6.0)", root.toString())
    }

    @Test
    fun testMulOnNull (){
        val root = parseStringExpression("1*n*3*0*m*5")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(0.0)", root.toString())
    }

    @Test
    fun testMulVariableSimplyfication (){
        val root = parseStringExpression("2*n*0.5")
        baseOperationsDefinitions.computeExpressionTree(root.children[0])
        assertEquals("(n)", root.toString())
    }

    @Test
    fun testMul2Variable (){
        val root = parseStringExpression("2*n*0.5*y")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(*(n;y))", root.toString())
    }

    @Test
    fun testMul2VariableDigit (){
        val root = parseStringExpression("2*n*2.5*y")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(*(n;y;5.0))", root.toString())
    }

    @Test
    fun testDiv (){
        val root = parseStringExpression("20/2/5")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(2.0)", root.toString())
    }

    @Test
    fun testDivFirstVariable (){
        val root = parseStringExpression("first/20/2/5/2/second")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(/(first;second;400.0))", root.toString())
    }

    @Test
    fun testDivFirstVariableRed (){
        val root = parseStringExpression("first/0.05/2/5/2/second")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(/(first;second))", root.toString())
    }

    @Test
    fun testDivFirstVariableNoChanges (){
        val root = parseStringExpression("first/1.0")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(/(first;1.0))", root.toString())
    }

    @Test
    fun testDivInfinity (){
        val root = parseStringExpression("9.0/0.0")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(Infinity)", root.toString())
    }

    @Test
    fun testDivNotFirstVariable (){
        val root = parseStringExpression("20/f/2/5/2/second")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(/(1.0;f;second))", root.toString())
    }

    @Test
    fun testMinusJustFirstVariable (){
        val root = parseStringExpression("-first")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0].children[0])
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(-(first))", root.toString())
    }

    @Test
    fun testPowComplex (){
        val root = parseStringExpression("(-1)^0.5")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0].children[0].children[0])
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0].children[0])
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(NaN)", root.toString())
    }

    @Test
    fun testPow (){
        val root = parseStringExpression("n^n^7.0^m^2^2^3^n^0")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(^(n;n;7.0;m;256.0))", root.toString())
    }

    @Test
    fun testPowInfinity (){
        val root = parseStringExpression("10^10^10^10")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(Infinity)", root.toString())
    }

    @Test
    fun testMod (){
        val root = parseStringExpression("mod(5.5, 2.5)")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(0.5)", root.toString())
    }

    @Test
    fun testModNull (){
        val root = parseStringExpression("mod(0.0, second)")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(0.0)", root.toString())
    }

    @Test
    fun testModNullByNull (){
        val root = parseStringExpression("mod(0.0, 0.0)")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(0.0)", root.toString())
    }

    @Test
    fun testModByNull (){
        val root = parseStringExpression("mod(1.0, 0.0)")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(NaN)", root.toString())
    }

    @Test
    fun testModVars (){
        val root = parseStringExpression("mod(f, s)")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals("(mod(f;s))", root.toString())
    }

    @Test
    fun testSin (){
        val root = parseStringExpression("sin(1.5)")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals(true, root.toString().startsWith("(0.9974949"))
    }

    @Test
    fun testCos (){
        val root = parseStringExpression("cos(1.5)")
        baseOperationsDefinitions.applyOperationToExpressionNode(root.children[0])
        assertEquals(true, root.toString().startsWith("(0.070737"))
    }

    @Test
    fun testExpressionTreeComputation (){
        val root = parseStringExpression("cos(0)*6^2+tg(0)+ln(exp(2))")
        baseOperationsDefinitions.computeExpressionTree(root.children[0])
        assertEquals("(38.0)", root.toString())
    }

    @Test
    fun testBoolean (){
        val root = parseStringExpression("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>a</mi><mo>&amp;</mo><mi>b</mi><mo>&amp;</mo><mn>0</mn><mo>)</mo><mo>|</mo><mi>a</mi><mo>|</mo><mo>(</mo><mi>x</mi><mi>o</mi><mi>r</mi><mo>(</mo><mn>1</mn><mo>,</mo><mn>0</mn><mo>,</mo><mn>1</mn><mo>,</mo><mn>0</mn><mo>)</mo><mo>)</mo><mo>|</mo><mo>(</mo><mi>a</mi><mi>l</mi><mi>l</mi><mi>e</mi><mi>q</mi><mo>(</mo><mn>1</mn><mo>,</mo><mi>n</mi><mi>o</mi><mi>t</mi><mo>(</mo><mn>0</mn><mo>)</mo><mo>,</mo><mi>b</mi><mo>)</mo><mo>)</mo></math>")
        baseOperationsDefinitions.computeExpressionTree(root.children[0])
        assertEquals("(or(a;alleq(b;1.0)))", root.toString())
    }

    @Test
    fun testSumN (){
        val root = parseStringExpression("S(i,n*0,4-2,i^2)")
        baseOperationsDefinitions.computeExpressionTree(root.children[0])
        assertEquals("(5.0)", root.toString())
    }

    @Test
    fun testProdN (){
        val root = parseStringExpression("P(i,S(j,-4,-8,n),S(j,1,2,j),P(j,1,i,j))")
        baseOperationsDefinitions.computeExpressionTree(root.children[0])
        assertEquals("(12.0)", root.toString())
    }

    @Test
    fun testProdNwrongCounterNames (){
        val root = parseStringExpression("P(i,S(j,-4,-8,n),S(i,1,2,i),P(j,1,i,j))")
        baseOperationsDefinitions.computeExpressionTree(root.children[0])
        assertEquals("(12.0)", root.toString())
    }

    @Test
    fun testProdNull (){
        val root = parseStringExpression("P(i,-6,4/2,i^2*n)")
        baseOperationsDefinitions.computeExpressionTree(root.children[0])
        assertEquals("(0.0)", root.toString())
    }

    @Test
    fun testProdNullFractions (){
        val root = parseStringExpression("P(i,-6,4.6/2.3,i^2*n)")
        baseOperationsDefinitions.computeExpressionTree(root.children[0])
        assertEquals("(0.0)", root.toString())
    }
}