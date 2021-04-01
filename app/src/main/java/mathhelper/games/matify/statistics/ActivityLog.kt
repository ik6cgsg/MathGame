package mathhelper.games.matify.statistics

import com.google.gson.Gson
import org.json.JSONObject
import mathhelper.games.matify.activities.PlayActivity
import mathhelper.games.matify.level.Level
import java.sql.Timestamp

enum class Action(val str: String) {
    START("start"),
    WIN("win"),
    LOOSE("loose"),
    UNDO("undo"),
    MENU("menu"),
    RESTART("restart"),
    RULE("rule"),
    PLACE("place"),
    SIGN("sign"),
    HELP("help"),
    SAVE("save"),
    INTERIM("interim");
}

data class ActivityLog (
    var appCode: String = "",
    var activityTypeCode: String = "",
    var clientActionTs: Timestamp? = null,
    var tasksetCode: String? = null,
    var tasksetVersion: Int? = null,
    var taskCode: String? = null,
    var taskVersion: Int? = null,
    var autoSubTaskCode: String? = null,
    var originalExpression: String? = null,
    var goalExpression: String? = null,
    var goalPattern: String? = null,
    var difficulty: Double? = null,
    var currSolution: String? = null, // TODO: how to fill??
    var currExpression: String? = null,
    var nextExpression: String? = null,
    var appliedRule: MutableMap<String, *>? = null,
    var selectedPlace: String? = null,
    var currTimeMs: Long? = null,
    var timeFromLastActionMs: Long? = null,
    var currStepsNumber: Double? = null,
    var nextStepsNumber: Double? = null,
    var subActionNumber: Int? = null,
    var subActionsAfterLastTransformation: Int? = null,
    var otherData: MutableMap<String, *>? = null,
    var otherGameStepData: MutableMap<String, *>? = null,
    var otherSolutionStepData: MutableMap<String, *>? = null,
    // Result data
    var qualityData: MutableMap<String, *>? = null,
    var baseAward: Double? = null
) {
    fun additionalFrom(activity: PlayActivity, level: Level, action: Action) {
        this.activityTypeCode = action.str
        // Level consts
        this.taskCode = level.levelCode
        // TODO: this.taskVersion
        this.tasksetCode = level.game.gameCode
        // TODO: this.tasksetVersion

        this.originalExpression = level.startExpressionStr
        this.goalExpression = level.endExpressionStr
        this.goalPattern = level.endPatternStr

        // TODO: this.taskType = level.type.str
        // TODO: this.totalTimeMS = level.time * 1000

        this.difficulty = level.difficulty
        // TODO: do we need this? this.minSteps = level.stepsNum
        // TODO: convert? this.awardCoefs = level.awardCoeffs
        // TODO: remove?? this.showWrongRules = level.showWrongRules
        // TODO: remove?? this.showSubstResult = level.showSubstResult
        // TODO: remove?? this.undoConsideringPolicy = level.undoPolicy.str
        // TODO: remove?? this.longExpressionCroppingPolicy = level.longExpressionCroppingPolicy
    }

    override fun toString(): String {
        var gson = Gson()
        return gson.toJson(this)
    }
}



