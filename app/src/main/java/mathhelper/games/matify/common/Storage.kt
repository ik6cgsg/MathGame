package mathhelper.games.matify.common

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import mathhelper.games.matify.AuthStatus
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.game.FullTaskset
import mathhelper.games.matify.game.GameResult
import mathhelper.games.matify.level.LevelResult
import mathhelper.games.matify.level.StateType
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.absoluteValue

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
    PRELOADED_GAMES("preloadedGames"),
    LOADED_GAMES("loadedGames")
}

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

    fun clearSettings() {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    //endregion

    //region TASKSETS

    fun saveTaskset(code: String, tasksetJson: String, rulePacks: String? = null) {
        val context = context.get() ?: return
        val prefs = context.getSharedPreferences(code, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        //if (!prefs.contains("taskset") || prefs.getString("taskset", "") != tasksetJson) {
            prefEdit.putString("taskset", tasksetJson)
        //}
        val settings = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val settingsEdit = settings.edit()
        if (!prefs.contains("rulePacks") && !rulePacks.isNullOrEmpty()) {
            prefEdit.putString("rulePacks", rulePacks)
            val preloaded = HashSet(settings.getStringSet(SettingInfo.PRELOADED_GAMES.str, setOf())!!)
            val loaded = HashSet(settings.getStringSet(SettingInfo.LOADED_GAMES.str, setOf())!!)
            preloaded.remove(code)
            loaded.add(code)
            settingsEdit.putStringSet(SettingInfo.PRELOADED_GAMES.str, preloaded)
            settingsEdit.putStringSet(SettingInfo.LOADED_GAMES.str, loaded)
        } else {
            val preloaded = HashSet(settings.getStringSet(SettingInfo.PRELOADED_GAMES.str, setOf())!!)
            val loaded = HashSet(settings.getStringSet(SettingInfo.LOADED_GAMES.str, setOf())!!)
            loaded.remove(code)
            preloaded.add(code)
            settingsEdit.putStringSet(SettingInfo.PRELOADED_GAMES.str, preloaded)
            settingsEdit.putStringSet(SettingInfo.LOADED_GAMES.str, loaded)
        }
        settingsEdit.commit()
        prefEdit.commit()
    }

    fun gotAnySavedTasksets(): Boolean {
        val context = context.get() ?: return false
        val settings = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val preloaded = settings.getStringSet(SettingInfo.PRELOADED_GAMES.str, setOf())
        val loaded = settings.getStringSet(SettingInfo.LOADED_GAMES.str, setOf())
        return !(preloaded.isNullOrEmpty() && loaded.isNullOrEmpty())
    }

    fun getAllSavedTasksetCodes(): Set<String> {
        val context = context.get() ?: return setOf()
        val settings = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val allCodes = settings.getStringSet(SettingInfo.PRELOADED_GAMES.str, setOf())!!
        allCodes += settings.getStringSet(SettingInfo.LOADED_GAMES.str, setOf())!!
        return allCodes
    }

    fun getAllSavedTasksets(): List<String> {
        val context = context.get() ?: return listOf()
        val allTasksets = arrayListOf<String>()
        val allCodes = getAllSavedTasksetCodes()
        for (code in allCodes) {
            val tasksetFile = context.getSharedPreferences(code, Context.MODE_PRIVATE)
            val json = tasksetFile.getString("taskset", null)
            if (!json.isNullOrEmpty()) {
                allTasksets.add(json)
            }
        }
        return allTasksets
    }

    fun tryGetFullTaskset(code: String): FullTaskset? {
        val context = context.get() ?: return null
        var res: FullTaskset? = null
        val prefs = context.getSharedPreferences(code, Context.MODE_PRIVATE)
        if (prefs.contains("taskset") && prefs.contains("rulePacks")) {
            res = FullTaskset(
                taskset = JsonParser.parseString(prefs.getString("taskset", "")).asJsonObject,
                rulePacks = Gson().fromJson(prefs.getString("rulePacks", ""), Array<JsonObject>::class.java).toList()
            )
        }
        return res
    }

    fun clearSpecifiedGames(codes: List<String>) {
        val context = context.get() ?: return
        if (codes.isNotEmpty()) {
            val settings = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
            val settingsEdit = settings.edit()
            val preloaded = HashSet(settings.getStringSet(SettingInfo.PRELOADED_GAMES.str, setOf())!!)
            val loaded = HashSet(settings.getStringSet(SettingInfo.LOADED_GAMES.str, setOf())!!)
            codes.forEach {
                loaded.remove(it)
                preloaded.remove(it)
                val prefs = context.getSharedPreferences(it, Context.MODE_PRIVATE)
                prefs.edit().clear().commit()
            }
            settingsEdit.putStringSet(SettingInfo.PRELOADED_GAMES.str, preloaded)
            settingsEdit.putStringSet(SettingInfo.LOADED_GAMES.str, loaded)
            settingsEdit.commit()
        }
    }

    //endregion
}