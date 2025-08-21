// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SimpleArgsParserTest {

    @Test
    fun testHasOptionAndGetOptionValue() {
        val args = arrayOf("--foo", "bar", "-x", "42")
        val parser = SimpleArgsParser(args)

        assertTrue(parser.hasOption("--foo"))
        assertEquals("bar", parser.getOptionValue("--foo"))
        assertEquals("42", parser.getOptionValue("-x"))
    }

    @Test
    fun testGetIntOptionValue() {
        val args = arrayOf("--delay", "3", "-n", "notint")
        val parser = SimpleArgsParser(args)

        assertEquals(3, parser.getIntOptionValue("--delay"))
        // Non-integer should return null
        assertNull(parser.getIntOptionValue("-n"))
        // Missing option returns null
        assertNull(parser.getIntOptionValue("--missing"))
    }

    @Test
    fun testPeekNextAndIteration() {
        val args = arrayOf("one", "two")
        val parser = SimpleArgsParser(args)

        assertEquals("one", parser.peek())
        assertEquals("one", parser.next())
        assertEquals("two", parser.next())
    }
}
