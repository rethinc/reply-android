package ch.rethinc.reply.core

interface FeedbackOutput {

    fun presentScreenshot(screenshot: ByteArray)
    fun addFeedback(item: FeedbackItem)
    fun updateFeedback(number:Int, item: FeedbackItem)
    fun removeFeedback(number: Int)
    fun finishFeedback()
}