package ch.rethinc.reply

import android.app.Activity
import android.os.Bundle
import ch.rethinc.reply.android.Reply
import ch.rethinc.reply.channel.jira.JiraChannel
import ch.rethinc.reply.channel.jira.JiraConfiguration

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        Reply(this.application, JiraChannel.create(
                JiraConfiguration(
                        "",
                        "",
                        "",
                        "",
                        ""))
        )
    }
}
