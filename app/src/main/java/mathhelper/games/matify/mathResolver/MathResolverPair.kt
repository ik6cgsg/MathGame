package mathhelper.games.matify.mathResolver

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.text.getSpans
import androidx.core.text.set
import expressiontree.ExpressionNode
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.ColorName
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.common.ThemeController

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

    private fun clearColorSpans (start: Int, end: Int, multiSelect: Boolean = false) {
        val colorSpans = matrix.getSpans<ForegroundColorSpan>(start, end)
        if (multiSelect) {
            for (cs in colorSpans.sortedBy { matrix.getSpanStart(it) }) {
                val spanStart = matrix.getSpanStart(cs)
                val spanEnd = matrix.getSpanEnd(cs)
                if (spanStart >= start && spanEnd <= end) {
                    matrix.removeSpan(cs)
                    val defColor = ThemeController.shared.getColorByTheme(Storage.shared.theme(PlayScene.shared.playActivity!!), ColorName.MULTISELECTION_COLOR)
                    val color = AndroidUtil.lighterColor(cs.foregroundColor, defColor)
                    matrix.setSpan(ForegroundColorSpan(color ?: continue), spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
            }
        } else {
            for (cs in colorSpans) {
                matrix.removeSpan(cs)
            }
        }
    }

    private fun clearBoldSpans (start: Int, end: Int, multiSelect: Boolean = false) {
        val boldSpans = matrix.getSpans<StyleSpan>(start, end)
        if (multiSelect) {
            for (cs in boldSpans) {
                if (matrix.getSpanStart(cs) == start && matrix.getSpanEnd(cs) == end) {
                    matrix.removeSpan(cs)
                }
            }
        } else {
            for (bs in boldSpans) {
                matrix.removeSpan(bs)
            }
        }
    }

    private fun clearAllSpans (start: Int, end: Int, multiSelect: Boolean = false) {
        clearColorSpans(start, end, multiSelect)
        clearBoldSpans(start, end, multiSelect)
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
        applyStyleToMathNode(mathNode ?: return) { start, end -> clearAllSpans(start, end, true) }
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
                    applyStyleToMathNode(mathNode) { start, end ->
                        matrix.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        matrix.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
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
        clearAllSpans(0, matrix.length)
        if (topItem != null) { // TODO: was ist das
            applyStyleToMathNode(findNodeByExpression(tree, topItem) ?: return) { start, end ->
                matrix.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        val selectedItemsSorted = selectedItems.sortedBy { it.nodeId }
        for (node in selectedItemsSorted) {
            applyStyleToMathNode(findNodeByExpression(tree, node) ?: return) { start, end ->
                val colorSpans = matrix.getSpans<ForegroundColorSpan>(start, end)
                val selectionColor = AndroidUtil.darkenColor(color, colorSpans.size)
                matrix.setSpan(ForegroundColorSpan(selectionColor), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
    }
}