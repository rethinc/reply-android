package ch.rethinc.reply.android

import android.os.Environment
import android.os.FileObserver
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream


class ScreenshotDetector {

    lateinit var fileObserver: FileObserver

    fun startDetecting(onMadeScreenshot: (ByteArray) -> Unit) {

        val screenshotPath = (Environment.getExternalStorageDirectory().path + File.separator + Environment.DIRECTORY_PICTURES + File.separator + "Screenshots" + File.separator)
        var lastCreatedPath: String? = null

        fileObserver = object : FileObserver(screenshotPath, FileObserver.CREATE or FileObserver.CLOSE_WRITE) {
            override fun onEvent(event: Int, path: String?) {
                if (event == FileObserver.CREATE) {
                    lastCreatedPath = path
                } else if (lastCreatedPath != null && event == FileObserver.CLOSE_WRITE) {
                    val file = File(screenshotPath + lastCreatedPath)
                    val bytes = ByteArray(file.length().toInt())
                    val buf = BufferedInputStream(FileInputStream(file))
                    buf.read(bytes, 0, bytes.size)
                    buf.close()
                    lastCreatedPath = null
                    onMadeScreenshot(bytes)
                }
            }
        }
        fileObserver.startWatching()
    }
}