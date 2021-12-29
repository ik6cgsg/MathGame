package mathhelper.twf.visualization

import mathhelper.twf.standartlibextensions.findClosestPlaceToTargetOnTheSameLevel
import mathhelper.twf.standartlibextensions.findFirstNotInTagNotInMtext
import mathhelper.twf.standartlibextensions.skipClosingTags
import mathhelper.twf.standartlibextensions.skipFromRemainingExpressionWhileClosingTagNotFound

data class ColoringTask(
        val startPosition: Int,
        val endPosition: Int,
        val color: String
) {
    fun texColor() = when (color) {
        "7F00FF" -> "purple"
        "007F00" -> "green"
        "FF00FF" -> "yellow"
        "FF0000" -> "red"
        else -> "black"
    }
}

fun setBackgroundColorMathMl(string: String, color: String) = string.replaceFirst("MathML\">", "MathML\" mathcolor=\"#${color}\">")

fun brushMathMl(string: String, coloringTasks: List<ColoringTask>): String { //coloringTasks cannot have intersections
    val result = StringBuilder()
    var lastHandledPosition = 0
    val sortedColoringTasks = coloringTasks.filter { it.startPosition <= it.endPosition }.sortedBy { it.startPosition }
    for (task in sortedColoringTasks) {
        val startIndexPair = string.findAnyOf(listOf("<mo>", "<mn>", "<mi>"), task.startPosition - 4)
        val approxStartPosition = if (startIndexPair != null && startIndexPair.first <= task.startPosition) startIndexPair.first
        else task.startPosition
        val startPosition = skipClosingTags(string, findFirstNotInTagNotInMtext(string, approxStartPosition))
        if ((task.startPosition == task.endPosition) || (startPosition >= string.lastIndex)){
            result.append(colorOnlyNewLineSplittingSign(string.substring(lastHandledPosition, startPosition)))
            result.append(coloringStart(task.color) + underlining + coloringEnd)
            lastHandledPosition = startPosition
        } else {
            val endPosition = findClosestPlaceToTargetOnTheSameLevel(string, startPosition, task.endPosition)
            result.append(string.substring(lastHandledPosition, startPosition))
            result.append(coloringStart(task.color))
            result.append(colorOnlyNewLineSplittingSign(string.substring(startPosition, endPosition)))
            result.append(coloringEnd)
            lastHandledPosition = endPosition
        }
    }
    result.append(string.substring(lastHandledPosition))
    return result.toString()
}

fun dropPerformedMathMLBrushing (string: String): String {
    var result = dropPerformedMathMLBrushingInternal(string)
    var resultLast = string
    while (result != resultLast){
        val newResult = dropPerformedMathMLBrushingInternal(result)
        resultLast = result
        result = newResult
    }
    return result
}

fun dropPerformedMathMLBrushingInternal (string: String): String {
    val result = StringBuilder()
    var currentPosition = 0
    while (currentPosition < string.length) {
        val indexPair = string.findAnyOf(listOf(
                coloringBoldHeaderStart, coloringBoldHeaderStartGeneral,
                coloringHeaderStart, coloringHeaderStartGeneral,
                boldHeaderStart, boldHeaderStartGeneral), currentPosition)
        if (indexPair != null){
            if (indexPair.second == coloringBoldHeaderStart) {
                result.append(string.substring(currentPosition, indexPair.first))
                currentPosition = indexPair.first + coloringBoldStartLength
                val newCurrentPosition = skipFromRemainingExpressionWhileClosingTagNotFound("mrow", string, currentPosition)
                result.append(string.substring(currentPosition, newCurrentPosition))
                currentPosition = newCurrentPosition + coloringEndLength
            } else if (indexPair.second == coloringHeaderStart) {
                result.append(string.substring(currentPosition, indexPair.first))
                currentPosition = indexPair.first + coloringStartLength
                val newCurrentPosition = skipFromRemainingExpressionWhileClosingTagNotFound("mrow", string, currentPosition)
                result.append(string.substring(currentPosition, newCurrentPosition))
                currentPosition = newCurrentPosition + coloringEndLength
            } else if (indexPair.second == boldHeaderStart) {
                result.append(string.substring(currentPosition, indexPair.first))
                currentPosition = indexPair.first + boldStartLength
                val newCurrentPosition = skipFromRemainingExpressionWhileClosingTagNotFound("mrow", string, currentPosition)
                result.append(string.substring(currentPosition, newCurrentPosition))
                currentPosition = newCurrentPosition + coloringEndLength
            } else if (indexPair.second == coloringBoldHeaderStartGeneral){
                result.append(string.substring(currentPosition, indexPair.first))
                currentPosition = indexPair.first + " mathvariant=\"bold\" mathcolor=\"#123456\"".length
            } else if (indexPair.second == coloringHeaderStartGeneral){
                result.append(string.substring(currentPosition, indexPair.first))
                currentPosition = indexPair.first + " mathcolor=\"#123456\"".length
            } else if (indexPair.second == boldHeaderStartGeneral){
                result.append(string.substring(currentPosition, indexPair.first))
                currentPosition = indexPair.first + " mathvariant=\"bold\"".length
            } else break
        } else {
            result.append(string.substring(currentPosition))
            break
        }
    }
    return result.toString()
}

private fun coloringStart(color: String) = "$coloringBoldHeaderStart$color\">"
private val coloringEnd = "</mrow>"
private val coloringBoldHeaderStartGeneral = " mathvariant=\"bold\" mathcolor=\"#"
private val coloringBoldHeaderStart = "<mrow mathvariant=\"bold\" mathcolor=\"#"
private val coloringBoldStartLength = "<mrow mathvariant=\"bold\" mathcolor=\"#123456\">".length
private val coloringHeaderStartGeneral = " mathcolor=\"#"
private val coloringHeaderStart = "<mrow mathcolor=\"#"
private val coloringStartLength = "<mrow mathcolor=\"#123456\">".length
private val boldHeaderStartGeneral = " mathvariant=\"bold\""
private val boldHeaderStart = "<mrow mathvariant=\"bold\""
private val boldStartLength = "<mrow mathvariant=\"bold\">".length
private val coloringEndLength = coloringEnd.length
private val coloringOffset = coloringStartLength + coloringEndLength
private val underlining = "<mi>_</mi>"  //or "<mo>&#x25A1;</mo>"

private fun colorOnlyNewLineSplittingSign (splittingSign: String): String {
    var newSplittingSign = splittingSign
    if (splittingSign.isEmpty() || splittingSign.startsWith("<mspace linebreak=\"newline\"/>")){
        newSplittingSign = "<mo>=</mo><mo>&gt;</mo>$splittingSign"
    }
    return newSplittingSign
}
