package ch.rethinc.reply.channel.jira

import okhttp3.Credentials
import okhttp3.Request


class JiraRequestBuilder(val hostUrl: String, val username: String, val password: String) : Request.Builder() {

    override fun build(): Request {
        header("Authorization", Credentials.basic(username, password))
        return super.build()
    }

    fun path(path: String): JiraRequestBuilder {
        url(hostUrl + (if (hostUrl.last() == '/') "" else "/") + "rest/api/2/" + path)
        return this
    }
}