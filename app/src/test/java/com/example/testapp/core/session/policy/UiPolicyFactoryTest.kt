package com.example.testapp.core.session.registry

import com.example.testapp.core.session.policy.UiPolicyFactory
import com.example.testapp.domain.session.BottomBarMode
import com.example.testapp.domain.session.GestureMode
import com.example.testapp.domain.session.SessionCapabilitiesPresets
import com.example.testapp.domain.session.TopBarMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UiPolicyFactoryTest {
    @Test
    fun browseCapabilities_navOnlyBottomBar_noRevealDelay() {
        val contract = UiPolicyFactory.from(SessionCapabilitiesPresets.browse)
        assertEquals(BottomBarMode.NavOnly, contract.bottomBar)
        assertEquals(GestureMode.HistoryBrowse, contract.gesture)
        assertEquals(TopBarMode.Minimal, contract.topBar)
        assertEquals(0L, contract.resultDisplayDelayMs)
        assertFalse(contract.menu.showAi)
    }

    @Test
    fun practiceCapabilities_fullBottomBar_withRevealDelay() {
        val contract = UiPolicyFactory.from(SessionCapabilitiesPresets.practice)
        assertEquals(BottomBarMode.Full, contract.bottomBar)
        assertTrue(contract.resultDisplayDelayMs > 0L)
        assertTrue(contract.menu.showAi)
    }
}
