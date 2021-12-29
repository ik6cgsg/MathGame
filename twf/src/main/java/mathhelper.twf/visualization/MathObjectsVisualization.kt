package mathhelper.twf.visualization

import mathhelper.twf.expressiontree.ExpressionSubstitution
import mathhelper.twf.factstransformations.ComparableTransformationsPart
import mathhelper.twf.factstransformations.FactSubstitution
import mathhelper.twf.factstransformations.MainChain

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


