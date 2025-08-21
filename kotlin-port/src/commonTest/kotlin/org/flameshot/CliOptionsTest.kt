// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.flameshot.core.CaptureRequest

class CliOptionsTest {

    @Test
    fun testApplyCommonOptions() {
        val args = arrayOf("--delay", "5", "-p", "/tmp/out.png", "-c")
        val parser = SimpleArgsParser(args)
        val req = CaptureRequest.GRAPHICAL_MODE
        val out = applyCommonOptions(req, parser)

        assertEquals(5u, out.delay)
        assertTrue(out.tasks.contains(CaptureRequest.ExportTask.SAVE))
        assertTrue(out.tasks.contains(CaptureRequest.ExportTask.COPY))
        assertEquals("/tmp/out.png", out.path)
    }
}
