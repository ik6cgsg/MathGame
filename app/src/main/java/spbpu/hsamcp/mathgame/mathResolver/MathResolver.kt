package spbpu.hsamcp.mathgame.mathResolver

import android.text.SpannableStringBuilder
import com.twf.api.stringToExpression
import com.twf.expressiontree.ExpressionNode
import java.lang.Integer.max
import java.lang.Math.ceil
import kotlin.math.ceil

fun String.replaceByIndex(i: Int, replacement: String): String {
    return this.substring(0, i) + replacement + this.substring(i + replacement.length)
}

class MathResolver {
    companion object {
        private lateinit var stringMatrix: ArrayList<String>
        private var baseString = 0
        private lateinit var currentViewTree: MathResolverNodeBase
        private var spannableArray = ArrayList<SpanInfo>()

        fun resolveToPlain(expression: ExpressionNode): SpannableStringBuilder {
            currentViewTree = MathResolverNodeBase.getTree(expression)
            return getPlainString()
        }

        fun resolveToPlain(expression: String): SpannableStringBuilder {
            val realExpression = stringToExpression(expression)
            if (realExpression.identifier.contentEquals("()")) {
                return SpannableStringBuilder("parsing error")
            }
            currentViewTree = MathResolverNodeBase.getTree(realExpression)
            return getPlainString()
        }

        fun getRule(stringMatrixLeft: String, stringmatrixRight: String, delim: String): String {
            val matrixLeft = stringMatrixLeft.split("\n")
            val matrixRight = stringmatrixRight.split("\n")
            var result = ""
            val hLeft = matrixLeft.size - 1
            val hRight = matrixRight.size - 1
            val maxH = maxOf(hLeft, hRight)
            val edge = (maxH - minOf(hLeft, hRight)) / 2
            val middle = maxH / 2
            if (maxH == hLeft) {
                for (i in 0 until hLeft) {
                    result += matrixLeft[i]
                    result += if (i == middle) {
                        delim
                    } else {
                        " ".repeat(delim.length)
                    }
                    result += if (i >= edge && i < hLeft - edge) {
                        matrixRight[i - edge]
                    } else {
                        " ".repeat(matrixRight[0].length)
                    }
                    result += "\n"
                }
            } else {
                for (i in 0 until hRight) {
                    result += if (i >= edge && i < hLeft - edge) {
                        matrixLeft[i - edge]
                    } else {
                        " ".repeat(matrixLeft[0].length)
                    }
                    result += if (i == middle) {
                        delim
                    } else {
                        " ".repeat(delim.length)
                    }
                    result += matrixRight[i]
                    result += "\n"
                }
            }
            return result
        }

        private fun getPlainString(): SpannableStringBuilder {
            var result = SpannableStringBuilder("")
            // matrix init
            stringMatrix = ArrayList()
            for (i in 0 until currentViewTree.height) {
                stringMatrix.add(" ".repeat(currentViewTree.length))
            }
            baseString = currentViewTree.height / 2
            currentViewTree.getPlainNode(stringMatrix, spannableArray)
            for (str in stringMatrix) {
                result.append(str).append("\n")
            }
            for (si in spannableArray) {
                //val off = si.strInd * (currentViewTree.length + 1)
                for (i in 0 until currentViewTree.height - 1) {
                    val off = i * (currentViewTree.length + 1)
                    result.setSpan(si.span, off + si.start, off + si.end, si.flag)
                }
            }
            return result
        }
    }
}