package mathhelper.games.matify.game
import mathhelper.games.matify.common.SavableResult


class GameResult(val levelsPassed: Int, val levelsPaused: Int): SavableResult {
    override fun toString(): String {
        return "$levelsPassed/%d ✅   " + if (levelsPaused != 0) "$levelsPaused ⏸️" else ""
    }

    override fun saveString(): String {
        return "$levelsPassed $levelsPaused"
    }
}