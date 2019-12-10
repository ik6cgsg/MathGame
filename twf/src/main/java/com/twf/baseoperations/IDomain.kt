package com.twf.baseoperations

import java.util.function.Function

interface IDomain<PointType> {
    fun test(point: PointType): Boolean
    fun generate(): PointType
    fun intersect(other: IDomain<PointType>): IDomain<PointType>
}


class DomainAll<PointType>: IDomain<PointType> {
    override fun test(point: PointType): Boolean = true

    override fun generate(): PointType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun intersect(other: IDomain<PointType>): IDomain<PointType> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class DomainPointGap<PointType>(val gaps: Set<PointType>): IDomain<PointType> {
    override fun test(point: PointType): Boolean = gaps.contains(point)

    override fun generate(): PointType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun intersect(other: IDomain<PointType>): IDomain<PointType> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class DomainAnalyticalGaps<PointType>(val bounds: Set<Function<PointType, Boolean>>): IDomain<PointType> {
    override fun test(point: PointType): Boolean {
        for (f in bounds) {
            if (!f.apply(point)) {
                return false;
            }
        }
        return true;
    }

    override fun generate(): PointType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun intersect(other: IDomain<PointType>): IDomain<PointType> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

