package ch.rethinc.reply.channel.jira

data class CreateIssueRequest(val fields: IssueFields)

data class IssueFields(val summary: String, val description: String, val project: Key, val issuetype: Id)

data class Key(val key: String)

data class Id(val id: String)

data class JiraError(val errors: CreateIssueErrors)

data class CreateIssueErrors(val isssuetype: String? = null, val project: String? = null)