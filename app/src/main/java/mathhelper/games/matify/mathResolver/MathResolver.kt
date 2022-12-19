package mathhelper.games.matify.mathResolver

import android.text.Spannable
import android.text.SpannableStringBuilder
import mathhelper.twf.api.stringToExpression
import mathhelper.twf.api.structureStringToExpression
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.games.matify.common.Logger

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
        private var spannableArrayLeft = ArrayList<SpanInfo>()
        private var spannableArrayRight = ArrayList<SpanInfo>()
        private var leftSubstProcessing = false
        private val ruleDelim = " ↬ "

        fun resolveToPlain(expression: ExpressionNode, style: VariableStyle = VariableStyle.DEFAULT,
                           taskType: TaskType = TaskType.DEFAULT,
                           exprType: ExpressionType = ExpressionType.GLOBAL): MathResolverPair {
            if (expression.toString() == "()") {
                Logger.e("MathResolver", "TWF parsing failed")
                return MathResolverPair(null, SpannableStringBuilder("parsing error"))
            }
            currentViewTree = MathResolverNodeBase.getTree(expression, style, taskType)
                ?: return MathResolverPair(null, SpannableStringBuilder("parsing error"))
            return MathResolverPair(currentViewTree, getPlainString(exprType))
        }

        fun resolveToPlain(expression: String, style: VariableStyle = VariableStyle.DEFAULT,
                           taskType: TaskType = TaskType.DEFAULT, structureString: Boolean = false,
                           exprType: ExpressionType = ExpressionType.GLOBAL): MathResolverPair {
            val realExpression = if (!structureString) {
                stringToExpression(expression)
            } else structureStringToExpression(expression)
            return resolveToPlain(realExpression, style, taskType, exprType)
        }

        fun getRule(left: ExpressionNode, right: ExpressionNode,
                               style: VariableStyle = VariableStyle.DEFAULT,
                               taskType: String? = null
        ): SpannableStringBuilder {
            leftSubstProcessing = true
            val from = when (taskType) {
                TaskType.SET.str -> resolveToPlain(left, style, TaskType.SET, ExpressionType.RULE)
                else -> resolveToPlain(left, style, exprType = ExpressionType.RULE)
            }
            leftSubstProcessing = false
            val to = when (taskType) {
                TaskType.SET.str -> resolveToPlain(right, style, TaskType.SET, ExpressionType.RULE)
                else -> resolveToPlain(right, style, exprType = ExpressionType.RULE)
            }
            return getRule(from, to)
        }

        private fun getRule(left: MathResolverPair, right: MathResolverPair): SpannableStringBuilder {
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
            val span = MatifySpan(ExpressionType.RULE)
            for (ls in spannableArrayLeft) {
                if (ls.span is MatifyMultiplierSpan) {
                    for (startEnds in ls.getStartsEnds(totalLen, leftCorr)) {
                        span.setSizeMultiplier(startEnds.first, startEnds.second, ls.span.multiplier)
                    }
                }
            }
            for (rs in spannableArrayRight) {
                val offset = matrixLeft[0].length + ruleDelim.length
                if (rs.span is MatifyMultiplierSpan) {
                    for (startEnds in rs.getStartsEnds(totalLen, rightCorr)) {
                        span.setSizeMultiplier(startEnds.first + offset, startEnds.second + offset, rs.span.multiplier)
                    }
                }
            }
            ruleStr.setSpan(span, 0, ruleStr.count(), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
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
            val spannableArray = ArrayList<SpanInfo>()
            if (type == ExpressionType.RULE) {
                if (leftSubstProcessing) {
                    spannableArrayLeft = ArrayList()
                }
                spannableArrayRight = ArrayList()
            }
            for (i in 0 until currentViewTree.height) {
                stringMatrix.add(" ".repeat(currentViewTree.length))
            }
            baseString = currentViewTree.height / 2
            currentViewTree.getPlainNode(stringMatrix, spannableArray)
            for (i in stringMatrix.indices) {
                result.append(stringMatrix[i])
                //if (i != stringMatrix.size - 1) // TODO: smek how to remove
                    result.append("\n")
            }
            when {
                type != ExpressionType.RULE -> {
                    val span = MatifySpan(type)
                    for (si in spannableArray) {
                        if (si.span is MatifyMultiplierSpan) {
                            for (startEnds in si.getStartsEnds(currentViewTree.length + 1)) {
                                span.setSizeMultiplier(startEnds.first, startEnds.second, si.span.multiplier)
                            }
                        }
                    }
                    result.setSpan(span, 0, currentViewTree.height * (currentViewTree.length + 1) - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
                leftSubstProcessing -> {
                    spannableArrayLeft = spannableArray
                }
                else -> {
                    spannableArrayRight = spannableArray
                }
            }
            return result
        }
    }
}