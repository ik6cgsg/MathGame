package expressionstests

import com.twf.api.compareWithoutSubstitutions
import com.twf.config.CompiledConfiguration
import org.junit.Assert
import org.junit.Test

class ProbabilityComparisonFewDomainTests {
    val compiledConfiguration: CompiledConfiguration

    init {
        compiledConfiguration = CompiledConfiguration()
        compiledConfiguration.comparisonSettings.useOldSimpleProbabilityTesting = false
    }

    @Test
    fun compareTestOverlappingWrong() {
        val result = compareWithoutSubstitutions(
                "sqrt(x)*sqrt(0.2-x)*ln(x)/sqrt(0.2-x)",
                "sqrt(x)*sqrt(0.1-x)*ln(x)/sqrt(0.1-x)",
                compiledConfiguration
        )
        Assert.assertEquals(true, result)
    }

    @Test
    fun test10() {
        val result = compareWithoutSubstitutions(
                "(-((-asin(tg(x)-0.56))+((0.15)^0.5)))",
                "(acos(tg(ctg(x))))",
                compiledConfiguration
        )
        Assert.assertEquals(false, result)
    }

    @Test
    fun test18() {
        val result = compareWithoutSubstitutions(
                "asin(-ctg(ln(x)))",
                "(tg(-((-x)+asin(ctg(0.1))))-0.91)",
                compiledConfiguration
        )
        Assert.assertEquals(false, result)
    }

    @Test
    fun test1() {
        val result = compareWithoutSubstitutions(
                "(((((((((tg(x)^0.5)+tg(x))+0.84)+0.36))^0.5))+0.7)-0.44)",
                "tg(-(-((acos(y-x-x+x)+asin(ctg(0.51)))-0.52)))",
                compiledConfiguration
        )
        Assert.assertEquals(false, result)
    }

    @Test
    fun test2() {
        val result = compareWithoutSubstitutions(
                "(ln(-(-(y+(y^0.5)-ln(y)+y)))+ln(0.93))",
                "(-((-asin(asin(((-(x+ln(x)))^0.5))))+0.25))",
                compiledConfiguration
        )
        Assert.assertEquals(false, result)
    }

    @Test
    fun test3() {
        val result = compareWithoutSubstitutions(
                "((-(((-ln(y+x+asin(tg(x))))-0.85)-0.74))+ctg(0.71))",
                "((tg(((y+(ln(y)^0.5))-0.59)))+((0.72)^0.5))",
                compiledConfiguration
        )
        Assert.assertEquals(false, result)
    }
}