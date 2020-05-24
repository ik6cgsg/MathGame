package spbpu.hsamcp.mathgame.statistics

import android.util.Log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

enum class Pages(val value: String) {
    SIGNIN("/api/auth/signin"),
    SIGNUP("/api/auth/signup"),
    EDIT("/api/auth/edit"),
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
    var headers: HashMap<String, String> = hashMapOf(
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
    companion object {
        private var reqQueue = LinkedList<RequestData>()
        private var isConnected = false
        private var isWorking = false
        private lateinit var job: Deferred<Unit>

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

        fun doSyncRequest(requestData: RequestData): ResponseData {
            var response = ResponseData()
            val requestTask = GlobalScope.launch {
                val job = async {
                    asyncRequest(requestData)
                }
                response = job.await()
            }
            while (!requestTask.isCompleted) {
                continue
            }
            return response
        }

        fun signRequest(req: RequestData): String {
            Log.d("signUpRequest", req.toString())
            val res = doSyncRequest(req)
            Log.d("signUpRequestReturnCode", res.returnValue.toString())
            Log.d("signUpRequestResultBody", res.body)
            val json = JSONObject(res.body)
            Log.d("signUpServerToken", json.optString("token", "test_token"))
            return json.optString("token", "test_token")
        }

        fun editRequest(req: RequestData) {
            Log.d("editRequest", req.toString())
            val res = doSyncRequest(req)
        }
    }
}