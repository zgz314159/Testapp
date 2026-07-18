package com.example.testapp.presentation.screen.home

import com.example.testapp.presentation.screen.home.components.HomeQuestionBankCachePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeQuestionBankCachePolicyTest {

    @Test
    fun `shouldEagerCompose for modest home library`() {
        assertTrue(HomeQuestionBankCachePolicy.shouldEagerCompose(fileCount = 29, folderCount = 6))
        assertTrue(HomeQuestionBankCachePolicy.shouldEagerCompose(fileCount = 48, folderCount = 0))
    }

    @Test
    fun `shouldEagerCompose falls back for large libraries`() {
        assertFalse(HomeQuestionBankCachePolicy.shouldEagerCompose(fileCount = 49, folderCount = 0))
        assertFalse(HomeQuestionBankCachePolicy.shouldEagerCompose(fileCount = 40, folderCount = 10))
    }
}
