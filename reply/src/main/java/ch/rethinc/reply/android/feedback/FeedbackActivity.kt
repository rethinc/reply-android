package ch.rethinc.reply.android.feedback

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import ch.rethinc.feedback.DiContainer
import ch.rethinc.reply.android.R
import ch.rethinc.reply.core.FeedbackInteractor
import ch.rethinc.reply.core.FeedbackItem
import ch.rethinc.reply.core.FeedbackOutput
import kotlinx.android.synthetic.main.feedback_activity.*

class FeedbackActivity : Activity(), FeedbackOutput {

    lateinit var interactor: FeedbackInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feedback_activity)
        DiContainer.instance.inject(this)
        interactor.attach(this)
    }

    override fun presentScreenshot(screenshot: ByteArray) {
        screenshotView.setImageBitmap(BitmapFactory.decodeByteArray(screenshot, 0, screenshot.size))
    }

    override fun presentFeedbackItems(items: List<FeedbackItem>) {}

    override fun finishFeedback() {}
}
