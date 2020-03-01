package com.twf.tasksolutions

import com.twf.factstransformations.FactConstructorViewer
import com.twf.factstransformations.parseFromFactIdentifier
import com.twf.logs.log
import com.twf.mainpoints.checkFactsInMathML
import com.twf.mainpoints.compiledConfigurationBySettings
import com.twf.mainpoints.configSeparator
import com.twf.org.junit.Assert
import com.twf.org.junit.Ignore
import com.twf.org.junit.Test

class Physics {
    val wellKnownFunctions = "" +
            "${configSeparator}0$configSeparator" +
            "${configSeparator}1$configSeparator" +
            "+$configSeparator-1$configSeparator" +
            "-$configSeparator-1$configSeparator" +
            "*$configSeparator-1$configSeparator" +
            "/$configSeparator-1$configSeparator" +
            "^$configSeparator-1"

    val wellKnownFunctionsWithoutDegree = "" +
            "${configSeparator}0$configSeparator" +
            "${configSeparator}1$configSeparator" +
            "+$configSeparator-1$configSeparator" +
            "-$configSeparator-1$configSeparator" +
            "*$configSeparator-1$configSeparator" +
            "/$configSeparator-1"

    val expressionTransformationRules = "" +
            "U${configSeparator}I*R$configSeparator" +
            "A${configSeparator}U*q$configSeparator" +
            "R${configSeparator}p*l/S"

    val quantumExpressionTransformationRules = "E;;;h*v;;;v;;;c/l;;;lambda;;;l;;;Plank_constant;;;h;;;light_speed;;;c";

