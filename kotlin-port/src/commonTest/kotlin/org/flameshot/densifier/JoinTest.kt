// SPDX-License-Identifier: MIT
package org.flameshot.densifier

import kotlin.test.Test
import kotlin.test.assertEquals

class JoinTest {
    @Test
    fun testJoinApply() {
        val j = Join(10) { x: Int -> x * 2 }
        assertEquals(20, j.apply())
    }
}
