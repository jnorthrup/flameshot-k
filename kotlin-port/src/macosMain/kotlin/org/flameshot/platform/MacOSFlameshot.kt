// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.platform

import org.flameshot.core.*
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.AppKit.*
import platform.CoreGraphics.*
import platform.ImageIO.*
import platform.CoreServices.*

/**
 * macOS-specific Flameshot implementation
 */
class MacOSFlameshot : Flameshot() {

    // macOS-specific hotkey references
    private var hotkeyScreenshotCapture: Any? = null
    private var hotkeyScreenshotHistory: Any? = null

    override fun platformGui(req: CaptureRequest): Any? {
        // Create and show capture widget for macOS
        println("Creating macOS capture GUI for request: $req")
        // TODO: Implement native macOS capture window
        return null
    }

    override fun platformScreen(req: CaptureRequest, screenNumber: Int) {
        println("Capturing screen $screenNumber on macOS with request: $req")
    captureScreen(screenNumber, req)
    }

    override fun platformFull(req: CaptureRequest) {
        println("Capturing full screen on macOS with request: $req")
        captureFullScreen(req)
    }

    override fun platformLauncher() {
        println("Showing launcher on macOS")
        // TODO: Implement launcher window
    }

    override fun platformConfig() {
        println("Showing configuration on macOS")
        // TODO: Implement config window
    }

    override fun platformInfo() {
        println("Showing info window on macOS")
        // TODO: Implement info window
    }

    override fun platformOpenSavePath() {
        println("Opening save path on macOS")
        // Open Finder to the save path if AppKit is available, otherwise just print path
        val savePath = try {
            System.getProperty("user.home") + "/Pictures"
        } catch (_: Throwable) {
            "/tmp"
        }

        try {
            // Try to use AppKit if available at runtime (Kotlin/Native macOS)
            val cls = Class.forName("platform.AppKit.NSWorkspace")
            // If the class exists, attempt to call via reflection is non-trivial in K/N,
            // so for now we simply print the path. Native builds will have NSWorkspace available.
            println("Would open Finder at: $savePath")
        } catch (_: Throwable) {
            println("Save path: $savePath")
        }
    }

    override fun platformRequestCapture(request: CaptureRequest) {
        println("Requesting capture on macOS: $request")
        when (request.mode) {
            CaptureRequest.CaptureMode.FULLSCREEN_MODE -> platformFull(request)
            CaptureRequest.CaptureMode.GRAPHICAL_MODE -> platformGui(request)
            CaptureRequest.CaptureMode.SCREEN_MODE -> platformScreen(request, 0)
        }
    }

    override fun platformExportCapture(screenshot: Screenshot, selection: Rectangle, req: CaptureRequest) {
        println("Exporting capture on macOS")
        // Delegate common export logic to shared utility via an adapter
        val adapter = object : org.flameshot.ScreenshotLike {
            override fun save(path: String): Boolean {
                return screenshot.save(path)
            }

            override fun copyToClipboard() {
                screenshot.copyToClipboard()
            }
        }

        org.flameshot.exportCaptureTasks(adapter, req)
    }

    private fun captureScreen(screenNumber: Int, req: CaptureRequest) {
    println("Capturing screen #$screenNumber on macOS with request: $req")
    try {
        // For now map screenNumber 0 -> main display; enumeration can be added later
        val displayId = CGMainDisplayID()
        val cgImage = CGDisplayCreateImage(displayId)
        if (cgImage != null) {
            val pngBytes = cgImageToPng(cgImage)
            val screenshot = org.flameshot.core.Screenshot(pngBytes)
            val w = CGImageGetWidth(cgImage).toInt()
            val h = CGImageGetHeight(cgImage).toInt()
            val selection = Rectangle(0, 0, w, h)
            Flameshot.instance().exportCapture(screenshot, selection, req)
            println("Capture successful: ${pngBytes.size} bytes")
        } else {
            println("Failed to create CGImage for display")
            simulateCapture(req)
        }
    } catch (e: Throwable) {
        println("captureScreen error: ${e.message}")
        simulateCapture(req)
    }
    }

    private fun captureFullScreen(req: CaptureRequest) {
    println("Capturing full screen on macOS with request: $req")
    try {
        val displayId = CGMainDisplayID()
        val cgImage = CGDisplayCreateImage(displayId)
        if (cgImage != null) {
            val pngBytes = cgImageToPng(cgImage)
            val screenshot = org.flameshot.core.Screenshot(pngBytes)
            val w = CGImageGetWidth(cgImage).toInt()
            val h = CGImageGetHeight(cgImage).toInt()
            val selection = Rectangle(0, 0, w, h)
            Flameshot.instance().exportCapture(screenshot, selection, req)
            println("Fullscreen capture successful: ${pngBytes.size} bytes")
        } else {
            println("Failed to create CGImage for full screen")
            simulateCapture(req)
        }
    } catch (e: Throwable) {
        println("captureFullScreen error: ${e.message}")
        simulateCapture(req)
    }
    }

    private fun simulateCapture(req: CaptureRequest) {
        // Simulate a successful capture for now by creating an empty image data
        val empty = ByteArray(0)
    // Create the common Screenshot actual implementation (provided in PlatformBridge)
    val mockScreenshot = org.flameshot.core.Screenshot(empty)

        // Use a default selection rectangle
        val selection = Rectangle(0, 0, 0, 0)

        // Export the capture according to the request tasks
        Flameshot.instance().exportCapture(mockScreenshot, selection, req)

        println("Simulated capture completed and exported")
    }

    // Convert a CGImageRef to PNG bytes using ImageIO
    private fun cgImageToPng(image: CPointer<CGImage>?): ByteArray {
        if (image == null) return ByteArray(0)
        return memScoped {
            val cfData = CFDataCreateMutable(kCFAllocatorDefault, 0u) ?: return@memScoped ByteArray(0)
            val uti = kUTTypePNG
            val dest = CGImageDestinationCreateWithData(cfData, uti, 1u, null) ?: return@memScoped ByteArray(0)
            CGImageDestinationAddImage(dest, image, null)
            if (!CGImageDestinationFinalize(dest)) return@memScoped ByteArray(0)

            val len = CFDataGetLength(cfData).toInt()
            if (len == 0) return@memScoped ByteArray(0)

            val ptr = CFDataGetBytePtr(cfData)?.reinterpret<ByteVar>()
            if (ptr == null) return@memScoped ByteArray(0)

            val out = ByteArray(len)
            for (i in 0 until len) {
                out[i] = ptr[i].toByte()
            }
            out
        }
    }

    private fun generateDefaultPath(): String {
        val homeDir = try {
            System.getProperty("user.home") ?: "/"
        } catch (_: Throwable) {
            "/"
        }
        val timestamp = System.currentTimeMillis() / 1000L
        return "$homeDir/Pictures/flameshot_$timestamp.png"
    }
}

/**
 * macOS-local Screenshot implementation (platform package).
 * This is a lightweight stub; platform-specific binary image handling
 * or clipboard wiring can be added later.
 */
class MacPlatformScreenshot(private val imageData: ByteArray) {
    fun save(path: String): Boolean {
        println("macOS: save called for path=$path (not fully implemented)")
        return false
    }

    fun copyToClipboard() {
        println("macOS: copyToClipboard called (not implemented)")
    }
}

// Type alias used elsewhere in this file for clarity
typealias MacOSScreenshot = MacPlatformScreenshot