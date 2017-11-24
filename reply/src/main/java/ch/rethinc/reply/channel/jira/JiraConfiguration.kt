package ch.rethinc.reply.channel.jira

data class JiraConfiguration(val hostUrl: String,
                             val username: String,
                             val password: String,
                             val projectKey: String,
                             val issueTypeId: String
)