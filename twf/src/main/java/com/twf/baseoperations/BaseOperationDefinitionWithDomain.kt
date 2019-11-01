package com.twf.baseoperations

data class BaseOperationDefinitionWithDomain<PointType>(
        val baseOp: BaseOperationsDefinition,
        val generalValuesDomain: IDomain<PointType>,
        val generalDefinitionDomain: List<IDomain<PointType>>
) {
    fun calculateDomainValues(argDomains: List<IDomain<PointType>>): IDomain<PointType>? {
        TODO("not implemented")
    }

    fun generatePointOfDefinedValues(argDomains: List<IDomain<PointType>>): PointType {
        TODO("not implemented")
    }

    fun generateValueByPoints(argPoints: List<PointType>): PointType? {
        TODO("not implemented")
    }
}

