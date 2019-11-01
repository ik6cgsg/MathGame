import com.twf.baseoperations.BaseOperationsDefinitions
import com.twf.config.CompiledConfiguration
import com.twf.expressiontree.*
import org.junit.Test
import substitutiontests.parseStringExpression
import kotlin.test.assertEquals

class CompareWithTreeTransformationTests {
    val compiledConfiguration = CompiledConfiguration()
    val expressionNodeConstructor = ExpressionNodeConstructor()
    val baseOperationsDefinitions = BaseOperationsDefinitions()
    val expressionComporator = compiledConfiguration.factComporator.expressionComporator
    val definedFunctionNameNumberOfArgsSet = compiledConfiguration.definedFunctionNameNumberOfArgsSet
    init {
        compiledConfiguration.comparisonSettings.compareExpressionsAndFactsWithProbabilityRules = false
        compiledConfiguration.compiledFunctionDefinitions.clear()
    }

    @Test
    fun correctUnm (){
        val left = parseStringExpression("U(n,m)")
        val right = parseStringExpression("n^m")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun incorrectUnm (){
        val left = parseStringExpression("A(n,m)")
        val right = parseStringExpression("n^m")
        assertEquals(false, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctSort (){
        val left = parseStringExpression("ab*abc*y*x*z*x")
        val right = parseStringExpression("x*abc*x*z*y*ab")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun applyAllSubstitutionsSimple (){
        val root = parseStringExpression("(n!)!")
        root.applyAllSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
        assertEquals(true, expressionComporator.compareAsIs(expressionNodeConstructor.construct("(P(i;1;P(i;1;n;i);i))"), root))
    }

    @Test
    fun applyAllSubstitutionsDouble (){
        val root = parseStringExpression("((n!)!)!")
        root.applyAllSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
        assertEquals(true, expressionComporator.compareAsIs(expressionNodeConstructor.construct("(P(i;1;P(i;1;P(i;1;n;i);i);i))"), root))
    }

    @Test
    fun replaceNonBaseFunctionsOnVariables (){
        val root = parseStringExpression("(f(g(f(a))-f(a))*f(a+b)f(b+a))+ g(f(a)) + (f(a))", true)
        val map = mutableMapOf<ExpressionNode, String>()
        root.normalizeSubTree(sorted = true)
        root.replaceNotDefinedFunctionsOnVariables(map, definedFunctionNameNumberOfArgsSet)
        assertEquals("(+(*(sys_def_var_replace_fun_0;sys_def_var_replace_fun_0;sys_def_var_replace_fun_3);sys_def_var_replace_fun_1;sys_def_var_replace_fun_2))", root.toString())
    }

    @Test
    fun replaceNonBaseFunctionsOnVariablesEmpty (){
        val root = parseStringExpression("()", true)
        val map = mutableMapOf<ExpressionNode, String>()
        root.normalizeSubTree(sorted = true)
        root.replaceNotDefinedFunctionsOnVariables(map, definedFunctionNameNumberOfArgsSet)
        assertEquals("()", root.toString())
        baseOperationsDefinitions.computeExpressionTree(root)
        assertEquals("0", root.toString())
    }

    @Test
    fun correctFactorial (){
        val left = parseStringExpression("(n!)!")
        val right = parseStringExpression("((n-1)!*n)!")
        left.applyAllSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
        right.applyAllSubstitutions(compiledConfiguration.compiledImmediateTreeTransformationRules)
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun correctCnmExpression (){
        val left = parseStringExpression("m*((m)!/ (m-n+1-1)!/(n)!)")
        val right = parseStringExpression("m*((m)!/ (m-n)!/(n)!)")
        assertEquals(true, expressionComporator.fullExpressionsCompare(left, right))
    }

    @Test
    fun correctCnm (){
        val left = parseStringExpression("m*C(m,n)")
        val right = parseStringExpression("m*((m)!/ (m-n)!/(n)!)")
        assertEquals("(*(m;C(m;n)))", left.toString())
        assertEquals("(*(m;/(factorial(m);factorial(+(m;-(n)));factorial(n))))", right.toString())
        assertEquals(true, expressionComporator.fullExpressionsCompare(left, right))
    }

    @Test
    fun allowedNoSubstitution (){
        val left = parseStringExpression("F(n+1)")
        val right = parseStringExpression("F(n) + F(n-2)")
        assertEquals("(F(+(n;1)))", left.toString())
        assertEquals("(+(F(n);F(+(n;-(2)))))", right.toString())
        assertEquals(false, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun allowedSubstitution (){
        val left = parseStringExpression("F(n+1)")
        val right = parseStringExpression("F(n) + F(n-1)")
        assertEquals("(F(+(n;1)))", left.toString())
        assertEquals("(+(F(n);F(+(n;-(1)))))", right.toString())
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun noSubstitution (){
        val left = parseStringExpression("F(n+1)")
        val right = parseStringExpression("F(n) + F(n-1)")
        assertEquals("(F(+(n;1)))", left.toString())
        assertEquals("(+(F(n);F(+(n;-(1)))))", right.toString())
        assertEquals(false, expressionComporator.compareWithoutSubstitutions(left, right))
    }

    @Test
    fun piRuleCorrect (){
        val left = parseStringExpression("(&#x3C0;)")
        val right = parseStringExpression("(pi)")
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }

    @Test
    fun factorialSubstitution (){
        val left = parseStringExpression("<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>m</mi><mo>!</mo></math>")
        val right = parseStringExpression("<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>m</mi><mo>*</mo><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo></math>")
        assertEquals("(factorial(m))", left.toString())
        assertEquals("(*(m;factorial(+(m;-(1)))))", right.toString())
        assertEquals(true, expressionComporator.compareWithTreeTransformationRules(left, right, compiledConfiguration.compiledExpressionTreeTransformationRules))
    }
}