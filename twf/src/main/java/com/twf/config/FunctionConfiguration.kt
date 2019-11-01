package com.twf.config

enum class StringDefinitionType {
    BINARY_OPERATION,
    UNARY_LEFT_OPERATION,
    UNARY_RIGHT_OPERATION,
    FUNCTION
}

data class FunctionIdentifier(
        val name: String,
        val numberOfArguments: Int
) {
    companion object {
        fun getIdentifier(name: String, numberOfArguments: Int) = "${name}_$numberOfArguments"
    }
    fun getIdentifier() = getIdentifier(name, numberOfArguments)
}

data class FunctionProperties(
        val function: String,
        val mainFunction: String,
        val priority: Double,
        val numberOfArguments: Int,
        val isCommutativeWithNullWeight: Boolean = false,
        val numberOfDefinitionArguments: Int = 0, //number of first arguments, that defines used in function vars, for example i in S(i, a, b, f(i))
        val isNameForRuleDesignations: Boolean = false, //need for matching function (for example, in rules for 'S(i, a, b, f(i) + g(i))')
        val userRepresentation: String = function,
        val defaultStringDefinitionType: StringDefinitionType = StringDefinitionType.FUNCTION,
        val minNumberOfPointsForEquality: Int = 2
)

data class FunctionStringDefinition(
        val function: FunctionProperties,
        val definitionType: StringDefinitionType,
        val definition: String,
        val subAsLast: Boolean = false
)

data class FunctionDefinition(
        val definitionLeftExpression: String,
        val definitionRightExpression: String
)

data class TreeTransformationRule(
        val definitionLeftExpression: String,
        val definitionRightExpression: String,
        val isImmediate: Boolean = false,
        val weight: Double = 1.0
)

class FunctionConfiguration {
    var notChangesOnVariablesInComparisonFunction = mutableListOf<FunctionIdentifier>(
            FunctionIdentifier("", 0),
            FunctionIdentifier("", 1),
            FunctionIdentifier("+", -1),
            FunctionIdentifier("-", -1),
            FunctionIdentifier("*", -1),
            FunctionIdentifier("/", -1),
            FunctionIdentifier("^", -1),
            FunctionIdentifier("mod", 2),
            FunctionIdentifier("S", 4),
            FunctionIdentifier("P", 4),

            FunctionIdentifier("and", -1),
            FunctionIdentifier("or", -1),
            FunctionIdentifier("xor", -1),
            FunctionIdentifier("alleq", -1),
            FunctionIdentifier("not", 1),

            FunctionIdentifier("sin", 1),
            FunctionIdentifier("cos", 1),
            FunctionIdentifier("sh", 1),
            FunctionIdentifier("ch", 1),
            FunctionIdentifier("th", 1),
            FunctionIdentifier("tg", 1),
            FunctionIdentifier("asin", 1),
            FunctionIdentifier("acos", 1),
            FunctionIdentifier("atg", 1),
            FunctionIdentifier("exp", 1),
            FunctionIdentifier("ln", 1),
            FunctionIdentifier("abs", 1)
    )

    var notChangesOnVariablesInComparisonFunctionWithoutTransformations = notChangesOnVariablesInComparisonFunction

