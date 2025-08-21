// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot

import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleArgsParserExtraGreenTest {

    @Test
    fun testGetIntOptionValueWithDefault() {
        val args = arrayOf("--delay", "notint")
        val parser = SimpleArgsParser(args)

        // When the option exists but isn't an int, the provided default should be returned
        assertEquals(5, parser.getIntOptionValue("--delay", 5))

        // When the option is missing, the provided default should be returned
        assertEquals(10, parser.getIntOptionValue("--missing", 10))
    }

    @Test
    fun testPeekReturnsNullAfterConsume() {
        val args = arrayOf("one")
        val parser = SimpleArgsParser(args)

        // consume the only element
        assertEquals("one", parser.next())
        // now peek should return null
        assertEquals(null, parser.peek())
    }

    @Test
    fun testPeekOnEmptyReturnsNull() {
        val args = arrayOf<String>()
        val parser = SimpleArgsParser(args)

        // peek on empty argument list should be null
        assertEquals(null, parser.peek())
    }

    @Test
    fun testGetOptionValueWhenOptionIsLastReturnsNull() {
        val args = arrayOf("--flag")
        val parser = SimpleArgsParser(args)

        // option present but no value after it
        assertEquals(null, parser.getOptionValue("--flag"))
    }

    @Test
    fun testGetOptionValueReturnsFirstOccurrence() {
        val args = arrayOf("--opt", "first", "--opt", "second")
        val parser = SimpleArgsParser(args)

        // The parser should return the value associated with the first occurrence
        assertEquals("first", parser.getOptionValue("--opt"))
    }

    @Test
    fun testEqualsStyleOptionParsing() {
        val args = arrayOf("--flag=on", "--num=7")
        val parser = SimpleArgsParser(args)

        // equals-style options should be parsed
        assertEquals("on", parser.getOptionValue("--flag"))
        assertEquals(7, parser.getIntOptionValue("--num"))
    }

    @Test
    fun testShortEqualsStyleAndHasOption() {
        val args = arrayOf("-d=4", "-x=val")
        val parser = SimpleArgsParser(args)

        // hasOption should detect presence even with =value form
        assertEquals(true, parser.hasOption("-d"))
        assertEquals("4", parser.getOptionValue("-d"))
        assertEquals("val", parser.getOptionValue("-x"))
    }

    @Test
    fun testCombinedShortFlagsAreParsedAsSeparateFlags() {
        val args = arrayOf("-abc")
        val parser = SimpleArgsParser(args)

        // combined short flags should be visible individually
        assertEquals(true, parser.hasOption("-a"))
        assertEquals(true, parser.hasOption("-b"))
        assertEquals(true, parser.hasOption("-c"))
    }

    @Test
    fun testShortAttachedValueParsing() {
        val args = arrayOf("-ovalue", "-p=path")
        val parser = SimpleArgsParser(args)

        // -ovalue should be parsed as option -o with value "value"
        assertEquals("value", parser.getOptionValue("-o"))
        // existing = style should still work
        assertEquals("path", parser.getOptionValue("-p"))
    }
}
