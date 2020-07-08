package com.twf.standartlibextensions

fun Double.abs () = if (this < 0) -this else this

fun isNumberValuesEqual (l: String, r: String): Boolean {
    if (r == l){
        return true
    }
    val lValue = l.toDoubleOrNull() ?: return false
    val rValue = r.toDoubleOrNull() ?: return false
    return (lValue - rValue).abs() < 11.9e-7  // todo: unify with approach in BaseOperationDefinitions.kt
}