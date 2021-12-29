package mathhelper.twf.mainpoints

import mathhelper.twf.config.*
import mathhelper.twf.expressiontree.ExpressionNodeConstructor
import mathhelper.twf.expressiontree.ExpressionSubstitution
import mathhelper.twf.factstransformations.*
import mathhelper.twf.logs.MessageType
import mathhelper.twf.logs.log


fun compiledConfigurationBySettings(
        wellKnownFunctionsString: String = "",
        expressionTransformationRulesString: String = "",
        maxExpressionTransformationWeightString: String = "",
        unlimitedWellKnownFunctionsString: String = "",
        taskContextExpressionTransformationRulesString: String = "",
        maxDistBetweenDiffStepsString: String = "",
        scopeFilterString: String = "",

        wellKnownFunctions: List<FunctionIdentifier> = listOf(),
        unlimitedWellKnownFunctions: List<FunctionIdentifier> = listOf(),
        expressionTransformationRules: List<ExpressionSubstitution> = listOf()

): CompiledConfiguration {
    val functionConfiguration = FunctionConfiguration(scopeFilterString.split(configSeparator).map { it.trim() }.toSet())
    if (wellKnownFunctions.isNotEmpty()){
        functionConfiguration.notChangesOnVariablesInComparisonFunction = wellKnownFunctions
    } else if (wellKnownFunctionsString.isNotBlank()) {
        val pairs = pairsFromString(wellKnownFunctionsString)
        functionConfiguration.notChangesOnVariablesInComparisonFunction = pairs.map { FunctionIdentifier(it.first, it.second.toInt()) }.toMutableList()
    }
    if (unlimitedWellKnownFunctions.isNotEmpty()){
        functionConfiguration.notChangesOnVariablesInComparisonFunctionWithoutTransformations = unlimitedWellKnownFunctions
    } else if (unlimitedWellKnownFunctionsString.isNotBlank()) {
        val pairs = pairsFromString(unlimitedWellKnownFunctionsString)
        functionConfiguration.notChangesOnVariablesInComparisonFunctionWithoutTransformations = pairs.map { FunctionIdentifier(it.first, it.second.toInt()) }.toMutableList()
    }
    if (expressionTransformationRulesString.isNotEmpty()) {
        functionConfiguration.treeTransformationRules = functionConfiguration.treeTransformationRules.filter { it.isImmediate == true }.toMutableList()
        val pairs = pairsFromString(expressionTransformationRulesString)
        functionConfiguration.treeTransformationRules.addAll(pairs.map { TreeTransformationRule(it.first, it.second) })
    }
    if (taskContextExpressionTransformationRulesString.isNotEmpty()) {
        functionConfiguration.taskContextTreeTransformationRules = functionConfiguration.taskContextTreeTransformationRules.filter { it.isImmediate == true }.toMutableList()
        val pairs = pairsFromString(taskContextExpressionTransformationRulesString)
        functionConfiguration.taskContextTreeTransformationRules.addAll(pairs.map { TreeTransformationRule(it.first, it.second) })
    }
    val compiledConfiguration = CompiledConfiguration(
            functionConfiguration = functionConfiguration
    )
    if (expressionTransformationRules.isNotEmpty()){
        compiledConfiguration.compiledExpressionTreeTransformationRules.clear()
        compiledConfiguration.compiledExpressionTreeTransformationRules.addAll(expressionTransformationRules)
    }
    if (maxExpressionTransformationWeightString.isNotBlank()) {
        compiledConfiguration.comparisonSettings.maxExpressionTransformationWeight = maxExpressionTransformationWeightString.toDouble()
    }
    if (maxDistBetweenDiffStepsString.isNotBlank()) {
        compiledConfiguration.comparisonSettings.maxDistBetweenDiffSteps = maxDistBetweenDiffStepsString.toDouble()
    }
    return compiledConfiguration
}


fun combineSolutionRoot(targetFactIdentifier: String, transformationChainParser: TransformationChainParser, compiledConfiguration: CompiledConfiguration,
                        startExpressionIdentifier: String = "", endExpressionIdentifier: String = "", comparisonSign: String = ""): MainLineAndNode {
    if (startExpressionIdentifier.isNotBlank() && transformationChainParser.root.factTransformationChains.isEmpty() &&
            transformationChainParser.root.expressionTransformationChains.size == 1) {
        val expressionNodeConstructor = ExpressionNodeConstructor(compiledConfiguration.functionConfiguration)
        val startExpression = expressionNodeConstructor.construct(startExpressionIdentifier)
        transformationChainParser.root.expressionTransformationChains.first().chain.add(0, Expression(data = startExpression, parent = transformationChainParser.root))

        if (endExpressionIdentifier.isNotBlank()){
            val endExpression = expressionNodeConstructor.construct(endExpressionIdentifier)
            transformationChainParser.root.expressionTransformationChains.first().chain.add(Expression(data = endExpression, parent = transformationChainParser.root))
        }

        if (comparisonSign.isNotEmpty()) {
            transformationChainParser.root.expressionTransformationChains.first().comparisonType = valueOfComparisonType(comparisonSign)
        }
    }
    return if (targetFactIdentifier.contains("}{=}{") || targetFactIdentifier.contains("}{<}{") || targetFactIdentifier.contains("}{>}{") || targetFactIdentifier.contains("}{<=}{") || targetFactIdentifier.contains("}{>=}{")) {
        val taskTargetFact = log.factConstructorViewer.constructFactByIdentifier(targetFactIdentifier)
        val taskTargetRoot = if (taskTargetFact.type() == transformationChainParser.root.type()) taskTargetFact
        else MainLineAndNode(inFacts = mutableListOf(taskTargetFact))
        val newRoot = MainLineAndNode(factTransformationChains = mutableListOf(MainChain(mutableListOf(
                transformationChainParser.root,
                taskTargetRoot
        ))))
        log.addMessageWithFactDetail({ "solution with task target joined: " }, newRoot, MessageType.USER)
        newRoot
    } else {
        transformationChainParser.root
    }
}


fun pairsFromString(data: String): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    val parts = data.trim().split(configSeparator).map { it.trim() }
    var i = 1
    while (i < parts.size) {
        result.add(Pair(parts[i - 1], parts[i]))
        i += 2
    }
    return result
}

fun pairsStringIntFromString(data: String): List<Pair<String, Int>> {
    val result = mutableListOf<Pair<String, Int>>()
    val parts = data.trim().split(configSeparator).map { it.trim() }
    var i = 1
    while (i < parts.size) {
        result.add(Pair(parts[i - 1], parts[i].toInt()))
        i += 2
    }
    return result
}

fun FactConstructorViewer.additionalFactsFromItsIdentifiers(additionalFactsIdentifiers: String): List<MainChainPart> {
    val identifiers = additionalFactsIdentifiers.split(configSeparator).map { it.trim() }.filter { it.isNotEmpty() }
    return identifiers.map { constructFactByIdentifier(it) }
}

val configSeparator = ";;;"
val errorPrefix = "Error"
val syntaxErrorPrefix = "Syntax"