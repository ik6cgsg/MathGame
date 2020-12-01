package visualization

import expressiontree.ExpressionSubstitution
import factstransformations.ComparableTransformationsPart
import factstransformations.FactSubstitution
import factstransformations.MainChain

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


