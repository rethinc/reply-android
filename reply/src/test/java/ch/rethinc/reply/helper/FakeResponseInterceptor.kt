package ch.rethinc.reply.helper

import com.google.gson.Gson
import okhttp3.*
import okio.Buffer
import java.io.IOException

class FakeResponseInterceptor : Interceptor {
    var requests = mutableListOf<Request>()
    private val queuedResponses = mutableListOf<Any>()

    inner class NetworkErrorResponse()

    override fun intercept(chain: Interceptor.Chain?): Response {
        var request = chain!!.request()
        val contentType = request.body()?.contentType().toString()
        if (contentType != null) {
            request = request.newBuilder().header("Content-Type", contentType).build()
        }
        requests.add(request)

        if (queuedResponses[requests.size - 1] is NetworkErrorResponse) {
            throw IOException("network exception")
        }
        val responseBuilder = queuedResponses[requests.size - 1] as Response.Builder
        return responseBuilder.request(request).build()
    }

    fun body(index: Int): String {
        val buffer = Buffer()
        this.requests[index].body()!!.writeTo(buffer)
        return buffer.readUtf8()
    }

    fun enqueueJsonResponse(body: Any, statusCode: Int) {
        queuedResponses.add(Response.Builder()
                .code(statusCode)
                .body(ResponseBody.create(MediaType.parse("application/json"), Gson().toJson(body)))
                .protocol(Protocol.HTTP_1_1).message("OK"))
    }

    fun enqueueResponse(body: String, statusCode: Int) {
        queuedResponses.add(Response.Builder()
                .code(statusCode)
                .body(ResponseBody.create(MediaType.parse("text/html"), body))
                .protocol(Protocol.HTTP_1_1).message("OK"))
    }

    fun enqueNetworkError() {
        queuedResponses.add(NetworkErrorResponse())
    }
}