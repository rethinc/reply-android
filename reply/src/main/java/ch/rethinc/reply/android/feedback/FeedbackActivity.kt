package ch.rethinc.reply.android.feedback

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import ch.rethinc.feedback.DiContainer
import ch.rethinc.reply.android.R
import ch.rethinc.reply.core.Coordinate
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
        interactor.onInit(this)


        class NewFeedbackDetector : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(p0: MotionEvent?): Boolean {
                return true
            }

            override fun onSingleTapUp(event: MotionEvent?): Boolean {
                interactor.onNewFeedback(Coordinate(event!!.x.toInt(), event!!.y.toInt()))
                return true
            }
        }
        val detectNewFeedback = GestureDetector(this, NewFeedbackDetector())
        screenshotArea.setOnTouchListener { _, event ->
            detectNewFeedback.onTouchEvent(event)
        }
    }

    override fun presentScreenshot(screenshot: ByteArray) {
        screenshotImageView.setImageBitmap(BitmapFactory.decodeByteArray(screenshot, 0, screenshot.size))
    }

    override fun addFeedback(item: FeedbackItem) {
        val scale = resources.displayMetrics.density
        val dps: (dp: Int) -> Float = { (it * scale + 0.5f) }
        val itemView = layoutInflater.inflate(R.layout.feedback_item, null) as TextView
        itemView.text = (item.number).toString()
        itemView.tag = item.number
        itemView.x = item.coordinate.x.toFloat() - dps(15)
        itemView.y = item.coordinate.y.toFloat() - dps(15)
        screenshotArea.addView(itemView)

    }

    override fun updateFeedback(number: Int, item: FeedbackItem) {
        val itemView = screenshotArea.findViewWithTag<TextView>(number)
        itemView.text = item.number.toString()
    }

    override fun removeFeedback(number: Int) {
        val itemView = screenshotArea.findViewWithTag<TextView>(number)
        screenshotArea.removeView(itemView)
    }

    override fun finishFeedback() {

    }
}
