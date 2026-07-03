package com.example.testapp.presentation.screen.questionbank

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionBankDrawerExpansionTest {
    @Test
    fun searchModeDefaultsExpandedUntilCollapsed() {
        val snapshot = QuestionBankExpansionSnapshot(
            isSearchMode = true,
            searchCollapsedFiles = setOf("a.json")
        )
        assertTrue(snapshot.isFileExpanded("b.json"))
        assertFalse(snapshot.isFileExpanded("a.json"))
    }

    @Test
    fun browseModeUsesExpandedFilesSet() {
        val snapshot = QuestionBankExpansionSnapshot(
            isSearchMode = false,
            expandedFiles = setOf("quiz.json")
        )
        assertTrue(snapshot.isFileExpanded("quiz.json"))
        assertFalse(snapshot.isFileExpanded("other.json"))
    }
}
