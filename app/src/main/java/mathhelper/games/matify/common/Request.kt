package mathhelper.games.matify.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mathhelper.games.matify.GlobalScene
import mathhelper.games.matify.R
import mathhelper.games.matify.common.Constants
import mathhelper.games.matify.common.Logger
import org.json.JSONObject
import mathhelper.games.matify.common.RequestTimer
import mathhelper.games.matify.common.Storage
import java.lang.ref.WeakReference
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

enum class RequestPage(val value: String) {
    SIGNIN("/api/auth/signin"),
    SIGNUP("/api/auth/signup"),
    EDIT("/api/auth/edit"),
    GOOGLE_SIGN_IN("/api/auth/google_sign_in"),
    ACTIVITY_LOG("/api/log/activity/create"),
    USER_HISTORY("/api/log/user_statistics"),
    TASKSETS_PREVIEW("/api/taskset?form=link"),
    TASKSETS_FULL("/api/taskset/play/${Constants.appCode}/endless")
}

enum class RequestMethod {
    POST, GET, DELETE
}

data class RequestData(
    val page: RequestPage,
    val securityToken: String = "",
    var method: RequestMethod = RequestMethod.POST,
    var url: String = "https://mathhelper.space:8089${page.value}",
    var body: String = "",
    var headers: Map<String, String> = mapOf(
        "Content-type" to "application/json; charset=UTF-8",
        "Authorization" to ("Bearer " + securityToken)
        //"Bearer" to securityToken
    )
)

data class ResponseData(
    var returnValue: Int = 404,
    var body: String = "",
    var headers: HashMap<String, String> = hashMapOf()
)

class MathHelperSpaceHostnameVerifier : HostnameVerifier {
    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        return true
    }
}

class TrustAllCertsManager : X509TrustManager {
    override fun getAcceptedIssuers (): Array<X509Certificate?>? = null

    override fun checkClientTrusted(
        certs: Array<X509Certificate?>?,
        authType: String?
    ) {
    }

    override fun checkServerTrusted(
        certs: Array<X509Certificate?>?,
        authType: String?
    ) {
    }
}

class Request {
    class TimeoutException(message: String): Exception(message)
    class UndefinedException(message: String): Exception(message)
    class TokenNotFoundException(message: String): Exception(message)
    class UserMessageException(message: String): Exception(message)

