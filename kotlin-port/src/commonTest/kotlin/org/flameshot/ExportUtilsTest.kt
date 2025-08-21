package org.flameshot

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import org.flameshot.core.CaptureRequest

class FakeScreenshot(var savedPath: String? = null, var copied: Boolean = false) : ScreenshotLike {
    override fun save(path: String): Boolean {
        savedPath = path
        return true
    }

    override fun copyToClipboard() {
        copied = true
    }
}

class ExportUtilsTest {
    @Test
    fun testExportSaveAndCopy() {
        val req = CaptureRequest().addSaveTask("/tmp/test.png").addTask(CaptureRequest.ExportTask.COPY)
        val fake = FakeScreenshot()

        exportCaptureTasks(fake, req)

        assertEquals("/tmp/test.png", fake.savedPath)
        assertTrue(fake.copied)
    }
}
