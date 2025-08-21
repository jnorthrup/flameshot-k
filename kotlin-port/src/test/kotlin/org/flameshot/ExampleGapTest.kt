package org.flameshot

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

class ExampleGapTest {
    @Test
    fun testGapShouldFail() {
        // Intentionally failing gap test to drive red-first TDD in CI
        assertTrue(false, "This Gap test intentionally fails to enforce red TDD")
    }
}
