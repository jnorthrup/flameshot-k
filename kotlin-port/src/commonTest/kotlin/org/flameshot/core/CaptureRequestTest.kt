// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CaptureRequestTest {
    
    @Test
    fun testDefaultCaptureRequest() {
        val request = CaptureRequest()
        
        assertEquals(CaptureRequest.CaptureMode.GRAPHICAL_MODE, request.mode)
        assertEquals(0u, request.delay)
        assertTrue(request.tasks.isEmpty())
    }
    
    @Test
    fun testAddTask() {
        val request = CaptureRequest()
            .addTask(CaptureRequest.ExportTask.SAVE)
            .addTask(CaptureRequest.ExportTask.COPY)
        
        assertTrue(request.tasks.contains(CaptureRequest.ExportTask.SAVE))
        assertTrue(request.tasks.contains(CaptureRequest.ExportTask.COPY))
        assertEquals(2, request.tasks.size)
    }
    
    @Test
    fun testRemoveTask() {
        val request = CaptureRequest()
            .addTask(CaptureRequest.ExportTask.SAVE)
            .addTask(CaptureRequest.ExportTask.COPY)
            .removeTask(CaptureRequest.ExportTask.SAVE)
        
        assertFalse(request.tasks.contains(CaptureRequest.ExportTask.SAVE))
        assertTrue(request.tasks.contains(CaptureRequest.ExportTask.COPY))
        assertEquals(1, request.tasks.size)
    }
    
    @Test
    fun testAddSaveTask() {
        val testPath = "/tmp/test.png"
        val request = CaptureRequest().addSaveTask(testPath)
        
        assertTrue(request.tasks.contains(CaptureRequest.ExportTask.SAVE))
        assertEquals(testPath, request.path)
    }
    
    @Test
    fun testStaticId() {
        val testId = 12345u
        val request = CaptureRequest().setStaticId(testId)
        
        assertEquals(testId, request.id)
    }
    
    @Test
    fun testCaptureModeConstants() {
        assertEquals(CaptureRequest.CaptureMode.GRAPHICAL_MODE, CaptureRequest.GRAPHICAL_MODE.mode)
        assertEquals(CaptureRequest.CaptureMode.FULLSCREEN_MODE, CaptureRequest.FULLSCREEN_MODE.mode)
        assertEquals(CaptureRequest.CaptureMode.SCREEN_MODE, CaptureRequest.SCREEN_MODE.mode)
    }
}

class RectangleTest {
    
    @Test
    fun testDefaultRectangle() {
        val rect = Rectangle()
        
        assertEquals(0, rect.x)
        assertEquals(0, rect.y)
        assertEquals(0, rect.width)
        assertEquals(0, rect.height)
    }
    
    @Test
    fun testRectangleWithValues() {
        val rect = Rectangle(10, 20, 100, 200)
        
        assertEquals(10, rect.x)
        assertEquals(20, rect.y)
        assertEquals(100, rect.width)
        assertEquals(200, rect.height)
    }
}