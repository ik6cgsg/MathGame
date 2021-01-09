package mathhelper.games.matify.game

import android.util.Log
import api.expressionSubstitutionFromStructureStrings
import api.findSubstitutionPlacesInExpression
import expressiontree.ExpressionNode
import expressiontree.ExpressionSubstitution
import mathhelper.games.matify.common.Constants.Companion.defaultRulePriority
import org.json.JSONObject
import mathhelper.games.matify.level.Type
import mathhelper.games.matify.level.Type.*

enum class PackageField(val str: String) {
    RULE_PACK("rulePack"),
    RULE_LEFT("left"),
    RULE_RIGHT("right"),
    BASED_ON_TASK_CONTEXT("basedOnTaskContext"),
    MATCH_JUMBLED_AND_NESTED("matchJumbledAndNested"),
    SIMPLE_ADDITIONAL("simpleAdditional"),
    IS_EXTENDING("isExtending"),
    PRIORITY("priority"),
    NAME_EN("nameEn"),
    NAME_RU("nameRu"),
    CODE("code"),

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
                        ((ruleInfo.has(PackageField.RULE_LEFT.str) && ruleInfo.has(PackageField.RULE_RIGHT.str)) || ruleInfo.has(PackageField.CODE.str)) -> {
                        resPckg.rules.add(parseRule(ruleInfo, type))
                    }
                    else -> return null
                }
            }
            return resPckg
        }

        fun parseRule(ruleInfo: JSONObject, type: Type): ExpressionSubstitution {
            val from = ruleInfo.optString(PackageField.RULE_LEFT.str, "")
            val to = ruleInfo.optString(PackageField.RULE_RIGHT.str, "")
            val basedOnTaskContext = ruleInfo.optBoolean(PackageField.BASED_ON_TASK_CONTEXT.str, false)
            val matchJumbledAndNested = ruleInfo.optBoolean(PackageField.MATCH_JUMBLED_AND_NESTED.str, false)
            val simpleAdditional = ruleInfo.optBoolean(PackageField.SIMPLE_ADDITIONAL.str, false)
            val isExtending = ruleInfo.optBoolean(PackageField.IS_EXTENDING.str, false)
            val priority = ruleInfo.optInt(PackageField.PRIORITY.str, defaultRulePriority)
            val nameEn = ruleInfo.optString(PackageField.NAME_EN.str, "")
            val nameRu = ruleInfo.optString(PackageField.NAME_RU.str, "")
            val code = ruleInfo.optString(PackageField.CODE.str, "")
            return expressionSubstitutionFromStructureStrings(from, to, basedOnTaskContext, matchJumbledAndNested, simpleAdditional, isExtending, priority, code, nameEn, nameRu)
        }
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