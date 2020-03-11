package spbpu.hsamcp.mathgame.statistics

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

enum class RequestMethod(val value: String) {
    POST("POST"),
    GET("GET")
}

data class RequestData(
    var method: RequestMethod = RequestMethod.POST,
    var url: String = "https://mathhelper.space:8443/math_game_log",
    var body: String = "",
    var headers: HashMap<String, String> = hashMapOf()
)

data class ResponseData(
    var returnValue: Int = 404,
    var body: String = "",
    var headers: HashMap<String, String> = hashMapOf()
)

class Request {
    companion object {
        private fun asyncRequest(requestData: RequestData): ResponseData {
            val response = ResponseData()
            try {
                val url = URL(requestData.url)
                with(url.openConnection() as HttpURLConnection)
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
                    if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                        val inStream = if (responseCode != HttpURLConnection.HTTP_OK) {
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
                Log.e("Request", e.message)
            }
            return response
        }

        fun doRequest(requestData: RequestData): ResponseData {
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
    }
}