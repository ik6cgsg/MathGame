package spbpu.hsamcp.mathgame.level

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.twf.api.expressionSubstitutionFromStrings
import com.twf.api.expressionToString
import com.twf.api.findSubstitutionPlacesInExpression
import com.twf.api.stringToExpression
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import org.json.JSONObject
import java.lang.Exception
import android.content.Context.MODE_PRIVATE
import com.twf.factstransformations.Rule
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.statistics.AuthInfo

data class RuleStr(val left: String, val right: String)

enum class Type(val str: String) {
    SET("setTheory"),
    ALGEBRA("algebra"),
    TRIGONOMETRY("trigonometry")
}

enum class LevelField(val str: String) {
    TASK_ID("taskId"),
    NAME("name"),
    DIFFICULTY("difficulty"),
    TYPE("type"),
    STEPS_NUM("stepsNum"),
    TIME("time"),
    AWARD_COEFFS("awardCoeffs"),
    SHOW_WRONG_RULES("showWrongRules"),
    SHOW_SUBST_RESULT("showSubstResult"),
    UNDO_CONSIDERING_POLICY("undoConsideringPolicy"),
    LONG_EXPRESSION_CROPPING_POLICY("longExpressionCroppingPolicy"),
    ORIGINAL_EXPRESSION("originalExpression"),
    FINAL_EXPRESSION("finalExpression"),
    ALL_SUBSTITUTIONS("allSubstitutions"),
    RULE_LEFT("left"),
    RULE_RIGHT("right"),
    RESULT("resultTaskId"),
}

class Level(var fileName: String) {
    private var rules = ArrayList<ExpressionSubstitution>()
    private var rulesStr = ArrayList<RuleStr>()
    lateinit var startFormula: ExpressionNode
    private lateinit var startFormulaStr: String
    lateinit var endFormula: ExpressionNode
    private lateinit var endFormulaStr: String
    lateinit var type: Type
    var taskId = 0
    var name = "test"
    var awardCoeffs = "0.88 0.65 0.45"
    var awardMultCoeff = 1f
    var showWrongRules = false
    var showSubstResult = false
    var undoPolicy = UndoPolicy.NONE
    var longExpressionCroppingPolicy = ""
    var lastResult: Result? = null
    var difficulty = 0
    var stepsNum = 1
    var time: Long = 180
    var timeMultCoeff = 1f
    private var exprSet = false
    var coeffsSet = false
    val fullyLoaded: Boolean
        get() = this.exprSet && this.coeffsSet

    companion object {
        private val TAG = "Level"

        fun create(fileName: String, context: Context): Level? {
            Log.d(TAG, "create")
            var res: Level? = Level(fileName)
            if (!res!!.preLoad(context)) {
                res = null
            }
            return res
        }
    }

