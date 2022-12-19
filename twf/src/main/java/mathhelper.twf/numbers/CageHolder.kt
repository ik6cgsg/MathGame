package mathhelper.twf.numbers

class CageHolder(private val minPoint : ArrayList<Double>, private val maxPoint : ArrayList<Double>) {
    init {
        if (minPoint.size != maxPoint.size) {
            throw IllegalArgumentException("Points have different dimensions")
        }

        for (i in 0..minPoint.size) {
            if (minPoint[i] > maxPoint[i]) {
                throw IllegalArgumentException("Min point greater than max point in position $i")
            }
        }
    }

    companion object {
        fun fullSpaceCageHolder(dimension : Int) : CageHolder {
            val minPoint = ArrayList<Double>()
            val maxPoint = ArrayList<Double>()

            for (i in 0..dimension) {
                minPoint.add(Double.NEGATIVE_INFINITY)
                maxPoint.add(Double.POSITIVE_INFINITY)
            }

            return CageHolder(minPoint, maxPoint)
        }

        fun emptyCageHolder(dimension: Int) : CageHolder {
            val minPoint = ArrayList<Double>()
            val maxPoint = ArrayList<Double>()

            for (i in 0..dimension) {
                minPoint.add(Double.POSITIVE_INFINITY)
                maxPoint.add(Double.POSITIVE_INFINITY)
            }

            return CageHolder(minPoint, maxPoint)
        }

        fun domainIntersection(domains : List<CageHolder>, dimension: Int) : CageHolder {
            val intersectionMinPoint = ArrayList<Double>()
            val intersectionMaxPoint = ArrayList<Double>()

            if (domains.isEmpty()) {
                return fullSpaceCageHolder(dimension)
            }

            for (domain in domains) {
                if (domain.getDimension() != dimension) {
                    throw IllegalArgumentException("Different point's dimensions")
                }
            }

            for (i in 0..dimension) {
                intersectionMinPoint.add(domains.maxByOrNull { it.minPoint[i] }?.minPoint!![i])
                intersectionMaxPoint.add(domains.minByOrNull { it.maxPoint[i] }?.maxPoint!![i])
            }

            for (i in 0..dimension) {
                if (intersectionMaxPoint[i] < intersectionMinPoint[i]) {
                    return emptyCageHolder(dimension)
                }
            }

            return CageHolder(intersectionMinPoint, intersectionMaxPoint)
        }
    }

    fun pointInDomain(point : ArrayList<Double>) : Boolean {
        if (point.size != minPoint.size) {
            throw IllegalArgumentException("Point's dimension ${point.size} != cage dimension ${minPoint.size}$")
        }

        for (i in (0..getDimension())) {
            if (point[i] < minPoint[i] || point[i] > maxPoint[i]) {
                return false;
            }
        }

        return true
    }

    fun getDimension() : Int {
        return minPoint.size
    }

    fun isEmpty() : Boolean {
        for (i in 0..getDimension()) {
            if (minPoint[i] != Double.POSITIVE_INFINITY) {
                return false
            }
        }

        return true
    }
}