    var functionDefinitions = mutableListOf<FunctionDefinition>(
            FunctionDefinition("n!", "P(i, 1, n, i)"),
            FunctionDefinition("U(m,n)", "m^n"),
            FunctionDefinition("A(m,n)", "m! / (m - n)!"),
            FunctionDefinition("P(n)", "n!"),
            FunctionDefinition("C(m,n)", "m! / (m - n)! / n!"),
            FunctionDefinition("V(m,n)", "(m + n - 1)! / (m - 1)! / n!"),
            FunctionDefinition("S1(n,k)", "S(i,0,k,(-1)^(k-i) * i^n * k! / (k - i)! / i!)"),
            FunctionDefinition("S2(n,k)", "S1(n,k) / n!"),
            FunctionDefinition("B(m)", "S(n,0,m,S2(m,n))"),
            FunctionDefinition("F(n)", "5^(-0.5) * (((1 + 5^0.5)/2)^(n+1) - ((1 - 5^0.5)/2)^(n+1))"),
            FunctionDefinition("C(n)", "(2*n)! / (n!)^2 / (n+1)"),
            FunctionDefinition("sec(x)", "1 / cos(x)"),
            FunctionDefinition("csc(x)", "1 / sin(x)"),
            FunctionDefinition("sech(x)", "1 / ch(x)"),
            FunctionDefinition("csch(x)", "1 / sh(x)"),
            FunctionDefinition("ctg(x)", "1 / tg(x)"),
            FunctionDefinition("actg(x)", "atg(1/x)"),
            FunctionDefinition("cth(x)", "1 / th(x)"),
            FunctionDefinition("log(b,a)", "ln(a) / ln(b)")
    )

    var treeTransformationRules = mutableListOf<TreeTransformationRule>(
            TreeTransformationRule("sin(x)^2", "1 - cos(x)^2"),
            TreeTransformationRule("sin(pi/2 - x)", "cos(x)"),
            TreeTransformationRule("sin(x)^2", "1 - cos(x)^2"),
            TreeTransformationRule("S(i, a, a, f(i))", "f(a)"),
            TreeTransformationRule("S(i, a, b, f(i))", "S(i, a, b-1, f(i)) + f(b)"),
            TreeTransformationRule("S(i, a, b, f(i))", "S(i, a+1, b, f(i)) + f(a)"),
            TreeTransformationRule("S(i, a, c, f(i)) + S(i, c+1, b, f(i))", "S(i, a, b, f(i))"),
            TreeTransformationRule("P(i, a, a, f(i))", "f(a)"),
            TreeTransformationRule("P(i, a, b, f(i))", "P(i, a, b-1, f(i)) * f(b)"),
            TreeTransformationRule("P(i, a, b, f(i))", "P(i, a+1, b, f(i)) * f(a)"),
            TreeTransformationRule("P(i, a, c, f(i)) * P(i, c+1, b, f(i))", "P(i, a, b, f(i))"),
            TreeTransformationRule("U(m,n)", "m^n"),
            TreeTransformationRule("A(m,n)", "m! / (m - n)!"),
            TreeTransformationRule("P(n)", "n!"),
            TreeTransformationRule("P(n)", "A(n,n)"),
            TreeTransformationRule("C(m,n)", "m! / (m - n)! / n!"),
            TreeTransformationRule("V(m,n)", "(m + n - 1)! / (m - 1)! / n!"),
            TreeTransformationRule("V(m,n)", "C(m + n - 1, n)"),
            TreeTransformationRule("S1(n,k)", "S(i,0,k,(-1)^(k-i) * i^n * k! / (k - i)! / i!)"),
            TreeTransformationRule("S2(n,k)", "S1(n,k) / n!"),
            TreeTransformationRule("S2(n,k)", "S2(m-1,n-1) + n*S2(m-1,n)"),
            TreeTransformationRule("B(m)", "S(n,0,m,S2(m,n))"),
            TreeTransformationRule("F(n)", "5^(-0.5) * (((1 + 5^0.5)/2)^(n+1) - ((1 - 5^0.5)/2)^(n+1))"),
            TreeTransformationRule("F(n)", "F(n-1) + F(n-2)"),
            TreeTransformationRule("C(n)", "(2*n)! / (n!)^2 / (n+1)"),
            TreeTransformationRule("a^(b+c)", "a^b*a^c"),

            TreeTransformationRule("sqrt(x)", "x^0.5", true),
            TreeTransformationRule("root(x,p)", "x^(1/p)", true),
            TreeTransformationRule("n!", "P(i,1,n,i)", true),
            TreeTransformationRule("d(expr)", "d(expr,x)", true)
    )

    var taskContextTreeTransformationRules = mutableListOf<TreeTransformationRule>()

