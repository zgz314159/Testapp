package com.example.testapp.presentation.screen.home

import com.example.testapp.domain.usecase.FileStatistics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeDashboardPipelineTest {

    private val pipeline = HomeDashboardPipeline

    @Test
    fun `resolveSubtitle returns learning prompt`() {
        assertEquals("从今天的题库开始吧", pipeline.resolveSubtitle())
    }

    @Test
    fun `reorderFileNames puts storedFileName first`() {
        val names = listOf("a.txt", "b.txt", "c.txt")
        val result = pipeline.reorderFileNames(names, "b.txt", listOf("c.txt"))
        assertEquals("b.txt", result.first())
        assertTrue(result.containsAll(names))
    }

    @Test
    fun `buildDashboard with empty inputs returns defaults`() {
        val state = pipeline.buildDashboard(
            fileNames = emptyList(),
            fileStatistics = emptyMap(),
            practiceProgressCompleted = emptyMap(),
            storedFileName = "",
        )
        assertEquals("", state.continueFileName)
        assertEquals(false, state.showContinueCard)
        assertEquals(0, state.totalQuestions)
    }

    @Test
    fun `buildDashboard with single file`() {
        val stats = mapOf(
            "test.txt" to FileStatistics(
                questionCount = 50,
                wrongCount = 5,
                favoriteCount = 3,
            ),
        )
        val state = pipeline.buildDashboard(
            fileNames = listOf("test.txt"),
            fileStatistics = stats,
            practiceProgressCompleted = mapOf("test.txt" to 10),
            storedFileName = "test.txt",
        )
        assertTrue(state.showContinueCard)
        assertEquals("test.txt", state.continueFileName)
        assertEquals(50, state.totalQuestions)
        assertEquals(5, state.wrongCount)
        assertEquals(3, state.favoriteCount)
        assertEquals(20, state.continueProgressPercent)
    }

    @Test
    fun `buildDashboard aggregates statistics`() {
        val stats = mapOf(
            "a.txt" to FileStatistics(questionCount = 10, wrongCount = 2, favoriteCount = 1),
            "b.txt" to FileStatistics(questionCount = 20, wrongCount = 3, favoriteCount = 5),
        )
        val state = pipeline.buildDashboard(
            fileNames = listOf("a.txt", "b.txt"),
            fileStatistics = stats,
            practiceProgressCompleted = emptyMap(),
            storedFileName = "",
        )
        assertEquals(30, state.totalQuestions)
        assertEquals(5, state.wrongCount)
        assertEquals(6, state.favoriteCount)
    }

    @Test
    fun `reorderFileNames handles missing storedFileName`() {
        val names = listOf("x.txt", "y.txt")
        val result = pipeline.reorderFileNames(names, "z.txt", listOf("y.txt"))
        assertEquals("y.txt", result.first())
        assertEquals(listOf("y.txt", "x.txt"), result)
    }

    @Test
    fun `reorderFileNames keeps all recent usage then original import order`() {
        val names = listOf("import-1", "import-2", "import-3", "import-4", "import-5")
        val result = pipeline.reorderFileNames(
            fileNames = names,
            storedFileName = "import-4",
            recentFileNames = listOf("import-4", "import-2", "import-5", "import-1"),
        )

        assertEquals(
            listOf("import-4", "import-2", "import-5", "import-1", "import-3"),
            result,
        )
    }

    @Test
    fun `reorderFileNames empty input`() {
        val result = pipeline.reorderFileNames(
            fileNames = emptyList(),
            storedFileName = "",
            recentFileNames = emptyList(),
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `resolveHomeColumnCount returns 1 for small screens`() {
        assertEquals(1, pipeline.resolveHomeColumnCount(360f))
        assertEquals(1, pipeline.resolveHomeColumnCount(599f))
    }

    @Test
    fun `resolveHomeColumnCount returns 2 for large screens`() {
        assertEquals(2, pipeline.resolveHomeColumnCount(600f))
        assertEquals(2, pipeline.resolveHomeColumnCount(960f))
    }

    @Test
    fun `resolveQuestionBankCardLayout uses dense for grid`() {
        assertEquals(
            HomeDashboardPipeline.QuestionBankCardLayout.Dense,
            pipeline.resolveQuestionBankCardLayout(useGridLayout = true, parentWidthDp = 900f),
        )
    }

    @Test
    fun `resolveQuestionBankCardLayout uses compact for narrow column`() {
        assertEquals(
            HomeDashboardPipeline.QuestionBankCardLayout.Compact,
            pipeline.resolveQuestionBankCardLayout(useGridLayout = false, parentWidthDp = 360f),
        )
    }

    @Test
    fun `progress percent is clamped to 0-100`() {
        val stats = mapOf("test.txt" to FileStatistics(questionCount = 0))
        val state = pipeline.buildDashboard(
            fileNames = listOf("test.txt"),
            fileStatistics = stats,
            practiceProgressCompleted = emptyMap(),
            storedFileName = "test.txt",
        )
        assertEquals(0, state.continueProgressPercent)
    }

    @Test
    fun `cleanupDisplayName_removes_txt_ext`() {
        assertEquals("hello", pipeline.cleanupDisplayName("hello.txt"))
        assertEquals("myfile", pipeline.cleanupDisplayName("myfile.json"))
    }

    @Test
    fun `cleanupDisplayName keeps generated suffixes so similar banks stay distinguishable`() {
        assertEquals("data_final", pipeline.cleanupDisplayName("data_final.txt"))
        assertEquals("notes_20260714", pipeline.cleanupDisplayName("notes_20260714.txt"))
        assertEquals("summary_v2", pipeline.cleanupDisplayName("summary_v2.json"))
        assertEquals(
            "高压电工完整题库（1347题）_final_2",
            pipeline.cleanupDisplayName("高压电工完整题库（1347题）_final_2.txt"),
        )
        assertEquals(
            "（旧版）技师计算题_20260620_1455",
            pipeline.cleanupDisplayName("（旧版）技师计算题_20260620_1455.xlsx"),
        )
    }
}
