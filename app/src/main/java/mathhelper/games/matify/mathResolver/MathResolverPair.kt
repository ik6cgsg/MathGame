package mathhelper.games.matify.mathResolver

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.text.getSpans
import expressiontree.ExpressionNode

class MathResolverPair(val tree: MathResolverNodeBase?, val matrix: SpannableStringBuilder) {
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

    fun clearColorSpans (start: Int, end: Int) {
        val colorSpans = matrix.getSpans<ForegroundColorSpan>(start, end)
        for (cs in colorSpans) {
            matrix.removeSpan(cs)
        }
    }

    fun clearBoldSpans (start: Int, end: Int) {
        val boldSpans = matrix.getSpans<StyleSpan>(start, end)
        for (bs in boldSpans) {
            matrix.removeSpan(bs)
        }
    }

    fun clearAllSpans (start: Int, end: Int) {
        clearColorSpans(start, end)
        clearBoldSpans(start, end)
    }

    fun applyStyleToMathNode (mathNode: MathResolverNodeBase, styleLambda: (start: Int, end: Int) -> Unit) {
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
        applyStyleToMathNode(mathNode ?: return,
            { start, end -> clearAllSpans(start, end)}
        )
    }

    fun getColoredAtom(offset: Int, multiSelectionMode: Boolean = false, color: Int = Color.RED): ExpressionNode? {
        var resNode: ExpressionNode? = null
        if (tree != null && offset % (tree.length + 1) != tree.length) {
            val lines = offset / (tree.length + 1)
            val correctedOffset = offset - lines
            val y = correctedOffset / tree.length
            val x = correctedOffset % tree.length
            if (insideBox(x, y, tree.leftTop, tree.rightBottom)) {
                val mathNode = getNode(tree, x, y) ?: tree
                resNode = mathNode.origin
                if (!multiSelectionMode) {
                    clearAllSpans(0, matrix.length)
                    applyStyleToMathNode(mathNode,
                        { start, end -> run {
                            matrix.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                            matrix.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        }}
                    )
                }
            }
        }
        return resNode
    }

    fun recolorExpressionInMultiSelectionMode (selectedItems: List<ExpressionNode>, topItem: ExpressionNode?, color: Int = Color.RED, otherColor: Int = Color.YELLOW) {
        clearAllSpans(0, matrix.length)
        if (topItem != null) {
            applyStyleToMathNode(findNodeByExpression(tree, topItem) ?: return, { start, end -> matrix.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE) })
        }
        val selectedItemsSorted = selectedItems.sortedBy { it.nodeId }
        for (node in selectedItemsSorted) {
            applyStyleToMathNode(
                findNodeByExpression(tree, node) ?: return, { start, end -> run {
                    val colorSpans = matrix.getSpans<ForegroundColorSpan>(start, end)
                    val firstColorSpansCount = colorSpans.count { it.foregroundColor == color }
                    val otherColorSpansCount = colorSpans.count { it.foregroundColor == otherColor }
                    val selectionColor = if (firstColorSpansCount > otherColorSpansCount) {
                        // internal selection
                        otherColor
                    } else {
                        color
                    }
                    matrix.setSpan(ForegroundColorSpan(selectionColor), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }})
        }
    }
}