package org.flameshot

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

class ExampleGapJvmTest {
    @Test
    fun testGapShouldFail() {
        // intentionally fail to drive red-first gap analysis
        assertTrue(false, "Intentional Gap test failure")
    }
}
