package com.example.testapp.domain.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionCapabilitiesPresetsTest {
    @Test
    fun browse_disablesSubmitAndPersist() {
        val caps = SessionCapabilitiesPresets.browse
        assertFalse(caps.canSubmit)
        assertFalse(caps.canPersistProgress)
        assertFalse(caps.canRestoreProgress)
    }

    @Test
    fun forKind_mapsBrowse() {
        val kind = QuestionSessionKind.Browse("a.json", targetQuestionId = 1)
        assertEquals(SessionCapabilitiesPresets.browse, SessionCapabilitiesPresets.forKind(kind))
    }

    @Test
    fun forKind_mapsQuestionEdit() {
        val kind = QuestionSessionKind.QuestionEdit("a.json", questionId = 42)
        assertEquals(SessionCapabilitiesPresets.questionEdit, SessionCapabilitiesPresets.forKind(kind))
        assertTrue(SessionCapabilitiesPresets.forKind(kind).canEditQuestion)
    }
}