    @Test
    fun testQuantum() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mi>l</mi><mi>a</mi><mi>m</mi><mi>b</mi><mi>d</mi><mi>a</mi><mo>=</mo><mi>l</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>P</mi><mi>l</mi><mi>a</mi><mi>n</mi><mi>k</mi><mi>_</mi><mi>c</mi><mi>o</mi><mi>n</mi><mi>s</mi><mi>tan</mi><mi>t</mi><mo>=</mo><mi>h</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>l</mi><mi>i</mi><mi>g</mi><mi>h</mi><mi>t</mi><mi>_</mi><mi>s</mi><mi>p</mi><mi>e</mi><mi>e</mi><mi>d</mi><mo>=</mo><mi>c</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi>E</mi><mo>=</mo><mi>h</mi><mo>*</mo><mi>v</mi><mo>=</mo><mfrac><mrow><mi>h</mi><mo>*</mo><mi>c</mi></mrow><mi>l</mi></mfrac></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "",
                targetVariablesNames = "E",
                additionalFactsIdentifiers = "",
                unlimitedWellKnownFunctions = wellKnownFunctionsWithoutDegree,
                taskContextExpressionTransformationRules = quantumExpressionTransformationRules,
                allowedVariablesNames = "h;;;c;;;l"
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mo>[</mo><mi>l</mi><mi>a</mi><mi>m</mi><mi>b</mi><mi>d</mi><mi>a</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>l</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>P</mi><mi>l</mi><mi>a</mi><mi>n</mi><mi>k</mi><mi>_</mi><mi>c</mi><mi>o</mi><mi>n</mi><mi>s</mi><mi>tan</mi><mi>t</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>h</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>l</mi><mi>i</mi><mi>g</mi><mi>h</mi><mi>t</mi><mi>_</mi><mi>s</mi><mi>p</mi><mi>e</mi><mi>e</mi><mi>d</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>c</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi>E</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>h</mi><mo>*</mo><mi>v</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mfrac><mrow><mi>h</mi><mo>*</mo><mi>c</mi></mrow><mi>l</mi></mfrac></math>",
                result)
    }


    @Test
    fun testQuantumWrong() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mi>l</mi><mi>a</mi><mi>m</mi><mi>b</mi><mi>d</mi><mi>a</mi><mo>=</mo><mi>l</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>P</mi><mi>l</mi><mi>a</mi><mi>n</mi><mi>k</mi><mi>_</mi><mi>c</mi><mi>o</mi><mi>n</mi><mi>s</mi><mi>tan</mi><mi>t</mi><mo>=</mo><mi>h</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>l</mi><mi>i</mi><mi>g</mi><mi>h</mi><mi>t</mi><mi>_</mi><mi>s</mi><mi>p</mi><mi>e</mi><mi>e</mi><mi>d</mi><mo>=</mo><mi>c</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi>E</mi><mo>=</mo><mi>h</mi><mo>*</mo><mi>c</mi><mo>=</mo><mfrac><mrow><mi>h</mi><mo>*</mo><mi>c</mi></mrow><mi>l</mi></mfrac></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "",
                targetVariablesNames = "E",
                additionalFactsIdentifiers = "",
                unlimitedWellKnownFunctions = wellKnownFunctionsWithoutDegree,
                taskContextExpressionTransformationRules = quantumExpressionTransformationRules,
                allowedVariablesNames = "h;;;c;;;l"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mo>[</mo><mi>l</mi><mi>a</mi><mi>m</mi><mi>b</mi><mi>d</mi><mi>a</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>l</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>P</mi><mi>l</mi><mi>a</mi><mi>n</mi><mi>k</mi><mi>_</mi><mi>c</mi><mi>o</mi><mi>n</mi><mi>s</mi><mi>tan</mi><mi>t</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>h</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>l</mi><mi>i</mi><mi>g</mi><mi>h</mi><mi>t</mi><mi>_</mi><mi>s</mi><mi>p</mi><mi>e</mi><mi>e</mi><mi>d</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>c</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi>E</mi><mrow mathvariant=\"bold\" mathcolor=\"#FF0000\"><mo>=</mo></mrow><mi>h</mi><mo>*</mo><mi>c</mi><mo>=</mo><mfrac><mrow><mi>h</mi><mo>*</mo><mi>c</mi></mrow><mi>l</mi></mfrac><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: Unclear transformation between '(E)' and '(*(h;c))' </mtext></math>",
                result)
    }


    @Test
    fun testOmmLaw() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>I</mi><mo>=</mo><mfrac><mi>U</mi><mi>R</mi></mfrac><mo>=</mo><mfrac><mrow><mi>U</mi><mo>*</mo><mi>S</mi></mrow><mrow><mi>p</mi><mo>*</mo><mi>l</mi></mrow></mfrac></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "EXPRESSION_COMPARISON{(I)}{=}{(/(*(U;S);*(p;l)))}",
                targetVariablesNames = "",
                additionalFactsIdentifiers = "",
                unlimitedWellKnownFunctions = wellKnownFunctionsWithoutDegree,
                taskContextExpressionTransformationRules = expressionTransformationRules
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi>I</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mfrac><mi>U</mi><mi>R</mi></mfrac><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mfrac><mrow><mi>U</mi><mo>*</mo><mi>S</mi></mrow><mrow><mi>p</mi><mo>*</mo><mi>l</mi></mrow></mfrac></math>",
                result)
    }


    @Test
    fun testOmmLawLongWorkInBrowser() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mi>U</mi><mo>=</mo><mi>U</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>l</mi><mo>=</mo><mi>l</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>p</mi><mo>=</mo><mi>p</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>S</mi><mo>=</mo><mi>S</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">I</mi><mo>=</mo><mi>U</mi><mo>/</mo><mi>R</mi><mo>=</mo><mfrac><mrow><mi>U</mi><mo>*</mo><mi>S</mi></mrow><mrow><mi>l</mi><mo>*</mo><mi>p</mi></mrow></mfrac></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "EXPRESSION_COMPARISON{(I)}{=}{(/(*(U;S);*(p;l)))}",
                targetVariablesNames = "",
                additionalFactsIdentifiers = "",
                unlimitedWellKnownFunctions = wellKnownFunctionsWithoutDegree,
                taskContextExpressionTransformationRules = expressionTransformationRules
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mo>[</mo><mi>U</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>U</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>l</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>l</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>p</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>p</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>S</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>S</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">I</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>U</mi><mo>/</mo><mi>R</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mfrac><mrow><mi>U</mi><mo>*</mo><mi>S</mi></mrow><mrow><mi>l</mi><mo>*</mo><mi>p</mi></mrow></mfrac></math>",
                result)
    }


    @Test
    fun testOmmLawNotCompleteSolution() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mi>U</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>l</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>p</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>S</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi>I</mi><mo>=</mo><mi>U</mi><mo>/</mo><mi>R</mi><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Syntax&#xA0;error&#xA0;(underlined): Unexpected: '.'</mtext></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "",
                targetVariablesNames = "I",
                additionalFactsIdentifiers = "",
                unlimitedWellKnownFunctions = wellKnownFunctionsWithoutDegree,
                taskContextExpressionTransformationRules = expressionTransformationRules,
                allowedVariablesNames = "U;;;S;;;l;;;p"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mo>[</mo><mi>U</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>l</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>p</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>S</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi>I</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>U</mi><mo>/</mo><mi>R</mi><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: right part of the result contains unknown variables: 'R'</mtext></math>",
                result)
    }

    @Test
    fun testOmmLawError() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>I</mi><mo>=</mo><mi>U</mi><mo>*</mo><mi>R</mi><mo>=</mo><mfrac><mrow><mi>U</mi><mo>*</mo><mi>S</mi></mrow><mrow><mi>p</mi><mo>*</mo><mi>l</mi></mrow></mfrac></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "EXPRESSION_COMPARISON{(I)}{=}{(/(*(U;S);*(p;l)))}",
                targetVariablesNames = "",
                additionalFactsIdentifiers = "",
                unlimitedWellKnownFunctions = wellKnownFunctionsWithoutDegree,
                taskContextExpressionTransformationRules = expressionTransformationRules
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi>I</mi><mrow mathvariant=\"bold\" mathcolor=\"#FF0000\"><mo>=</mo></mrow><mi>U</mi><mo>*</mo><mi>R</mi><mo>=</mo><mfrac><mrow><mi>U</mi><mo>*</mo><mi>S</mi></mrow><mrow><mi>p</mi><mo>*</mo><mi>l</mi></mrow></mfrac><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: Unclear transformation between '(I)' and '(*(U;R))' </mtext></math>",
                result)
    }

    @Test
    @Ignore
    fun oldIdentifierToNew() {
        val factConstructorViewer: FactConstructorViewer = FactConstructorViewer()
        val fact = parseFromFactIdentifier("I;ec;=;ec;U*S/(p*l)")!!
        val newIdentifier = factConstructorViewer.constructIdentifierByFact(fact)
        print(newIdentifier)
    }
}