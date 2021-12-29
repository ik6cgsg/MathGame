package mathhelper.games.matify.game

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import mathhelper.games.matify.common.Constants.Companion.defaultRulePriority
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.parser.GsonParser
import mathhelper.games.matify.parser.Required
import mathhelper.twf.api.expressionSubstitutionFromStructureStrings
import mathhelper.twf.expressiontree.ExpressionSubstitution
import mathhelper.twf.expressiontree.ExpressionSubstitutionNormType

data class Rule(
    /** Required values **/
    var leftStructureString: String = "",
    var rightStructureString: String = "",
    var code: String = "",
    /** Optional values **/
    var nameEn: String = "",
    var nameRu: String = "",
    var descriptionShortEn: String = "",
    var descriptionShortRu: String = "",
    var descriptionEn: String = "",
    var descriptionRu: String = "",
    var basedOnTaskContext: Boolean = false,
    var matchJumbledAndNested: Boolean = false,
    var isExtending: Boolean = false,
    var simpleAdditional: Boolean = false,
    var priority: Int = defaultRulePriority,
    var normalizationType: String = ExpressionSubstitutionNormType.ORIGINAL.name,
    var weight: Double = 1.0,
) {
    lateinit var substitution: ExpressionSubstitution
}

data class RulePackLink(
    var namespaceCode: String,
    var rulePackCode: String,
    var rulePackNameEn: String,
    var rulePackNameRu: String
)

data class RulePackage(
    /** Required values **/
    @property:Required
    var namespaceCode: String = "",
    @property:Required
    var code: String = "",
    @property:Required
    var version: Int = 0,
    /** Optional values **/
    var nameEn: String = "",
    var nameRu: String = "",
    var descriptionShortEn: String = "",
    var descriptionShortRu: String = "",
    var descriptionEn: String = "",
    var descriptionRu: String = "",
    var rulePacks: List<RulePackLink>? = null,
    var rules: List<JsonObject>? = null,
    var otherData: JsonElement? = null
) {
    var rulesExpr: ArrayList<Rule> = ArrayList()
    var children = ArrayList<RulePackage>()

    companion object {
        private val TAG = "RulePackage"

        fun parse(code: String, rulePacksJsons: HashMap<String, JsonObject>, rulePacks: HashMap<String, RulePackage>): RulePackage? {
            Logger.d(TAG, "parse pack with code = $code")
            val packJson = rulePacksJsons[code]!!
            val resPckg = GsonParser.parse<RulePackage>(packJson)
            if (resPckg?.rules != null) {
                for (ruleJson in resPckg.rules!!) {
                    resPckg.rulesExpr.add(parseRule(ruleJson) ?: continue)
                }
            }
            if (resPckg?.rulePacks != null) {
                for (link in resPckg.rulePacks!!) {
                    when {
                        rulePacks.containsKey(link.rulePackCode) -> resPckg.children.add(rulePacks[link.rulePackCode]!!)
                        rulePacksJsons.containsKey(link.rulePackCode) -> {
                            val newRulePack = parse(link.rulePackCode, rulePacksJsons, rulePacks)
                            if (newRulePack != null) {
                                resPckg.children.add(newRulePack)
                                rulePacks[link.rulePackCode] = newRulePack
                            }
                        }
                        else -> {
                            Logger.e(TAG,"Can't parse child")
                        }
                    }
                }
            }
            return resPckg
        }

        fun parseRule(ruleInfo: JsonObject): Rule? {
            //Logger.d(TAG, "parseRule")
            var rule = GsonParser.parse<Rule>(ruleInfo)
            if (rule != null) {
                try {
                    rule.substitution = expressionSubstitutionFromStructureStrings(
                        leftStructureString = rule.leftStructureString,
                        rightStructureString = rule.rightStructureString,
                        basedOnTaskContext = rule.basedOnTaskContext,
                        matchJumbledAndNested = rule.matchJumbledAndNested,
                        simpleAdditional = rule.simpleAdditional,
                        isExtending = rule.isExtending,
                        priority = rule.priority,
                        code = rule.code,
                        nameEn = rule.nameEn,
                        nameRu = rule.nameRu,
                        normalizationType = ExpressionSubstitutionNormType.valueOf(rule.normalizationType)
                    )
                } catch(e: Exception) {
                    Logger.e(TAG, "Parse rule error: $e")
                    rule = null
                }
            }
            return rule
        }
    }

    fun getAllRules(): List<ExpressionSubstitution>? {
        var res : ArrayList<ExpressionSubstitution> = rulesExpr.map { it.substitution } as ArrayList<ExpressionSubstitution>
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