package spbpu.hsamcp.mathgame.level

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

enum class Type(val str: String) {
    SET("setTheory"),
    ALGEBRA("algebra"),
    TRIGONOMETRY("trigonometry")
}

class Level(var fileName: String) {
    var rules = ArrayList<ExpressionSubstitution>()
    lateinit var startFormula: ExpressionNode
    lateinit var endFormula: ExpressionNode
    lateinit var endFormulaString: String
    lateinit var type: Type
    var taskId = 0
    var name = "test"
    var awardCoeffs = "0.88 0.65 0.45"
    var showWrongRules = false
    var showSubstResult = false
    var undoConsideringPolicy = ""
    var longExpressionCroppingPolicy = ""
    var lastResult: Result? = null
    var difficulty = 0
    var stepsNum = 1
    var time: Long = 180

    companion object {
        private val TAG = "Level"

        fun create(fileName: String, assets: AssetManager? = null): Level? {
            Log.d(TAG, "create")
            var res: Level? = Level(fileName)
            if (!res!!.load(assets)) {
                res = null
            }
            return res
        }
    }

    fun load(assets: AssetManager? = null): Boolean {
        Log.d(TAG, "load")
        return when {
            assets != null -> {
                val json = assets.open(fileName).bufferedReader().use { it.readText() }
                val levelJson = JSONObject(json)
                taskId = levelJson.optInt("taskId", taskId)
                name = levelJson.optString("name", name)
                difficulty = levelJson.optInt("difficulty", difficulty)
                val typeStr = levelJson.getString("type")
                try {
                    type = Type.valueOf(typeStr.toUpperCase())
                } catch (e: Exception) {
                    return false
                }
                stepsNum = levelJson.optInt("stepsNum", stepsNum)
                time = levelJson.optLong("time", time)
                awardCoeffs = levelJson.optString("awardCoeffs", awardCoeffs)
                showWrongRules = levelJson.optBoolean("showWrongRules", showWrongRules)
                showSubstResult = levelJson.optBoolean("showSubstResult", showSubstResult)
                undoConsideringPolicy = levelJson.optString("undoConsideringPolicy", undoConsideringPolicy)
                longExpressionCroppingPolicy = levelJson.optString("longExpressionCroppingPolicy",
                    longExpressionCroppingPolicy)
                val startFormulaStr = levelJson.getString("originalExpression")
                val endFormulaStr = levelJson.getString("finalExpression")
                startFormula = when (type) {
                    Type.SET -> stringToExpression(startFormulaStr, type.str)
                    else -> stringToExpression(startFormulaStr)
                }
                endFormula = when (type) {
                    Type.SET -> stringToExpression(endFormulaStr, type.str)
                    else -> stringToExpression(endFormulaStr)
                }
                endFormulaString = expressionToString(endFormula)
                val rulesJson = levelJson.getJSONArray("allSubstitutions")
                for (i in 0 until rulesJson.length()) {
                    val rule = rulesJson.getJSONObject(i)
                    val from = rule.getString("left")
                    val to = rule.getString("right")
                    val ruleSubst = when (type) {
                        Type.SET -> expressionSubstitutionFromStrings(from, to, type.str)
                        else -> expressionSubstitutionFromStrings(from, to)
                    }
                    rules.add(ruleSubst)
                }
                rules.shuffle()
                // TODO: read last resultx
                true
            }
            else -> false
        }
    }

    fun checkEnd(formula: ExpressionNode): Boolean {
        Log.d(TAG, "checkEnd")
        val currStr = expressionToString(formula)
        Log.d(TAG, "current: $currStr | end: $endFormulaString")
        return currStr == endFormulaString
    }

    fun getAward(resultTime: Long, resultStepsNum: Int): Award {
        Log.d(TAG, "getAward")
        return if (resultStepsNum < stepsNum) {
            Award(AwardType.PLATINUM, 1.0)
        } else {
            val awards = awardCoeffs.split(" ").map { it.toDouble() }
            val mark = (1 - resultTime.toDouble() / time + stepsNum.toDouble() / resultStepsNum) / 2
            when {
                mark >= awards[0] -> Award(AwardType.GOLD, mark)
                awards[1] <= mark && mark < awards[0] -> Award(AwardType.SILVER, mark)
                awards[2] <= mark && mark < awards[1] -> Award(AwardType.BRONZE, mark)
                else -> Award(AwardType.NONE, mark)
            }
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

    fun save() {
        // TODO: save result
    }
}