    var functionProperties = mutableListOf<FunctionProperties>(
            FunctionProperties("+", "+", 1.0, -1, isCommutativeWithNullWeight = true, defaultStringDefinitionType = StringDefinitionType.BINARY_OPERATION),
            FunctionProperties("-", "+", 1.0, -1, defaultStringDefinitionType = StringDefinitionType.BINARY_OPERATION),
            FunctionProperties("*", "*", 2.0, -1, isCommutativeWithNullWeight = true, defaultStringDefinitionType = StringDefinitionType.BINARY_OPERATION),
            FunctionProperties("/", "/", 1.5, -1, defaultStringDefinitionType = StringDefinitionType.BINARY_OPERATION),  //FunctionProperties("/", "*", 2.0, -1)
            FunctionProperties("^", "^", 3.0, -1, defaultStringDefinitionType = StringDefinitionType.BINARY_OPERATION),
            FunctionProperties("S", "S", 5.0, 4, numberOfDefinitionArguments = 1, minNumberOfPointsForEquality = Int.MAX_VALUE),
            FunctionProperties("P", "P", 5.0, 4, numberOfDefinitionArguments = 1, minNumberOfPointsForEquality = Int.MAX_VALUE),
            FunctionProperties("sin", "sin", 5.0, 1),
            FunctionProperties("cos", "cos", 5.0, 1),
            FunctionProperties("sh", "sh", 5.0, 1),
            FunctionProperties("ch", "ch", 5.0, 1),
            FunctionProperties("sec", "sec", 5.0, 1),
            FunctionProperties("csc", "csc", 5.0, 1),
            FunctionProperties("tg", "tg", 5.0, 1),
            FunctionProperties("ctg", "ctg", 5.0, 1),
            FunctionProperties("th", "th", 5.0, 1),
            FunctionProperties("cth", "cth", 5.0, 1),
            FunctionProperties("sech", "sech", 5.0, 1),
            FunctionProperties("csch", "csch", 5.0, 1),
            FunctionProperties("asin", "asin", 5.0, 1),
            FunctionProperties("acos", "acos", 5.0, 1),
            FunctionProperties("atg", "atg", 5.0, 1),
            FunctionProperties("actg", "actg", 5.0, 1),
            FunctionProperties("exp", "exp", 5.0, 1),
            FunctionProperties("ln", "ln", 5.0, 1),
            FunctionProperties("abs", "abs", 5.0, 1, minNumberOfPointsForEquality = Int.MAX_VALUE),
            FunctionProperties("log", "log", 5.0, 2),
            FunctionProperties("mod", "mod", 5.0, 2),

            FunctionProperties("and", "and", 0.5, -1, isCommutativeWithNullWeight = true, userRepresentation = "&", defaultStringDefinitionType = StringDefinitionType.BINARY_OPERATION),
            FunctionProperties("or", "or", 0.5, -1, isCommutativeWithNullWeight = true, userRepresentation = "|", defaultStringDefinitionType = StringDefinitionType.BINARY_OPERATION),
            FunctionProperties("xor", "xor", 0.5, -1, isCommutativeWithNullWeight = true, defaultStringDefinitionType = StringDefinitionType.BINARY_OPERATION),
            FunctionProperties("alleq", "alleq", 0.5, -1, isCommutativeWithNullWeight = true, defaultStringDefinitionType = StringDefinitionType.BINARY_OPERATION),

            FunctionProperties("not", "not", 5.0, 1, userRepresentation = "!", defaultStringDefinitionType = StringDefinitionType.UNARY_LEFT_OPERATION),

            FunctionProperties("sqrt", "sqrt", 5.0, 1), //maybe these functions should be automatically translated to ^ - solved by ImmediateTreeTransformationRules
            FunctionProperties("root", "root", 5.0, 2), //maybe these functions should be automatically translated to ^ - solved by ImmediateTreeTransformationRules
            FunctionProperties("mfenced", "mfenced", 5.0, 1),

            FunctionProperties("U", "U", 5.0, 2),
            FunctionProperties("P", "P", 5.0, 1),
            FunctionProperties("A", "A", 5.0, 2),
            FunctionProperties("C", "C", 5.0, 2),
            FunctionProperties("V", "V", 5.0, 2),
            FunctionProperties("B", "B", 5.0, 1),
            FunctionProperties("S1", "S1", 5.0, 2),
            FunctionProperties("S2", "S2", 5.0, 2),
            FunctionProperties("F", "F", 5.0, 1), //fib
            FunctionProperties("C", "C", 5.0, 1), //catalan
            FunctionProperties("factorial", "factorial", 4.0, 1),
            FunctionProperties("double_factorial", "factorial", 4.0, 1),
            FunctionProperties("subfactorial", "subfactorial", 4.0, 1),

            FunctionProperties("partial_differential", "partial_differential", 0.5, 1),
            FunctionProperties("d", "d", 0.5, 2),
            FunctionProperties("d", "d", 0.5, 1),

            //rules functions:
            FunctionProperties("f", "f", 0.5, 1, isNameForRuleDesignations = true),
            FunctionProperties("g", "g", 0.5, 1, isNameForRuleDesignations = true)
    )

