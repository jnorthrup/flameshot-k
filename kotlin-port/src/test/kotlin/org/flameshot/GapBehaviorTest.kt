package org.flameshot

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class GapBehaviorTest {
    @Test
    fun testGapRecordsReasons() {
        // Start from a clean state for repeatability
        Gap.clear()
        assertTrue(Gap.reasons().isEmpty(), "reasons initially empty")

        Gap.record("demo-reason")
        assertEquals(listOf("demo-reason"), Gap.reasons())
    }
}
