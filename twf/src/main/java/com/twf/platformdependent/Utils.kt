package com.twf.platformdependent

import kotlin.random.Random

val random = Random
fun defaultRandom() = random.nextDouble()
fun random(from: Double, to: Double) = (random.nextDouble() * (to - from) + from)

fun abs(x: Int) = if (x > 0) x else -x

fun Int.toStringWithMinLength (minLength: Int, suffixChar: Char): String{
    val res = this.toString()
    val suffixLen = minLength - res.length
    return if (suffixLen > 0){
        res + suffixChar.toString().repeat(suffixLen)
    } else {
        res
    }
}