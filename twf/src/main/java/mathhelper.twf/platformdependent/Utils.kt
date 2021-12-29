package mathhelper.twf.platformdependent

import kotlin.random.Random

val random = Random
fun defaultRandom() = random.nextDouble()
fun random(from: Double, to: Double) = (random.nextDouble() * (to - from) + from)
fun randomInt(from: Int, to: Int) = if (from == to) from else random.nextInt(from, to) // generate int number in [from, to)
fun randomBoolean() = (randomInt(0, 2) == 0)

fun abs(x: Int) = if (x > 0) x else -x

fun Int.toStringWithMinLength(minLength: Int, suffixChar: Char): String {
    val res = this.toString()
    val suffixLen = minLength - res.length
    return if (suffixLen > 0) {
        res + suffixChar.toString().repeat(suffixLen)
    } else {
        res
    }
}

fun escapeCharacters(string: String, characterEscapingDepth: Int = 1): String {
    return escapeBackSlash(string, characterEscapingDepth)
}

fun escapeBackSlash(string: String, characterEscapingDepth: Int = 1): String {
    val replacement = "\\".repeat(characterEscapingDepth)
    return string.replace("\\", replacement)
}

fun Double.toShortString(): String {
    var stringValue = this.toString()
    if (stringValue.contains('.')) {
        if (stringValue.contains("999")) {
            val fractionPart = stringValue.substringAfter(".").substringBefore("999")
            stringValue = stringValue.substringBefore(".")
            if (fractionPart.isNotEmpty() && fractionPart != "9")
                stringValue = stringValue + "." + fractionPart.substringBefore("999")
            if (stringValue.last() != '9') {
                val newLast = stringValue.last() + 1
                stringValue = stringValue.removeSuffix(stringValue.last().toString()) + newLast
            } else {
                stringValue = (stringValue.toInt() + 1).toString()
            }
        } else {
            val fractionPart = stringValue.substringAfter(".").substringBefore("000")
            stringValue = stringValue.substringBefore(".")
            if (fractionPart.isNotEmpty() && fractionPart != "0")
                stringValue = stringValue + "." + fractionPart.substringBefore("000")
        }
    }
    return stringValue
}

fun lazyPrintln(condition: Boolean, message: () -> String) {
    if (condition) {
        println(message.invoke())
    }
}