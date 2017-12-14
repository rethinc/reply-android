package ch.rethinc.reply.core

class FeedbackInteractor(private val channel: ReplyChannel, screenshot: ByteArray) {
    lateinit var output: FeedbackOutput
    val feedback = Feedback(screenshot)

    fun attach(output: FeedbackOutput) {
        this.output = output
        this.output.presentScreenshot(feedback.screenshot)
    }

    fun onFeedback(message: String, coordinate: Coordinate) {
        feedback.add(FeedbackItem(message, coordinate))
        output.presentFeedbackItems(feedback.feedbackItems)
    }

    fun onRemoveFeedback(feedbackItem: FeedbackItem) {
        feedback.remove(feedbackItem)
        output.presentFeedbackItems(feedback.feedbackItems)
    }

    fun onChangeFeedback(feedbackItem: FeedbackItem, message: String) {
        feedback.replace(feedbackItem, FeedbackItem(message, feedbackItem.coordinate))
        output.presentFeedbackItems(feedback.feedbackItems)
    }

    fun onSendFeedback() {
        channel.sendFeedback(feedback, {output.finishFeedback()}, {})
    }
}