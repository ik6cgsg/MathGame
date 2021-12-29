package mathhelper.games.matify.parser

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import mathhelper.games.matify.common.Logger
import java.lang.Exception
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Required

class GsonParser {
    companion object {
        inline fun <reified T: Any> parse(json: String): T? {
            var res: T? = null
            try {
                val jsonObj = Gson().fromJson(json, JsonObject::class.java)
                res = parse(jsonObj)
            } catch (e: Exception) {
                Logger.e("[GsonParser]", "ERROR: Not valid json!! for class ${T::class.java}")
                Logger.e("[GsonParser]", e.message ?: "unknown")
            }
            return res
        }

        inline fun <reified T: Any> parse(json: JsonObject): T? {
            var res: T? = null
            try {
                val req = T::class.declaredMemberProperties.filter { it.hasAnnotation<Required>() }
                val required = req.map { it.name }
                for (reqField in required) {
                    if (json.get(reqField) == null) {
                        Logger.e("[GsonParser]", "ERROR: No required field for class ${T::class.java}: '$reqField'")
                        return null
                    }
                }
                res = Gson().fromJson(json, T::class.java)
            } catch (e: Exception) {
                Logger.e("[GsonParser]", "ERROR: Not valid json!! for class ${T::class.java}")
                Logger.e("[GsonParser]", e.message ?: "unknown")
            }
            return res
        }
    }
}