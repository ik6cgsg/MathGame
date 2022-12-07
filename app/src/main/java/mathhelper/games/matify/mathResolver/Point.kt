package mathhelper.games.matify.mathResolver

data class Point(var x: Int, var y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    operator fun div(value: Int): Point {
        return Point(x / value, y / value)
    }

    operator fun times(value: Int): Point {
        return Point(x * value, y * value)
    }
}