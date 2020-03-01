package spbpu.hsamcp.mathgame.mathResolver

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.text.getSpans
import com.twf.expressiontree.ExpressionNode

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

    fun getColoredAtom(offset: Int, color: Int = Color.RED): ExpressionNode? {
        var resNode: ExpressionNode? = null
        if (tree != null && offset % (tree.length + 1) != tree.length) {
            val lines = offset / (tree.length + 1)
            val correctedOffset = offset - lines
            val y = correctedOffset / tree.length
            val x = correctedOffset % tree.length
            if (insideBox(x, y, tree.leftTop, tree.rightBottom)) {
                val mathNode = getNode(tree, x, y) ?: tree
                resNode = mathNode.origin
                val colorSpans = matrix.getSpans<ForegroundColorSpan>(0, matrix.length)
                for (cs in colorSpans) {
                    matrix.removeSpan(cs)
                }
                val boldSpans = matrix.getSpans<StyleSpan>(0, matrix.length)
                for (bs in boldSpans) {
                    matrix.removeSpan(bs)
                }
                for (i in mathNode.leftTop.y..mathNode.rightBottom.y) {
                    val start = i * (tree.length + 1) + mathNode.leftTop.x
                    val end = i * (tree.length + 1) + mathNode.rightBottom.x + 1
                    matrix.setSpan(ForegroundColorSpan(color),
                        start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                    matrix.setSpan(StyleSpan(Typeface.BOLD),
                        start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
            }
        }
        return resNode
    }
}