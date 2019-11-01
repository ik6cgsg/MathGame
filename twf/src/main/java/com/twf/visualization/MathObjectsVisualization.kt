package com.twf.visualization

import com.twf.expressiontree.ExpressionSubstitution
import com.twf.factstransformations.ComparableTransformationsPart
import com.twf.factstransformations.FactSubstitution
import com.twf.factstransformations.MainChain

fun transformationPartsToLog (transformationsPart: ComparableTransformationsPart): String{
    return transformationsPart.computeIdentifier(false)
}

fun transformationPartsToLog (transformation: ExpressionSubstitution): String{
    return transformation.computeIdentifier(false)
}

fun transformationPartsToLog (transformation: FactSubstitution): String{
    return transformation.computeIdentifier(false)
}

fun transformationPartsToLog (transformation: MainChain): String{
    return transformation.computeIdentifier(false)
}


