package mathhelper.twf.numbers

class LineSegmentHolder(private var from : Double, private var to : Double) {
    init {
        if (from > to) {
            val temporary = from;
            from = to;
            to = temporary;
        }
    }

    companion object {
        val fullSegment = LineSegmentHolder(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
        val emptySegment = LineSegmentHolder(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        val positiveSegment = LineSegmentHolder(0.0, Double.POSITIVE_INFINITY)
    }

    fun pointInSegment(point : Double) = point in from..to

    fun isSubzero() = to < 0

    fun isSubSegment(segment : LineSegmentHolder) = segment.from <= from && segment.to >= to

    fun hasIntersection(segment: LineSegmentHolder) = !(segment.from > to || from > segment.to)
}