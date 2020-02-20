package com.twf.mainpoints

import com.twf.logs.log
import com.twf.org.junit.Test

import com.twf.org.junit.Assert.*
import com.twf.org.junit.Ignore

class CheckFactInMathMLKtTest {
    @Test
    fun proveTaskCorrectSolutionRulesDeclaredOnUpLevel() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mi>U</mi><mo>=</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>A</mi><mo>=</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi>V</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>,</mo><mi>b</mi><mo>)</mo><mo>=</mo><mi>P</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mo>=</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>*</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>+</mo><mi>b</mi><mo>&#xA0;</mo><mo>=</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>*</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mo>=</mo><mi>A</mi><mo>*</mo><mi>A</mi><mo>+</mo><mi>b</mi></math>",//"<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>V</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>,</mo><mi>b</mi><mo>)</mo><mo>=</mo><mi>P</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mo>=</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>*</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>+</mo><mi>b</mi><mo>&#xA0;</mo><mo>=</mo><mo>[</mo><mi>U</mi><mo>=</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>]</mo><mo>=</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>*</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mo>=</mo><mo>[</mo><mi>A</mi><mo>=</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>]</mo><mo>=</mo><mi>A</mi><mo>*</mo><mi>A</mi><mo>+</mo><mi>b</mi></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "P(x)${configSeparator}x*x${configSeparator}V(a,b)${configSeparator}P(a)+b",
                targetFactIdentifier = "EXPRESSION_COMPARISON{(+(*(A;A);b))}{=}{(V(*(I;R;q);b))}",
                targetVariablesNames = "",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))};;;EXPRESSION_COMPARISON{(A)}{=}{(*(U;q))}"
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mo>[</mo><mi>U</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>I</mi><mo>*</mo><mi>R</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>A</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>U</mi><mo>*</mo><mi>q</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi>V</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>,</mo><mi>b</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>P</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>*</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>+</mo><mi>b</mi><mo>&#xA0;</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>*</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>A</mi><mo>*</mo><mi>A</mi><mo>+</mo><mi>b</mi></math>", result)
    }

    @Test
    fun proveTaskCorrectSolutionRulesDeclaredInExpressionChain() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi>V</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>,</mo><mi>b</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>P</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>*</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>+</mo><mi>b</mi><mo>&#xA0;</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>[</mo><mi>U</mi><mo>=</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>]</mo><mo>=</mo></mrow><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>*</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>[</mo><mi>A</mi><mo>=</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>]</mo><mo>=</mo></mrow><mi>A</mi><mo>*</mo><mi>A</mi><mo>+</mo><mi>b</mi></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "P(x)${configSeparator}x*x${configSeparator}V(a,b)${configSeparator}P(a)+b",
                targetFactIdentifier = "EXPRESSION_COMPARISON{(V(*(I;R;q);b))}{=}{(+(*(A;A);b))}",
                targetVariablesNames = "",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(A)}{=}{(*(U;q))};;;EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))}"
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi>V</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>,</mo><mi>b</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>P</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>*</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>+</mo><mi>b</mi><mo>&#xA0;</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>[</mo><mi>U</mi><mo>=</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>]</mo><mo>=</mo></mrow><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>*</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>[</mo><mi>A</mi><mo>=</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>]</mo><mo>=</mo></mrow><mi>A</mi><mo>*</mo><mi>A</mi><mo>+</mo><mi>b</mi></math>", result)
    }

    @Test
    fun proveTaskWrongSolutionRulesDeclaredInExpressionChainAreIncorrect() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow mathcolor=\"#7F00FF\"><mi>V</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>,</mo><mi>b</mi><mo>)</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mi>P</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>*</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>+</mo><mi>b</mi><mo>&#xA0;</mo><mo>=</mo><mo>[</mo><mi>U</mi><mo mathvariant=\"bold\" mathcolor=\"#FF0000\">=</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>]</mo><mo>=</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>*</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mo>=</mo><mo>[</mo><mi>A</mi><mo>=</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>]</mo><mo>=</mo><mi>A</mi><mo>*</mo><mi>A</mi><mo>+</mo><mi>b</mi></mrow></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "P(x)${configSeparator}x*x${configSeparator}V(a,b)${configSeparator}P(a)+b",
                targetFactIdentifier = "EXPRESSION_COMPARISON{(+(*(A;A);b))}{=}{(V(*(I;R;q);b))}",
                targetVariablesNames = "",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(R;R))};;;EXPRESSION_COMPARISON{(A)}{=}{(*(U;q))}"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi>V</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>,</mo><mi>b</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>P</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>*</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>+</mo><mi>b</mi><mo>&#xA0;</mo><mo>=</mo><mo>[</mo><mi>U</mi><mrow mathvariant=\"bold\" mathcolor=\"#FF0000\"><mo>=</mo></mrow><mi>I</mi><mo>*</mo><mi>R</mi><mo>]</mo><mo>=</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>*</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mo>=</mo><mo>[</mo><mi>A</mi><mo>=</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>]</mo><mo>=</mo><mi>A</mi><mo>*</mo><mi>A</mi><mo>+</mo><mi>b</mi><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: RULE VERIFICATION_FAILED</mtext></math>", result)
    }

    @Test
    fun proveTaskWrongSolution() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow mathcolor=\"#7F00FF\"><mi>V</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>,</mo><mi>b</mi><mo>)</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mi>P</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>*</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>+</mo><mi>b</mi><mo>&#xA0;</mo><mrow mathcolor=\"#FF00FF\"><mo mathvariant=\"bold\">=</mo><mo mathvariant=\"bold\">[</mo><mi mathvariant=\"bold\">U</mi><mo mathvariant=\"bold\">=</mo><mi mathvariant=\"bold\">I</mi><mo mathvariant=\"bold\">*</mo><mi mathvariant=\"bold\">R</mi><mo mathvariant=\"bold\">]</mo><mo mathvariant=\"bold\">=</mo></mrow><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>*</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>=</mo><mo>[</mo><mi>A</mi><mo>=</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>]</mo><mo>=</mo><mi>A</mi><mo>*</mo><mi>A</mi><mo>+</mo><mi>b</mi></mrow></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "P(x)${configSeparator}x*x${configSeparator}V(a,b)${configSeparator}P(a)+b",
                targetFactIdentifier = "EXPRESSION_COMPARISON{(+(*(A;A);b))}{=}{(V(*(I;R;q);b))}",
                targetVariablesNames = "",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))};;;EXPRESSION_COMPARISON{(A)}{=}{(*(U;q))}"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi>V</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>,</mo><mi>b</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>P</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>*</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>+</mo><mi>b</mi><mo>&#xA0;</mo><mrow mathvariant=\"bold\" mathcolor=\"#FF00FF\"><mo>=</mo><mo>[</mo><mi>U</mi><mo>=</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>]</mo><mo>=</mo></mrow><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>*</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>=</mo><mo>[</mo><mi>A</mi><mo>=</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>]</mo><mo>=</mo><mi>A</mi><mo>*</mo><mi>A</mi><mo>+</mo><mi>b</mi><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: Unclear transformation between '(+(*(*(I;R);q;*(I;R);q);b))' and '(*(*(U;q);*(U;q)))' even with rule</mtext></math>", result)
    }

    @Test
    fun proveTaskWrongSolutionTarget() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>[</mo><mi>U</mi><mo mathcolor=\"#007F00\">=</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>]</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>A</mi><mo mathcolor=\"#007F00\">=</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>]</mo><mspace linebreak=\"newline\"/><mi>V</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>,</mo><mi>b</mi><mo>)</mo><mo mathcolor=\"#007F00\">=</mo><mi>P</mi><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi><mo mathcolor=\"#007F00\">=</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>*</mo><mo>(</mo><mi>I</mi><mo>*</mo><mi>R</mi><mo>)</mo><mo>*</mo><mi>q</mi><mo>+</mo><mi>b</mi><mo>&#xA0;</mo><mo mathcolor=\"#007F00\">=</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>*</mo><mo>(</mo><mi>U</mi><mo>*</mo><mi>q</mi><mo>)</mo><mo>+</mo><mi>b</mi></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "P(x)${configSeparator}x*x${configSeparator}V(a,b)${configSeparator}P(a)+b",
                targetFactIdentifier = "EXPRESSION_COMPARISON{(+(*(A;A);b))}{=}{(V(*(I;R;q);b))}",
                targetVariablesNames = "",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))};;;EXPRESSION_COMPARISON{(A)}{=}{(*(U;q))}"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
    }

    @Test
    fun expressTaskCorrectSolution() {
        val result = checkFactsInMathML(
                brushedMathML = "<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>=</mo><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mo>[</mo><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mo>(</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>!</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>!</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>*</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">&gt;</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: ERROR: right part of the result contains unknown variables: 'x'</mtext></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "",
                targetFactIdentifier = "",
                targetVariablesNames = "x",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))};;;EXPRESSION_COMPARISON{(C(+(x;1);x))}{=}{(k)}"
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">k</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>&gt;</mo></mrow><mspace linebreak=\"newline\"/><mo>[</mo><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mo>(</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>!</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>!</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>*</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>&gt;</mo></mrow><mspace linebreak=\"newline\"/><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>&gt;</mo></mrow><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn></math>",
                result)
    }

    @Test
    fun expressTaskCorrectSolutionNotPainedSequenceSignCase() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>=</mo><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mo>[</mo><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mo>(</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>!</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>!</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>*</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>/</mo><mo>*</mo><mi>comment</mi><mo>*</mo><mo>/</mo><mspace linebreak=\"newline\"/><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mo mathvariant=\"bold\">=</mo><mo mathvariant=\"bold\">&gt;</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "",
                targetFactIdentifier = "",
                targetVariablesNames = "x",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))};;;EXPRESSION_COMPARISON{(C(+(x;1);x))}{=}{(k)}"
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">k</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>&gt;</mo></mrow><mspace linebreak=\"newline\"/><mo>[</mo><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mo>(</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>!</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>!</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>*</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>/</mo><mo>*</mo><mi>comment</mi><mo>*</mo><mo>/</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>&gt;</mo></mrow><mspace linebreak=\"newline\"/><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>&gt;</mo></mrow><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn></math>",
                result)
    }

    @Test
    fun allRulesUsing() {
        val result = checkFactsInMathML(
                brushedMathML = "<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>=</mo><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mo>[</mo><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mo>(</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>!</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>!</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>*</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">&gt;</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: ERROR: right part of the result contains unknown variables: 'x'</mtext></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = " ",
                targetFactIdentifier = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>A</mi><mo>(</mo><mi>m</mi><mo>,</mo><mi>n</mi><mo>)</mo><mo>=</mo><mfrac><mrow><mi>m</mi><mo>!</mo></mrow><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>)</mo><mo>!</mo></mrow></mfrac><mo>=</mo><mfrac><mrow><mi>m</mi><mo>*</mo><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo></mrow><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>)</mo><mo>!</mo></mrow></mfrac><mo>=</mo><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>&#xD7;</mo><mo>(</mo><mfrac><mrow><mi>m</mi><mo>-</mo><mi>n</mi></mrow><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>)</mo><mo>!</mo></mrow></mfrac><mo>+</mo><mfrac><mi>n</mi><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>)</mo><mo>!</mo></mrow></mfrac><mo>)</mo><mo>=</mo><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>&#xD7;</mo><mfenced><mrow><mfrac><mn>1</mn><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo></mrow></mfrac><mo>+</mo><mfrac><mi>n</mi><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>)</mo><mo>!</mo></mrow></mfrac></mrow></mfenced><mo>=</mo><mfrac><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo></mrow><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo></mrow></mfrac><mo>+</mo><mfrac><mrow><mi>n</mi><mo>*</mo><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo></mrow><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>)</mo><mo>!</mo></mrow></mfrac><mo>=</mo><mi>A</mi><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>,</mo><mi>n</mi><mo>)</mo><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>n</mi><mo>*</mo><mi>A</mi><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>,</mo><mi>n</mi><mo>-</mo><mn>1</mn><mo>)</mo></math>",
                targetVariablesNames = "x",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))};;;EXPRESSION_COMPARISON{(C(+(x;1);x))}{=}{(k)}"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mo>[</mo><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mo>(</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>!</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>!</mo><mrow mathvariant=\"bold\" mathcolor=\"#FF0000\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>*</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mo>=</mo><mo>&gt;</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: Unclear transformation between '(/(factorial(+(x;1));factorial(x);factorial(1)))' and '(/(factorial(+(x;1));factorial(x);1))' </mtext></math>",
                result)
    }

    @Test
    fun expressTaskWrongSolution() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi>C</mi><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi>x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>k</mi><mspace linebreak=\"newline\"/><mo>/</mo><mo>*</mo><mi>c</mi><mi>o</mi><mi>m</mi><mi>m</mi><mi>e</mi><mi>n</mi><mi>t</mi><mo>*</mo><mo>/</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>C</mi><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi>x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#FF0000\"><mo>=</mo></mrow><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi>x</mi><mo>!</mo><mo>=</mo><mi>x</mi><mo>!</mo><mo>*</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>/</mo><mi>x</mi><mo>!</mo><mo>=</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mi>x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi>k</mi><mspace linebreak=\"newline\"/><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mn>1</mn><mo>=</mo><mi>k</mi><mo>-</mo><mn>1</mn><mo>=</mo><mo>&gt;</mo><mspace linebreak=\"newline\"/><mi>x</mi><mo>=</mo><mi>k</mi><mo>-</mo><mn>1</mn><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: Unclear transformation between '(C(+(x;1);x))' and '(/(factorial(+(x;1));factorial(x)))' </mtext></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "",
                targetFactIdentifier = "",
                targetVariablesNames = "x",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))};;;EXPRESSION_COMPARISON{(C(+(x;1);x))}{=}{(k)}"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi>C</mi><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi>x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi>k</mi><mspace linebreak=\"newline\"/><mo>/</mo><mo>*</mo><mi>c</mi><mi>o</mi><mi>m</mi><mi>m</mi><mi>e</mi><mi>n</mi><mi>t</mi><mo>*</mo><mo>/</mo><mspace linebreak=\"newline\"/><mo>[</mo><mi>C</mi><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi>x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#FF0000\"><mo>=</mo></mrow><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi>x</mi><mo>!</mo><mo>=</mo><mi>x</mi><mo>!</mo><mo>*</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>/</mo><mi>x</mi><mo>!</mo><mo>=</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mi>x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi>k</mi><mspace linebreak=\"newline\"/><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mn>1</mn><mo>=</mo><mi>k</mi><mo>-</mo><mn>1</mn><mo>=</mo><mo>&gt;</mo><mspace linebreak=\"newline\"/><mi>x</mi><mo>=</mo><mi>k</mi><mo>-</mo><mn>1</mn><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: Unclear transformation between '(C(+(x;1);x))' and '(/(factorial(+(x;1));factorial(x)))' </mtext></math>",
                result)
    }

    @Test
    fun expressTaskWrongSolutionTargetUnexpressedVariable() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>=</mo><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">k</mi><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "",
                targetFactIdentifier = "",
                targetVariablesNames = "x",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))};;;EXPRESSION_COMPARISON{(C(+(x;1);x))}{=}{(k)}"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">k</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>&gt;</mo></mrow><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">k</mi><mo>&#xA0;</mo><mo>=</mo><mo>&#xA0;</mo><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: variable 'x' is not expressed</mtext></math>",
                result)
    }

    @Test
    fun expressTaskWrongSolutionTargetWronglyExpressedVariable() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>=</mo><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mo>[</mo><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mo>(</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>!</mo><mo>=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>!</mo><mo>=</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>*</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mo>=</mo><mo>&gt;</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mo>+</mo><mi>x</mi><mo>-</mo><mi>x</mi></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "",
                targetFactIdentifier = "",
                targetVariablesNames = "x",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))};;;EXPRESSION_COMPARISON{(C(+(x;1);x))}{=}{(k)}"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">k</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>&gt;</mo></mrow><mspace linebreak=\"newline\"/><mo>[</mo><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mo>(</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>!</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mo>!</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>*</mo><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>/</mo><mi mathvariant=\"normal\">x</mi><mo>!</mo><mo>/</mo><mn>1</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>]</mo><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>&gt;</mo></mrow><mspace linebreak=\"newline\"/><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>-</mo><mn>1</mn><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>&gt;</mo></mrow><mspace linebreak=\"newline\"/><mi mathvariant=\"normal\">x</mi><mo>=</mo><mi mathvariant=\"normal\">k</mi><mo>-</mo><mn>1</mn><mo>+</mo><mi>x</mi><mo>-</mo><mi>x</mi><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: right part of the result contains unknown variables: 'x'</mtext></math>",
                result)
    }

    @Test
    fun expressTaskWrongSolutionTargetFunctionInLeftPart() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mo>=</mo><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: variable 'x' is not expressed</mtext></math>",
                wellKnownFunctions = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1",
                expressionTransformationRules = "",
                targetFactIdentifier = "",
                targetVariablesNames = "x",
                additionalFactsIdentifiers = "EXPRESSION_COMPARISON{(U)}{=}{(*(I;R))};;;EXPRESSION_COMPARISON{(C(+(x;1);x))}{=}{(k)}"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi mathvariant=\"normal\">C</mi><mo>(</mo><mi mathvariant=\"normal\">x</mi><mo>+</mo><mn>1</mn><mo>,</mo><mi mathvariant=\"normal\">x</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mi mathvariant=\"normal\">k</mi><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: variable 'x' is not expressed</mtext></math>",
                result)
    }

    @Test
    fun correctChainNoExpressionsLimitationsProbabitityCheck() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>sin</mi><mo>(</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo>)</mo><mo>=</mo><mo>(</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>+</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>y</mi><mo>)</mo><mo>)</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>cos</mi><mo>(</mo><mi>y</mi><mo>)</mo><mo>=</mo><mo>(</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>+</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>y</mi><mo>)</mo><mo>)</mo><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo>)</mo><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>sin</mi><mo>(</mo><mi>y</mi><mo>)</mo><mo>)</mo></math>",
                wellKnownFunctions = "",
                expressionTransformationRules = "",
                targetFactIdentifier = "",
                targetVariablesNames = "",
                additionalFactsIdentifiers = ""
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi>sin</mi><mo>(</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>+</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>y</mi><mo>)</mo><mo>)</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>cos</mi><mo>(</mo><mi>y</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>+</mo><mi>t</mi><mi>g</mi><mo>(</mo><mi>y</mi><mo>)</mo><mo>)</mo><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo>)</mo><mo>&#xA0;</mo><mo>+</mo><mo>&#xA0;</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>sin</mi><mo>(</mo><mi>y</mi><mo>)</mo><mo>)</mo></math>",
                result)
    }

    @Test
    fun proveTaskCorrectSolutionPart() {
        val result = checkFactsInMathML(
                brushedMathML = "<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mi>A</mi><mo>(</mo><mi>m</mi><mo>,</mo><mi>n</mi><mo>)</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mfrac><mrow><mi>m</mi><mo>!</mo></mrow><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>)</mo><mo>!</mo></mrow></mfrac><mo>&#xA0;</mo><mo>=</mo><mo>[</mo><mi>m</mi><mo>!</mo><mo mathvariant=\"bold\" mathcolor=\"#FF0000\">=</mo><mi>m</mi><mo>*</mo><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>]</mo><mo>=</mo><mo>&#xA0;</mo><mfrac><mrow><mi>m</mi><mo>*</mo><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo></mrow><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>)</mo><mo>!</mo></mrow></mfrac></math>",
                wellKnownFunctions = "",
                expressionTransformationRules = "",
                targetFactIdentifier = "",
                targetVariablesNames = "",
                additionalFactsIdentifiers = ""
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mi>A</mi><mo>(</mo><mi>m</mi><mo>,</mo><mi>n</mi><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mfrac><mrow><mi>m</mi><mo>!</mo></mrow><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>)</mo><mo>!</mo></mrow></mfrac><mo>&#xA0;</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo><mo>[</mo><mi>m</mi><mo>!</mo><mo>=</mo><mi>m</mi><mo>*</mo><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo><mo>]</mo><mo>=</mo></mrow><mo>&#xA0;</mo><mfrac><mrow><mi>m</mi><mo>*</mo><mo>(</mo><mi>m</mi><mo>-</mo><mn>1</mn><mo>)</mo><mo>!</mo></mrow><mrow><mo>(</mo><mi>m</mi><mo>-</mo><mi>n</mi><mo>)</mo><mo>!</mo></mrow></mfrac></math>",
                result)
    }

    @Test
    fun testDeleteErrorString() {
        val brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mn>3</mn><mo>+</mo><mn>4</mn><mo>*</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mn>2</mn><mo>*</mo><mi>x</mi><mo>)</mo><mo>+</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mn>4</mn><mo>*</mo><mi>x</mi><mo>)</mo><mo>=</mo><mo>.</mo><mo>.</mo><mo>.</mo><mo>=</mo><mn>8</mn><mo>*</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mi>x</mi><mo>)</mo><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Syntax&#xA0;error: Unexpected: '.'</mtext></math>"
        val mathMLWithoutSuffix = deleteErrorStringFromMathMLSolution(brushedMathML, listOf(errorPrefix, syntaxErrorPrefix))
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mn>3</mn><mo>+</mo><mn>4</mn><mo>*</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mn>2</mn><mo>*</mo><mi>x</mi><mo>)</mo><mo>+</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mn>4</mn><mo>*</mo><mi>x</mi><mo>)</mo><mo>=</mo><mo>.</mo><mo>.</mo><mo>.</mo><mo>=</mo><mn>8</mn><mo>*</mo><mi>c</mi><mi>o</mi><mi>s</mi><mo>(</mo><mi>x</mi><mo>)</mo></math>",
                mathMLWithoutSuffix)
    }

    @Test
    fun correctMathMlTagsAccordingToBracketsFromEnd() {
        val res = correctMathMlTagsAccordingToBracketsFromEnd("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mo>(</mo><mi>cos</mi><msup><mrow><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo></mrow></msup><mo>)</mo><mo>'</mo><mo>=</mo><mo>(</mo><msup><mi>e</mi><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup><mo>)</mo><mo>'</mo><mo>=</mo><msup><mi>e</mi><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup><mo>*</mo><mo>(</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>)</mo><mo>'</mo><mo>=</mo><msup><mi>e</mi><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup><mo>*</mo><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>+</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mfrac><mrow><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>'</mo></mrow><mrow><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo></mrow></mfrac><mo>)</mo><mo>=</mo><msup><mi>e</mi><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup><mo>*</mo><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>-</mo><mfrac><mrow><msup><mrow><mo>(</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow><mn>2</mn></msup></mrow><mrow><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo></mrow></mfrac><mo>)</mo></math>")
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><msup><mrow><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo></mrow></msup><mo>)</mo><mo>'</mo><mo>=</mo><mo>(</mo><msup><mi>e</mi><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup><mo>)</mo><mo>'</mo><mo>=</mo><msup><mi>e</mi><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup><mo>*</mo><mo>(</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>)</mo><mo>'</mo><mo>=</mo><msup><mi>e</mi><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup><mo>*</mo><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>+</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mfrac><mrow><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>'</mo></mrow><mrow><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo></mrow></mfrac><mo>)</mo><mo>=</mo><msup><mi>e</mi><mrow><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup><mo>*</mo><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>*</mo><mi>ln</mi><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo><mo>-</mo><mfrac><mrow><msup><mrow><mo>(</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow><mn>2</mn></msup></mrow><mrow><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo></mrow></mfrac><mo>)</mo></math>",
                res)
    }

    @Test
    fun correctMathMlTagsAccordingToBracketsFromEndDoubleMsup() {
        val res = correctMathMlTagsAccordingToBracketsFromEnd("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mo>(</mo><mi>cos</mi><msup><mrow><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow><mrow><mo>(</mo><mi>sin</mi><msup><mrow><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow><mrow><mo>(</mo><mi>c</mi><mi>h</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup></mrow></msup><mo>)</mo><mo>'</mo></math>")
        assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><msup><mrow><mo>(</mo><mi>cos</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow><mrow><msup><mrow><mo>(</mo><mi>sin</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow><mrow><mo>(</mo><mi>c</mi><mi>h</mi><mo>(</mo><mi>x</mi><mo>)</mo><mo>)</mo></mrow></msup></mrow></msup><mo>)</mo><mo>'</mo></math>",
                res)
    }
}