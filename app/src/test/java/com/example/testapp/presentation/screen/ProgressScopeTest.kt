package com.example.testapp.presentation.screen

import com.example.testapp.core.common.buildExamProgressId
import com.example.testapp.core.common.buildPracticeProgressId
import com.example.testapp.core.common.practiceProgressBaseId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressScopeTest {
    @Test
    fun `buildPracticeProgressId keeps default practice sessions on the base id`() {
        assertEquals(
            "practice_file1",
            buildPracticeProgressId(
                id = "file1",
                questionCount = 0,
                randomEnabled = false,
                memoryModeEnabled = false,
                memoryBatchSize = 0,
                memoryWrongMode = 0,
                memoryPoolMode = 0
            )
        )
    }

    @Test
    fun `buildExamProgressId scopes random limited exam sessions`() {
        val progressId = buildExamProgressId(
            id = "file1",
            questionCount = 30,
            randomEnabled = true,
            memoryModeEnabled = false,
            memoryBatchSize = 0,
            memoryWrongMode = 0,
            memoryPoolMode = 0
        )

        assertTrue(progressId.startsWith("exam_file1__scope="))
        assertTrue(progressId.contains("q=30"))
        assertTrue(progressId.contains("r=1"))
    }

    @Test
    fun `practiceProgressBaseId strips an existing scope suffix`() {
        assertEquals(
            "practice_file1",
            practiceProgressBaseId("practice_file1__scope=q=20;r=1")
        )
    }
}
