package ch.rethinc.reply.core

data class Feedback(val screenshot: ByteArray, private val items: MutableList<FeedbackItem> = mutableListOf()) {

    val feedbackItems: List<FeedbackItem>
        get() = items.map { it.copy() }

    fun add(feedbackItem: FeedbackItem) {
        items.add(feedbackItem)
    }

    fun remove(feedbackItem: FeedbackItem) {
        items.remove(feedbackItem)
    }

    fun replace(feedbackItem: FeedbackItem, withItem: FeedbackItem) {
        items[items.indexOf(feedbackItem)] = withItem
    }
}