package com.twf.platformdependent

import kotlin.random.Random

val random = Random
fun defaultRandom() = random.nextDouble()
fun random(from: Double, to: Double) = (random.nextDouble() * (to - from) + from)
fun randomInt(from: Int, to: Int) = random.nextInt(from, to)

fun abs(x: Int) = if (x > 0) x else -x
fun abs(x: Double) = if (x > 0) x else -x

fun Int.toStringWithMinLength (minLength: Int, suffixChar: Char): String{
    val res = this.toString()
    val suffixLen = minLength - res.length
    return if (suffixLen > 0){
        res + suffixChar.toString().repeat(suffixLen)
    } else {
        res
    }
}

fun escapeCharacters (string: String, characterEscapingDepth: Int = 1): String{
    return escapeBackSlash(string, characterEscapingDepth)
}

fun escapeBackSlash (string: String, characterEscapingDepth: Int = 1): String{
    val replacement = "\\".repeat(characterEscapingDepth)
    return string.replace("\\", replacement)
}