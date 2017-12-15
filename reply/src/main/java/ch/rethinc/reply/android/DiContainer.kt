package ch.rethinc.feedback

import ch.rethinc.reply.android.feedback.FeedbackActivity
import ch.rethinc.reply.core.FeedbackInteractor
import ch.rethinc.reply.core.ReplyChannel


class DiContainer private constructor() {

    lateinit var screenshot: ByteArray
    lateinit var channel: ReplyChannel

    private object Holder {
        val INSTANCE = DiContainer()
    }

    companion object {
        val instance: DiContainer by lazy { Holder.INSTANCE }
    }

    fun inject(feedbackActivity: FeedbackActivity) {
        feedbackActivity.interactor = FeedbackInteractor(channel, screenshot)
    }
}