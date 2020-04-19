package spbpu.hsamcp.mathgame.mathResolver

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.core.text.getSpans
import com.twf.api.stringToExpression
import com.twf.expressiontree.ExpressionNode
import java.lang.Integer.max
import java.lang.Math.ceil
import kotlin.math.ceil

fun String.replaceByIndex(i: Int, replacement: String): String {
    return this.substring(0, i) + replacement + this.substring(i + replacement.length)
}

enum class VariableStyle {
    DEFAULT,
    GREEK
}

class MathResolver {
    companion object {
        private lateinit var stringMatrix: ArrayList<String>
        private var baseString = 0
        private lateinit var currentViewTree: MathResolverNodeBase
        private var spannableArray = ArrayList<SpanInfo>()
        //private val ruleDelim = " ⟼ "
        //private const val ruleDelim = " → "
        private const val ruleDelim = " ~> "

        fun resolveToPlain(expression: ExpressionNode, style: VariableStyle = VariableStyle.DEFAULT): MathResolverPair {
            currentViewTree = MathResolverNodeBase.getTree(expression, style)
            return MathResolverPair(currentViewTree, getPlainString())
        }

        fun resolveToPlain(expression: String, style: VariableStyle = VariableStyle.DEFAULT): MathResolverPair {
            val realExpression = stringToExpression(expression)
            if (realExpression.identifier.contentEquals("()")) {
                return MathResolverPair(null, SpannableStringBuilder("parsing error"))
            }
            currentViewTree = MathResolverNodeBase.getTree(realExpression, style)
            return MathResolverPair(currentViewTree, getPlainString())
        }

        fun getRule(left: MathResolverPair, right: MathResolverPair): SpannableStringBuilder {
            val leftSpans = SpanInfo.getSpanInfoArray(left.matrix)
            val rightSpans = SpanInfo.getSpanInfoArray(right.matrix)
            val matrixLeft = left.matrix.split("\n") as ArrayList
            val matrixRight = right.matrix.split("\n") as ArrayList
            matrixLeft.removeAt(matrixLeft.size - 1)
            matrixRight.removeAt(matrixRight.size - 1)
            val leadingTree: MathResolverNodeBase
            val secTree: MathResolverNodeBase
            var leftCorr = 0
            var rightCorr = 0
            if (left.tree!!.height > right.tree!!.height) {
                leadingTree = left.tree
                secTree = right.tree
                rightCorr = correctMatrix(matrixRight, leadingTree, secTree)
            } else {
                leadingTree = right.tree
                secTree = left.tree
                leftCorr = correctMatrix(matrixLeft, leadingTree, secTree)
            }
            val ruleStr = SpannableStringBuilder(mergeMatrices(matrixLeft, matrixRight, leadingTree.baseLineOffset))
            val totalLen = matrixLeft[0].length + ruleDelim.length + matrixRight[0].length + 1
            for (ls in leftSpans) {
                val offset = (ls.strInd + leftCorr) * totalLen
                ruleStr.setSpan(ls.span, offset + ls.start, offset + ls.end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
            for (rs in rightSpans) {
                val offset = (rs.strInd + rightCorr) * totalLen + matrixLeft[0].length + ruleDelim.length
                ruleStr.setSpan(rs.span, offset + rs.start, offset + rs.end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
            return ruleStr
        }

        private fun correctMatrix(matrix: ArrayList<String>, leadingTree: MathResolverNodeBase,
                                  secTree: MathResolverNodeBase): Int {
            for (i in 0 until leadingTree.height - secTree.height) {
                matrix.add(" ".repeat(secTree.length))
            }
            val diff = leadingTree.baseLineOffset - secTree.baseLineOffset
            if (diff > 0) {
                for (i in 0 until diff) {
                    matrix.add(0, matrix[matrix.size - 1])
                    matrix.removeAt(matrix.size - 1)
                }
            }
            return diff
        }

        private fun mergeMatrices(left: ArrayList<String>, right: ArrayList<String>, baseLine: Int): String {
            var res = ""
            val linesNum = left.size
            for (i in 0 until linesNum) {
                res += left[i]
                res += if (i == baseLine) {
                    ruleDelim
                } else {
                    " ".repeat(ruleDelim.length)
                }
                res += right[i]
                if (i != linesNum - 1) {
                    res += "\n"
                }
            }
            return res
        }

        private fun getPlainString(): SpannableStringBuilder {
            var result = SpannableStringBuilder("")
            // matrix init
            stringMatrix = ArrayList()
            spannableArray = ArrayList()
            for (i in 0 until currentViewTree.height) {
                stringMatrix.add(" ".repeat(currentViewTree.length))
            }
            baseString = currentViewTree.height / 2
            currentViewTree.getPlainNode(stringMatrix, spannableArray)
            for (str in stringMatrix) {
                result.append(str).append("\n")
            }
            for (si in spannableArray) {
                val off = si.strInd * (currentViewTree.length + 1)
                result.setSpan(si.span, off + si.start, off + si.end, si.flag)
            }
            return result
        }
    }
}