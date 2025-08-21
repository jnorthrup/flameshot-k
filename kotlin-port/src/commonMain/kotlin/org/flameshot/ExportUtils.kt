package org.flameshot

import org.flameshot.core.CaptureRequest

/**
 * Minimal screenshot-like interface usable in common tests and utilities.
 */
interface ScreenshotLike {
    fun save(path: String): Boolean
    fun copyToClipboard()
}

/**
 * Process export tasks from a CaptureRequest using a ScreenshotLike implementation.
 */
fun exportCaptureTasks(screenshot: ScreenshotLike, req: CaptureRequest) {
    req.tasks.forEach { task ->
        when (task) {
            CaptureRequest.ExportTask.SAVE -> {
                val path = if (req.path.isEmpty()) {
                    // default path
                    "./flameshot.png"
                } else req.path
                screenshot.save(path)
            }
            CaptureRequest.ExportTask.COPY -> screenshot.copyToClipboard()
            else -> {
                // no-op for unimplemented tasks in common utility
            }
        }
    }
}
