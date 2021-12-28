package mathhelper.games.matify.common

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import mathhelper.games.matify.AuthStatus
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.R
import mathhelper.games.matify.game.FullTaskset
import mathhelper.games.matify.game.GameResult
import mathhelper.games.matify.level.LevelResult
import mathhelper.games.matify.level.StateType
import mathhelper.games.matify.common.RequestData
import java.io.File
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.absoluteValue
import kotlin.text.Charsets.UTF_8

enum class AuthInfo(val str: String) {
    UUID("uuid"),
    SERVER_TOKEN("serverToken"),
    LOGIN("login"),
    PASSWORD("password"),
    NAME("name"),
    FULL_NAME("fullName"),
    ADDITIONAL("additional"),
    AUTHORIZED("authorized"),
    AUTH_STATUS("authStatus")
}

enum class BaseInfo(val str: String) {
    DEVICE_ID("deviceId")
}

enum class SettingInfo(val str: String) {
    THEME("theme"),
    LANGUAGE("language"),
    ORDER("order"),
    PINNED("pinned")
}

enum class LogInfo(val str: String) {
    REQUESTS("requests")
}

data class TasksetInfo (
    val taskset: JsonObject,
    val rulePacks: List<JsonObject>? = null,
    val version: Int = 0,
    val isPreview: Boolean = true,
    val isDefault: Boolean = false
)

data class AuthInfoObjectFull(
    val base: AuthInfoObjectBase = AuthInfoObjectBase(),
    val uuid: String? = null
)

data class AuthInfoObjectBase(
    var login: String? = null,
    var password: String? = null,
    val name: String? = null,
    val fullName: String? = null,
    val additional: String? = null,
    val authorized: Boolean? = null,
    val authStatus: AuthStatus? = null,
    val serverToken: String? = null
)

interface SavableResult {
    fun saveString(): String
}

@SuppressLint("ApplySharedPref")
class Storage {
    companion object {
        private const val userInfoFile = "USER_INFO"
        private const val settingFile = "SETTINGS"
        private const val logFile = "LOGS"
        private const val base = "BASE"
        private const val resultsFile = "RESULTS"
        val shared = Storage()
    }

    lateinit var context: WeakReference<Context>

    fun checkDeviceId() {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(base, Context.MODE_PRIVATE)
        if (!prefs.contains(BaseInfo.DEVICE_ID.str)) {
            val prefEdit = prefs.edit()
            prefEdit.putString(BaseInfo.DEVICE_ID.str, UUID.randomUUID().toString())
            prefEdit.commit()
        }
    }

    fun deviceId(): String {
        val context = context.get() ?: return ""
        return context.getSharedPreferences(base, Context.MODE_PRIVATE)
            .getString(BaseInfo.DEVICE_ID.str, "")!!
    }

    /*fun swapValueInStringSets(file: String, src: String, dst: String, value: String) {
        val context = context.get() ?: return
        val settings = context.getSharedPreferences(file, Context.MODE_PRIVATE)
        val settingsEdit = settings.edit()
        val srcSet = HashSet(settings.getStringSet(src, setOf())!!)
        val dstSet = HashSet(settings.getStringSet(dst, setOf())!!)
        srcSet.remove(value)
        dstSet.add(value)
        settingsEdit.putStringSet(src, srcSet)
        settingsEdit.putStringSet(dst, dstSet)
        settingsEdit.commit()
    }*/

    //region REQUESTS

    fun saveLogRequests(reqs: LinkedList<RequestData>) {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(logFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        val json = Gson().toJson(reqs)
        prefEdit.putString(LogInfo.REQUESTS.str, json)
        prefEdit.commit()
    }

    fun saveOneLogRequest(req: RequestData) {
        val reqs = getLogRequests()
        reqs.addFirst(req)
        saveLogRequests(reqs)
    }

    fun getLogRequests(): LinkedList<RequestData> {
        val context = context.get() ?: return LinkedList<RequestData>()
        val prefs = context.getSharedPreferences(logFile, Context.MODE_PRIVATE)
        val json = prefs.getString(LogInfo.REQUESTS.str, "[]")
        try {
            val data = Gson().fromJson(json, Array<RequestData>::class.java)
            val reqs = LinkedList(data.toList())
            return reqs
        } catch (e: Exception) {
            return LinkedList<RequestData>()
        }
    }

    //endregion

    //region USER INFO

    fun isUserAuthorized(): Boolean {
        val context = context.get() ?: return false
        return context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
            .getBoolean(AuthInfo.AUTHORIZED.str, false)
    }

    fun invalidateUser() {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, false)
        prefEdit.commit()
    }

