package mathhelper.games.matify.mathResolver

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.text.getSpans
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.ColorName
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.common.ThemeController

class MathResolverPair(val tree: MathResolverNodeBase?, val matrix: SpannableStringBuilder) {
    var height = matrix.count { it == '\n' }

    private fun insideBox(x: Int, y: Int, lt: Point, rb: Point): Boolean {
        if (x >= lt.x && x <= rb.x && y >= lt.y && y <= rb.y) {
            return true
        }
        return false
    }

    private fun getNode(node: MathResolverNodeBase, x: Int, y: Int): MathResolverNodeBase? {
        var resNode: MathResolverNodeBase? = null
        for (n in node.children) {
            if (insideBox(x, y, n.leftTop, n.rightBottom)) {
                if (n.children.isEmpty()) {
                    resNode = n
                    break
                }
                resNode = getNode(n, x, y) ?: n
            }
        }
        return resNode
    }

    private fun findNodeByExpression(currentTree: MathResolverNodeBase?, expressionNode: ExpressionNode): MathResolverNodeBase? {
        if (currentTree == null)
            return null
        if (expressionNode.nodeId == currentTree.origin.nodeId)
            return currentTree
        var res: MathResolverNodeBase? = null
        for (child in currentTree.children)
            res = findNodeByExpression(child, expressionNode)?:res
        return res
    }

    private fun applyStyleToMathNode (mathNode: MathResolverNodeBase, styleLambda: (start: Int, end: Int) -> Unit) {
        if (tree != null) {
            for (i in mathNode.leftTop.y..mathNode.rightBottom.y) {
                val start = i * (tree.length + 1) + mathNode.leftTop.x
                val end = i * (tree.length + 1) + mathNode.rightBottom.x + 1
                styleLambda(start, end)
            }
        }
    }

    fun deleteSpanForAtom(atom: ExpressionNode) {
        val mathNode = findNodeByExpression(tree, atom)
        applyStyleToMathNode(mathNode ?: return) { start, end -> MatifySpan.unselect(start, end, true) }
    }

    fun getColoredAtom(x: Int, y: Int, multiSelectionMode: Boolean = false, color: Int = Color.RED): ExpressionNode? {
        var resNode: ExpressionNode? = null
        if (tree != null ) {
            if (insideBox(x, y, tree.leftTop, tree.rightBottom)) {
                val mathNode = getNode(tree, x, y) ?: tree
                resNode = mathNode.origin
                if (!multiSelectionMode) {
                    MatifySpan.clearSelected()
                    applyStyleToMathNode(mathNode) { start, end ->
                        MatifySpan.select(start, end, color)
                    }
                }
            }
        }
        return resNode
    }

    fun recolorExpressionInMultiSelectionMode(
        selectedItems: List<ExpressionNode>,
        topItem: ExpressionNode?,
        color: Int = Color.YELLOW
        //otherColor: Int = Color.YELLOW
    ) {
        MatifySpan.clearSelected()
        if (topItem != null) {
            applyStyleToMathNode(findNodeByExpression(tree, topItem) ?: return) { start, end ->
                MatifySpan.select(start, end)
            }
        }
        val selectedItemsSorted = selectedItems.sortedBy { it.nodeId }
        for (node in selectedItemsSorted) {
            applyStyleToMathNode(findNodeByExpression(tree, node) ?: return) { start, end ->
                MatifySpan.select(start, end, multiselection = true)
            }
        }
    }
}