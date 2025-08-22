// SPDX-License-Identifier: GPL-3.0-or-later
package org.flameshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimpleArgsParserCaseInsensitiveGapTest {

    @Test
    fun longOptionsShouldBeCaseInsensitive_hasOption() {
        val args = arrayOf("--foo", "bar")
        val parser = SimpleArgsParser(args)

        // User story: long option names should match case-insensitively
        assertTrue(parser.hasOption("--Foo"), "Expected --Foo to match --foo (case-insensitive)")
    }

    @Test
    fun longOptionsShouldBeCaseInsensitive_getOptionValue() {
        val args = arrayOf("--foo", "bar")
        val parser = SimpleArgsParser(args)

        // User story: retrieving option value with different case should work
        assertEquals("bar", parser.getOptionValue("--Foo"))
    }
}