    fun authStatus(): AuthStatus {
        val context = context.get() ?: return AuthStatus.GUEST
        return AuthStatus.value(
            context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
                .getString(AuthInfo.AUTH_STATUS.str, AuthStatus.GUEST.str)!!
        )!!
    }

    fun login(): String {
        val context = context.get() ?: return ""
        return context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
            .getString(AuthInfo.LOGIN.str, "")!!
    }

    fun password(): String {
        val context = context.get() ?: return ""
        return context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
            .getString(AuthInfo.PASSWORD.str, "")!!
    }

    fun serverToken(): String {
        val context = context.get() ?: return ""
        return context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
            .getString(AuthInfo.SERVER_TOKEN.str, "")!!
    }

    fun initWithUuid(): UUID {
        val context = context.get() ?: return UUID.randomUUID()
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        val uuid = UUID.randomUUID()
        prefEdit.putString(AuthInfo.UUID.str, uuid.toString())
        prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, true)
        prefEdit.commit()
        return uuid
    }

    fun initUserInfo(info: AuthInfoObjectBase) {
        val context = context.get() ?: return
        val uuid = initWithUuid()
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        if (info.login.isNullOrBlank()) {
            info.login = when (info.authStatus) {
                AuthStatus.GUEST -> "guest-" + uuid.hashCode().absoluteValue
                AuthStatus.MATH_HELPER -> "user-" + uuid.hashCode().absoluteValue
                else -> info.login
            }
        }
        if (info.password.isNullOrBlank()){
            info.password = info.login
        }
        prefEdit.commit()
        setUserInfo(info)
    }

    fun setServerToken(serverToken: String) {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.putString(AuthInfo.SERVER_TOKEN.str, serverToken)
        prefEdit.commit()
    }

    fun setUserInfo(info: AuthInfoObjectBase) {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        if (info.authStatus != null) {
            prefEdit.putString(AuthInfo.AUTH_STATUS.str, info.authStatus.str)
            GlobalScene.shared.authStatus = info.authStatus
        }
        if (info.login != null) {
            prefEdit.putString(AuthInfo.LOGIN.str, info.login)
        }
        if (info.password != null) {
            prefEdit.putString(AuthInfo.PASSWORD.str, info.password)
        }
        if (info.name != null) {
            prefEdit.putString(AuthInfo.NAME.str, info.name)
        }
        if (info.fullName != null) {
            prefEdit.putString(AuthInfo.FULL_NAME.str, info.fullName)
        }
        if (info.additional != null) {
            prefEdit.putString(AuthInfo.ADDITIONAL.str, info.additional)
        }
        if (info.serverToken != null) {
            prefEdit.putString(AuthInfo.SERVER_TOKEN.str, info.serverToken)
        }
        prefEdit.commit()
    }

    fun getUserInfoBase(): AuthInfoObjectBase {
        val context = context.get() ?: return AuthInfoObjectBase()
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        return AuthInfoObjectBase(
            login = prefs.getString(AuthInfo.LOGIN.str, null),
            password = prefs.getString(AuthInfo.PASSWORD.str, null),
            name = prefs.getString(AuthInfo.NAME.str, null),
            fullName = prefs.getString(AuthInfo.FULL_NAME.str, null),
            additional = prefs.getString(AuthInfo.ADDITIONAL.str, null),
            authorized = prefs.getBoolean(AuthInfo.AUTHORIZED.str, false),
            authStatus = AuthStatus.value(prefs.getString(AuthInfo.AUTH_STATUS.str, "")!!),
            serverToken = prefs.getString(AuthInfo.SERVER_TOKEN.str, null)
        )
    }

    fun getFullUserInfo(): AuthInfoObjectFull {
        val context = context.get() ?: return AuthInfoObjectFull()
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        return AuthInfoObjectFull(
            base = getUserInfoBase(),
            uuid = prefs.getString(AuthInfo.UUID.str, "")
        )
    }

    fun clearUserInfo() {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.clear()
        prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, false)
        prefEdit.commit()
    }

    //endregion

    //region RESULTS

    fun resetResults() {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(resultsFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.clear()
        prefEdit.commit()
    }

    fun saveResult(result: String?, gameCode: String, levelCode: String? = null) {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(resultsFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        var key = gameCode
        if (levelCode != null) {
            key += "_$levelCode"
        }
        if (result == null) {
            prefEdit.remove(key)
        } else {
            prefEdit.putString(key, result)
        }
        prefEdit.commit()
    }

    fun loadResult(gameCode: String, levelCode: String? = null): String {
        val context = context.get() ?: return ""
        var result = ""
        var key = gameCode
        if (levelCode != null) {
            key += "_$levelCode"
        }
        val prefs = context.getSharedPreferences(resultsFile, Context.MODE_PRIVATE)
        result = prefs.getString(key, result) ?: result
        return result
    }

    fun saveResultFromServer(userStat: String) {
        if (userStat.isNotEmpty()) {
            val stat = Gson().fromJson(userStat, UserStatForm::class.java)
            for (tsStat in stat.tasksetStatistics) {
                val gameRes = GameResult(tsStat.passedCount, tsStat.pausedCount)
                saveResult(gameRes.saveString(), tsStat.code)
                for (tStat in tsStat.tasksStat) {
                    val levelRes =
                        LevelResult(tStat.steps, tStat.time / 1000, StateType.valueOf(tStat.state), tStat.expression ?: "")
                    saveResult(levelRes.saveString(), tsStat.code, tStat.code)
                }
            }
        }
    }

    //endregion

    //region SETTINGS

    fun setTheme(theme: ThemeName?) {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.putString(SettingInfo.THEME.str, theme.toString())
        prefEdit.commit()
    }

    fun theme(): ThemeName {
        val context = context.get() ?: return ThemeName.DARK
        val theme = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
            .getString(SettingInfo.THEME.str, "")
        return when (theme) {
            "DARK" -> ThemeName.DARK
            "LIGHT" -> ThemeName.LIGHT
            else -> ThemeName.DARK
        }
    }

    fun themeInt(): Int {
        return theme().resId
    }

    fun setLanguage(language: String?) {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        val saveStr = when (language) {
            "ru", "rus" -> "ru"
            "en", "eng" -> "en"
            else -> "ru"
        }
        prefEdit.putString(SettingInfo.LANGUAGE.str, saveStr)
        prefEdit.commit()
    }

    fun language(): String {
        val context = context.get() ?: return ""
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        return prefs.getString(SettingInfo.LANGUAGE.str, "ru")!!
    }

    /*
    fun clearAllGames() {
        val context = context.get() ?: return
        val codes = getAllSavedTasksetCodes()
        for (code in codes) {
            context.getSharedPreferences(code, Context.MODE_PRIVATE).edit().clear().commit()
            File("${context.filesDir.parent}/shared_prefs/$code.xml").delete()
        }
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE).edit()
        prefs.remove(SettingInfo.PRELOADED_GAMES.str)
        prefs.remove(SettingInfo.LOADED_GAMES.str)
        prefs.commit()
    }*/

    //endregion

    //region TASKSETS

    fun saveTaskset(code: String, tasksetJson: JsonObject, rulePacks: List<JsonObject>? = null, isDefault: Boolean = false) {
        val context = context.get() ?: return
        val dir = context.getDir("games", Context.MODE_PRIVATE) ?: return
        val file = File(dir, code)
        val version = tasksetJson.get("version")?.asInt ?: 0
        val isPreview = rulePacks.isNullOrEmpty()
        val info = TasksetInfo(taskset = tasksetJson, version = version,
                rulePacks = rulePacks, isPreview = isPreview, isDefault = isDefault)
        file.outputStream().write(Gson().toJson(info).toByteArray())
    }

    private fun saveTasksetInfo(code: String, info: TasksetInfo) {
        val context = context.get() ?: return
        val dir = context.getDir("games", Context.MODE_PRIVATE) ?: return
        val file = File(dir, code)
        file.outputStream().write(Gson().toJson(info).toByteArray())
    }

    fun deleteTaskset(code: String) {
        val context = context.get() ?: return
        val dir = context.getDir("games", Context.MODE_PRIVATE) ?: return
        val file = File(dir, code)
        file.delete()
        val edit = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE).edit()
        val pinned = ArrayList(getPinnedTasksetCodes())
        if (code in pinned) {
            pinned.remove(code)
            edit.putString(SettingInfo.PINNED.str, pinned.joinToString(","))
            edit.commit()
        }
        val ordered = ArrayList(getOrderedTasksetCodes())
        ordered.remove(code)
        edit.putString(SettingInfo.ORDER.str, pinned.joinToString(","))
        edit.commit()
    }

    fun gotAnySavedTasksets(): Boolean {
        val context = context.get() ?: return false
        val dir = context.getDir("games", Context.MODE_PRIVATE) ?: return false
        return dir.list()?.isNotEmpty() ?: false
    }

    fun getAllSavedTasksetCodes(): Array<String> {
        val context = context.get() ?: return arrayOf()
        val dir = context.getDir("games", Context.MODE_PRIVATE) ?: return arrayOf()
        return dir.list() ?: arrayOf()
    }

    fun getPinnedTasksetCodes(): List<String> {
        val context = context.get() ?: return arrayListOf()
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val pinned = prefs.getString(SettingInfo.PINNED.str, null) ?: return arrayListOf()
        return pinned.split(",").filter { it.isNotBlank() }
    }

    fun getOrderedTasksetCodes(): List<String> {
        val context = context.get() ?: return arrayListOf()
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val ordered = prefs.getString(SettingInfo.ORDER.str, null) ?: return arrayListOf()
        return ordered.split(",").filter { it.isNotBlank() }
    }

    fun saveOrder(codes: List<String>) {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putString(SettingInfo.ORDER.str, codes.joinToString(","))
        edit.commit()
    }

    fun savePin(code: String) {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        val pinned = getPinnedTasksetCodes()
        edit.putString(SettingInfo.PINNED.str, (pinned + code).joinToString(","))
        edit.commit()
    }

    fun deletePin(code: String) {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        val pinned = getPinnedTasksetCodes()
        edit.putString(SettingInfo.PINNED.str, (pinned - code).joinToString(","))
        edit.commit()
    }

    fun getAllSavedTasksets(): List<TasksetInfo> {
        val context = context.get() ?: return arrayListOf()
        val res = arrayListOf<TasksetInfo>()
        // loaded
        val dir = context.getDir("games", Context.MODE_PRIVATE) ?: return res
        val loadedCodes = dir.list() ?: return res
        val mapOfLoadedInfos = hashMapOf<String, TasksetInfo>()
        for (code in loadedCodes) {
            val file = File(dir, code)
            try {
                val text = file.readText()
                val info = Gson().fromJson(text, TasksetInfo::class.java)
                res.add(info)
                mapOfLoadedInfos[code] = info
            } catch (e: Exception) {
                print(e)
            }
        }
        // default
        val gameFiles = context.assets.list("default_games")
        for (file in gameFiles ?: arrayOf()) {
            val code = file.replace(".json", "")
            if (code in loadedCodes) continue
            val input = context.assets.open("default_games/$file")
            val json = input.readBytes().toString(UTF_8)
            input.close()
            val parsed = Gson().fromJson(json, FullTaskset::class.java)
            val version = parsed.taskset.get("version")?.asInt ?: 0
            if (version > mapOfLoadedInfos[code]?.version ?: -1) {
                val isPreview = parsed.rulePacks.isNullOrEmpty()
                val info = TasksetInfo(
                    taskset = parsed.taskset, rulePacks = parsed.rulePacks,
                    version = version, isPreview = isPreview, isDefault = true
                )
                res.add(info)
                saveTasksetInfo(code, info)
            }
        }
        return res
    }

    fun tryGetFullTaskset(code: String): TasksetInfo? {
        val context = context.get() ?: return null
        val dir = context.getDir("games", Context.MODE_PRIVATE) ?: return null
        val file = File(dir, code)
        return try {
            val info = Gson().fromJson(file.readText(), TasksetInfo::class.java)
            if (info.isPreview) null else info
        } catch (e: Exception) { null }
    }

    fun clearSpecifiedGames(codes: List<String>) {
        val context = context.get() ?: return
        val dir = context.getDir("games", Context.MODE_PRIVATE) ?: return
        for (code in codes) {
            val file = File(dir, code)
            file.delete()
        }
    }

    fun clearAllGames() {
        val context = context.get() ?: return
        val dir = context.getDir("games", Context.MODE_PRIVATE) ?: return
        dir.deleteRecursively()
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE).edit()
        prefs.remove(SettingInfo.ORDER.str)
        prefs.remove(SettingInfo.PINNED.str)
        prefs.commit()
    }
    //endregion
}