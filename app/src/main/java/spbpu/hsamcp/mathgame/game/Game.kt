package spbpu.hsamcp.mathgame.game

import android.content.Context
import android.util.Log
import org.json.JSONObject
import spbpu.hsamcp.mathgame.level.*

enum class GameField(val str: String) {
    GAMESPACE("gameSpace"),
    GAME_CODE("gameCode"),
    NAME("name"),
    VERSION("version"),
    LEVELS("levels"),
    RULE_PACKS("rulePacks")
}

class Game(var fileName: String) {
    lateinit var levels: ArrayList<Level>
    lateinit var levelsJsons: ArrayList<JSONObject>
    lateinit var rulePacks: HashMap<String, RulePackage>
    lateinit var rulePacksJsons: HashMap<String, JSONObject>
    lateinit var gameSpace: String
    lateinit var gameCode: String
    lateinit var name: String
    var version: Long = 0
    var loaded = false

    companion object {
        private val TAG = "Game"

        fun create(fileName: String, context: Context): Game? {
            Log.d(TAG, "create")
            var res: Game? = Game(fileName)
            if (res!!.preload(context)) {
                res.loadResult(context)
            } else {
                res = null
            }
            return res
        }
    }

    fun preload(context: Context): Boolean {
        Log.d(TAG, "preload")
        return when {
            context.assets != null -> {
                val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
                val gameJson = JSONObject(json)
                preparse(gameJson)
            }
            else -> false
        }
    }

    fun load(context: Context): Boolean {
        Log.d(TAG, "load")
        return loaded || parse(context)
    }

    private fun preparse(gameJson: JSONObject): Boolean {
        if (!gameJson.has(GameField.GAMESPACE.str) || !gameJson.has(GameField.GAME_CODE.str) ||
            !gameJson.has(GameField.NAME.str) || !gameJson.has(GameField.VERSION.str) ||
            !gameJson.has(GameField.LEVELS.str)) {
            return false
        }
        gameSpace = gameJson.getString(GameField.GAMESPACE.str)
        gameCode = gameJson.getString(GameField.GAME_CODE.str)
        name = gameJson.getString(GameField.NAME.str)
        version = gameJson.getLong(GameField.VERSION.str)
        rulePacks = HashMap()
        rulePacksJsons = HashMap()
        val packsJson = gameJson.getJSONArray(GameField.RULE_PACKS.str)
        for (i in 0 until packsJson.length()) {
            val packInfo = packsJson.getJSONObject(i)
            val name = packInfo.getString(PackageField.NAME.str)
            if (!rulePacksJsons.containsKey(name)) {
                rulePacksJsons[name] = packInfo
            }
        }
        levels = ArrayList()
        levelsJsons = ArrayList()
        val levelsJson = gameJson.getJSONArray(GameField.LEVELS.str)
        for (i in 0 until levelsJson.length()) {
            levelsJsons.add(levelsJson.getJSONObject(i))
        }
        return true
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
        for (json in levelsJsons) {
            val level = Level.parse(this, json, context)
            if (level != null) {
                levels.add(level)
            }
        }
        loaded = true
        return true
    }

    private fun save(context: Context) {}
    private fun loadResult(context: Context) {
        // TODO: read from storage levels passed percentage
    }
}