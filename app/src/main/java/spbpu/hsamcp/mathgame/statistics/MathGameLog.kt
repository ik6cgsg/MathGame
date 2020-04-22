package spbpu.hsamcp.mathgame.statistics

import org.json.JSONObject
import spbpu.hsamcp.mathgame.activities.PlayActivity
import spbpu.hsamcp.mathgame.level.Level

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

data class MathGameLog (
    var game: String = "MathGame_IK_an",
    var action: String = "", // "start" | "win" | "loose" | "undo" | "menu" | "restart" | "rule" | "place" | "sign" | "mark"
    var comment: String = "", // additional field for any optional information (programm errors, explanations, ...)
    // time
    var deviceTs: Long = -1,
    //user info
    var login: String = "",
    var name: String = "",
    var surname: String = "",
    var secondName: String = "",
    var group: String = "", // произвольная строка, необязательна для заполнения игроком
    var institution: String = "", // образовательное учреждение, произвольная строка, необязательна для заполнения игроком
    var age: Int = -1, // произвольная строка, необязательна для заполнения игроком
    var userMark: String = "", //user mark about situation (in future warning button must appear in all games and allow user to click on it and to leave a mark)
    var userComment: String = "", //user comment about situation (in future warning button must appear in all games and allow user to click on it and to leave a comment)
    // user device info
    var hardwareDeviceId: String = "", //empty if the device is not known
    var hardwareProperties: String = "",
    // user level info
    var totalTimeMultCoef: Float = 1f,
    var totalAwardMultCoef: Float = 1f,
    // Хар-ки уровня
    // * заполняются на каждый action (кроме sign)
    var taskId: Int = -1,
    var taskType: String = "", // "trigonometry", "setTheory", ...
    var totalTimeMS: Long = -1,
    var difficulty: Float = 0f,
    var minSteps: Int = -1,
    var awardCoefs: String = "",
    var showWrongRules: Boolean = false,
    var showSubstResult: Boolean = false,
    var undoConsideringPolicy: String = "",
    var longExpressionCroppingPolicy: String = "",
    // Текущее состояние прохождения уровня игроком
    var currTimeMS: Long = -1,
    var timeFromLastActionMS: Long = -1,
    var leftTimeMS: Long = -1,
    var subActionsNumber: Int = -1,
    var subActionsAfterLastTransformation: Int = -1,
    var endExpressionHide: Boolean = false,
    var expressionSize: Float = 0f,
    // Need to set implicitly
    var currStepsNumber: Float = 0f,
    var nextStepsNumber: Float = 0f, // * action == rule || action == restart || action == undo
    var currAwardCoef: Float = 0f, // * action == win
    var currRule: String = "", // * action == rule
    var currSelectedPlace: String = "",
    var currExpression: String = "",
    var nextExpression: String = "" // * action == rule || action == restart || action == undo

) {
    fun addInfoFrom(activity: PlayActivity, level: Level, action: Action) {
        this.action = action.str
        // Level consts
        this.taskId = level.taskId
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
        root.put("game", game)
        root.put("action", action)
        root.put("comment", comment)
        root.put("deviceTs", deviceTs)
        root.put("login", login)
        root.put("name", name)
        root.put("surname", surname)
        root.put("secondName", secondName)
        root.put("group", group)
        root.put("institution", institution)
        root.put("age", age)
        root.put("userMark", userMark)
        root.put("userComment", userComment)
        root.put("hardwareDeviceId", hardwareDeviceId)
        root.put("hardwareProperties", hardwareProperties)
        root.put("totalTimeMultCoef", totalTimeMultCoef)
        root.put("totalAwardMultCoef", totalAwardMultCoef)
        root.put("taskId", taskId)
        root.put("taskType", taskType)
        root.put("totalTimeMS", totalTimeMS)
        root.put("difficulty", difficulty)
        root.put("minSteps", minSteps)
        root.put("awardCoefs", awardCoefs)
        root.put("showWrongRules", showWrongRules)
        root.put("showSubstResult", showSubstResult)
        root.put("undoConsideringPolicy", undoConsideringPolicy)
        root.put("longExpressionCroppingPolicy", longExpressionCroppingPolicy)
        root.put("currTimeMS", currTimeMS)
        root.put("timeFromLastActionMS", timeFromLastActionMS)
        root.put("currStepsNumber", currStepsNumber)
        root.put("nextStepsNumber", nextStepsNumber)
        root.put("subActionsNumber", subActionsNumber)
        root.put("subActionsAfterLastTransformation", subActionsAfterLastTransformation)
        root.put("leftTimeMS", leftTimeMS)
        root.put("currAwardCoef", currAwardCoef)
        root.put("currRule", currRule)
        root.put("currSelectedPlace", currSelectedPlace)
        root.put("currExpression", currExpression)
        root.put("nextExpression", nextExpression)
        root.put("endExpressionHide", endExpressionHide)
        root.put("expressionSize", expressionSize)
        return root.toString()
    }
}


