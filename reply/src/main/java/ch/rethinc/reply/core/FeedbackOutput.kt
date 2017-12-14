package ch.rethinc.reply.core

interface FeedbackOutput {

    fun presentScreenshot(image: ByteArray)
    fun presentFeedbackItems(items: List<FeedbackItem>)
    fun finishFeedback()
}