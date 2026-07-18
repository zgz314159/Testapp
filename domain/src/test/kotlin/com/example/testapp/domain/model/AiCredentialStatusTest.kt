package com.example.testapp.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiCredentialStatusTest {

    @Test
    fun chat_requiresDeepSeekOrManaged() {
        assertFalse(AiCredentialStatus().readyFor(AiCapability.CHAT))
        assertTrue(
            AiCredentialStatus(deepSeekConfigured = true).readyFor(AiCapability.CHAT),
        )
        assertTrue(
            AiCredentialStatus(managedAccessAvailable = true).readyFor(AiCapability.CHAT),
        )
    }

    @Test
    fun correctOnline_requiresDeepSeekPlusAnySearchKeyOrManaged() {
        assertFalse(
            AiCredentialStatus(deepSeekConfigured = true).readyFor(AiCapability.CORRECT_ONLINE),
        )
        assertFalse(
            AiCredentialStatus(bochaConfigured = true).readyFor(AiCapability.CORRECT_ONLINE),
        )
        assertTrue(
            AiCredentialStatus(
                deepSeekConfigured = true,
                bochaConfigured = true,
            ).readyFor(AiCapability.CORRECT_ONLINE),
        )
        assertTrue(
            AiCredentialStatus(
                deepSeekConfigured = true,
                tavilyConfigured = true,
            ).readyFor(AiCapability.CORRECT_ONLINE),
        )
        assertTrue(
            AiCredentialStatus(managedAccessAvailable = true)
                .readyFor(AiCapability.CORRECT_ONLINE),
        )
    }

    @Test
    fun onlineChat_requiresDeepSeekPlusAnySearchKeyOrManaged() {
        assertFalse(
            AiCredentialStatus(deepSeekConfigured = true).readyFor(AiCapability.CHAT_ONLINE),
        )
        assertTrue(
            AiCredentialStatus(
                deepSeekConfigured = true,
                bochaConfigured = true,
            ).readyFor(AiCapability.CHAT_ONLINE),
        )
        assertTrue(
            AiCredentialStatus(managedAccessAvailable = true).readyFor(AiCapability.CHAT_ONLINE),
        )
    }
}
