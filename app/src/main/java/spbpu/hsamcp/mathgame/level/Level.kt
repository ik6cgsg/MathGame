package spbpu.hsamcp.mathgame.level

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import org.json.JSONObject
import java.lang.Exception
import android.content.Context.MODE_PRIVATE
import com.twf.api.*
import com.twf.expressiontree.ExpressionStructureConditionNode
import com.twf.factstransformations.Rule
import spbpu.hsamcp.mathgame.common.AuthInfo
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.common.Storage
import spbpu.hsamcp.mathgame.game.Game
import spbpu.hsamcp.mathgame.game.PackageField
import spbpu.hsamcp.mathgame.game.RulePackage

data class RuleStr(val left: String, val right: String)

enum class Type(val str: String) {
    SET("setTheory"),
    ALGEBRA("algebra"),
    TRIGONOMETRY("trigonometry"),
    OTHER("other")
}

enum class LevelField(val str: String) {
    IGNORE("ignore"),
    LEVEL_CODE("levelCode"),
    NAME("name"),
    DIFFICULTY("difficulty"),
    TYPE("type"),
    STEPS_NUM("stepsNum"),
    TIME("time"),
    ENDLESS("endless"),
    AWARD_COEFFS("awardCoeffs"),
    SHOW_WRONG_RULES("showWrongRules"),
    SHOW_SUBST_RESULT("showSubstResult"),
    UNDO_CONSIDERING_POLICY("undoConsideringPolicy"),
    LONG_EXPRESSION_CROPPING_POLICY("longExpressionCroppingPolicy"),
    ORIGINAL_EXPRESSION("originalExpression"),
    FINAL_EXPRESSION("finalExpression"),
    FINAL_PATTERN("finalPattern"),
    RULES("rules")
}

class Level {
    private var packages = ArrayList<String>()
    private var rules = ArrayList<ExpressionSubstitution>()
    lateinit var startExpressionStr: String
    lateinit var startExpression: ExpressionNode
    lateinit var endExpression: ExpressionNode
    lateinit var endExpressionStr: String
    lateinit var endPattern: ExpressionStructureConditionNode
    lateinit var endPatternStr: String
    lateinit var type: Type
    lateinit var levelCode: String
    lateinit var game: Game
    lateinit var name: String
    var awardCoeffs = "0.88 0.65 0.45"
    var awardMultCoeff = 1f
    var showWrongRules = false
    var showSubstResult = false
    var undoPolicy = UndoPolicy.NONE
    var longExpressionCroppingPolicy = ""
    var lastResult: Result? = null
    var difficulty = 0f
    var stepsNum = 1
    var time: Long = 180
    var timeMultCoeff = 1f
    var endless = true

    companion object {
        private val TAG = "Level"

        fun parse(game: Game, levelJson: JSONObject, context: Context): Level? {
//TODO            Log.d(TAG, "parse")
            var res: Level? = Level()
            res!!.game = game
            if (res.load(levelJson)) {
                res.setGlobalCoeffs(context)
                res.loadResult(context)
            } else {
                res = null
            }
            return res
        }
    }

    fun load(levelJson: JSONObject): Boolean {
        if (!levelJson.has(LevelField.LEVEL_CODE.str) || !levelJson.has(LevelField.NAME.str) ||
            !levelJson.has(LevelField.DIFFICULTY.str) || !levelJson.has(LevelField.TYPE.str) ||
            !levelJson.has(LevelField.ORIGINAL_EXPRESSION.str) || !levelJson.has(LevelField.FINAL_EXPRESSION.str)) {
            return false
        }
        if (levelJson.optBoolean(LevelField.IGNORE.str, false)) {
            return false
        }
        levelCode = levelJson.getString(LevelField.LEVEL_CODE.str)
        name = levelJson.getString(LevelField.NAME.str)
        difficulty = levelJson.getDouble(LevelField.DIFFICULTY.str).toFloat()
        val typeStr = levelJson.getString(LevelField.TYPE.str)
        try {
            type = Type.valueOf(typeStr.toUpperCase())
        } catch (e: Exception) {
            return false
        }
        stepsNum = levelJson.optInt(LevelField.STEPS_NUM.str, stepsNum)
        time = levelJson.optLong(LevelField.TIME.str, time)
        endless = levelJson.optBoolean(LevelField.ENDLESS.str, endless)
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
        /** EXPRESSIONS */
        startExpressionStr = levelJson.getString(LevelField.ORIGINAL_EXPRESSION.str)
        startExpression = structureStringToExpression(startExpressionStr)
        endExpressionStr = levelJson.getString(LevelField.FINAL_EXPRESSION.str)
        endExpression = structureStringToExpression(endExpressionStr)
        endExpressionStr = expressionToString(endExpression)
        endPatternStr = levelJson.optString(LevelField.FINAL_PATTERN.str, "")
        endPattern = when (type) {
            Type.SET -> stringToExpressionStructurePattern(endPatternStr, type.str)
            else -> stringToExpressionStructurePattern(endPatternStr)
        }
        val rulesJson = levelJson.getJSONArray(LevelField.RULES.str)
        for (i in 0 until rulesJson.length()) {
            val rule = rulesJson.getJSONObject(i)
            when {
                /** PACKAGE */
                rule.has(PackageField.RULE_PACK.str) -> {
                    packages.add(rule.getString(PackageField.RULE_PACK.str))
                }
                /** SUBSTITUTION */
                rule.has(PackageField.RULE_LEFT.str) && rule.has(PackageField.RULE_RIGHT.str) -> {
                    rules.add(RulePackage.parseRule(rule, type))
                }
                else -> return false
            }
        }
        return true
    }

