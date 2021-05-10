package mathhelper.games.matify.statistics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
    var appCode: String = "MATIFY_ANDROID",
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
        this.taskCode = level.code
        this.taskVersion = level.version
        this.tasksetCode = level.game.code
        this.tasksetVersion = level.game.version
        this.originalExpression = level.originalExpressionStructureString
        this.goalExpression = level.goalExpressionStructureString
        this.goalPattern = level.goalPattern
        this.difficulty = level.difficulty
    }

    override fun toString(): String {
        var gson = GsonBuilder().serializeNulls().create()
        val res =  gson.toJson(this)
        return res
    }
}



