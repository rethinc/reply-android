package ch.rethinc.reply.android

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import ch.rethinc.reply.android.feedback.FeedbackActivity
import ch.rethinc.reply.core.ReplyChannel

class Reply(application: Application, channel: ReplyChannel) {

    private val screenshotRepository =  ScreenshotRepository()
    private var currentActivity: Activity? = null
    private val screenshotDetector = ScreenshotDetector()

    init {
        application.registerActivityLifecycleCallbacks(LifecycleCallbacks(this))
        screenshotDetector.startDetecting {
            screenshotRepository.save(it)
            startFeedbackCycle()
        }
    }

    private fun startFeedbackCycle() {
        val activity = currentActivity.let { it } ?: return
        val feedbackIntent = Intent(currentActivity, FeedbackActivity::class.java)
        activity.startActivity(feedbackIntent)
    }

    class LifecycleCallbacks(private val reply: Reply) : Application.ActivityLifecycleCallbacks {

        override fun onActivityPaused(p0: Activity?) {
            reply.currentActivity = null
        }

        override fun onActivityResumed(activity: Activity?) {
            reply.currentActivity = activity
        }

        override fun onActivityStarted(p0: Activity?) {}
        override fun onActivityDestroyed(p0: Activity?) {}
        override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {}
        override fun onActivityStopped(p0: Activity?) {}
        override fun onActivityCreated(p0: Activity?, p1: Bundle?) {}

    }
}