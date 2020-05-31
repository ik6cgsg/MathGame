package mathhelper.games.matify.statistics

import org.json.JSONObject
import mathhelper.games.matify.activities.PlayActivity
import mathhelper.games.matify.level.Level

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
    MARK("mark"),
    PROBLEM("problem"),
    HELP("help")
}

data class ActivityLog (
    var activityTypeCode: String = "", // "start" | "win" | "loose" | "undo" | "menu" | "restart" | "rule" | "place" | "sign" | "mark"
    var gameCode: String = "",
    var levelCode: String = "",

    var originalExpression: String = "",
    var finalExpression: String = "",
    var finalPattern: String = "",

    var difficulty: Float = 0f,

    var currentExpression: String = "",
    var nextExpression: String = "", // * action == rule || action == restart || action == undo
    var appliedRule: String = "",
    var selectedPlace: String = "",

    var currTimeMS: Long = -1,
    var timeFromLastActionMS: Long = -1,

    var currStepsNumber: Float = 0f,
    var nextStepsNumber: Float = 0f, // * action == rule || action == restart || action == undo

    var subActionsNumber: Int = -1,
    var subActionsAfterLastTransformation: Int = -1,

    var clientActionTime: Long = -1,
    var additionalData: JSONObject = JSONObject(),

    // all fields bellow are deprecated

    var hardwareDeviceId: String = "", //empty if the device is not known
    var hardwareProperties: String = "",
    var applicationVersion: String = "",
    // user level info
    var totalTimeMultCoef: Float = 1f,
    var totalAwardMultCoef: Float = 1f,
    // Хар-ки уровня
    // * заполняются на каждый action (кроме sign)
    var taskId: Int = -1,
    var taskType: String = "", // "trigonometry", "setTheory", ...
    var totalTimeMS: Long = -1,
    var minSteps: Int = -1,
    var awardCoefs: String = "",
    var showWrongRules: Boolean = false,
    var showSubstResult: Boolean = false,
    var undoConsideringPolicy: String = "",
    var longExpressionCroppingPolicy: String = "",
    // Текущее состояние прохождения уровня игроком
    var leftTimeMS: Long = -1,
    var endExpressionHide: Boolean = false,
    var expressionSize: Float = 0f,
    // Need to set implicitly
    var currAwardCoef: Float = 0f // * action == win

) {
    fun additionalFrom(activity: PlayActivity, level: Level, action: Action) {
        this.activityTypeCode = action.str
        // Level consts
        this.levelCode = level.levelCode
        this.gameCode = level.game.gameCode

        this.originalExpression = level.startExpressionStr
        this.finalExpression = level.endExpressionStr
        this.finalPattern = level.endPatternStr

        this.taskType = level.type.str
        this.totalTimeMS = level.time * 1000

        this.difficulty = level.difficulty
        this.minSteps = level.stepsNum
        this.awardCoefs = level.awardCoeffs
        this.showWrongRules = level.showWrongRules
        this.showSubstResult = level.showSubstResult
        this.undoConsideringPolicy = level.undoPolicy.str
        this.longExpressionCroppingPolicy = level.longExpressionCroppingPolicy
        // UI statistics
        this.endExpressionHide = activity.endExpressionHide()
        this.expressionSize = activity.globalMathView.textSize
    }

    override fun toString(): String {
        val root = JSONObject()
        root.put("activityTypeCode", activityTypeCode)
        root.put("gameCode", gameCode)
        root.put("levelCode", levelCode)

        root.put("originalExpression", originalExpression)
        root.put("finalExpression", finalExpression)
        if (finalPattern.isNotBlank()) {
            root.put("finalPattern", finalPattern)
        }
        root.put("difficulty", difficulty)

        root.put("currentExpression", currentExpression)
        root.put("nextExpression", nextExpression)

        root.put("appliedRule", appliedRule)
        root.put("selectedPlace", selectedPlace)

        root.put("currTimeMS", currTimeMS)
        root.put("timeFromLastActionMS", timeFromLastActionMS)

        root.put("currStepsNumber", currStepsNumber)
        root.put("nextStepsNumber", nextStepsNumber)

        root.put("subActionsNumber", subActionsNumber)
        root.put("subActionsAfterLastTransformation", subActionsAfterLastTransformation)

        root.put("clientActionTime", clientActionTime)
        root.put("additionalData", additionalData.toString())

        root.put("applicationVersion", applicationVersion)

        return root.toString()
    }
}



