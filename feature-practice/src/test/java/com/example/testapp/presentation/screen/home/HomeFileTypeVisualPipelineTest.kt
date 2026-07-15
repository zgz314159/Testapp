package com.example.testapp.presentation.screen.home

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.domain.usecase.QuestionTypeStat
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFileTypeVisualPipelineTest {

    @Test
    fun resolveKind_usesImportedFileTypeWhenAvailable() {
        assertEquals(
            HomeQuestionBankVisualKind.Spreadsheet,
            HomeFileTypeVisualPipeline.resolveKind("（旧版）技师题库.xlsx", stats(QuestionTypes.SINGLE)),
        )
        assertEquals(
            HomeQuestionBankVisualKind.Document,
            HomeFileTypeVisualPipeline.resolveKind("铁路法规.docx", stats(QuestionTypes.SINGLE)),
        )
        assertEquals(
            HomeQuestionBankVisualKind.Database,
            HomeFileTypeVisualPipeline.resolveKind("本地题库.sqlite", FileStatistics()),
        )
    }

    @Test
    fun resolveKind_usesExplicitQuestionTypeInTitleBeforeFileType() {
        assertEquals(
            HomeQuestionBankVisualKind.Fill,
            HomeFileTypeVisualPipeline.resolveKind("集团题库（填空题）.xlsx", stats(QuestionTypes.BLANK)),
        )
        assertEquals(
            HomeQuestionBankVisualKind.Drawing,
            HomeFileTypeVisualPipeline.resolveKind("高级技师绘图题.docx", stats("DRAWING")),
        )
    }

    @Test
    fun resolveKind_usesActualQuestionStatisticsWithoutFileExtension() {
        assertEquals(
            HomeQuestionBankVisualKind.Judge,
            HomeFileTypeVisualPipeline.resolveKind("网络安全题库", stats(QuestionTypes.JUDGE)),
        )
        assertEquals(
            HomeQuestionBankVisualKind.MultipleChoice,
            HomeFileTypeVisualPipeline.resolveKind("安规练习", stats(QuestionTypes.MULTI)),
        )
    }

    @Test
    fun resolveKind_marksQuestionBanksWithSeveralTypesAsMixed() {
        val statistics = FileStatistics(
            primaryQuestionType = QuestionTypes.SINGLE,
            questionTypeStats = listOf(
                QuestionTypeStat(QuestionTypes.SINGLE, 8),
                QuestionTypeStat(QuestionTypes.JUDGE, 4),
            ),
        )

        assertEquals(
            HomeQuestionBankVisualKind.Mixed,
            HomeFileTypeVisualPipeline.resolveKind("综合题库", statistics),
        )
    }

    private fun stats(type: String): FileStatistics = FileStatistics(
        primaryQuestionType = type,
        questionTypeStats = listOf(QuestionTypeStat(type, 1)),
    )
}
