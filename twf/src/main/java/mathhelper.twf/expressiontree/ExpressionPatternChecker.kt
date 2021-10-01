package mathhelper.twf.expressiontree


fun ExpressionNode.patternDoubleMinus() : Boolean { //works only from ""
    for (child in children) {
        if (child.value == "-") {
            if (child.children.any{
                c1 -> c1.value == "+" && c1.children.size == 1 && c1.children.any{
                    c2 -> c2.value == "-"
                }
            }) {
                return true
            }
        }
        if (child.patternDoubleMinus()) {
            return true
        }
    }
    return false
}

fun ExpressionNode.patternStartWithUnaryMinus() : Boolean {
    var cur = this
    while ((cur.value == "" || cur.value == "+") && cur.children.size == 1) {
        cur = cur.children[0]
    }
    return cur.value == "-" && cur.children.size == 1
}

fun ExpressionNode.patternUnaryMinus() : Boolean {
    if (!patternStartWithUnaryMinus()) {
        for (child in children) {
            if (child.patternUnaryMinus()) {
                return true
            }
        }
        return false
    }
    return true
}

fun ExpressionNode.patternDoubleMinusInFraction() : Boolean { //works only from ""
    for (child in children) {
        if (child.value == "/") {
            if (child.children[0].patternStartWithUnaryMinus() && child.children[1].patternStartWithUnaryMinus()) {
                return true
            }
        }
        if (child.patternDoubleMinusInFraction()) {
            return true
        }
    }
    return false
}

fun ExpressionNode.patternThreeLevelsInFraction() : Boolean {
    if (value == "/") {
        if (children[0].value == "/" || children[1].value == "/") {
            return true
        }
    }
    for (child in children) {
        if (child.patternThreeLevelsInFraction()) {
            return true
        }
    }
    return false
}

fun ExpressionNode.patternTooManyLevelsInFraction() : Boolean {
    if (value == "/") {
        if (children[0].patternThreeLevelsInFraction() || children[1].patternThreeLevelsInFraction()) {
            return true
        }
    }
    for (child in children) {
        if (child.patternTooManyLevelsInFraction()) {
            return true
        }
    }
    return false
}

fun ExpressionNode.patternFractionExist() : Boolean {
    if (value == "/") {
        return true
    }
    for (child in children) {
        if (child.patternFractionExist()) {
            return true
        }
    }
    return false
}

fun ExpressionNode.patternThreeLevelsExist() : Boolean {
    if (value == "/") {
        if (children[0].patternFractionExist() || children[1].patternFractionExist()) {
            return true
        }
    }
    for (child in children) {
        if (child.patternThreeLevelsExist()) {
            return true
        }
    }
    return false
}

fun ExpressionNode.patternTooManyLevelsExist(): Boolean {
    if (value == "/") {
        if (children[0].patternThreeLevelsExist() || children[1].patternThreeLevelsExist()) {
            return true
        }
    }
    for (child in children) {
        if (child.patternTooManyLevelsExist()) {
            return true
        }
    }
    return false
}

fun ExpressionNode.patternConstMulConst() : Boolean {
    if (value == "*") {
        if (children[0].getContainedFunctions().isEmpty() && children[1].getContainedFunctions().isEmpty()) {
            return true
        }
    }
    for (child in children) {
        if (child.patternConstMulConst()) {
            return true
        }
    }
    return false
}

/*fun ExpressionNode.patternPiInDenominator() : Boolean {

}*/

/*fun ExpressionNode.patternMinusWithEvenDegree() : Boolean {

}*/