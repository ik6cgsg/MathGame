package mathhelper.games.matify.statistics

import android.util.Log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import mathhelper.games.matify.common.RequestTimer
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

enum class Pages(val value: String) {
    SIGNIN("/api/auth/signin"),
    SIGNUP("/api/auth/signup"),
    EDIT("/api/auth/edit"),
    GOOGLE_SIGN_IN("/api/auth/google_sing_in"),
    ACTIVITY_LOG("/api/activity_log/create")
}

enum class RequestMethod(val value: String) {
    POST("POST"),
    GET("GET")
}

data class RequestData(
    val page: String,
    val securityToken: String = "",
    var method: RequestMethod = RequestMethod.POST,
    var url: String = "https://mathhelper.space:8443" + page,
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

    companion object {
        private var reqQueue = LinkedList<RequestData>()
        private var isConnected = false
        private var isWorking = false
        private lateinit var job: Deferred<Unit>
        private const val timeoutMaxInSec = 5
        private var timer = RequestTimer(timeoutMaxInSec.toLong())
        var timeout = false

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
                                Log.d("asyncRequest", reqQueue.last.toString())
                                val response = asyncRequest(reqQueue.last)
                                Log.d("Request", "sended")
                                if (response.returnValue != 500 || response.returnValue != 404) {
                                    Log.d("asyncRequestReturnCode", response.returnValue.toString())
                                    Log.d("asyncRequestResultBody", response.body)
                                    reqQueue.removeLast()
                                    Log.d("Request", "removed")
                                }
                            } catch (e: Exception) {
                                Log.e("Request", e.message ?: "Error while request queue handling")
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
                    requestMethod = requestData.method.value
                    requestData.headers.map {
                        setRequestProperty(it.key, it.value)
                    }
                    if (requestMethod != RequestMethod.GET.value) {
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
                Log.e("Request", e.message ?: "Error while async request")
            }
            return response
        }

        fun sendRequest(requestData: RequestData) {
            Log.d("Request", "Request body: ${requestData.body}")
            reqQueue.addFirst(requestData)
            isConnected = true
        }

        @Throws(TimeoutException::class)
        private fun doSyncRequest(requestData: RequestData): ResponseData {
            var response = ResponseData()
            timer.start()
            val requestTask = GlobalScope.launch {
                val job = async {
                    asyncRequest(requestData)
                }
                response = job.await()
            }
            while (!timeout && !requestTask.isCompleted) {
                continue
            }
            if (timeout) {
                throw TimeoutException("More than $timeoutMaxInSec passed...")
            }
            timer.cancel()
            return response
        }

        @Throws(UndefinedException::class, TokenNotFoundException::class)
        fun signRequest(req: RequestData): String {
            Log.d("signRequest", req.toString())
            val res = doSyncRequest(req)
            Log.d("signRequestReturnCode", res.returnValue.toString())
            Log.d("signRequestResultBody", res.body)
            if (res.returnValue != 200) {
                throw UndefinedException("Something went wrong... (returnCode != 200)")
            }
            val json = JSONObject(res.body)
            if (!json.has("token")) {
                throw TokenNotFoundException("Can't extract token from response")
            }
            Log.d("signServerToken", json.getString("token"))
            return json.getString("token")
        }

        fun editRequest(req: RequestData) {
            Log.d("editRequest", req.toString())
            val res = doSyncRequest(req)
            if (res.returnValue != 200) {
                throw UndefinedException("Something went wrong... (returnCode != 200)")
            }
        }
    }
}