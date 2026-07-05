package com.example.testapp.presentation.screen.exam

import org.junit.Assert.assertEquals
import org.junit.Test

class ExamProgressIdPipelineTest {
    @Test
    fun ensurePrefix_addsWhenMissing() {
        assertEquals("exam_quiz1", ExamProgressIdPipeline.ensurePrefix("quiz1"))
    }

    @Test
    fun ensurePrefix_keepsExisting() {
        assertEquals("exam_quiz1", ExamProgressIdPipeline.ensurePrefix("exam_quiz1"))
    }

    @Test
    fun ensurePrefix_preservesScopedId() {
        val scoped = "exam_quiz1__scope=q=30;r=1"
        assertEquals(scoped, ExamProgressIdPipeline.ensurePrefix(scoped))
    }
}
