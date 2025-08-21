// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot

import kotlin.test.Test
import kotlin.test.assertFailsWith

class SimpleArgsParserGapTest {

    @Test
    fun testNextOnEmptyThrows() {
        val args = arrayOf<String>()
        val parser = SimpleArgsParser(args)

        // Calling next() with no elements should throw a clearer NoSuchElementException.
        assertFailsWith<NoSuchElementException> {
            parser.next()
        }
    }
}
