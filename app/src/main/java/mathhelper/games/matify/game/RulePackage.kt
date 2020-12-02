package mathhelper.games.matify.game

import android.util.Log
import api.expressionSubstitutionFromStructureStrings
import api.findSubstitutionPlacesInExpression
import expressiontree.ExpressionNode
import expressiontree.ExpressionSubstitution
import org.json.JSONObject
import mathhelper.games.matify.level.Type
import mathhelper.games.matify.level.Type.*

enum class PackageField(val str: String) {
    RULE_PACK("rulePack"),
    RULE_LEFT("left"),
    RULE_RIGHT("right"),
    BASED_ON_TASK_CONTEXT("basedOnTaskContext"),
    MATCH_JUMBLED_AND_NESTED("matchJumbledAndNested"),
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
            val basedOnTaskContext = ruleInfo.optBoolean(PackageField.BASED_ON_TASK_CONTEXT.str, false)
            val matchJumbledAndNested = ruleInfo.optBoolean(PackageField.MATCH_JUMBLED_AND_NESTED.str, false)
            return expressionSubstitutionFromStructureStrings(from, to, basedOnTaskContext, matchJumbledAndNested)
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

    fun getAllRules(): List<ExpressionSubstitution>? {
        var res : ArrayList<ExpressionSubstitution> = rules
        for (pckg in children) {
            val rulesFromPack = pckg.getAllRules()
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