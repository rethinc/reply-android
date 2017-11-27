package ch.rethinc.reply.channel

import ch.rethinc.reply.channel.jira.*
import ch.rethinc.reply.helper.FakeResponseInterceptor
import ch.rethinc.reply.core.Feedback
import ch.rethinc.reply.core.ReplyChannelException
import com.google.gson.Gson
import okhttp3.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.hamcrest.Matchers.`is` as toBe


class JiraChannelTest {

    lateinit var interceptor: FakeResponseInterceptor
    lateinit var sut: JiraChannel
    val configuration = JiraConfiguration("https://host.com", "username", "password", "projectKey", "issueTypeId")
    val lock = CountDownLatch(1)

    @Nested
    inner class SendFeedbackSuccessful {

        @BeforeEach
        fun setup() {
            interceptor = FakeResponseInterceptor()
            interceptor.enqueueJsonResponse(Key("IssueKey"), 201)
            interceptor.enqueueJsonResponse(Id("attachmentId"), 201)
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
            sut = JiraChannel(configuration, client)
        }

        @Test
        fun sendFeedback_shouldPostToIssuePath() {
            sut.sendFeedback(Feedback(ByteArray(42)), { lock.countDown() }, { lock.countDown() })

            lock.await(2000, TimeUnit.MILLISECONDS)
            val request = interceptor.requests[0]
            assertThat(request.url().encodedPath(), toBe("/rest/api/2/iss"))
            assertThat(request.method(), toBe("POST"))
        }

        @Test
        fun sendFeedback_shouldAddConentTypeHeader() {
            sut.sendFeedback(Feedback(ByteArray(42)), { lock.countDown() }, { lock.countDown() })

            lock.await(2000, TimeUnit.MILLISECONDS)
            val request = interceptor.requests[0]
            assertThat(request.header("Content-Type"), toBe("application/json;charset=utf-8"))
        }

        @Test
        fun sendFeedback_shouldAddIssuePayload() {
            sut.sendFeedback(Feedback(ByteArray(42)), { lock.countDown() }, { lock.countDown() })

            lock.await(2000, TimeUnit.MILLISECONDS)
            val requestBody = Gson().fromJson(interceptor.body(0), CreateIssueRequest::class.java)
            assertThat(requestBody.fields.summary, toBe("Feedback"))
            assertThat(requestBody.fields.description, toBe("dummy message"))
            assertThat(requestBody.fields.project.key, toBe("projectKey"))
            assertThat(requestBody.fields.issuetype.id, toBe("issueTypeId"))
        }

        @Test
        fun sendFeedback_shouldAddAttachementAfterIssueIsCreated() {
            sut.sendFeedback(Feedback(ByteArray(42)), { lock.countDown() }, { lock.countDown() })

            lock.await(2000, TimeUnit.MILLISECONDS)
            val request = interceptor.requests[1]
            assertThat(interceptor.body(1), not(toBe("")))
            assertThat(request.url().encodedPath(), toBe("/rest/api/2/issue/IssueKey/attachments"))
            assertThat(request.header("Content-Type"), startsWith("multipart/form-data"))
            assertThat(request.header("X-Atlassian-Token"), startsWith("no-check"))

            val filePart = request.body()!! as MultipartBody
            assertThat(filePart.part(0).headers()!!["Content-Disposition"], toBe("form-data;name=\"file\";filename=\"Screenshot.png\""))
        }

        @Test
        fun sendFeedback_shouldInvokeOnSuccess() {
            var invoked = false
            sut.sendFeedback(Feedback(ByteArray(42)), {
                invoked = true
                lock.countDown()
            }) { lock.countDown() }

            lock.await(2000, TimeUnit.MILLISECONDS)
            assertThat(invoked, toBe(true))
        }
    }

    @Nested
    inner class CreateIssueError {

        @BeforeEach
        fun setup() {
            interceptor = FakeResponseInterceptor()
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
            sut = JiraChannel(configuration, client)
        }

        @Test
        fun sendFeedback_shouldInvokeOnError() {
            interceptor.enqueNetworkError()

            var invoked = false
            sut.sendFeedback(Feedback(ByteArray(42)), {}, {
                invoked = true
                lock.countDown()
            })

            lock.await(2000, TimeUnit.MILLISECONDS)
            assertThat(invoked, toBe(true))
        }

        @Test
        fun sendFeedback_invalidIssueType_shouldCreateErrorWithMessage() {
            interceptor.enqueueJsonResponse(JiraError(CreateIssueErrors("valid issue type is required")), 400)

            var exception: Exception? = null
            sut.sendFeedback(Feedback(ByteArray(42)), {}, { e ->
                exception = e
                lock.countDown()
            })

            waitForCallback()
            assertThat(exception is ReplyChannelException, toBe(true))
            assertThat(exception!!.message, toBe("issue type does not exist, check your configuration"))
        }

        @Test
        fun sendFeedback_invalidProjectType_shouldCreateErrorWithMessage() {
            interceptor.enqueueJsonResponse(JiraError(CreateIssueErrors(project = "project is required")), 400)

            var exception: Exception? = null
            sut.sendFeedback(Feedback(ByteArray(42)), {}, { e ->
                exception = e
                lock.countDown()
            })

            waitForCallback()
            assertThat(exception is ReplyChannelException, toBe(true))
            assertThat(exception!!.message, toBe("project does not exist, check your configuration"))
        }

        @ParameterizedTest(name = "Server error with status code \"{0}\"")
        @ValueSource(ints = intArrayOf(500, 501, 502, 503))
        fun sendFeedback_ServerError_shouldCreateErrorWithMessage(statusCode: Int) {
            interceptor.enqueueResponse("Server Error", statusCode)

            var exception: Exception? = null
            sut.sendFeedback(Feedback(ByteArray(42)), {}, { e ->
                exception = e
                lock.countDown()
            })

            waitForCallback()
            assertThat(exception is ReplyChannelException, toBe(true))
            assertThat(exception!!.message, toBe("Server Error (${statusCode})"))
        }
    }

    @Nested
    inner class AddAttachmentError {

        @BeforeEach
        fun setup() {
            interceptor = FakeResponseInterceptor()
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
            sut = JiraChannel(configuration, client)
            interceptor.enqueueJsonResponse(Key("IssueKey"), 201)
        }

        @Test
        fun sendFeedback_shouldInvokeOnErrorWithMessage() {
            interceptor.enqueNetworkError()

            var exception: Exception? = null
            sut.sendFeedback(Feedback(ByteArray(42)), {}, { e ->
                exception = e
                lock.countDown()
            })

            waitForCallback()
            assertThat(exception is ReplyChannelException, toBe(true))
            assertThat(exception!!.message, toBe("Network Error. Note: Issue with key 'IssueKey' may be created in your JIRA instance"))
        }

        @ParameterizedTest(name = "Server error with status code \"{0}\"")
        @ValueSource(ints = intArrayOf(400, 401, 402, 500, 501, 502, 503))
        fun sendFeedback_ServerError_shouldCreateErrorWithMessage(statusCode: Int) {
            interceptor.enqueueResponse("Server Error", statusCode)

            var exception: Exception? = null
            sut.sendFeedback(Feedback(ByteArray(42)), {}, { e ->
                exception = e
                lock.countDown()
            })

            waitForCallback()
            assertThat(exception is ReplyChannelException, toBe(true))
            assertThat(exception!!.message, toBe("Server Error (${statusCode}). Note: Issue with key 'IssueKey' may be created in your JIRA instance"))
        }
    }

    private fun waitForCallback() {
        lock.await(2000, TimeUnit.MILLISECONDS)
    }
}