package mathhelper.twf.expressiontree

import mathhelper.twf.baseoperations.*


fun calcSinCosDomain(argNum: Int, parentDomain: DefinitionDomain, inverseFunc: (Double) -> Double): DefinitionDomain {
//    assert(argNum == 0)  // sinus has only one argument

    val generalValues = DefinitionDomain.inclusive(1.0, -1.0)
    val x = generalValues.intersectWith(parentDomain)
    val leftVal = inverseFunc(x.leftMostElement())
    val rightVal = inverseFunc(x.rightMostElement())

    val segments = mutableSetOf(
            DomainSegment(
                    leftVal - 2*kotlin.math.PI, false,
                    rightVal - 2*kotlin.math.PI, false
            ),
            DomainSegment(
                    leftVal, false,
                    rightVal, false
            ),
            DomainSegment(
                    leftVal + 2*kotlin.math.PI, false,
                    rightVal + 2*kotlin.math.PI, false
            )
    )

    return DefinitionDomain(segments = segments)
}

val knownOps = listOf(
        BaseOperationDefinitionWithDomain(
                "+",
                generalValuesDomain = DomainAll,
                generalDefinitionDomain = listOf(DomainAll, DomainAll),
                funcToCall={
                    node: ExpressionNode, argNum: Int, plusValues: DefinitionDomain -> plusValues
                }
        ),
        BaseOperationDefinitionWithDomain(
                "-",
                generalValuesDomain = DomainAll,
                generalDefinitionDomain = listOf(DomainAll, DomainAll),
                funcToCall={
                    node: ExpressionNode, argNum: Int, parentDomain: DefinitionDomain -> parentDomain
                }
        ),
        BaseOperationDefinitionWithDomain(
                "",
                generalValuesDomain = DomainAll,
                generalDefinitionDomain = listOf(DomainAll),
                funcToCall = {
                    node: ExpressionNode, argNum: Int, parentDomain: DefinitionDomain -> parentDomain
                }
        ),

        BaseOperationDefinitionWithDomain(
                "*",
                generalValuesDomain = DomainAll,
                generalDefinitionDomain = listOf(DomainAll, DomainAll),
                funcToCall = {
                    node: ExpressionNode, argNum: Int, parentDomain: DefinitionDomain -> parentDomain
                }
        ),
        BaseOperationDefinitionWithDomain(
                "/",
                generalValuesDomain = DomainAll,
                generalDefinitionDomain = listOf(DomainAll, DomainAll.except(0.0)),
                funcToCall = {
                    node: ExpressionNode, argNum: Int, parentDomain: DefinitionDomain -> if (argNum == 0) parentDomain else parentDomain.except(0.0)
                }
        ),
        BaseOperationDefinitionWithDomain(
               "sin",
                generalValuesDomain = DefinitionDomain.inclusive(-1.0, 1.0),
                generalDefinitionDomain = listOf(DomainAll),
                funcToCall = {node, argnum, parentDomain -> calcSinCosDomain(argnum, parentDomain) { a: Double -> kotlin.math.asin(a)} }
        ),
        BaseOperationDefinitionWithDomain(
                baseOp = "cos",
                generalValuesDomain = DefinitionDomain.inclusive(-1.0, 1.0),
                generalDefinitionDomain = listOf(DomainAll),
                funcToCall = {node, argnum, parentDomain -> calcSinCosDomain(argnum, parentDomain) { a: Double -> kotlin.math.acos(a)} }
        )
)

val knownOpsAssociation = knownOps.associateBy { it.baseOp }

class DomainCalculator(
        val expressionNode: ExpressionNode
) {
    val varDomain = MultivariateDefinitionDomain(expressionNode)
    val baseOperationsDefinitions = BaseOperationsDefinitions()

    fun calculate(): DomainCalculator {
        calcInner(expressionNode, DomainAll)
        return this
    }

    private fun calcInner(expressionNode: ExpressionNode, parentDomain: DefinitionDomain) {
        // calcDownUp(expressionNode)
        calcTopDown(expressionNode, parentDomain)
    }

    private fun calcTopDown(expressionNode: ExpressionNode, parentDomain: DefinitionDomain): Unit = when (expressionNode.nodeType) {
        NodeType.VARIABLE -> {
            if (expressionNode.value.toDoubleOrNull() == null) {
                val curval = varDomain.variables.get(expressionNode.value)!!
                varDomain.set(
                        expressionNode.value,
                        parentDomain.intersectWith(curval)
                )
            }
            Unit
        }
        NodeType.FUNCTION -> {
            val domainOp: BaseOperationDefinitionWithDomain = knownOpsAssociation[expressionNode.value] ?: throw UnsupportedOperationException("operation %s is not supported")
            val updatedParentDomain = scaleDomain(expressionNode, parentDomain)
            for ((index, child) in expressionNode.children.withIndex())  {
                val argDomain: DefinitionDomain = domainOp.funcToCall.invoke(child, index, updatedParentDomain)
                calcTopDown(child, argDomain)
            }
            Unit
        }
        NodeType.EMPTY, NodeType.ERROR -> {
        }
    }

    private fun scaleDomain(expressionNode: ExpressionNode, parentDomain: DefinitionDomain): DefinitionDomain {
        val constants = expressionNode.children.mapNotNull { subtreeConstantValueOrNull(it) }
        return constants.fold(parentDomain,  { acc, d -> when (expressionNode.value) {
            "+" -> acc.shiftBy(-d)
            "-" -> acc.shiftBy(d)
            "*" -> acc.scaleBy(1 / d)
            "/" -> acc.scaleBy(d)
            else -> acc
            }
        })
    }

    private fun subtreeConstantValueOrNull(tree: ExpressionNode): Double? =
        when (tree.nodeType) {
            NodeType.FUNCTION -> {
                val innerDoubleValue = if (tree.children.size != 1) null else subtreeConstantValueOrNull(tree.children[0])
                val opToCall = if (innerDoubleValue == null) null else baseOperationsDefinitions.getOperation(tree.value, 1)
                opToCall?.calculatingFunction?.invoke(tree)?.value?.toDoubleOrNull()

            }
            NodeType.VARIABLE -> tree.value.toDoubleOrNull()
            NodeType.EMPTY, NodeType.ERROR -> null
        }
}