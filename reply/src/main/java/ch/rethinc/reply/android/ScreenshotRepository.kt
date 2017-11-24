package ch.rethinc.reply.android

class ScreenshotRepository {

    private var data: ByteArray? = null

    fun save(data: ByteArray) {
        this.data = data
    }

    fun get() : ByteArray? {
        return data
    }
}