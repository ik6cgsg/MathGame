package expressionstests;

import com.twf.api.compareWithoutSubstitutions
import com.twf.config.ComparisonType
import com.twf.config.CompiledConfiguration
import org.junit.Ignore
import org.junit.Test
import substitutiontests.parseStringExpression
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpressionComparisonTest {
    @Test
    fun testEqualTrigonometricExpressionsCorrect() {
        val compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.EQUAL
        val result = compareWithoutSubstitutions(
                "2 * sin(x) * cos(x)",
                "sin(2x)",
                compiledConfiguration
        )
        assertTrue(result)
    }

    @Test
    fun testEqualTrigonometricExpressionsWrong() {
        val compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.EQUAL
        val result = compareWithoutSubstitutions(
                "2 * sin(x) * cos(x)",
                "cos(2x)",
                compiledConfiguration
        )
        assertFalse(result)
    }

    @Test
    fun testFunctionWithSmallDomainOfDefinition() {
        val compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.EQUAL
        compiledConfiguration.comparisonSettings.useOldSimpleProbabilityTesting = false
        val result = compareWithoutSubstitutions(
                "ln(1 - sqrt(x)) + ln(y)",
                "ln(y * (1 - sqrt(x)))",
                compiledConfiguration
        )
        assertTrue(result)
    }

    @Test
    fun testSimpleAndComplexEqual() {
        val compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.EQUAL
        compiledConfiguration.comparisonSettings.useOldSimpleProbabilityTesting = false
        val result = compareWithoutSubstitutions(
                "x + y",
                "(x + y) * ((sin(x))^2 + (cos(x))^2) * sqrt(-x^2) / sqrt(-x^2)",
                compiledConfiguration
        )
        assertTrue(result)
    }

    @Test
    fun testPrimitiveEquality() {
        val compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.EQUAL
        var result = compareWithoutSubstitutions(
                "x + y",
                "-(-x - y)",
                compiledConfiguration
        )
        assertTrue(result)

        result = compareWithoutSubstitutions(
                "x * (1 / y)",
                "x / y",
                compiledConfiguration
        )
        assertTrue(result)
    }

    // This test does not work and difference between left and right is always 2 * PI * i or more
    @Test
    @Ignore
    fun testSumOfThreeLogsWithEmptyDomain() {
        val compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.EQUAL
        compiledConfiguration.comparisonSettings.useOldSimpleProbabilityTesting = false
        val result = compareWithoutSubstitutions(
                "ln(-sqrt(x)) + ln(-y^2) + ln(-z^2)",
                "ln((y * z)^2 * (-sqrt(x)))",
                compiledConfiguration
        )
        assertTrue(result)
    }

    @Test
    fun testFastTestOnEquality() {
        val left = parseStringExpression("sqrt(x * y)")
        val right = parseStringExpression("sqrt(x) * sqrt(y)")
        val compiledConfiguration = CompiledConfiguration()

        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.EQUAL
        compiledConfiguration.comparisonSettings.useOldSimpleProbabilityTesting = false

        assertTrue(compiledConfiguration.factComporator
                .expressionComporator.probabilityTestComparison(left, right))
    }

    // Approximately 11 out of 24 tests pass
    @Test
    fun testSumOfThreeLogsWithSmallDomain() {
        val compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.EQUAL
        compiledConfiguration.comparisonSettings.useOldSimpleProbabilityTesting = false
        val result = compareWithoutSubstitutions(
                "ln(1 - sqrt(x)) + ln(y) + ln(z)",
                "ln(z * y * (1 - sqrt(x)))",
                compiledConfiguration
        )
        assertTrue(result)
    }

    // Approximately 12 out of 14 tests pass
    @Test
    fun testSumOfThreeLogsWithBigDomain() {
        val compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.EQUAL
        compiledConfiguration.comparisonSettings.useOldSimpleProbabilityTesting = false
        val result = compareWithoutSubstitutions(
                "ln(x) + ln(y) + ln(z)",
                "ln(z * y * x)",
                compiledConfiguration
        )
        assertTrue(result)
    }

    @Test
    @Ignore //TODO solve problem with n-placement functions
    fun testFactorialExpansion() {
        val compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.defaultComparisonType = ComparisonType.EQUAL
        compiledConfiguration.comparisonSettings.useOldSimpleProbabilityTesting = false
        val result = compareWithoutSubstitutions(
                "m!/(m-n)!",
                "m*(m-1)!/(m-n)!",
                compiledConfiguration
        )
        assertTrue(result)
    }
}
