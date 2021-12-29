package mathhelper.twf.standartlibextensions

import mathhelper.twf.config.VariableConfiguration

fun Double.abs () = if (this < 0) -this else this

fun isNumberValuesEqual (l: String, r: String): Boolean {
    var l_copy = l
    var r_copy = r
    if (r == l){
        return true
    } else {
        val confVar = VariableConfiguration()
        if (confVar.variableImmediateReplacementMap.containsKey(l)) {
            l_copy = confVar.variableImmediateReplacementMap[l]!!
        }
        if (confVar.variableImmediateReplacementMap.containsKey(r)) {
            r_copy = confVar.variableImmediateReplacementMap[r]!!
        }

    }
    val lValue = l_copy.toDoubleOrNull() ?: return false
    val rValue = r_copy.toDoubleOrNull() ?: return false
    return (lValue - rValue).abs() < 11.9e-7  // todo: unify with approach in BaseOperationDefinitions.kt
}