    var functionPropertiesByName = functionProperties.associateBy { it.function + "_" + it.numberOfArguments }

    var stringDefinitions = mutableListOf<FunctionStringDefinition>(
            FunctionStringDefinition(functionPropertiesByName["+_-1"]!!, StringDefinitionType.BINARY_OPERATION, "+"),
            FunctionStringDefinition(functionPropertiesByName["+_-1"]!!, StringDefinitionType.FUNCTION, "+"),
            FunctionStringDefinition(functionPropertiesByName["+_-1"]!!, StringDefinitionType.FUNCTION, "add"),
            FunctionStringDefinition(functionPropertiesByName["-_-1"]!!, StringDefinitionType.BINARY_OPERATION, "-"),
            FunctionStringDefinition(functionPropertiesByName["-_-1"]!!, StringDefinitionType.FUNCTION, "-"),
            FunctionStringDefinition(functionPropertiesByName["-_-1"]!!, StringDefinitionType.FUNCTION, "sub"),
            FunctionStringDefinition(functionPropertiesByName["factorial_1"]!!, StringDefinitionType.UNARY_RIGHT_OPERATION, "!"),
            FunctionStringDefinition(functionPropertiesByName["double_factorial_1"]!!, StringDefinitionType.UNARY_RIGHT_OPERATION, "!!"),
            FunctionStringDefinition(functionPropertiesByName["subfactorial_1"]!!, StringDefinitionType.UNARY_LEFT_OPERATION, "!"),
            FunctionStringDefinition(functionPropertiesByName["factorial_1"]!!, StringDefinitionType.FUNCTION, "factorial"),
            FunctionStringDefinition(functionPropertiesByName["double_factorial_1"]!!, StringDefinitionType.FUNCTION, "double_factorial"),
            FunctionStringDefinition(functionPropertiesByName["subfactorial_1"]!!, StringDefinitionType.FUNCTION, "subfactorial"),
            FunctionStringDefinition(functionPropertiesByName["*_-1"]!!, StringDefinitionType.BINARY_OPERATION, "*"),
            FunctionStringDefinition(functionPropertiesByName["*_-1"]!!, StringDefinitionType.BINARY_OPERATION, "&#xD7"),
            FunctionStringDefinition(functionPropertiesByName["*_-1"]!!, StringDefinitionType.BINARY_OPERATION, "&#xB7"),
            FunctionStringDefinition(functionPropertiesByName["*_-1"]!!, StringDefinitionType.FUNCTION, "*"),
            FunctionStringDefinition(functionPropertiesByName["*_-1"]!!, StringDefinitionType.FUNCTION, "mul"),
            FunctionStringDefinition(functionPropertiesByName["/_-1"]!!, StringDefinitionType.BINARY_OPERATION, "/"),
            FunctionStringDefinition(functionPropertiesByName["/_-1"]!!, StringDefinitionType.FUNCTION, "/"),
            FunctionStringDefinition(functionPropertiesByName["/_-1"]!!, StringDefinitionType.FUNCTION, "div"),
            FunctionStringDefinition(functionPropertiesByName["/_-1"]!!, StringDefinitionType.FUNCTION, "mfrac"),
            FunctionStringDefinition(functionPropertiesByName["^_-1"]!!, StringDefinitionType.BINARY_OPERATION, "^"),
            FunctionStringDefinition(functionPropertiesByName["^_-1"]!!, StringDefinitionType.FUNCTION, "^"),
            FunctionStringDefinition(functionPropertiesByName["^_-1"]!!, StringDefinitionType.FUNCTION, "pow"),
            FunctionStringDefinition(functionPropertiesByName["^_-1"]!!, StringDefinitionType.FUNCTION, "msup"),
            FunctionStringDefinition(functionPropertiesByName["mod_2"]!!, StringDefinitionType.FUNCTION, "mod"),

            FunctionStringDefinition(functionPropertiesByName["and_-1"]!!, StringDefinitionType.BINARY_OPERATION, "&amp"),
            FunctionStringDefinition(functionPropertiesByName["or_-1"]!!, StringDefinitionType.BINARY_OPERATION, "|"),
            FunctionStringDefinition(functionPropertiesByName["and_-1"]!!, StringDefinitionType.BINARY_OPERATION, "&#x2227"),
            FunctionStringDefinition(functionPropertiesByName["or_-1"]!!, StringDefinitionType.BINARY_OPERATION, "&#x2228"),
            FunctionStringDefinition(functionPropertiesByName["xor_-1"]!!, StringDefinitionType.BINARY_OPERATION, "&#x2295"),
            FunctionStringDefinition(functionPropertiesByName["not_1"]!!, StringDefinitionType.UNARY_LEFT_OPERATION, "&#xAC"),

            FunctionStringDefinition(functionPropertiesByName["and_-1"]!!, StringDefinitionType.FUNCTION, "and"),
            FunctionStringDefinition(functionPropertiesByName["or_-1"]!!, StringDefinitionType.FUNCTION, "or"),
            FunctionStringDefinition(functionPropertiesByName["xor_-1"]!!, StringDefinitionType.FUNCTION, "xor"),
            FunctionStringDefinition(functionPropertiesByName["alleq_-1"]!!, StringDefinitionType.FUNCTION, "alleq"),
            FunctionStringDefinition(functionPropertiesByName["not_1"]!!, StringDefinitionType.FUNCTION, "not"),

            FunctionStringDefinition(functionPropertiesByName["partial_differential_1"]!!, StringDefinitionType.UNARY_LEFT_OPERATION, "&#x2202"),

            FunctionStringDefinition(functionPropertiesByName["d_1"]!!, StringDefinitionType.UNARY_RIGHT_OPERATION, "'"),
            FunctionStringDefinition(functionPropertiesByName["d_2"]!!, StringDefinitionType.FUNCTION, "msubsup\'"),
            FunctionStringDefinition(functionPropertiesByName["d_1"]!!, StringDefinitionType.FUNCTION, "d"),
            FunctionStringDefinition(functionPropertiesByName["d_2"]!!, StringDefinitionType.FUNCTION, "d"),
            FunctionStringDefinition(functionPropertiesByName["d_1"]!!, StringDefinitionType.FUNCTION, "diff"),
            FunctionStringDefinition(functionPropertiesByName["d_2"]!!, StringDefinitionType.FUNCTION, "diff"),

            FunctionStringDefinition(functionPropertiesByName["S_4"]!!, StringDefinitionType.FUNCTION, "munderover&#x2211"),
            FunctionStringDefinition(functionPropertiesByName["P_4"]!!, StringDefinitionType.FUNCTION, "munderover&#x220F"),
            FunctionStringDefinition(functionPropertiesByName["S_4"]!!, StringDefinitionType.FUNCTION, "S"),
            FunctionStringDefinition(functionPropertiesByName["P_4"]!!, StringDefinitionType.FUNCTION, "P"),

            FunctionStringDefinition(functionPropertiesByName["sin_1"]!!, StringDefinitionType.FUNCTION, "sin"),
            FunctionStringDefinition(functionPropertiesByName["cos_1"]!!, StringDefinitionType.FUNCTION, "cos"),
            FunctionStringDefinition(functionPropertiesByName["tg_1"]!!, StringDefinitionType.FUNCTION, "tg"),
            FunctionStringDefinition(functionPropertiesByName["ctg_1"]!!, StringDefinitionType.FUNCTION, "ctg"),
            FunctionStringDefinition(functionPropertiesByName["tg_1"]!!, StringDefinitionType.FUNCTION, "tan"),
            FunctionStringDefinition(functionPropertiesByName["ctg_1"]!!, StringDefinitionType.FUNCTION, "cot"),

            FunctionStringDefinition(functionPropertiesByName["asin_1"]!!, StringDefinitionType.FUNCTION, "asin"),
            FunctionStringDefinition(functionPropertiesByName["acos_1"]!!, StringDefinitionType.FUNCTION, "acos"),
            FunctionStringDefinition(functionPropertiesByName["atg_1"]!!, StringDefinitionType.FUNCTION, "atg"),
            FunctionStringDefinition(functionPropertiesByName["atg_1"]!!, StringDefinitionType.FUNCTION, "atan"),
            FunctionStringDefinition(functionPropertiesByName["actg_1"]!!, StringDefinitionType.FUNCTION, "actg"),
            FunctionStringDefinition(functionPropertiesByName["asin_1"]!!, StringDefinitionType.FUNCTION, "arcsin"),
            FunctionStringDefinition(functionPropertiesByName["acos_1"]!!, StringDefinitionType.FUNCTION, "arccos"),
            FunctionStringDefinition(functionPropertiesByName["atg_1"]!!, StringDefinitionType.FUNCTION, "arctg"),
            FunctionStringDefinition(functionPropertiesByName["actg_1"]!!, StringDefinitionType.FUNCTION, "arcctg"),

            FunctionStringDefinition(functionPropertiesByName["sh_1"]!!, StringDefinitionType.FUNCTION, "sh"),
            FunctionStringDefinition(functionPropertiesByName["ch_1"]!!, StringDefinitionType.FUNCTION, "ch"),
            FunctionStringDefinition(functionPropertiesByName["th_1"]!!, StringDefinitionType.FUNCTION, "th"),
            FunctionStringDefinition(functionPropertiesByName["cth_1"]!!, StringDefinitionType.FUNCTION, "cth"),

            FunctionStringDefinition(functionPropertiesByName["sec_1"]!!, StringDefinitionType.FUNCTION, "sec"),
            FunctionStringDefinition(functionPropertiesByName["csc_1"]!!, StringDefinitionType.FUNCTION, "cosec"),
            FunctionStringDefinition(functionPropertiesByName["csc_1"]!!, StringDefinitionType.FUNCTION, "csc"),
            FunctionStringDefinition(functionPropertiesByName["sech_1"]!!, StringDefinitionType.FUNCTION, "sech"),
            FunctionStringDefinition(functionPropertiesByName["csch_1"]!!, StringDefinitionType.FUNCTION, "csch"),

            FunctionStringDefinition(functionPropertiesByName["exp_1"]!!, StringDefinitionType.FUNCTION, "exp"),
            FunctionStringDefinition(functionPropertiesByName["ln_1"]!!, StringDefinitionType.FUNCTION, "ln"),
            FunctionStringDefinition(functionPropertiesByName["ln_1"]!!, StringDefinitionType.FUNCTION, "log"),
            FunctionStringDefinition(functionPropertiesByName["log_2"]!!, StringDefinitionType.FUNCTION, "log"),
            FunctionStringDefinition(functionPropertiesByName["log_2"]!!, StringDefinitionType.FUNCTION, "{log}", true),
            FunctionStringDefinition(functionPropertiesByName["abs_1"]!!, StringDefinitionType.FUNCTION, "abs"),
            FunctionStringDefinition(functionPropertiesByName["abs_1"]!!, StringDefinitionType.FUNCTION, "mfenced_|__|"),

            FunctionStringDefinition(functionPropertiesByName["sqrt_1"]!!, StringDefinitionType.FUNCTION, "msqrt"),
            FunctionStringDefinition(functionPropertiesByName["root_2"]!!, StringDefinitionType.FUNCTION, "mroot"),
            FunctionStringDefinition(functionPropertiesByName["sqrt_1"]!!, StringDefinitionType.FUNCTION, "sqrt"),
            FunctionStringDefinition(functionPropertiesByName["root_2"]!!, StringDefinitionType.FUNCTION, "root"),
            FunctionStringDefinition(functionPropertiesByName["mfenced_1"]!!, StringDefinitionType.FUNCTION, "mfenced"),

            FunctionStringDefinition(functionPropertiesByName["U_2"]!!, StringDefinitionType.FUNCTION, "U"),
            FunctionStringDefinition(functionPropertiesByName["P_1"]!!, StringDefinitionType.FUNCTION, "P"),
            FunctionStringDefinition(functionPropertiesByName["A_2"]!!, StringDefinitionType.FUNCTION, "A"),
            FunctionStringDefinition(functionPropertiesByName["C_2"]!!, StringDefinitionType.FUNCTION, "C"),
            FunctionStringDefinition(functionPropertiesByName["V_2"]!!, StringDefinitionType.FUNCTION, "V"),
            FunctionStringDefinition(functionPropertiesByName["B_1"]!!, StringDefinitionType.FUNCTION, "B"),
            FunctionStringDefinition(functionPropertiesByName["S1_2"]!!, StringDefinitionType.FUNCTION, "S1"),
            FunctionStringDefinition(functionPropertiesByName["S2_2"]!!, StringDefinitionType.FUNCTION, "S2"),
            FunctionStringDefinition(functionPropertiesByName["F_1"]!!, StringDefinitionType.FUNCTION, "F"),
            FunctionStringDefinition(functionPropertiesByName["C_1"]!!, StringDefinitionType.FUNCTION, "C"),

            FunctionStringDefinition(functionPropertiesByName["f_1"]!!, StringDefinitionType.FUNCTION, "f"),
            FunctionStringDefinition(functionPropertiesByName["g_1"]!!, StringDefinitionType.FUNCTION, "g")
    )

    var functionStringDefinitionByName = stringDefinitions
            .filter { it.definitionType == StringDefinitionType.FUNCTION }
            .associateBy { FunctionIdentifier.getIdentifier(it.definition, it.function.numberOfArguments) }

    fun fastFindByNameAndNumberOfArguments(name: String, numberOfArguments: Int) =
            functionStringDefinitionByName.get(FunctionIdentifier.getIdentifier(name, numberOfArguments)) ?: functionStringDefinitionByName.get(FunctionIdentifier.getIdentifier(name, -1))

    fun findFunctionStringDefinition(name: String, type: StringDefinitionType, numberOfArguments: Int, nameIsPossible: Boolean = false, subAsLast: Boolean = false): FunctionStringDefinition? {
        for (stringDefinition in stringDefinitions) {
            if (stringDefinition.definitionType == type && stringDefinition.definition == name && subAsLast == stringDefinition.subAsLast &&
                    (nameIsPossible || !stringDefinition.function.isNameForRuleDesignations))
                if (stringDefinition.function.numberOfArguments == -1 || stringDefinition.function.numberOfArguments == numberOfArguments ||
                        (subAsLast && stringDefinition.function.numberOfArguments == 2))
                    return stringDefinition
        }
        return null
    }


}