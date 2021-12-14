package mathhelper.games.matify.game

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.common.TasksetInfo
import mathhelper.games.matify.level.Level
import mathhelper.games.matify.parser.GsonParser
import mathhelper.games.matify.parser.Required

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
    var isPreview = true
    var isDefault = false
    var isPinned = false

    fun getNameByLanguage (languageCode: String) = if (languageCode.equals("ru", true)) {
        nameRu
    } else {
        nameEn
    }

    companion object {
        private val TAG = "Game"

        fun create(info: TasksetInfo, context: Context): Game? {
            Logger.d(TAG, "create")
            val res = preload(info.taskset)
            res?.isPreview = info.isPreview
            if (!info.isPreview) {
                res?.preparseRulePacks(info.rulePacks!!)
            }
            res?.isDefault = info.isDefault
            res?.loadResult(context)
            return res
        }

        private fun preload(json: JsonObject): Game? {
            Logger.d(TAG, "preload")
            return GsonParser.parse(json)
        }
    }

    fun updateWithJsons(json: JsonObject, packsJson: List<JsonObject>) {
        val updated = GsonParser.parse<Game>(json) ?: return
        namespaceCode = updated.namespaceCode
        code = updated.code
        version = updated.version
        tasks = updated.tasks
        nameEn = updated.nameEn
        nameRu = updated.nameRu
        descriptionShortEn = updated.descriptionShortEn
        descriptionShortRu = updated.descriptionShortRu
        descriptionEn = updated.descriptionEn
        descriptionRu = updated.descriptionRu
        subjectType = updated.subjectType
        recommendedByCommunity = updated.recommendedByCommunity
        otherData = updated.otherData
        preparseRulePacks(packsJson)
        isPreview = false
    }

    private fun preparseRulePacks(packsJson: List<JsonObject>): Boolean {
        return try {
            //val packsJson: Array<JsonObject> = Gson().fromJson(packs, Array<JsonObject>::class.java)
            rulePacks = HashMap()
            rulePacksJsons = HashMap()
            for (pack in packsJson) {
                val code = pack.get("code").asString
                if (!rulePacksJsons.containsKey(code)) {
                    rulePacksJsons[code] = pack
                }
            }
            true
        } catch (e: Exception) { false }
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
        Storage.shared.saveResult(lastResult?.saveString(), code)
    }

    private fun loadResult(context: Context) {
        Logger.d(TAG, "loadResult")
        val resultStr = Storage.shared.loadResult(code)
        Logger.d(TAG, "loadResult", "loaded result = $resultStr")
        if (resultStr.isNotBlank()) {
            val resultVals = resultStr.split(" ", limit = 2)
            lastResult = GameResult(resultVals[0].toInt(), resultVals[1].toInt())
        }
    }
}