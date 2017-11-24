package ch.rethinc.reply.core

interface ReplyChannel {
    fun sendFeedback(feedback: Feedback, onSuccess: () -> Unit, onError: (e: Exception) -> Unit)
}