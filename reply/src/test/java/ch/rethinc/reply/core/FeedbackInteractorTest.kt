package ch.rethinc.reply.core

import com.nhaarman.mockito_kotlin.*

import org.junit.Assert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.hamcrest.Matchers.`is` as toBe

class FeedbackInteractorTest {

    lateinit var sut: FeedbackInteractor
    lateinit var outputSpy: FeedbackOutput
    lateinit var channelStub: ReplyChannel
    val screenshot = ByteArray(42)

    @BeforeEach
    fun setup() {
        channelStub = mock()
        sut = FeedbackInteractor(channelStub, screenshot)
        outputSpy = mock()
    }

    @Test
    fun screenshotExists_onInit_presentScreenshot() {
        sut.onInit(outputSpy)

        verify(outputSpy).presentScreenshot(screenshot)
    }

    @Nested
    inner class NoFeedbackItems {
        @BeforeEach
        fun init() {
            sut.onInit(outputSpy)
        }

        @Test
        fun validFeedbackItems_onNewFeedback_addFeedbackItems() {
            sut.onNewFeedback(Coordinate(1, 1))
            sut.onNewFeedback(Coordinate(5, 5))

            verify(outputSpy).addFeedback(argThat { number == 1 })
            verify(outputSpy).addFeedback(argThat { number == 2 })
        }

        @Test
        fun existingFeedbackItems_onChangeFeedback_updateFeedback() {
            sut.onNewFeedback(Coordinate(1, 1))

            sut.onChangeFeedback(1, "Message")

            verify(outputSpy).updateFeedback(eq(1), check {
                assertThat(it.message, toBe("Message"))
            })
        }

        @Test
        fun existingFeedbackItem_removeItem_removeFeedback() {
            sut.onNewFeedback(Coordinate(10, 10))

            sut.onRemoveFeedback(1)

            verify(outputSpy).removeFeedback(1)
        }

        @Test
        fun existingFeedbackItems_removeItem_updateItemNumbers() {
            sut.onNewFeedback(Coordinate(10, 10))
            sut.onNewFeedback(Coordinate(10, 10))
            sut.onNewFeedback(Coordinate(10, 10))

            sut.onRemoveFeedback(1)

            verify(outputSpy).removeFeedback(1)
            verify(outputSpy).updateFeedback(eq(2), argThat { number == 1 })
            verify(outputSpy).updateFeedback(eq(3), argThat { number == 2 })
        }

        @Test
        fun validFeedbackItem_onSendFeedback_sendFeedback() {
            sut.onNewFeedback(Coordinate(1, 1))
            sut.onChangeFeedback(1, "Message")

            sut.onSendFeedback()

            verify(channelStub).sendFeedback(argThat {
                feedbackItems[0].message == "Message"
            }, any(), any())
        }

        @Test
        fun existingValidFeedback_sendFeedbackSuccessful_finishFeedbackProcess() {
            sut.onNewFeedback(Coordinate(10, 10))
            whenever(channelStub.sendFeedback(any(), any(), any())).then {
                @Suppress("UNCHECKED_CAST")
                val success = it.arguments[1] as () -> Unit
                success()
                true
            }

            sut.onSendFeedback()

            verify(outputSpy).finishFeedback()
        }
    }
}