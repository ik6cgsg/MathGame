package mathhelper.games.matify.level

enum class UndoPolicy(val str: String) {
    NONE("none"),
    LINEAR("linear"),
    SQUARE("square")
}

class UndoPolicyHandler {
    companion object {
        private var delta = 0.1f

        fun getPenalty(policy: UndoPolicy, depth: Int): Float {
            return delta * when (policy) {
                UndoPolicy.NONE -> 0
                UndoPolicy.LINEAR -> depth
                UndoPolicy.SQUARE -> depth * depth
            }
        }
    }
}