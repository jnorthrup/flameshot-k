// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.config

import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigHandlerTest {

    @BeforeTest
    fun disablePersistence() {
        // Prevent tests from touching platform storage (Preferences)
        ConfigHandler.setPersistenceEnabled(false)
    }

    @AfterTest
    fun enablePersistence() {
        // Restore default for other runs (defensive)
        ConfigHandler.setPersistenceEnabled(true)
    }

    @Test
    fun testDefaultConfigSerialization() {
        // Ensure defaults serialize and deserialize correctly
        // persistence disabled in BeforeTest so this is deterministic
        ConfigHandler.loadConfig()
        val json = ConfigHandler.toJson()
        assertTrue(json.isNotEmpty())

        // Round-trip
        ConfigHandler.loadFromJson(json)
        val json2 = ConfigHandler.toJson()
        assertEquals(json, json2)
    }

    @Test
    fun testUpdateConfig() {
        val original = ConfigHandler.getConfig()
        val newPath = "/tmp/flameshot_test"
        ConfigHandler.setSavePath(newPath)
        assertEquals(newPath, ConfigHandler.savePath())

        // restore original
        ConfigHandler.updateConfig { original }
    }

    @Test
    fun testLoadFromInvalidJsonDoesNotThrow() {
        val before = ConfigHandler.getConfig()
        val invalid = "{ this is not valid json"
        // Should not throw and should leave config unchanged
        ConfigHandler.loadFromJson(invalid)
        val after = ConfigHandler.getConfig()
        assertEquals(before, after)
    }

    @Test
    fun testPlatformSaveHandlerCalledOnUpdate() {
        var savedJson: String? = null
        val restore = ConfigHandler.replacePlatformHandlers(saveHandler = { s -> savedJson = s }, loadHandler = { "" })
        try {
            ConfigHandler.setPersistenceEnabled(true)
            val newPath = "/tmp/handler_test"
            ConfigHandler.setSavePath(newPath)
            // platform handler should have been called with JSON containing the new path
            assertTrue(savedJson?.contains(newPath) == true)
        } finally {
            restore()
            ConfigHandler.setPersistenceEnabled(false)
        }
    }

    @Test
    fun testPlatformLoadHandlerUpdatesConfig() {
        val sample = "{\n  \"savePath\": \"/tmp/from_handler\",\n  \"saveAsFileExtension\": \"png\",\n  \"jpegQuality\": 75,\n  \"uiColor\": \"#663399\",\n  \"contrastUiColor\": \"#ffffff\",\n  \"contrastOpacity\": 190,\n  \"copyPathAfterSave\": false,\n  \"savePathFixed\": false,\n  \"shortcuts\": {\n    \"TAKE_SCREENSHOT\": \"Cmd+Shift+3\",\n    \"SCREENSHOT_HISTORY\": \"Cmd+Shift+4\"\n  }\n}"
        val restore = ConfigHandler.replacePlatformHandlers(saveHandler = { }, loadHandler = { sample })
        try {
            ConfigHandler.setPersistenceEnabled(true)
            ConfigHandler.loadConfig()
            assertEquals("/tmp/from_handler", ConfigHandler.savePath())
        } finally {
            restore()
            ConfigHandler.setPersistenceEnabled(false)
        }
    }

    @Test
    fun testPlatformSaveNotCalledWhenPersistenceDisabled() {
        var savedJson: String? = null
        val restore = ConfigHandler.replacePlatformHandlers(saveHandler = { s -> savedJson = s }, loadHandler = { "" })
        try {
            // Ensure persistence is disabled: platform handler should NOT be called
            ConfigHandler.setPersistenceEnabled(false)
            val newPath = "/tmp/should_not_save"
            ConfigHandler.setSavePath(newPath)
            // give a tiny moment for any async calls (there aren't any, but keep deterministic)
            Thread.sleep(10)
            assertTrue(savedJson == null)
        } finally {
            restore()
            ConfigHandler.setPersistenceEnabled(false)
        }
    }
}
