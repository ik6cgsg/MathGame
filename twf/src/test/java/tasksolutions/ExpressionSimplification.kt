package tasksolutions

import com.twf.factstransformations.FactConstructorViewer
import com.twf.factstransformations.parseFromFactIdentifier
import com.twf.logs.log
import com.twf.mainpoints.checkFactsInMathML
import com.twf.mainpoints.configSeparator
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class ExpressionSimplification {
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
            "*$configSeparator-1"

    @Test
    fun factorizationCorrect() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>2</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>3</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>-</mo><mn>35</mn><mo>=</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>+</mo><mn>2</mn><mo>)</mo><mo>-</mo><mn>35</mn><mo>=</mo><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mn>2</mn></msup><mo>+</mo><mn>2</mn><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>-</mo><mn>35</mn><mo>=</mo><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mn>2</mn></msup><mo>+</mo><mn>2</mn><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>+</mo><mn>1</mn><mo>-</mo><msup><mn>6</mn><mn>2</mn></msup><mo>=</mo><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mn>2</mn></msup><mo>-</mo><msup><mn>6</mn><mn>2</mn></msup><mo>=</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>5</mn><mo>+</mo><mn>6</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>5</mn><mo>-</mo><mn>6</mn><mo>)</mo><mo>=</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>11</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>-</mo><mn>1</mn><mo>)</mo></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "(+(*(+(x;1);+(x;2);+(x;3);+(x;4));-(35)))",
                targetVariablesNames = "x",
                minNumberOfMultipliersInAnswer = "2",
                additionalFactsIdentifiers = ""
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>2</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>3</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>-</mo><mn>35</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>+</mo><mn>2</mn><mo>)</mo><mo>-</mo><mn>35</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mn>2</mn></msup><mo>+</mo><mn>2</mn><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>-</mo><mn>35</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo></mrow><mn>2</mn></msup><mo>+</mo><mn>2</mn><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>+</mo><mn>1</mn><mo>-</mo><msup><mn>6</mn><mn>2</mn></msup><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mn>2</mn></msup><mo>-</mo><msup><mn>6</mn><mn>2</mn></msup><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>5</mn><mo>+</mo><mn>6</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>5</mn><mo>-</mo><mn>6</mn><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>11</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>-</mo><mn>1</mn><mo>)</mo></math>", result)
    }

    @Test
    fun factorizationFewMultipliers() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>2</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>3</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>-</mo><mn>35</mn><mo>=</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>+</mo><mn>2</mn><mo>)</mo><mo>-</mo><mn>35</mn></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "(+(*(+(x;1);+(x;2);+(x;3);+(x;4));-(35)))",
                targetVariablesNames = "x",
                minNumberOfMultipliersInAnswer = "2",
                additionalFactsIdentifiers = ""
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>2</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>3</mn><mo>)</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>-</mo><mn>35</mn><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>+</mo><mn>2</mn><mo>)</mo><mo>-</mo><mn>35</mn><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: Answer is not factorized</mtext></math>", result)
    }

    @Test
    fun factorizationWrongAnswer() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mn>2</mn></msup><mo>-</mo><msup><mn>6</mn><mn>2</mn></msup><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>5</mn><mo>+</mo><mn>6</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>5</mn><mo>-</mo><mn>6</mn><mo>)</mo><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>11</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>-</mo><mn>1</mn><mo>)</mo></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "(+(*(+(x;1);+(x;2);+(x;3);+(x;4));-(35)))",
                targetVariablesNames = "x",
                minNumberOfMultipliersInAnswer = "2",
                additionalFactsIdentifiers = ""
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>4</mn><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mn>2</mn></msup><mo>-</mo><msup><mn>6</mn><mn>2</mn></msup><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>5</mn><mo>+</mo><mn>6</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>5</mn><mo>-</mo><mn>6</mn><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>+</mo><mn>11</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>5</mn><mi>x</mi><mo>-</mo><mn>1</mn><mo>)</mo><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: Wrong start expression</mtext></math>", result)
    }

    @Test
    fun fractionReducingCorrect() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mo>=</mo><mfrac><mstyle displaystyle=\"true\"><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><mn>2</mn><mo>*</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn><mo>-</mo><msup><mi>x</mi><mn>2</mn></msup></mstyle><mstyle displaystyle=\"true\"><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mstyle></mfrac><mo>=</mo><mfrac><mstyle displaystyle=\"true\"><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mn>2</mn></msup><mo>-</mo><msup><mi>x</mi><mn>2</mn></msup></mstyle><mstyle displaystyle=\"true\"><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mstyle></mfrac><mo>=</mo><mfrac><mstyle displaystyle=\"true\"><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo></mstyle><mstyle displaystyle=\"true\"><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mstyle></mfrac><mo>=</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mi>x</mi><mo>+</mo><mn>1</mn></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "(/(+(^(x;4);^(x;2);1);+(^(x;2);-(x);1)))",
                targetVariablesNames = "",
                minNumberOfMultipliersInAnswer = "",
                additionalFactsIdentifiers = ""
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><mn>2</mn><mo>*</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn><mo>-</mo><msup><mi>x</mi><mn>2</mn></msup></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mfrac><mrow><msup><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mn>2</mn></msup><mo>-</mo><msup><mi>x</mi><mn>2</mn></msup></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mfrac><mrow><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mi>x</mi><mo>+</mo><mn>1</mn></math>",
                result)
    }

    @Test
    fun fractionReducingWrong() {
        val result = checkFactsInMathML(
                brushedMathML = "<math mathcolor=\"#7F00FF\" xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mo mathvariant=\"bold\" mathcolor=\"#007F00\">=</mo><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn><mo>+</mo><mi>x</mi><mo>-</mo><mi>x</mi></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "(/(+(^(x;4);^(x;2);1);+(^(x;2);-(x);1)))",
                targetVariablesNames = "",
                minNumberOfMultipliersInAnswer = "",
                additionalFactsIdentifiers = "",
                maxNumberOfDivisionsInAnswer = "0"
        )

        val logRef = log.getLogInPlainText()

        assert(result.contains("Error"))
        assert(result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn><mo>+</mo><mi>x</mi><mo>-</mo><mi>x</mi></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Error: Answer contains fraction</mtext></math>",
                result)
    }

    @Test
    fun fractionReducingCorrectNotStandardNotice() {
        val result = checkFactsInMathML(
                brushedMathML = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow><menclose mathcolor=\"#7F0000\" notation=\"bottom\"><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac></menclose><mo>=</mo></mrow><mrow><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn><mo>-</mo><mi>x</mi><mo>+</mo><mi>x</mi></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mo>=</mo><mn>1</mn><mo>+</mo><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><mi>x</mi></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mo>=</mo></mrow><mn>1</mn><mo>+</mo><mfrac><mrow><mi>x</mi><mo>*</mo><mo>(</mo><msup><mi>x</mi><mn>3</mn></msup><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mo>=</mo><mn>1</mn><mo>+</mo><mfrac><mrow><mi>x</mi><mo>*</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>*</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mo>=</mo><mn>1</mn><mo>+</mo><mi>x</mi><mo>*</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>=</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mi>x</mi><mo>+</mo><mn>1</mn><mspace linebreak=\"newline\"/><mtext mathvariant=\"bold\" mathcolor=\"#FF0000\">Syntax&#xA0;error&#xA0;(underlined): Unexpected: '&lt/mfrac'</mtext></math>",
                wellKnownFunctions = wellKnownFunctions,
                expressionTransformationRules = " ",
                targetFactIdentifier = "(/(+(^(x;4);^(x;2);1);+(^(x;2);-(x);1)))",
                targetVariablesNames = "",
                minNumberOfMultipliersInAnswer = "",
                additionalFactsIdentifiers = "",
                maxNumberOfDivisionsInAnswer = "0"
        )

        val logRef = log.getLogInPlainText()

        assert(!result.contains("Error"))
        assert(!result.contains("#FF"))
        Assert.assertEquals("<math xmlns=\"http://www.w3.org/1998/Math/MathML\" mathcolor=\"#7F00FF\"><mrow><mrow><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac></mrow><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow></mrow><mrow><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mn>1</mn><mo>-</mo><mi>x</mi><mo>+</mo><mi>x</mi></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mn>1</mn><mo>+</mo><mfrac><mrow><msup><mi>x</mi><mn>4</mn></msup><mo>+</mo><mi>x</mi></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow></mrow><mn>1</mn><mo>+</mo><mfrac><mrow><mi>x</mi><mo>*</mo><mo>(</mo><msup><mi>x</mi><mn>3</mn></msup><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mn>1</mn><mo>+</mo><mfrac><mrow><mi>x</mi><mo>*</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mo>*</mo><mo>(</mo><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo></mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>-</mo><mi>x</mi><mo>+</mo><mn>1</mn></mrow></mfrac><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><mn>1</mn><mo>+</mo><mi>x</mi><mo>*</mo><mo>(</mo><mi>x</mi><mo>+</mo><mn>1</mn><mo>)</mo><mrow mathvariant=\"bold\" mathcolor=\"#007F00\"><mo>=</mo></mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mi>x</mi><mo>+</mo><mn>1</mn></math>",
                result)
    }


    @Test
    @Ignore
    fun oldIdentifierToNew() {
        val factConstructorViewer: FactConstructorViewer = FactConstructorViewer()
        val fact = parseFromFactIdentifier("(x^4+x^2+1)/(x^2-x+1)")!!
        val newIdentifier = factConstructorViewer.constructIdentifierByFact(fact)
        print(newIdentifier)
    }
}