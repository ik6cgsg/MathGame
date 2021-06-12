package mathhelper.games.matify.mathResolver

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import api.stringToExpression
import api.structureStringToExpression
import expressiontree.ExpressionNode

fun String.replaceByIndex(i: Int, replacement: String): String {
    return this.substring(0, i) + replacement + this.substring(i + replacement.length)
}

enum class VariableStyle {
    DEFAULT,
    GREEK
}

enum class TaskType(val str: String) {
    DEFAULT(""),
    SET("setTheory")
}

class MathResolver {
    companion object {
        private lateinit var stringMatrix: ArrayList<String>
        private var baseString = 0
        private lateinit var currentViewTree: MathResolverNodeBase
        private var spannableArray = ArrayList<SpanInfo>()
        private val ruleDelim = " ↬ "

        fun resolveToPlain(expression: ExpressionNode, style: VariableStyle = VariableStyle.DEFAULT,
                           taskType: TaskType = TaskType.DEFAULT,
                           exprType: ExpressionType = ExpressionType.GLOBAL): MathResolverPair {
            if (expression.toString() == "()") {
                Log.e("MathResolver", "TWF parsing failed")
                return MathResolverPair(null, SpannableStringBuilder("parsing error"))
            }
            currentViewTree = MathResolverNodeBase.getTree(expression, style, taskType)
                ?: return MathResolverPair(null, SpannableStringBuilder("parsing error"))
            return MathResolverPair(currentViewTree, getPlainString(exprType))
        }

        fun resolveToPlain(expression: String, style: VariableStyle = VariableStyle.DEFAULT,
                           taskType: TaskType = TaskType.DEFAULT, structureString:Boolean = false,
                           exprType: ExpressionType = ExpressionType.GLOBAL): MathResolverPair {
            val realExpression = if (!structureString) {
                stringToExpression(expression)
            } else structureStringToExpression(expression)
            return resolveToPlain(realExpression, style, taskType, exprType)
        }

        fun getRule(left: MathResolverPair, right: MathResolverPair): SpannableStringBuilder {
            val leftSpans = SpanInfo.getSpanInfoArray(left.matrix)
            val rightSpans = SpanInfo.getSpanInfoArray(right.matrix)
            val matrixLeft = left.matrix.split("\n") as ArrayList
            val matrixRight = right.matrix.split("\n") as ArrayList
            matrixLeft.removeAt(matrixLeft.size - 1)
            matrixRight.removeAt(matrixRight.size - 1)
            val leadingTree: MathResolverNodeBase
            var leftCorr = 0
            var rightCorr = 0
            if (left.tree!!.baseLineOffset > right.tree!!.baseLineOffset) {
                leadingTree = left.tree
                rightCorr = correctMatrixByBaseLine(matrixRight, left.tree, right.tree)
                right.tree.height += rightCorr
            } else {
                leadingTree = right.tree
                leftCorr = correctMatrixByBaseLine(matrixLeft, right.tree, left.tree)
                left.tree.height += leftCorr
            }
            if (left.tree.height > right.tree.height) {
                correctMatrixByHeight(matrixRight, left.tree, right.tree)
            } else {
                correctMatrixByHeight(matrixLeft, right.tree, left.tree)
            }
            val ruleStr = SpannableStringBuilder(mergeMatrices(matrixLeft, matrixRight, leadingTree.baseLineOffset))
            val totalLen = matrixLeft[0].length + ruleDelim.length + matrixRight[0].length + 1
            /*for (ls in leftSpans) {
                val offset = (ls.strInd + leftCorr) * totalLen
                ruleStr.setSpan(ls.span, offset + ls.start, offset + ls.end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
            for (rs in rightSpans) {
                val offset = (rs.strInd + rightCorr) * totalLen + matrixLeft[0].length + ruleDelim.length
                ruleStr.setSpan(rs.span, offset + rs.start, offset + rs.end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }*/
            ruleStr.setSpan(MatifySpan(ExpressionType.RULE),
                0, ruleStr.count(), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            return ruleStr
        }


        private fun correctMatrixByBaseLine (matrix: ArrayList<String>, leadingTree: MathResolverNodeBase,
                                               secTree: MathResolverNodeBase): Int {
            val diff = leadingTree.baseLineOffset - secTree.baseLineOffset
            for (i in 0 until diff) {
                matrix.add(0, " ".repeat(secTree.length))
            }
            return diff
        }

        private fun correctMatrixByHeight (matrix: ArrayList<String>, leadingTree: MathResolverNodeBase,
                                             secTree: MathResolverNodeBase): Int {
            val diff = leadingTree.height - secTree.height
            for (i in 0 until diff) {
                matrix.add(" ".repeat(secTree.length))
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

        private fun getPlainString(type: ExpressionType): SpannableStringBuilder {
            var result = SpannableStringBuilder("")
            // matrix init
            stringMatrix = ArrayList()
            spannableArray = ArrayList()
            for (i in 0 until currentViewTree.height) {
                stringMatrix.add(" ".repeat(currentViewTree.length))
            }
            baseString = currentViewTree.height / 2
            currentViewTree.getPlainNode(stringMatrix, spannableArray)
            for (i in stringMatrix.indices) {
                result.append(stringMatrix[i])
                //if (i != stringMatrix.size - 1)
                    result.append("\n")
            }
            if (type == ExpressionType.GLOBAL) {
                result.setSpan(
                    MatifySpan(ExpressionType.GLOBAL),
                    0, currentViewTree.height * (currentViewTree.length + 1) - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
            /*for (si in spannableArray) {
                val off = si.strInd * (currentViewTree.length + 1)
                result.setSpan(si.span, off + si.start, off + si.end, si.flag)
            }*/
            return result
        }
    }
}