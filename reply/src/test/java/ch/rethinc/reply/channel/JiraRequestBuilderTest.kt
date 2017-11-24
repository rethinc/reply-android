package ch.rethinc.reply.channel

import ch.rethinc.reply.channel.jira.JiraRequestBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.hamcrest.CoreMatchers.`is` as toBe

class JiraRequestBuilderTest {

    @Test
    fun build_shouldAddAuthorizationHeader() {
        val sut = JiraRequestBuilder("https://host.net", "username", "password")

        val request = sut.url("https://anyurl.com").build()

        assertThat(request.header("Authorization"), toBe("Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
    }

    @Test
    fun path_shouldAppendPathToUrl() {
        val sut = JiraRequestBuilder("https://host.net/", "username", "password")

        val request = sut.path("service/path").build()

        assertThat(request.url().toString(), toBe("https://host.net/rest/api/2/service/path"))
    }

    @Test
    fun path_shouldAddMissingSlashes() {
        val sut = JiraRequestBuilder("https://host.net", "username", "password")

        val request = sut.path("service/path").build()

        assertThat(request.url().toString(), toBe("https://host.net/rest/api/2/service/path"))
    }
}
