package com.twf.tasksolutions

import com.twf.api.*
import com.twf.config.CompiledConfiguration
import com.twf.config.FactTransformationRule
import com.twf.config.FunctionConfiguration
import com.twf.factstransformations.FactConstructorViewer
import com.twf.factstransformations.FactSubstitution
import com.twf.factstransformations.MainLineAndNode
import com.twf.factstransformations.parseFromFactIdentifier
import com.twf.logs.log
import com.twf.mainpoints.checkFactsInMathML
import com.twf.mainpoints.compiledConfigurationBySettings
import com.twf.mainpoints.configSeparator
import com.twf.org.junit.Assert
import com.twf.org.junit.Ignore
import com.twf.org.junit.Test

class SetTheory {
    val wellKnownFunctions = "" +
            "${configSeparator}0"

    val wellKnownFunctionsWithoutDegree = "" +
            "${configSeparator}0"

    val expressionTransformationRules = "" +
            "A\\/(A/\\B)${configSeparator}A${configSeparator}" +
            "A/\\(A\\/B)${configSeparator}A${configSeparator}" +
            "A\\/A${configSeparator}A${configSeparator}" +
            "A/\\A${configSeparator}A${configSeparator}" +
            "A\\/(B\\/C)${configSeparator}A\\/B\\/C${configSeparator}" +
            "A/\\(B/\\C)${configSeparator}A/\\B/\\C${configSeparator}" +
            "A\\/(B/\\C)${configSeparator}(A\\/B)/\\(A\\/C)${configSeparator}" +
            "A/\\(B\\/C)${configSeparator}(A/\\B)\\/(A/\\C)${configSeparator}" +
            "!(A/\\B)${configSeparator}!A\\/!B${configSeparator}" +
            "!(A\\/B)${configSeparator}!A/\\!B${configSeparator}" +
            "A->B${configSeparator}!A\\/B${configSeparator}" +
            "A\\B${configSeparator}A/\\!B${configSeparator}" +

            "A\\/B${configSeparator}B\\/A${configSeparator}" +
            "A/\\B${configSeparator}B/\\A${configSeparator}" +

            "!(!(A))${configSeparator}A"


    @Test
    fun asIsWithoutSubstitutions() {
        val result = compareWithoutSubstitutions(
                "!(C/\\A)/\\(B\\/!C)",
                "!(A/\\C)/\\(B\\/!C)",
                "setTheory"
        )
        Assert.assertEquals(true, result)
    }

    @Test
    fun substitutionApplicationTest (){
        val places = findSubstitutionPlacesCoordinatesInExpressionJSON (
                "A|A",
                "A",
                "B",
                scope = "setTheory",
                basedOnTaskContext = true
        )
        Assert.assertEquals("{\"substitutionPlaces\":[{\"parentStartPosition\":\"0\",\"parentEndPosition\":\"3\",\"startPosition\":\"0\",\"endPosition\":\"1\"},{\"parentStartPosition\":\"0\",\"parentEndPosition\":\"3\",\"startPosition\":\"2\",\"endPosition\":\"3\"}]}", places)
    }

    @Test
    fun wrongWithoutSubstitutions() {
        val result = compareWithoutSubstitutions(
                "!(B/\\A)/\\(B\\/!C)",
                "!(A/\\C)/\\(B\\/!C)",
                "setTheory"
        )
        Assert.assertEquals(false, result)
    }

    @Test
    fun LongConjunction() {
        val result = compareWithoutSubstitutions(
                "C/\\A/\\B/\\D",
                "C/\\A/\\(B/\\D)",
                "setTheory"
        )
        Assert.assertEquals(true, result)
    }

    @Test
    fun LongConjunctionWithPermutation() {
        val result = compareWithoutSubstitutions(
                "(C/\\B)/\\A/\\D",
                "D/\\(A/\\(B/\\C))",
                "setTheory"
        )
        Assert.assertEquals(true, result)
    }

    @Test
    @Ignore
    fun proveTaskSet1() {
        val result = checkFactsInMathML(
                brushedMathML = "(!A/\\B)\\/C=(!A\\/!C)/\\(B\\/!C)=!(C/\\A)/\\(B\\/!C)",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = expressionTransformationRules,
                targetFactIdentifier = "EXPRESSION_COMPARISON{(or(and(not(A);B);C))}{=}{(and(not(and(C;A));or(B;not(C))))}",
                targetVariablesNames = "",
                additionalFactsIdentifiers = "",
                scopeFilter = ";;;setTheory"
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
//        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mn>3</mn><mo>+</mo><mn>4</mn><mo>*</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mn>2</mn><mo>*</mo><mi>x</mi><mo>)</mo><mo>+</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mn>4</mn><mo>*</mo><mi>x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mn>3</mn><mo>+</mo><mn>4</mn><mo>*</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mn>2</mn><mo>*</mo><mi>x</mi><mo>)</mo><mo>+</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mn>2</mn><mo>*</mo><mo>(</mo><mn>2</mn><mo>*</mo><mi>x</mi><mo>)</mo><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mn>2</mn><mo>+</mo><mn>4</mn><mo>*</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mn>2</mn><mo>*</mo><mi>x</mi><mo>)</mo><mo>+</mo><mn>2</mn><mo>*</mo><mi>c</mi><mi>o</mi><msup><mi>s</mi><mn>2</mn></msup><mo>(</mo><mn>2</mn><mo>*</mo><mi>x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mn>2</mn><mo>(</mo><mn>1</mn><mo>+</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mn>2</mn><mo>*</mo><mi>x</mi><mo>)</mo><msup><mo>)</mo><mn>2</mn></msup><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mn>2</mn><mo>(</mo><mn>2</mn><mo>*</mo><msup><mi>cos</mi><mn>2</mn></msup><mo>(</mo><mi>x</mi><mo>)</mo><msup><mo>)</mo><mn>2</mn></msup><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mn>8</mn><mo>*</mo><mo>(</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mi>x</mi><mrow><mo>)</mo><msup><mo>)</mo><mn>4</mn></msup></mrow></math>", result)
    }

    @Test
    fun proveTaskSet2() {
        val result = checkFactsInMathML(
                brushedMathML = "(!A/\\B)\\/C=(!A\\/!C)/\\(B)=!(C/\\A)/\\(B\\/!C)",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = expressionTransformationRules,
                targetFactIdentifier = "EXPRESSION_COMPARISON{(or(and(not(A);B);C))}{=}{(and(not(and(C;A));or(B;not(C))))}",
                targetVariablesNames = "",
                additionalFactsIdentifiers = "",
                scopeFilter = ";;;setTheory"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
    }

    @Test
    @Ignore
    fun oldIdentifierToNew() {
        val functionConfiguration = FunctionConfiguration(setOf("", "setTheory"))
        val factConstructorViewer: FactConstructorViewer = FactConstructorViewer(compiledConfiguration = CompiledConfiguration(functionConfiguration = functionConfiguration))
        val fact = parseFromFactIdentifier("(!A/\\B)\\/C;ec;=;ec;!(C/\\A)/\\(B\\/!C)", functionConfiguration = functionConfiguration)!!
        val newIdentifier = factConstructorViewer.constructIdentifierByFact(fact)
        print(newIdentifier)
    }
}