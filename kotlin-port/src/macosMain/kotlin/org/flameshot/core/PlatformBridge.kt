// SPDX-License-Identifier: GPL-3.0-or-later
// Bridge file providing actual implementations for common expects on macOS

package org.flameshot.core

import kotlinx.cinterop.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import org.flameshot.platform.MacOSFlameshot

@OptIn(ExperimentalStdlibApi::class, ExperimentalForeignApi::class)

/**
 * Actual Screenshot implementation for macOS. Keeps a ByteArray payload.
 */
actual class Screenshot(private val imageData: ByteArray) {
    actual fun save(path: String): Boolean {
        return try {
            val data = if (imageData.isEmpty()) {
                NSData.dataWithBytes(bytes = null, length = 0u)
            } else {
                // For now create NSData from Kotlin ByteArray via bytes array copy.
                // This avoids complex memScoped/pointer conversions in tests.
                NSData.create(bytes = imageData.toNSDataBytes(), length = imageData.size.toULong())
            }
            data.writeToFile(path, true)
        } catch (e: Throwable) {
            println("PlatformBridge: failed to save: ${e.message}")
            false
        }
    }

    actual fun copyToClipboard() {
        try {
            val pasteboard = NSPasteboard.generalPasteboard
            pasteboard.clearContents()
            val nsData = if (imageData.isEmpty()) {
                NSData.dataWithBytes(bytes = null, length = 0u)
            } else {
                NSData.create(bytes = imageData.toNSDataBytes(), length = imageData.size.toULong())
            }
            pasteboard.setData(nsData, NSPasteboardTypePNG)
        } catch (e: Throwable) {
            println("PlatformBridge: failed to copy to clipboard: ${e.message}")
        }
    }
}

// Helper: convert Kotlin ByteArray to CValuesRef<ByteVarOf<Byte>> usable with NSData.create
private fun ByteArray.toNSDataBytes(): CValuesRef<ByteVarOf<Byte>> =
    memScoped {
        val ptr = allocArray<ByteVar>(this@toNSDataBytes.size)
        for (i in this@toNSDataBytes.indices) {
            ptr[i] = this@toNSDataBytes[i]
        }
        ptr.readBytes(this@toNSDataBytes.size).usePinned { pinned ->
            pinned.addressOf(0)
        }
    }

// Helper: convert NSData to Kotlin ByteArray
private fun NSData.toByteArray(): ByteArray {
    val len = this.length.toInt()
    if (len == 0) return ByteArray(0)
    return memScoped {
        val raw = this@toByteArray.bytes?.reinterpret<ByteVar>() ?: return@memScoped ByteArray(0)
        val out = ByteArray(len)
        for (i in 0 until len) {
            out[i] = raw[i]
        }
        out
    }
}

/**
 * Factory function to create the platform Flameshot instance
 */
actual fun platformCreateInstance(): Flameshot {
    return MacOSFlameshot()
}
