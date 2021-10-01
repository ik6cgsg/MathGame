package mathhelper.twf.api

import mathhelper.twf.config.CompiledConfiguration
import mathhelper.twf.expressiontree.*


fun normalizeExpressionToUsualForm(
        expression: ExpressionNode,
        compiledConfiguration: CompiledConfiguration = CompiledConfiguration()
) {
    val topExpressionNode = expression.getTopNode()
    topExpressionNode.normalizeNullWeightCommutativeFunctions()
    topExpressionNode.reduceExtraSigns(setOf("+"), setOf("-"))
    topExpressionNode.normalizeSubtructions(compiledConfiguration.functionConfiguration)
    topExpressionNode.normalizeFunctionStringDefinitions(compiledConfiguration.functionConfiguration)
    topExpressionNode.normalizeNumbers()
    topExpressionNode.normalizeParentLinks()
    topExpressionNode.computeNodeIdsAsNumbersInDirectTraversalAndDistancesToRoot()
    topExpressionNode.computeIdentifier()
}