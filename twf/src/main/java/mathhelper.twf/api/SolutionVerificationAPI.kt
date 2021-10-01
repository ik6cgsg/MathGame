package mathhelper.twf.api

import mathhelper.twf.config.*
import mathhelper.twf.expressiontree.ExpressionSubstitution
import mathhelper.twf.expressiontree.NodeType
import mathhelper.twf.mainpoints.*


fun checkSolutionInTex(
        originalTexSolution: String, //string with learner solution in Tex format
        taskITR: TaskITR, //solving task
        rulePacksITR: Array<RulePackITR>, //corresponding rule packs
        shortErrorDescription: String = "0", //make error message shorter and easier to understand: crop parsed steps from error description
        skipTrivialCheck: Boolean = false //do not check completeness of transformations, only correctness
): TexVerificationResult {
    val scopeFilter = mutableSetOf<String>()
    if (taskITR.subjectType in listOf("set", "logic")) {
        scopeFilter.add("setTheory")
    }
    val functionConfiguration = FunctionConfiguration(scopeFilter)
    if (taskITR.otherCheckSolutionData != null && taskITR.otherCheckSolutionData[notChangesOnVariablesInComparisonFunctionJsonName] is List<*>) {
        functionConfiguration.notChangesOnVariablesInComparisonFunction = taskITR.otherCheckSolutionData[notChangesOnVariablesInComparisonFunctionJsonName] as List<FunctionIdentifier>
    }
    if (taskITR.otherCheckSolutionData != null && taskITR.otherCheckSolutionData[notChangesOnVariablesInComparisonFunctionWithoutTransformationsJsonName] is List<*>) {
        functionConfiguration.notChangesOnVariablesInComparisonFunctionWithoutTransformations = taskITR.otherCheckSolutionData[notChangesOnVariablesInComparisonFunctionWithoutTransformationsJsonName] as List<FunctionIdentifier>
    }
    if (taskITR.rules?.isNotEmpty() == true || taskITR.rulePacks?.isNotEmpty() == true) {
        functionConfiguration.treeTransformationRules = mutableListOf()
        functionConfiguration.taskContextTreeTransformationRules = mutableListOf()
    }

    val compiledConfiguration = CompiledConfiguration(
            functionConfiguration = functionConfiguration
    )

    val expressionSubstitutions = mutableListOf<ExpressionSubstitution>()
    taskITR.rules?.forEach {
        expressionSubstitutions.add(expressionSubstitutionFromRuleITR(it))
    }
    val rulePacksMap = rulePacksITR.filter { it.code != null }.associateBy { it.code!! }
    taskITR.rulePacks?.forEach {
        val rulePack = rulePacksMap[it.rulePackCode]
        if (rulePack != null) {
            expressionSubstitutions.addAll(expressionSubstitutionsFromRulePackITR(rulePack, rulePacksMap, true))
        }
    }
    if (expressionSubstitutions.isNotEmpty()) {
        compiledConfiguration.compiledExpressionTreeTransformationRules.clear()
        compiledConfiguration.compiledExpressionTreeTransformationRules.addAll(expressionSubstitutions.filter { it.left.nodeType == NodeType.FUNCTION && it.right.nodeType == NodeType.FUNCTION })
    }

    if (taskITR.otherCheckSolutionData != null && taskITR.otherCheckSolutionData["maxExpressionTransformationWeightString"] != null) {
        val maxExpressionTransformationWeightString = taskITR.otherCheckSolutionData["maxExpressionTransformationWeightString"] as String
        compiledConfiguration.comparisonSettings.maxExpressionTransformationWeight = maxExpressionTransformationWeightString.toDoubleOrNull() ?: 1.0
    }
    if (taskITR.otherCheckSolutionData != null && taskITR.otherCheckSolutionData["maxDistBetweenDiffStepsString"] != null) {
        val maxDistBetweenDiffStepsString = taskITR.otherCheckSolutionData["maxDistBetweenDiffStepsString"] as String
        compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps = maxDistBetweenDiffStepsString.toDoubleOrNull() ?: 1.0
    }

    return checkFactsInTex(
            originalTexSolution,
            taskITR.originalExpressionStructureString ?: "",
            taskITR.goalExpressionStructureString ?: "",
            "",
            taskITR.goalPattern ?: "",
            "", // TODO: support inequalities in tasks
            "",
            shortErrorDescription = shortErrorDescription,
            skipTrivialCheck = skipTrivialCheck,
            compiledConfiguration = compiledConfiguration,
            otherGoalData = taskITR.otherGoalData)
}

