// SPDX-License-Identifier: GPL-3.0-or-later

package org.flameshot.core

import java.awt.Robot
import java.awt.Rectangle as AwtRectangle
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

/**
 * JVM actuals for multiplatform expects used during tests.
 */
actual class Screenshot actual constructor(private val imageData: ByteArray) {
    actual fun save(path: String): Boolean {
        val img = ImageIO.read(ByteArrayInputStream(imageData)) ?: return false
        val out = File(path)
        out.parentFile?.mkdirs()
        return ImageIO.write(img, "png", out)
    }

    actual fun copyToClipboard() {
        // For headless CI or simple tests, we skip clipboard.
    }
}

/** JVM Flameshot using AWT Robot for screen capture. */
class JvmFlameshot : Flameshot() {
    override fun platformFull(req: CaptureRequest) {
        val toolkit = Toolkit.getDefaultToolkit()
        val screenSize = toolkit.screenSize
        val robot = Robot()
        val img: BufferedImage = robot.createScreenCapture(AwtRectangle(0, 0, screenSize.width, screenSize.height))
        val baos = ByteArrayOutputStream()
        ImageIO.write(img, "png", baos)
        val bytes = baos.toByteArray()
        val shot = Screenshot(bytes)
        // honor save task if present
        if (req.tasks.contains(CaptureRequest.ExportTask.SAVE)) {
            val path = if (req.path.isNotEmpty()) req.path else "/tmp/flameshot_capture.png"
            shot.save(path)
        }
    }
}

actual fun platformCreateInstance(): Flameshot {
    return JvmFlameshot()
}
