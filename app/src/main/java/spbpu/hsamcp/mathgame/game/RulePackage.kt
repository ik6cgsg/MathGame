package spbpu.hsamcp.mathgame.game

import android.util.Log
import com.twf.api.expressionSubstitutionFromStructureStrings
import com.twf.api.findSubstitutionPlacesInExpression
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import org.json.JSONObject
import spbpu.hsamcp.mathgame.level.Type
import spbpu.hsamcp.mathgame.level.Type.*

enum class PackageField(val str: String) {
    RULE_PACK("rulePack"),
    RULE_LEFT("left"),
    RULE_RIGHT("right"),
    TYPE("type"),
    NAME("name"),
    RULES("rules")
}

class RulePackage
private constructor(
    var name: String,
    var type: Type? = null
) {
    var rules: ArrayList<ExpressionSubstitution> = ArrayList()
    var children = ArrayList<RulePackage>()
    /*
    val allRules: ArrayList<ExpressionSubstitution>
        get() {
            var all = rules
            for (pack in children) {
                all = (all + pack.allRules) as ArrayList<ExpressionSubstitution>
            }
            return all
        }
    */
    companion object {
        fun parse(name: String, rulePacksJsons: HashMap<String, JSONObject>, rulePacks: HashMap<String, RulePackage>): RulePackage? {
            val packJson = rulePacksJsons[name]!!
            var type: Type? = null
            if (packJson.has(PackageField.TYPE.str)) {
                val typeStr = packJson.getString(PackageField.TYPE.str)
                try {
                    type = valueOf(typeStr.toUpperCase())
                } catch (e: Exception) {
                    Log.e("RulePackage::parse", e.message)
                    return null
                }
            }
            var resPckg = RulePackage(name, type)
            val rulesJson = packJson.getJSONArray(PackageField.RULES.str)
            for (i in 0 until rulesJson.length()) {
                val ruleInfo = rulesJson.getJSONObject(i)
                when {
                    /** PACKAGE */
                    ruleInfo.has(PackageField.RULE_PACK.str) -> {
                        val packageName = ruleInfo.getString(PackageField.RULE_PACK.str)
                        when {
                            rulePacks.containsKey(packageName) -> resPckg.children.add(rulePacks[packageName]!!)
                            rulePacksJsons.containsKey(packageName) -> {
                                val newRulePack = parse(packageName, rulePacksJsons, rulePacks)
                                if (newRulePack != null) {
                                    resPckg.children.add(newRulePack)
                                    rulePacks[packageName] = newRulePack
                                }
                            }
                            else -> {
                                Log.e("RulePackage::parse", "Can't parse child")
                            }
                        }
                    }
                    /** SUBSTITUTION */
                    type != null &&
                            ruleInfo.has(PackageField.RULE_LEFT.str) && ruleInfo.has(PackageField.RULE_RIGHT.str) -> {
                        resPckg.rules.add(parseRule(ruleInfo, type))
                    }
                    else -> return null
                }
            }
            return resPckg
        }

        fun parseRule(ruleInfo: JSONObject, type: Type): ExpressionSubstitution {
            val from = ruleInfo.getString(PackageField.RULE_LEFT.str)
            val to = ruleInfo.getString(PackageField.RULE_RIGHT.str)
            return expressionSubstitutionFromStructureStrings(from, to)
        }
    }

    fun getRulesFor(node: ExpressionNode, expression: ExpressionNode): List<ExpressionSubstitution>? {
        var res = rules
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
            }
        for (pckg in children) {
            val rulesFromPack = pckg.getRulesFor(node, expression)
            if (rulesFromPack != null) {
                res = (res + rulesFromPack) as ArrayList<ExpressionSubstitution>
            }
        }
        if (res.isEmpty()) {
            return null
        }
        return res
    }
}