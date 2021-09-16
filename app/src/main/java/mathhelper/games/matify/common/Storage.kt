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
    AUTH_STATUS("authStatus"),
    TIME_COEFF("timeCoeff"),
    AWARD_COEFF("awardCoeff"),
    UNDO_COEFF("undoCoeff"),
}

enum class BaseInfo(val str: String) {
    DEVICE_ID("deviceId")
}

enum class SettingInfo(val str: String) {
    THEME("theme"),
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

class Storage {
    companion object {
        private const val userInfoFile = "USER_INFO"
        private const val settingFile = "SETTINGS"
        private const val logFile = "LOGS"
        private const val base = "BASE"
        private const val resultsFile = "RESULTS"
        val shared = Storage()
    }

    @SuppressLint("ApplySharedPref")
    fun checkDeviceId(context: Context) {
        val prefs = context.getSharedPreferences(base, Context.MODE_PRIVATE)
        if (!prefs.contains(BaseInfo.DEVICE_ID.str)) {
            val prefEdit = prefs.edit()
            prefEdit.putString(BaseInfo.DEVICE_ID.str, UUID.randomUUID().toString())
            prefEdit.commit()
        }
    }

    fun isUserAuthorized(context: Context): Boolean {
        return context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
            .getBoolean(AuthInfo.AUTHORIZED.str, false)
    }

    @SuppressLint("ApplySharedPref")
    fun invalidateUser(context: Context) {
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, false)
        prefEdit.commit()
    }

    fun authStatus(context: Context): AuthStatus {
        return AuthStatus.value(
            context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
                .getString(AuthInfo.AUTH_STATUS.str, AuthStatus.GUEST.str)!!
        )!!
    }

    fun deviceId(context: Context): String {
        return context.getSharedPreferences(base, Context.MODE_PRIVATE)
            .getString(BaseInfo.DEVICE_ID.str, "")!!
    }

    fun login(context: Context): String {
        return context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
            .getString(AuthInfo.LOGIN.str, "")!!
    }

    fun password(context: Context): String {
        return context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
            .getString(AuthInfo.PASSWORD.str, "")!!
    }

    fun serverToken(context: Context): String {
        return context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
            .getString(AuthInfo.SERVER_TOKEN.str, "")!!
    }

    fun theme(context: Context) : ThemeName {
        val theme = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
            .getString(SettingInfo.THEME.str, "")
        return when (theme) {
            "DARK" -> ThemeName.DARK
            "LIGHT" -> ThemeName.LIGHT
            else -> ThemeName.DARK
        }
    }

    fun themeInt(context: Context) : Int {
        return theme(context).resId
    }

