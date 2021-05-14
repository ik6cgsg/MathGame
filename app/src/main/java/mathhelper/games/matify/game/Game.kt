package mathhelper.games.matify.game

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import org.json.JSONObject
import mathhelper.games.matify.level.*
import mathhelper.games.matify.parser.GsonParser
import mathhelper.games.matify.parser.Required
import org.json.JSONArray

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
    var otherData: JsonObject? = null,
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

        fun create(fileName: String, context: Context): Game? {
            Log.d(TAG, "create")
            val res = preload(fileName, context)
            res?.loadResult(context)
            return res
        }

        private fun preload(fileName: String, context: Context): Game? {
            Log.d(TAG, "preload")
            var res: Game? = null
            when {
                context.assets != null -> {
                    val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
                    val full = GsonParser.parse<FullTaskset>(json) ?: return null
                    res = GsonParser.parse(full.taskset)
                    if (res?.preparseRulePacks(full.rulePacks) != true) {
                        res = null
                    }
                }
                else -> res = null
            }
            return res
        }
    }

    private fun preparseRulePacks(packsJson: List<JsonObject>): Boolean {
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
        Log.d(TAG, "load")
        return loaded || parse(context)
    }

    private fun parse(context: Context): Boolean {
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
        val prefs = context.getSharedPreferences(code, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        if (lastResult == null) {
            prefEdit.remove(code)
        } else {
            prefEdit.putString(code, lastResult!!.saveString())
        }
        prefEdit.commit()
    }

    private fun loadResult(context: Context) {
        val prefs = context.getSharedPreferences(code, Context.MODE_PRIVATE)
        val resultStr = prefs.getString(code, "")
        if (!resultStr.isNullOrEmpty()) {
            val resultVals = resultStr.split(" ", limit = 2)
            lastResult = GameResult(resultVals[0].toInt(), resultVals[1].toInt())
        }
    }
}