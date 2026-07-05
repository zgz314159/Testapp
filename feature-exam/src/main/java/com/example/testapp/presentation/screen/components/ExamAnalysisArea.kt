package com.example.testapp.presentation.screen.exam.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.model.Question
import com.example.testapp.feature.exam.R
import com.example.testapp.uicommon.design.AnalysisSectionTone
import com.example.testapp.uicommon.design.analysisSectionColors

@Composable
fun ExamAnalysisArea(
    question: Question,
    currentIndex: Int,
    selectedOption: List<Int>,
    textAnswer: String,
    questionFontSize: Float,
    showResult: Boolean,
    expandedSection: Int,
    onToggleSection: (Int) -> Unit,
    explanationScroll: androidx.compose.foundation.ScrollState,
    noteScroll: androidx.compose.foundation.ScrollState,
    deepSeekScroll: androidx.compose.foundation.ScrollState,
    sparkScroll: androidx.compose.foundation.ScrollState,
    baiduScroll: androidx.compose.foundation.ScrollState,
    noteList: List<String?>,
    analysisText: String?,
    sparkText: String?,
    baiduText: String?,
    questionTextForAi: String,
    onEditNote: (String, Int, Int) -> Unit,
    onViewDeepSeek: (String, Int, Int) -> Unit,
    onViewSpark: (String, Int, Int) -> Unit,
    onViewBaidu: (String, Int, Int) -> Unit,
    onViewExplanation: (String) -> Unit = {},
    onShowDeleteNoteDialog: () -> Unit,
    onSetDeleteTargetAndShow: (String) -> Unit
) {
    val analysisLabel = stringResource(R.string.analysis_prefix)
    if (showResult) {
        com.example.testapp.presentation.screen.exam.components.AnswerResultRow(
            question = question,
            selectedOption = selectedOption,
            textAnswer = textAnswer,
            questionFontSize = questionFontSize
        )
        if (question.explanation.isNotBlank()) {
            val collapsed = expandedSection != 0
            val explanationText = analysisLabel + question.explanation
            val explanationColors = analysisSectionColors(AnalysisSectionTone.Explanation)
            ExamExplanation(
                text = explanationText,
                collapsed = collapsed,
                scrollState = explanationScroll,
                backgroundColor = explanationColors.container,
                contentColor = explanationColors.content,
                onToggle = { onToggleSection(if (collapsed) 0 else -1) },
                onDoubleTap = { onViewExplanation(explanationText) }
            )
        }
    }
    if (showResult) {
        val note = noteList.getOrNull(currentIndex)
        if (!note.isNullOrBlank()) {
            val collapsed = expandedSection != 1
            val noteColors = analysisSectionColors(AnalysisSectionTone.Note)
            ExamNoteSection(
                text = stringResource(R.string.note_prefix) + note,
                collapsed = collapsed,
                scrollState = noteScroll,
                backgroundColor = noteColors.container,
                contentColor = noteColors.content,
                onToggle = { onToggleSection(if (collapsed) 1 else -1) },
                onDoubleTap = { val noteText = note.takeIf { it.isNotBlank() } ?: " "; onEditNote(noteText, question.id, currentIndex) },
                onLongPress = { onShowDeleteNoteDialog() }
            )
        }
        if (!analysisText.isNullOrBlank() || !sparkText.isNullOrBlank() || !baiduText.isNullOrBlank()) {
            if (!analysisText.isNullOrBlank()) {
                val collapsed = expandedSection != 2
                val deepSeekColors = analysisSectionColors(AnalysisSectionTone.DeepSeek)
                ExamAIResults(
                    text = analysisText,
                    collapsed = collapsed,
                    scrollState = deepSeekScroll,
                    backgroundColor = deepSeekColors.container,
                    contentColor = deepSeekColors.content,
                    onToggle = { onToggleSection(if (collapsed) 2 else -1) },
                    onDoubleTap = { onViewDeepSeek(questionTextForAi, question.id, currentIndex) },
                    onLongPress = { onSetDeleteTargetAndShow("deepseek") }
                )
            }
            if (!sparkText.isNullOrBlank()) {
                val collapsed = expandedSection != 3
                val sparkColors = analysisSectionColors(AnalysisSectionTone.Spark)
                ExamAIResults(
                    text = sparkText,
                    collapsed = collapsed,
                    scrollState = sparkScroll,
                    backgroundColor = sparkColors.container,
                    contentColor = sparkColors.content,
                    onToggle = { onToggleSection(if (collapsed) 3 else -1) },
                    onDoubleTap = { onViewSpark(sparkText, question.id, currentIndex) },
                    onLongPress = { onSetDeleteTargetAndShow("spark") }
                )
            }
            if (!baiduText.isNullOrBlank()) {
                val collapsed = expandedSection != 4
                val baiduColors = analysisSectionColors(AnalysisSectionTone.Baidu)
                ExamAIResults(
                    text = baiduText,
                    collapsed = collapsed,
                    scrollState = baiduScroll,
                    backgroundColor = baiduColors.container,
                    contentColor = baiduColors.content,
                    onToggle = { onToggleSection(if (collapsed) 4 else -1) },
                    onDoubleTap = { onViewBaidu(baiduText, question.id, currentIndex) },
                    onLongPress = { onSetDeleteTargetAndShow("baidu") }
                )
            }
        }
    } else {
        Spacer(modifier = Modifier.height(16.dp))
    }
}