    @SuppressLint("ApplySharedPref")
    fun initWithUuid(context: Context): UUID {
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        val uuid = UUID.randomUUID()
        prefEdit.putString(AuthInfo.UUID.str, uuid.toString())
        prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, true)
        prefEdit.commit()
        return uuid
    }

    @SuppressLint("ApplySharedPref")
    fun initUserInfo(context: Context, info: AuthInfoObjectBase) {
        val uuid = initWithUuid(context)
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
        setUserInfo(context, info)
    }

    @SuppressLint("ApplySharedPref")
    fun setServerToken(context: Context, serverToken: String) {
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.putString(AuthInfo.SERVER_TOKEN.str, serverToken)
        prefEdit.commit()
    }

    @SuppressLint("ApplySharedPref")
    fun setUserInfo(context: Context, info: AuthInfoObjectBase) {
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

    fun setTheme(context: Context, theme: ThemeName?) {
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.putString(SettingInfo.THEME.str, theme.toString())
        prefEdit.commit()
    }

    fun getUserInfoBase(context: Context): AuthInfoObjectBase {
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

    fun getFullUserInfo(context: Context): AuthInfoObjectFull {
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        return AuthInfoObjectFull(
            base = getUserInfoBase(context),
            uuid = prefs.getString(AuthInfo.UUID.str, "")
        )
    }

    @SuppressLint("ApplySharedPref")
    fun resetResults(context: Context) {
        val prefs = context.getSharedPreferences(resultsFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.clear()
        prefEdit.commit()
    }

    @SuppressLint("ApplySharedPref")
    fun clearUserInfo(context: Context) {
        val prefs = context.getSharedPreferences(userInfoFile, Context.MODE_PRIVATE)
        val prefEdit = prefs.edit()
        prefEdit.clear()
        prefEdit.putBoolean(AuthInfo.AUTHORIZED.str, false)
        prefEdit.commit()
    }

    fun saveResult(context: Context, result: String?, gameCode: String, levelCode: String? = null) {
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

    fun loadResult(context: Context, gameCode: String, levelCode: String? = null): String {
        var result = ""
        var key = gameCode
        if (levelCode != null) {
            key += "_$levelCode"
        }
        val prefs = context.getSharedPreferences(resultsFile, Context.MODE_PRIVATE)
        result = prefs.getString(key, result) ?: result
        return result
    }

    fun saveResultFromServer(context: Context, userStat: String) {
        if (userStat.isNotEmpty()) {
            val stat = Gson().fromJson(userStat, UserStatForm::class.java)
            for (tsStat in stat.tasksetStatistics) {
                val gameRes = GameResult(tsStat.passedCount, tsStat.pausedCount)
                saveResult(context, gameRes.saveString(), tsStat.code)
                for (tStat in tsStat.tasksStat) {
                    val levelRes =
                        LevelResult(tStat.steps, tStat.time / 1000, StateType.valueOf(tStat.state), tStat.expression ?: "")
                    saveResult(context, levelRes.saveString(), tsStat.code, tStat.code)
                }
            }
        }
    }

    fun saveIfNeed(context: Context, file: String, data: String) {
        val oldFile = File("${context.filesDir.path}/${context.packageName}/shared_prefs/$file")
        if (!oldFile.exists()) {
            val prefs = context.getSharedPreferences(file, Context.MODE_PRIVATE)
            val prefEdit = prefs.edit()
            prefEdit.putString("data", data)
            prefEdit.commit()
        }
    }

    fun haveAnyFileStartWith(context: Context, name: String): Boolean {
        val file = File("${context.filesDir.path}/${context.packageName}/shared_prefs/$name*")
        return file.exists()
    }

    fun saveTaskset(context: Context, code: String, tasksetJson: String, rulePacks: String? = null) {
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

    fun gotAnySavedTasksets(context: Context): Boolean {
        val settings = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val preloaded = settings.getStringSet(SettingInfo.PRELOADED_GAMES.str, setOf())
        val loaded = settings.getStringSet(SettingInfo.LOADED_GAMES.str, setOf())
        return !(preloaded.isNullOrEmpty() && loaded.isNullOrEmpty())
    }

    fun getAllSavedTasksetCodes(context: Context): Set<String> {
        val settings = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        val allCodes = settings.getStringSet(SettingInfo.PRELOADED_GAMES.str, setOf())!!
        allCodes += settings.getStringSet(SettingInfo.LOADED_GAMES.str, setOf())!!
        return allCodes
    }

    fun getAllSavedTasksets(context: Context): List<String> {
        val allTasksets = arrayListOf<String>()
        val allCodes = getAllSavedTasksetCodes(context)
        for (code in allCodes) {
            val tasksetFile = context.getSharedPreferences(code, Context.MODE_PRIVATE)
            val json = tasksetFile.getString("taskset", null)
            if (!json.isNullOrEmpty()) {
                allTasksets.add(json)
            }
        }
        return allTasksets
    }

    fun tryGetFullTaskset(context: Context, code: String): FullTaskset? {
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

    fun clearSettings(context: Context) {
        val prefs = context.getSharedPreferences(settingFile, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    fun clearSpecifiedGames(context: Context, codes: List<String>) {
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
}