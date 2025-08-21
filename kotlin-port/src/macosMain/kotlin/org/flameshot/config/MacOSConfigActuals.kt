// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.config

import platform.Foundation.*

/**
 * macOS actual implementations for platform config I/O expected in commonMain
 */
actual fun platformSaveConfig(configJson: String) {
    try {
        val defaults = NSUserDefaults.standardUserDefaults
        defaults.setObject(configJson, "FlameshotConfig")
        defaults.synchronize()
    } catch (e: Throwable) {
        println("MacOS: Failed to save configuration: ${e.message}")
    }
}

actual fun platformLoadConfig(): String {
    return try {
        val defaults = NSUserDefaults.standardUserDefaults
        val configString = defaults.stringForKey("FlameshotConfig")
        configString?.toString() ?: ""
    } catch (e: Throwable) {
        println("MacOS: Failed to load configuration: ${e.message}")
        ""
    }
}
