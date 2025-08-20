// SPDX-License-Identifier: GPL-3.0-or-later 
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.platform

import org.flameshot.config.*
import kotlinx.cinterop.*
import platform.Foundation.*

/**
 * macOS-specific configuration implementation using NSUserDefaults
 */
actual fun platformSaveConfig(configJson: String) {
    try {
        val defaults = NSUserDefaults.standardUserDefaults
        defaults.setObject(configJson, "FlameshotConfig")
        defaults.synchronize()
        println("Configuration saved to macOS user defaults")
    } catch (e: Exception) {
        println("Failed to save configuration: ${e.message}")
    }
}

actual fun platformLoadConfig(): String {
    return try {
        val defaults = NSUserDefaults.standardUserDefaults
        val configString = defaults.stringForKey("FlameshotConfig")
        configString?.toString() ?: ""
    } catch (e: Exception) {
        println("Failed to load configuration: ${e.message}")
        ""
    }
}