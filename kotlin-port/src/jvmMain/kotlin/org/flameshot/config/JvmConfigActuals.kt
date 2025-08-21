// SPDX-License-Identifier: GPL-3.0-or-later

package org.flameshot.config

import java.util.prefs.Preferences

actual fun platformSaveConfig(configJson: String) {
    try {
        val prefs = Preferences.userRoot().node("org/flameshot")
        prefs.put("FlameshotConfig", configJson)
    } catch (e: Throwable) {
        println("JVM: Failed to save configuration: ${e.message}")
    }
}

actual fun platformLoadConfig(): String {
    return try {
        val prefs = Preferences.userRoot().node("org/flameshot")
        prefs.get("FlameshotConfig", "") ?: ""
    } catch (e: Throwable) {
        println("JVM: Failed to load configuration: ${e.message}")
        ""
    }
}
