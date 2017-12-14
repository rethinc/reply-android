package ch.rethinc.reply.core

import com.nhaarman.mockito_kotlin.*
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
    fun screenshotExists_attachingOutput_presentScreenshot() {
        sut.attach(outputSpy)

        verify(outputSpy).presentScreenshot(screenshot)
    }

    @Nested
    inner class OutputIsAttached {
        @BeforeEach
        fun attachOutput() {
            sut.attach(outputSpy)
        }

        @Test
        fun validFeedbackItem_onFeedback_presentAddedFeedbackItems() {
            val expectedItem = FeedbackItem("Feedback message", Coordinate(1, 1))

            sut.onFeedback("Feedback message", Coordinate(1, 1))

            verify(outputSpy).presentFeedbackItems(listOf(expectedItem))
        }

        @Nested
        inner class FeedbackItemExists {
            lateinit var existingItem: FeedbackItem

            @BeforeEach
            fun createFeedbackItem() {
                sut.onFeedback("Feedback message", Coordinate(1, 1))
                argumentCaptor<List<FeedbackItem>>().apply {
                    verify(outputSpy).presentFeedbackItems(capture())
                    existingItem = firstValue[0]
                }
            }

            @Test
            fun existingFeedbackItem_removeFeedbackItem_presentFeedbackItemsExludingRemoved() {
                sut.onRemoveFeedback(existingItem)

                verify(outputSpy).presentFeedbackItems(emptyList())
            }

            @Test
            fun existingFeedbackItem_onChangeExistingFeedbackItem_presentUpdatedFeedbackItem() {
                val expectedItem = FeedbackItem("Updated Message", Coordinate(1, 1))

                sut.onChangeFeedback(existingItem, expectedItem.message)

                verify(outputSpy).presentFeedbackItems(listOf(expectedItem))
            }

            @Test
            fun existingValidFeedback_sendFeedbackSuccessful_finishFeedbackProcess() {
                sut.onSendFeedback()

                argumentCaptor<()->Unit>().apply {
                    verify(channelStub).sendFeedback(any(),capture(), any())
                    firstValue()
                }

                verify(outputSpy).finishFeedback()
            }
        }
    }
}