fun checkSolutionInTex(
        originalTexSolution: String, //string with learner solution in Tex format

        //// individual task parameters:
        startExpressionIdentifier: String = "", //Expression, from which learner need to start the transformations in structure string
        targetFactPattern: String = "", //Pattern that specify criteria that learner's answer must meet
        additionalFactsIdentifiers: String = "", ///Identifiers split by configSeparator - task condition facts should be here, that can be used as rules only for this task

        endExpressionIdentifier: String = "", //Expression, which learner need to deduce
        targetFactIdentifier: String = "", //Fact that learner need to deduce. It is more flexible than startExpressionIdentifier and endExpressionIdentifier, allow to specify inequality like '''EXPRESSION_COMPARISON{(+(/(sin(x);+(1;cos(x)));/(+(1;cos(x));sin(x))))}{<=}{(/(2;sin(x)))}'''
        comparisonSign: String = "", //Comparison sign

        //// general configuration parameters
        //functions, which null-weight transformations allowed (if no other transformations), split by configSeparator
        //choose one of 2 api forms:
        wellKnownFunctions: List<FunctionIdentifier> = listOf(),
        wellKnownFunctionsString: String = "${configSeparator}0$configSeparator${configSeparator}1$configSeparator+$configSeparator-1$configSeparator-$configSeparator-1$configSeparator*$configSeparator-1$configSeparator/$configSeparator-1$configSeparator^$configSeparator-1",

        //functions, which null-weight transformations allowed with any other transformations, split by configSeparator
        //choose one of 2 api forms:
        unlimitedWellKnownFunctions: List<FunctionIdentifier> = wellKnownFunctions,
        unlimitedWellKnownFunctionsString: String = wellKnownFunctionsString,

        //expression transformation rules
        //choose one of api forms:
        expressionTransformationRules: List<ExpressionSubstitution> = listOf(), //full list of expression transformations rules
        expressionTransformationRulesString: String = "S(i, a, a, f(i))${configSeparator}f(a)${configSeparator}S(i, a, b, f(i))${configSeparator}S(i, a, b-1, f(i)) + f(b)", //function transformation rules, parts split by configSeparator; if it equals " " then expressions will be checked only by testing
        taskContextExpressionTransformationRules: String = "", //for expression transformation rules based on variables
        rulePacks: Array<String> = listOf<String>().toTypedArray(),

        maxExpressionTransformationWeight: String = "1.0",
        maxDistBetweenDiffSteps: String = "", //is it allowed to differentiate expression in one step
        scopeFilter: String = "", //subject scopes which user representation sings is used

        shortErrorDescription: String = "0", //make error message shorter and easier to understand: crop parsed steps from error description
        skipTrivialCheck: Boolean = false, //do not check completeness of transformations, only correctness
        otherGoalData: Map<String, Any>? = null // may contains key="hiddenGoalExpressions" with list of possible goal expression values in structure string
): TexVerificationResult {
    val compiledConfiguration = createConfigurationFromRulePacksAndDetailSolutionCheckingParams(
            rulePacks,
            wellKnownFunctionsString,
            expressionTransformationRulesString,
            maxExpressionTransformationWeight,
            unlimitedWellKnownFunctionsString,
            taskContextExpressionTransformationRules,
            maxDistBetweenDiffSteps,
            scopeFilter,

            wellKnownFunctions,
            unlimitedWellKnownFunctions,
            expressionTransformationRules)
    return checkFactsInTex(
            originalTexSolution,
            startExpressionIdentifier,
            endExpressionIdentifier,
            targetFactIdentifier,
            targetFactPattern,
            comparisonSign,
            additionalFactsIdentifiers,
            shortErrorDescription,
            skipTrivialCheck,
            compiledConfiguration,
            otherGoalData)
}


fun checkSolutionInTexWithCompiledConfiguration(
        originalTexSolution: String, //string with learner solution in Tex format
        compiledConfiguration: CompiledConfiguration,

        //// individual task parameters:
        startExpressionIdentifier: String = "", //Expression, from which learner need to start the transformations
        targetFactPattern: String = "", //Pattern that specify criteria that learner's answer must meet
        comparisonSign: String = "", //Comparison sign
        additionalFactsIdentifiers: String = "", ///Identifiers split by configSeparator - task condition facts should be here, that can be used as rules only for this task

        endExpressionIdentifier: String = "", //Expression, which learner need to deduce
        targetFactIdentifier: String = "", //Fact that learner need to deduce. It is more flexible than startExpressionIdentifier and endExpressionIdentifier, allow to specify inequality like '''EXPRESSION_COMPARISON{(+(/(sin(x);+(1;cos(x)));/(+(1;cos(x));sin(x))))}{<=}{(/(2;sin(x)))}'''

        shortErrorDescription: String = "0", //make error message shorter and easier to understand: crop parsed steps from error description
        skipTrivialCheck: Boolean = false, //do not check completeness of transformations, only correctness
        otherGoalData: Map<String, Any>? = null // may contains key="hiddenGoalExpressions" with list of possible goal expression values in structure string
): TexVerificationResult {
    return checkFactsInTex(
            originalTexSolution,
            startExpressionIdentifier,
            endExpressionIdentifier,
            targetFactIdentifier,
            targetFactPattern,
            comparisonSign,
            additionalFactsIdentifiers,
            shortErrorDescription,
            skipTrivialCheck,
            compiledConfiguration,
            otherGoalData)
}

fun checkChainCorrectnessInTex(originalTexSolution: String) = checkSolutionInTex(
        originalTexSolution = originalTexSolution,
        startExpressionIdentifier = "",
        targetFactPattern = "",
        additionalFactsIdentifiers = "",
        endExpressionIdentifier = "",
        targetFactIdentifier = "",
        comparisonSign = "",
        wellKnownFunctions = listOf(),
        wellKnownFunctionsString = "",
        unlimitedWellKnownFunctions = listOf(),
        unlimitedWellKnownFunctionsString = "",
        expressionTransformationRules = listOf(),
        expressionTransformationRulesString = "",
        taskContextExpressionTransformationRules = "",
        rulePacks = listOf("").toTypedArray(),
        maxExpressionTransformationWeight = "",
        maxDistBetweenDiffSteps = "",
        scopeFilter = "",
        shortErrorDescription = "",
        skipTrivialCheck = true,
        otherGoalData = null
)