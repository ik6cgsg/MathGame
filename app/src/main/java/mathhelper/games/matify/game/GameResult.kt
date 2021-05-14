package mathhelper.games.matify.game


class GameResult(val levelsPassed: Int, val levelsPaused: Int) {
    override fun toString(): String {
        return "$levelsPassed/%d ✅   " + if (levelsPaused != 0) "$levelsPaused ⏸️" else ""
    }

    fun saveString(): String {
        return "$levelsPassed $levelsPaused"
    }
}