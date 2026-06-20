package com.example.testapp.presentation.screen

import com.example.testapp.core.common.buildPracticeProgressId
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.presentation.screen.practice.buildHomePracticeProgressMap
import com.example.testapp.presentation.screen.practice.homePracticeProgressCount
import com.example.testapp.presentation.screen.practice.preferredHomePracticeProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {
    @Test
    fun `homePracticeProgressCount prefers persisted question state map over legacy answered list`() {
        val progress = PracticeProgress(
            id = "practice_file1.xlsx",
            currentIndex = 0,
            answeredList = emptyList(),
            selectedOptions = emptyList(),
            showResultList = emptyList(),
            analysisList = emptyList(),
            noteList = emptyList(),
            timestamp = 1L,
            questionStateMap = mapOf(
                1 to UnifiedQuestionState(questionId = 1, selectedOptions = listOf(0), showResult = true),
                2 to UnifiedQuestionState(questionId = 2, textAnswer = "答案2", showResult = true),
                3 to UnifiedQuestionState(questionId = 3)
            )
        )

        val count = homePracticeProgressCount(
            fileName = "file1.xlsx",
            progressById = mapOf(progress.id to progress)
        )

        assertEquals(2, count)
    }

    @Test
    fun `homePracticeProgressCount falls back to answered list for legacy progress`() {
        val progress = PracticeProgress(
            id = "practice_file1.xlsx",
            currentIndex = 0,
            answeredList = listOf(0, 2, 4),
            selectedOptions = emptyList(),
            showResultList = emptyList(),
            analysisList = emptyList(),
            noteList = emptyList(),
            timestamp = 1L,
            questionStateMap = emptyMap()
        )

        val count = homePracticeProgressCount(
            fileName = "file1.xlsx",
            progressById = mapOf(progress.id to progress)
        )

        assertEquals(3, count)
    }

    @Test
    fun `homePracticeProgressCount reads scoped practice entries for the same file`() {
        val progress = PracticeProgress(
            id = buildPracticeProgressId(
                id = "file1.xlsx",
                questionCount = 20,
                randomEnabled = true,
                memoryModeEnabled = false,
                memoryBatchSize = 0,
                memoryWrongMode = 0,
                memoryPoolMode = 0
            ),
            currentIndex = 0,
            answeredList = emptyList(),
            selectedOptions = emptyList(),
            showResultList = emptyList(),
            analysisList = emptyList(),
            noteList = emptyList(),
            timestamp = 1L,
            questionStateMap = mapOf(
                1 to UnifiedQuestionState(questionId = 1, selectedOptions = listOf(0), showResult = true),
                2 to UnifiedQuestionState(questionId = 2, textAnswer = "答案2", showResult = true)
            )
        )

        val count = homePracticeProgressCount(
            fileName = "file1.xlsx",
            progressById = mapOf(progress.id to progress)
        )

        assertEquals(2, count)
    }

    @Test
    fun `homePracticeProgressCount keeps highest answered count across scoped practice entries`() {
        val baseProgress = PracticeProgress(
            id = "practice_file1.xlsx",
            currentIndex = 0,
            answeredList = listOf(0),
            selectedOptions = emptyList(),
            showResultList = emptyList(),
            analysisList = emptyList(),
            noteList = emptyList(),
            timestamp = 1L,
            questionStateMap = emptyMap()
        )
        val scopedProgress = PracticeProgress(
            id = buildPracticeProgressId(
                id = "file1.xlsx",
                questionCount = 10,
                randomEnabled = false,
                memoryModeEnabled = true,
                memoryBatchSize = 5,
                memoryWrongMode = 1,
                memoryPoolMode = 0
            ),
            currentIndex = 0,
            answeredList = listOf(0, 1, 2),
            selectedOptions = emptyList(),
            showResultList = emptyList(),
            analysisList = emptyList(),
            noteList = emptyList(),
            timestamp = 2L,
            questionStateMap = emptyMap()
        )

        val count = homePracticeProgressCount(
            fileName = "file1.xlsx",
            progressById = mapOf(baseProgress.id to baseProgress, scopedProgress.id to scopedProgress)
        )

        assertEquals(3, count)
    }

    @Test
    fun `buildHomePracticeProgressMap skips zero progress entries`() {
        val progress = PracticeProgress(
            id = "practice_file1.xlsx",
            currentIndex = 0,
            answeredList = listOf(0, 1),
            selectedOptions = emptyList(),
            showResultList = emptyList(),
            analysisList = emptyList(),
            noteList = emptyList(),
            timestamp = 1L,
            questionStateMap = emptyMap()
        )

        val progressMap = buildHomePracticeProgressMap(
            fileNames = listOf("file1.xlsx", "file2.xlsx"),
            progressById = mapOf(progress.id to progress)
        )

        assertEquals(mapOf("file1.xlsx" to 2), progressMap)
    }

    @Test
    fun `buildHomePracticeProgressMap stays empty when no file has progress`() {
        val progressMap = buildHomePracticeProgressMap(
            fileNames = listOf("file1.xlsx", "file2.xlsx"),
            progressById = emptyMap()
        )

        assertEquals(emptyMap<String, Int>(), progressMap)
    }

    @Test
    fun `preferredHomePracticeProgress falls back to the latest scoped entry when base progress is absent`() {
        val olderScopedProgress = PracticeProgress(
            id = buildPracticeProgressId(
                id = "file1.xlsx",
                questionCount = 10,
                randomEnabled = true,
                memoryModeEnabled = false,
                memoryBatchSize = 0,
                memoryWrongMode = 0,
                memoryPoolMode = 0
            ),
            currentIndex = 0,
            answeredList = listOf(0),
            selectedOptions = emptyList(),
            showResultList = emptyList(),
            analysisList = emptyList(),
            noteList = emptyList(),
            timestamp = 1L,
            questionStateMap = emptyMap()
        )
        val latestScopedProgress = PracticeProgress(
            id = buildPracticeProgressId(
                id = "file1.xlsx",
                questionCount = 20,
                randomEnabled = false,
                memoryModeEnabled = false,
                memoryBatchSize = 0,
                memoryWrongMode = 0,
                memoryPoolMode = 0
            ),
            currentIndex = 0,
            answeredList = listOf(0, 1),
            selectedOptions = emptyList(),
            showResultList = emptyList(),
            analysisList = emptyList(),
            noteList = emptyList(),
            timestamp = 5L,
            questionStateMap = emptyMap()
        )

        val selected = preferredHomePracticeProgress(
            fileName = "file1.xlsx",
            progressById = mapOf(
                olderScopedProgress.id to olderScopedProgress,
                latestScopedProgress.id to latestScopedProgress
            )
        )

        assertEquals(latestScopedProgress.id, selected?.id)
    }
}