    companion object {
        private var reqQueue = LinkedList<RequestData>()
        private var isWorking = false
        private lateinit var job: Deferred<Unit>
        private const val timeoutMaxInSec = 7 // TODO: move to UI interface
        private var timer = RequestTimer(timeoutMaxInSec.toLong())
        var timeout = false
        private var isConnected = true
            set(value) {
                if (field != value) {
                    when(field) {
                        false -> reqQueue = Storage.shared.getLogRequests()
                        true -> Storage.shared.saveLogRequests(reqQueue)
                    }
                    field = value
                }
            }

        lateinit var context: WeakReference<Context>

        fun startWorkCycle() {
            if (isWorking) {
                return
            }
            // Install the all-trusting trust manager
            val sc: SSLContext = SSLContext.getInstance("SSL")
            sc.init(null, arrayOf<TrustManager> (TrustAllCertsManager()), SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)

            HttpsURLConnection.setDefaultHostnameVerifier(MathHelperSpaceHostnameVerifier())

            GlobalScope.launch {
                job = async {
                    while (true) {
                        while (reqQueue.isNotEmpty() && isConnected) {
                            try {
                                Logger.d("asyncRequest", reqQueue.last.toString())
                                val response = asyncRequest(reqQueue.last)
                                Logger.d("Request", "sended")
                                if (response.returnValue != 500 || response.returnValue != 404) {
                                    Logger.d("asyncRequestReturnCode", response.returnValue.toString())
                                    Logger.d("asyncRequestResultBody", response.body)
                                    reqQueue.removeLast()
                                    Logger.d("Request", "removed")
                                }
                            } catch (e: Exception) {
                                Logger.e("Request", e.message ?: "Error while request queue handling")
                            }
                        }
                    }
                }
            }
            isWorking = true
        }

        fun stopWorkCycle() {
            if (isWorking) {
                job.cancel()
                isWorking = false
            }
        }

        private fun asyncRequest(requestData: RequestData): ResponseData {
            val response = ResponseData()
            try {
                val url = URL(requestData.url)
                with(url.openConnection() as HttpsURLConnection)
                {
                    // Setting request
                    requestMethod = requestData.method.name
                    requestData.headers.map {
                        setRequestProperty(it.key, it.value)
                    }
                    if (requestMethod != RequestMethod.GET.name) {
                        outputStream.write(requestData.body.toByteArray())
                    }
                    // Getting response
                    headerFields.map {
                        response.headers.put(it.key, it.value[0])
                    }
                    response.returnValue = responseCode
                    if (responseCode != HttpsURLConnection.HTTP_NO_CONTENT) {
                        val inStream = if (responseCode != HttpsURLConnection.HTTP_OK) {
                            errorStream
                        } else {
                            inputStream
                        }
                        if (inStream != null) {
                            response.body = inStream.bufferedReader().readText()
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("Request", e.message ?: "Error while async request")
            }
            return response
        }

        @Throws(TimeoutException::class)
        fun doSyncRequest(requestData: RequestData): ResponseData {
            Logger.d("Request", requestData.toString())
            var response = ResponseData()
            timer.start()
            val requestTask = GlobalScope.launch {
                val job = async {
                    asyncRequest(requestData)
                }
                response = job.await()
            }
            while (!timeout && !requestTask.isCompleted) {
                Thread.sleep(1000)
            }
            if (timeout) {
                throw TimeoutException("More than $timeoutMaxInSec passed...")
            }
            timer.cancel()
            Logger.d("Request", response.toString())
            return response
        }

        //region Request patterns

        fun sendStatisticRequest(req: RequestData) {
            if (req.securityToken.isNullOrBlank()){
                Logger.d("sendStatisticRequest", "No securityToken found")
                return  //TODO: add in queue and try to take token until it will not be obtained because the user become authorized
            }
            Logger.d("Request", "Request body: ${req.body}")
            isConnected = ConnectionChecker.shared.isConnected
            if (isConnected) {
                reqQueue.addFirst(req)
            } else {
                Storage.shared.saveOneLogRequest(req)
            }
        }

        @Throws(UndefinedException::class, TokenNotFoundException::class)
        fun signRequest(req: RequestData): JSONObject {
            Logger.d("signRequest", req.toString())
            val res = doSyncRequest(req)
            Logger.d("signRequestReturnCode", res.returnValue.toString())
            Logger.d("signRequestResultBody", res.body)
            if (res.returnValue != 200) {
                if (res.returnValue in 400..401) {
                    throw TokenNotFoundException("Bad Credentials")
                } else {
                    throw UndefinedException("Something went wrong... (returnCode != 200)")
                }
            }
            val response = JSONObject(res.body)
            if (!response.has("token")) {
                throw TokenNotFoundException("Can't extract token from response")
            }
            Logger.d("signServerToken", response.getString("token"))
            return response
        }

        fun editRequest(req: RequestData) {
            if (req.securityToken.isNullOrBlank()){
                Logger.d("editRequest", "No securityToken found")
                throw TokenNotFoundException("Bad Credentials")
            }
            Logger.d("editRequest", req.toString())
            val res = doSyncRequest(req)
            if (res.returnValue != 200) {
                throw UndefinedException("Something went wrong... (returnCode != 200)")
            }
        }

        fun historyRequest(req: RequestData): String {
            Logger.d("historyRequest", req.toString())
            if (req.securityToken.isNullOrBlank()){
                Logger.d("historyRequest", "No securityToken found")
                throw TokenNotFoundException("Bad Credentials")
            }
            req.url += "?app=${Constants.appCode}"
            val res = doSyncRequest(req)
            if (res.returnValue != 200 && res.returnValue != 404) {
                throw UserMessageException(GlobalScene.shared.gamesActivity!!.getString(R.string.pers_stat_load_fail))
            }
            return if (res.returnValue == 404) "" else res.body
        }

        fun resetHistory(req: RequestData) {
            Logger.d("historyRequest", req.toString())
            if (req.securityToken.isNullOrBlank()){
                Logger.d("historyRequest", "No securityToken found")
                throw TokenNotFoundException("Bad Credentials")
            }
            req.url += "?app=${Constants.appCode}"
            val res = doSyncRequest(req)
            if (res.returnValue != 200) {
                throw UserMessageException(GlobalScene.shared.gamesActivity!!.getString(R.string.pers_stat_reset_fail))
            }
        }

        //endregion
    }
}