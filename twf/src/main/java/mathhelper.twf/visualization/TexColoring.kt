package mathhelper.twf.visualization

import mathhelper.twf.standartlibextensions.*

fun setBackgroundColorTex(string: String, color: String) = "\\textcolor{$color}{$string}"

fun brushTex(string: String, coloringTasks: List<ColoringTask>): String { //coloringTasks cannot have intersections
    val result = StringBuilder()
    var lastHandledPosition = 0
    val sortedColoringTasks = coloringTasks.filter { it.startPosition <= it.endPosition }.sortedBy { it.startPosition }
    for (task in sortedColoringTasks) {
        if (task.startPosition >= string.lastIndex) {
            result.append(string.substring(lastHandledPosition, string.lastIndex))
            result.append("\\textcolor{${task.texColor()}}{.}")
        } else {
            result.append(string.substring(lastHandledPosition, task.startPosition))
            if ((task.startPosition == task.endPosition) || (task.startPosition >= string.lastIndex)){
                result.append("\\textcolor{${task.texColor()}}{.}")
                lastHandledPosition = task.startPosition
            } else {
                val endPosition = findClosestPlaceToTargetOnTheSameLevel(string, task.startPosition, task.endPosition, isMathML = false)
                result.append("\\textcolor{${task.texColor()}}{")
                result.append(string.substring(task.startPosition, endPosition))
                result.append("}")
                lastHandledPosition = endPosition
            }
        }
    }
    result.append(string.substring(lastHandledPosition))
    return result.toString()
}

fun dropPerformedTexBrushing (string: String): String {
    var result = dropPerformedTexBrushingInternal(string)
    var resultLast = string
    while (result != resultLast){
        val newResult = dropPerformedTexBrushingInternal(result)
        resultLast = result
        result = newResult
    }
    return result
}

fun dropPerformedTexBrushingInternal (string: String): String {
    val result = StringBuilder()
    var currentPosition = 0
    while (currentPosition < string.length) {
        val index = string.findAnyOf(listOf("\\textcolor{"), currentPosition)
        if (index != null){
            result.append(string.substring(currentPosition, index.first))
            currentPosition = index.first + index.second.length
            currentPosition = skipFromRemainingExpressionWhile({ it != '}' }, string, currentPosition) + 1
            if (currentPosition < string.length && string[currentPosition] == '{') {
                currentPosition++
                val newCurrentPosition = skipFromRemainingExpressionWhileClosingBracketNotFound("}", "{", string, currentPosition)
                result.append(string.substring(currentPosition, newCurrentPosition))
                currentPosition = newCurrentPosition + 1
            }
        } else {
            val underlineIndex = string.findAnyOf(listOf("\\underline{"), currentPosition)
            if (underlineIndex != null) {
                result.append(string.substring(currentPosition, underlineIndex.first))
                currentPosition = underlineIndex.first + underlineIndex.second.length
                val newCurrentPosition = skipFromRemainingExpressionWhileClosingBracketNotFound("}", "{", string, currentPosition)
                result.append(string.substring(currentPosition, newCurrentPosition))
                currentPosition = newCurrentPosition + 1
            } else {
                result.append(string.substring(currentPosition))
                break
            }
        }
    }
    return result.toString()
}
