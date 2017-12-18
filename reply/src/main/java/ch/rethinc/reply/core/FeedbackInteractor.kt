package ch.rethinc.reply.core

class FeedbackInteractor(private val channel: ReplyChannel, screenshot: ByteArray) {
    val feedback = Feedback(screenshot)
    lateinit var output: FeedbackOutput

    fun onInit(output:FeedbackOutput) {
        this.output = output
        output.presentScreenshot(feedback.screenshot)
    }

    fun onNewFeedback(coordinate: Coordinate) {
        val item = FeedbackItem(feedback.feedbackItems.size + 1, "", coordinate)
        feedback.add(item)
        output.addFeedback(item)
    }

    fun onChangeFeedback(number: Int, message: String) {
        val item = feedback.feedbackItems.find { it.number == number }!!
        val updated = item.copy(message = message)
        feedback.replace(item, updated)
        output.updateFeedback(item.number, updated)
    }

    fun onRemoveFeedback(number: Int) {
        feedback.remove(feedback.feedbackItems.find { it.number == number}!!)
        output.removeFeedback(number)
        val items = feedback.feedbackItems.filter { it.number > number }
        items.forEach {
            val updated = it.copy(number=it.number-1)
            feedback.replace(it, updated)
            output.updateFeedback(it.number, updated)
        }
    }

    fun onSendFeedback() {
        channel.sendFeedback(feedback, {output.finishFeedback()}, {})
    }
}