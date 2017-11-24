package ch.rethinc.reply.channel.jira

import ch.rethinc.reply.core.Feedback
import ch.rethinc.reply.core.ReplyChannel
import ch.rethinc.reply.core.ReplyChannelException
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException


class JiraChannel(private val configuration: JiraConfiguration, private val client: OkHttpClient) : ReplyChannel {

    companion object {
        fun create(configuration: JiraConfiguration): ReplyChannel {
            return JiraChannel(configuration, OkHttpClient())
        }
    }

    private val gson = Gson()

    private fun requestBuilder(): JiraRequestBuilder {
        return JiraRequestBuilder(configuration.hostUrl, configuration.username, configuration.password)
    }

    override fun sendFeedback(feedback: Feedback, onSuccess: () -> Unit, onError: (e: Exception) -> Unit) {
        val body = CreateIssueRequest(IssueFields("Feedback", "dummy message", Key(configuration.projectKey), Id(configuration.issueTypeId)))
        val request = requestBuilder()
                .path("issue")
                .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), gson.toJson(body)))
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call?, response: Response) {
                when {
                    response.code() == 400 -> {
                        val error = gson.fromJson(response.body()!!.string(), JiraError::class.java)
                        var messsage = ""
                        if (error.errors.isssuetype != null) {
                            messsage = "issue type does not exist, check your configuration"
                        } else if (error.errors.project != null) {
                            messsage = "project does not exist, check your configuration"
                        }
                        onError(ReplyChannelException(messsage))
                    }
                    response.code() in 500..599 -> onError(ReplyChannelException("Server Error (${response.code()})"))
                    else -> {
                        val issue = gson.fromJson(response.body()!!.string(), Key::class.java)
                        addAttachment(issue, feedback.screenshot, onSuccess, onError)
                    }
                }
            }
        })
    }

    private fun addAttachment(issue: Key, screenshot: ByteArray, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data;name=\"file\";filename=\"Screenshot.png\""),
                        RequestBody.create(MediaType.parse("image/png"), screenshot)
                )
                .build()
        val request = requestBuilder()
                .path("issue/${issue.key}/attachments")
                .header("X-Atlassian-Token", "no-check")
                .post(body)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                onError(ReplyChannelException("Network Error. Note: Issue with key '${issue.key}' may be created in your JIRA instance"))
            }

            override fun onResponse(call: Call?, response: Response) {
                if (response.code() in 400..599) {
                    onError(ReplyChannelException("Server Error (${response.code()}). Note: Issue with key '${issue.key}' may be created in your JIRA instance"))
                } else {
                    onSuccess()
                }
            }
        })
    }
}