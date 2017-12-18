package ch.rethinc.reply

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import ch.rethinc.reply.android.Reply
import ch.rethinc.reply.channel.jira.JiraChannel
import ch.rethinc.reply.channel.jira.JiraConfiguration
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream


class MainActivity : Activity() {

    lateinit var reply: Reply

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        reply = Reply(this.application, JiraChannel.create(
                JiraConfiguration(
                        "",
                        "",
                        "",
                        "",
                        ""))
        )
    }

    override fun onResume() {
        super.onResume()
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.test_screenshot)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        reply.startFeedback(stream.toByteArray())
    }
}
