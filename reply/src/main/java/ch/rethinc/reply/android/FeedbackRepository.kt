package ch.rethinc.reply.android

import ch.rethinc.reply.core.Feedback

class FeedbackRepository {

    private var feedback: Feedback? = null

    fun save(feedback: Feedback) {
        this.feedback = feedback
    }

    fun get(): Feedback? {
        val currentFeedback = feedback
        return currentFeedback?.copy()
    }
}