    fun preLoad(context: Context): Boolean {
        Log.d(TAG, "load")
        return when {
            context.assets != null -> {
                val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
                val levelJson = JSONObject(json)
                if (parse(levelJson)) {
                    setGlobalCoeffs(context)
                    loadResult(context)
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    fun fullyLoad(context: Context) {
        setGlobalCoeffs(context)
        if (!exprSet) {
            startFormula = when (type) {
                Type.SET -> stringToExpression(startFormulaStr, type.str)
                else -> stringToExpression(startFormulaStr)
            }
            endFormula = when (type) {
                Type.SET -> stringToExpression(endFormulaStr, type.str)
                else -> stringToExpression(endFormulaStr)
            }
            endFormulaStr = expressionToString(endFormula)
            for (ruleStr in rulesStr) {
                val ruleSubst = when (type) {
                    Type.SET -> expressionSubstitutionFromStrings(ruleStr.left, ruleStr.right, type.str)
                    else -> expressionSubstitutionFromStrings(ruleStr.left, ruleStr.right)
                }
                rules.add(ruleSubst)
            }
            rules.shuffle()
            exprSet = true
        }
    }

    private fun setGlobalCoeffs(context: Context) {
        val prefs = context.getSharedPreferences(Constants.storage, MODE_PRIVATE)
        if (prefs.getBoolean(AuthInfo.AUTHORIZED.str, false) && !coeffsSet) {
            val undoInd = prefs.getInt(AuthInfo.UNDO_COEFF.str, 0)
            undoPolicy = UndoPolicy.values()[undoInd]
            timeMultCoeff = prefs.getFloat(AuthInfo.TIME_COEFF.str, timeMultCoeff)
            time = (time * timeMultCoeff).toLong()
            awardMultCoeff = prefs.getFloat(AuthInfo.AWARD_COEFF.str, awardMultCoeff)
            coeffsSet = true
        }
    }

    private fun parse(levelJson: JSONObject): Boolean {
        if (!levelJson.has(LevelField.TASK_ID.str) || !levelJson.has(LevelField.NAME.str) ||
            !levelJson.has(LevelField.DIFFICULTY.str) || !levelJson.has(LevelField.TYPE.str)) {
            return false
        }
        taskId = levelJson.getInt(LevelField.TASK_ID.str)
        name = levelJson.getString(LevelField.NAME.str)
        difficulty = levelJson.getInt(LevelField.DIFFICULTY.str)
        val typeStr = levelJson.getString(LevelField.TYPE.str)
        try {
            type = Type.valueOf(typeStr.toUpperCase())
        } catch (e: Exception) {
            return false
        }
        stepsNum = levelJson.optInt(LevelField.STEPS_NUM.str, stepsNum)
        time = levelJson.optLong(LevelField.TIME.str, time)
        awardCoeffs = levelJson.optString(LevelField.AWARD_COEFFS.str, awardCoeffs)
        showWrongRules = levelJson.optBoolean(LevelField.SHOW_WRONG_RULES.str, showWrongRules)
        showSubstResult = levelJson.optBoolean(LevelField.SHOW_SUBST_RESULT.str, showSubstResult)
        val undoPolicyStr = levelJson.optString(LevelField.UNDO_CONSIDERING_POLICY.str, UndoPolicy.NONE.str)
        try {
            undoPolicy = UndoPolicy.valueOf(undoPolicyStr.toUpperCase())
        } catch (e: Exception) {
            return false
        }
        longExpressionCroppingPolicy = levelJson.optString(LevelField.LONG_EXPRESSION_CROPPING_POLICY.str,
            longExpressionCroppingPolicy)
        startFormulaStr = levelJson.getString(LevelField.ORIGINAL_EXPRESSION.str)
        endFormulaStr = levelJson.getString(LevelField.FINAL_EXPRESSION.str)
        val rulesJson = levelJson.getJSONArray(LevelField.ALL_SUBSTITUTIONS.str)
        for (i in 0 until rulesJson.length()) {
            val rule = rulesJson.getJSONObject(i)
            val from = rule.getString(LevelField.RULE_LEFT.str)
            val to = rule.getString(LevelField.RULE_RIGHT.str)
            rulesStr.add(RuleStr(from, to))
        }
        return true
    }

    fun checkEnd(formula: ExpressionNode): Boolean {
        Log.d(TAG, "checkEnd")
        val currStr = expressionToString(formula)
        Log.d(TAG, "current: $currStr | end: $endFormulaStr")
        return currStr == endFormulaStr
    }

    fun getAward(resultTime: Long, resultStepsNum: Float): Award {
        Log.d(TAG, "getAward")
        val mark = if (resultStepsNum < stepsNum) {
            1.0
        } else {
            (1 - resultTime.toDouble() / time + stepsNum.toDouble() / resultStepsNum) / 2
        }
        return Award(getAwardByCoeff(mark), mark)
    }

    private fun getAwardByCoeff(mark: Double): AwardType {
        val awards = awardCoeffs.split(" ").map { it.toDouble() * awardMultCoeff }
        return when {
            mark == 1.0 -> AwardType.PLATINUM
            mark >= awards[0] -> AwardType.GOLD
            awards[1] <= mark && mark < awards[0] -> AwardType.SILVER
            awards[2] <= mark && mark < awards[1] -> AwardType.BRONZE
            else -> AwardType.NONE
        }
    }

    fun getRulesFor(node: ExpressionNode, formula: ExpressionNode): List<ExpressionSubstitution>? {
        Log.d(TAG, "getRulesFor")
        // TODO: smek with showWrongRules flag
        val res = rules
            .filter {
                val list = findSubstitutionPlacesInExpression(formula, it)
                if (list.isEmpty()) {
                    false
                } else {
                    val substPlace = list.find { sp ->
                        sp.originalValue.nodeId == node.nodeId
                    }
                    substPlace != null
                }
            }
        if (res.isEmpty()) {
            return null
        }
        return res
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(Constants.storage, MODE_PRIVATE)
        val prefEdit = prefs.edit()
        val resultStr = "${lastResult!!.steps} ${lastResult!!.time} ${lastResult!!.award.coeff}"
        val resultId = "${LevelField.RESULT.str}${taskId}"
        prefEdit.putString(resultId, resultStr)
        prefEdit.commit()
    }

    private fun loadResult(context: Context) {
        val prefs = context.getSharedPreferences(Constants.storage, MODE_PRIVATE)
        val resultId = "${LevelField.RESULT.str}${taskId}"
        val resultStr = prefs.getString(resultId, "")
        if (!resultStr.isNullOrEmpty()) {
            val resultVals = resultStr.split(" ", limit = 3)
            lastResult = Result(resultVals[0].toFloat(), resultVals[1].toLong(),
                Award(getAwardByCoeff(resultVals[2].toDouble()), resultVals[2].toDouble()))
        }
    }
}