package mathhelper.games.matify.game

import android.content.Context
import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.common.Storage
import org.json.JSONObject
import mathhelper.games.matify.level.*
import mathhelper.games.matify.parser.GsonParser
import mathhelper.games.matify.parser.Required
import org.jetbrains.annotations.Nullable
import org.json.JSONArray

data class FilterTaskset(
    var tasksets: List<JsonObject>
)

data class FullTaskset(
    @property:Required
    var taskset: JsonObject,
    @property:Required
    var rulePacks: List<JsonObject>
)

data class Game(
    /** Required values **/
    @property:Required
    var namespaceCode: String = "",
    @property:Required
    var code: String = "",
    @property:Required
    var version: Int = 0,
    @property:Required
    var nameEn: String = "",
    @property:Required
    var tasks: List<JsonObject> = listOf(),
    /** Optional values **/
    var nameRu: String = "",
    var descriptionShortEn: String = "",
    var descriptionShortRu: String = "",
    var descriptionEn: String = "",
    var descriptionRu: String = "",
    var subjectType: String = "",
    var recommendedByCommunity: Boolean = false,
    var otherData: JsonElement? = null,
) {
    lateinit var levels: ArrayList<Level>
    lateinit var rulePacks: HashMap<String, RulePackage>
    lateinit var rulePacksJsons: HashMap<String, JsonObject>
    var loaded = false
    var lastResult: GameResult? = null

    fun getNameByLanguage (languageCode: String) = if (languageCode.equals("ru", true)) {
        nameRu
    } else {
        nameEn
    }

    companion object {
        private val TAG = "Game"

        fun create(json: String, context: Context): Game? {
            Logger.d(TAG, "create")
            val res = preload(json)
            res?.loadResult(context)
            return res
        }

        private fun preload(json: String): Game? {
            Logger.d(TAG, "preload")
            return GsonParser.parse(json)
        }
    }

    fun preparseRulePacks(packsJson: List<JsonObject>): Boolean {
        rulePacks = HashMap()
        rulePacksJsons = HashMap()
        for (pack in packsJson) {
            val code = pack.get("code").asString
            if (!rulePacksJsons.containsKey(code)) {
                rulePacksJsons[code] = pack
            }
        }
        return true
    }

    fun load(context: Context): Boolean {
        Logger.d(TAG, "load")
        return loaded || parse(context)
    }

    private fun parse(context: Context): Boolean {
        Logger.d(TAG, "parse")
        for (key in rulePacksJsons.keys) {
            if (!rulePacks.containsKey(key)) {
                val pack = RulePackage.parse(key, rulePacksJsons, rulePacks)
                if (pack != null) {
                    rulePacks[key] = pack
                }
            }
        }
        levels = arrayListOf()
        for (json in tasks) {
            val level = Level.parse(this, json, context)
            if (level != null) {
                levels.add(level)
            }
        }
        loaded = true
        return true
    }

    fun save(context: Context) {
        Logger.d(TAG, "save", lastResult?.saveString())
        Storage.shared.saveResult(context, lastResult?.saveString(), code)
    }

    private fun loadResult(context: Context) {
        Logger.d(TAG, "loadResult")
        val resultStr = Storage.shared.loadResult(context, code)
        Logger.d(TAG, "loadResult", "loaded result = $resultStr")
        if (resultStr.isNotBlank()) {
            val resultVals = resultStr.split(" ", limit = 2)
            lastResult = GameResult(resultVals[0].toInt(), resultVals[1].toInt())
        }
    }
}