// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: 2017-2019 Alejandro Sirgo Rica & Contributors

package org.flameshot.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FlameshotTest {
    
    @Test
    fun testSingletonInstance() {
        val instance1 = Flameshot.instance()
        val instance2 = Flameshot.instance()
        
        // Should return the same instance
        assertTrue(instance1 === instance2)
        assertNotNull(instance1)
    }
    
    @Test
    fun testOriginSetAndGet() {
        Flameshot.setOrigin(Flameshot.Origin.CLI)
        assertEquals(Flameshot.Origin.CLI, Flameshot.origin())
        
        Flameshot.setOrigin(Flameshot.Origin.DAEMON)
        assertEquals(Flameshot.Origin.DAEMON, Flameshot.origin())
    }
    
    @Test
    fun testVersion() {
        val flameshot = Flameshot.instance()
        assertEquals("13.1.0", flameshot.getVersion())
    }
    
    @Test
    fun testExternalWidget() {
        val flameshot = Flameshot.instance()
        
        // Default should be false
        assertTrue(!flameshot.haveExternalWidget())
        
        flameshot.setExternalWidget(true)
        assertTrue(flameshot.haveExternalWidget())
        
        flameshot.setExternalWidget(false)
        assertTrue(!flameshot.haveExternalWidget())
    }
}