    private fun setGlobalCoeffs(context: Context) {
        if (Storage.shared.isUserAuthorized(context)) {
            val coeffs = Storage.shared.getUserCoeffs(context)
            undoPolicy = UndoPolicy.values()[coeffs.undoCoeff ?: 0]
            timeMultCoeff = coeffs.timeCoeff ?: timeMultCoeff
            time = (time * timeMultCoeff).toLong()
            awardMultCoeff = coeffs.awardCoeff ?: awardMultCoeff
        }
    }

    fun checkEnd(expression: ExpressionNode): Boolean {
        Log.d(TAG, "checkEnd")
        return if (endPatternStr.isBlank()) {
            val currStr = expressionToString(expression)
            Log.d(TAG, "current: $currStr | end: $endExpressionStr")
            currStr == endExpressionStr
        } else {
            val currStr = expressionToString(expression)
            Log.d(TAG, "current: $currStr | pattern: $endPatternStr")
            compareByPattern(expression, endPattern)
        }
    }

    fun getAward(resultTime: Long, resultStepsNum: Float): Award {
        Log.d(TAG, "getAward")
        val mark = when {
            resultStepsNum < stepsNum -> 1.0
            resultTime > time -> 0.0
            else -> (1 - resultTime.toDouble() / time + stepsNum.toDouble() / resultStepsNum) / 2
        }
        return Award(getAwardByCoeff(mark), mark)
    }

    private fun getAwardByCoeff(mark: Double): AwardType {
        val awards = awardCoeffs.split(" ").map { it.toDouble() * awardMultCoeff }
        return when {
            mark.equals(1.0) -> AwardType.PLATINUM
            mark >= awards[0] -> AwardType.GOLD
            awards[1] <= mark && mark < awards[0] -> AwardType.SILVER
            awards[2] <= mark && mark < awards[1] -> AwardType.BRONZE
            mark.equals(-1.0) -> AwardType.PAUSED
            else -> AwardType.NONE
        }
    }

    fun getRulesFor(node: ExpressionNode, expression: ExpressionNode): List<ExpressionSubstitution>? {
        Log.d(TAG, "getRulesFor")
        // TODO: smek with showWrongRules flag
        var res: ArrayList<ExpressionSubstitution> = rules
            .filter {
                val list = findSubstitutionPlacesInExpression(expression, it)
                if (list.isEmpty()) {
                    false
                } else {
                    val substPlace = list.find { sp ->
                        sp.originalValue.nodeId == node.nodeId
                    }
                    substPlace != null
                }
            } as ArrayList<ExpressionSubstitution>
        for (pckgName in packages) {
            val rulesFromPack = game.rulePacks[pckgName]!!.getRulesFor(node, expression)
            if (rulesFromPack != null) {
                res = (res + rulesFromPack) as ArrayList<ExpressionSubstitution>
            }
        }
        if (res.isEmpty()) {
            return null
        }
        res.sortByDescending { it.left.identifier.length }
        return res
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(game.gameCode, MODE_PRIVATE)
        val prefEdit = prefs.edit()
        if (lastResult == null) {
            prefEdit.remove(levelCode)
        } else {
            prefEdit.putString(levelCode, lastResult!!.saveString())
        }
        prefEdit.commit()
    }

    private fun loadResult(context: Context) {
        val prefs = context.getSharedPreferences(game.gameCode, MODE_PRIVATE)
        val resultStr = prefs.getString(levelCode, "")
        if (!resultStr.isNullOrEmpty()) {
            val resultVals = resultStr.split(" ", limit = 4)
            lastResult = Result(resultVals[0].toFloat(), resultVals[1].toLong(),
                Award(getAwardByCoeff(resultVals[2].toDouble()), resultVals[2].toDouble()))
            if (resultVals.size == 4) {
                lastResult!!.expression = resultVals[3]
            }
        }
    }
}