package spbpu.hsamcp.mathgame

import android.content.res.AssetManager
import android.util.JsonReader
import android.util.Log
import com.twf.api.expressionSubstitutionFromStrings
import com.twf.api.expressionToString
import com.twf.api.findSubstitutionPlacesInExpression
import com.twf.api.stringToExpression
import com.twf.config.CompiledConfiguration
import com.twf.config.FunctionConfiguration
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import org.json.JSONObject

class Level(var fileName: String) {
    var rules = ArrayList<ExpressionSubstitution>()
    lateinit var startFormula: ExpressionNode
    lateinit var endFormula: ExpressionNode
    lateinit var endFormulaString: String
    lateinit var name: String
    lateinit var type: String
    var lastResult: Result? = null
    var difficulty = 0
    var stepsNum = 0
    var time = 0

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
                name = levelJson.getString("name")
                difficulty = levelJson.getInt("difficulty")
                type = levelJson.getString("type")
                stepsNum = levelJson.getInt("stepsNum")
                time = levelJson.getInt("time")
                val startFormulaStr = levelJson.getString("originalExpression")
                val endFormulaStr = levelJson.getString("finalExpression")
                startFormula = when (type) {
                    "set" -> stringToExpression(startFormulaStr, "setTheory")
                    else -> stringToExpression(startFormulaStr)
                }
                endFormula = when (type) {
                    "set" -> stringToExpression(endFormulaStr, "setTheory")
                    else -> stringToExpression(endFormulaStr)
                }
                endFormulaString = expressionToString(endFormula)
                val rulesJson = levelJson.getJSONArray("allSubstitutions")
                for (i in 0 until rulesJson.length()) {
                    val rule = rulesJson.getJSONObject(i)
                    val from = rule.getString("left")
                    val to = rule.getString("right")
                    val ruleSubst = when (type) {
                        "set" -> expressionSubstitutionFromStrings(from, to, "setTheory")
                        else -> expressionSubstitutionFromStrings(from, to)
                    }
                    rules.add(ruleSubst)
                }
                rules.shuffle()
                // TODO: read last result
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
            Award.PLATINUM
        } else {
            val mark = (1 - resultTime.toDouble() / time + stepsNum.toDouble() / resultStepsNum) / 2
            when {
                mark >= 0.88 -> Award.GOLD
                0.65 <= mark && mark < 0.88 -> Award.SILVER
                0.45 <= mark && mark < 0.65 -> Award.BRONZE
                else -> Award.NONE
            }
        }
    }

    fun getRulesFor(node: ExpressionNode, formula: ExpressionNode): List<ExpressionSubstitution>? {
        Log.d(TAG, "getRulesFor")
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