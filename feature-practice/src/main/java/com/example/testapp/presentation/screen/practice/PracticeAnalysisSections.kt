package com.example.testapp.presentation.screen.practice

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.model.Question
import com.example.testapp.presentation.screen.components.ExamAnalysisSection
import com.example.testapp.presentation.screen.components.PracticeExplanationBox
import com.example.testapp.presentation.screen.components.PracticeNoteBox
import com.example.testapp.uicommon.design.AnalysisSectionTone
import com.example.testapp.uicommon.design.analysisSectionColors

@Composable
fun PracticeAnalysisSections(
    showResult: Boolean,
    question: Question,
    expandedSection: Int,
    explanationScroll: ScrollState,
    noteScroll: ScrollState,
    deepSeekScroll: ScrollState,
    sparkScroll: ScrollState,
    baiduScroll: ScrollState,
    analysisText: String?,
    sparkText: String?,
    baiduText: String?,
    note: String?,
    explanationPrefix: String,
    questionFontSize: Float,
    onSectionToggle: (Int) -> Unit,
    onDoubleTapNote: () -> Unit,
    onLongPressNote: () -> Unit,
    onDoubleTapDeepSeek: () -> Unit,
    onDoubleTapSpark: () -> Unit,
    onDoubleTapBaidu: () -> Unit,
    onLongPressDeepSeek: () -> Unit,
    onLongPressSpark: () -> Unit,
    onLongPressBaidu: () -> Unit
) {
    if (question.explanation.isNotBlank() && showResult) {
        val collapsed = expandedSection != 0
        PracticeExplanationBox(
            text = explanationPrefix + question.explanation,
            collapsed = collapsed,
            scrollStateProvider = { explanationScroll },
            questionFontSize = questionFontSize,
            onToggle = { onSectionToggle(if (collapsed) 0 else -1) }
        )
    }

    if (showResult && !note.isNullOrBlank()) {
        val collapsed = expandedSection != 1
        PracticeNoteBox(
            note = note,
            collapsed = collapsed,
            scrollStateProvider = { noteScroll },
            questionFontSize = questionFontSize,
            onToggle = { onSectionToggle(if (collapsed) 1 else -1) },
            onDoubleTap = onDoubleTapNote,
            onLongPress = onLongPressNote
        )
    }

    if (showResult && (!analysisText.isNullOrBlank() || !sparkText.isNullOrBlank() || !baiduText.isNullOrBlank())) {
        if (!analysisText.isNullOrBlank()) {
            val collapsed = expandedSection != 2
            ExamAnalysisSection(
                text = analysisText,
                collapsed = collapsed,
                scrollState = deepSeekScroll,
                backgroundColor = analysisSectionColors(AnalysisSectionTone.DeepSeek).container,
                onToggle = { onSectionToggle(if (collapsed) 2 else -1) },
                onDoubleTap = onDoubleTapDeepSeek,
                onLongPress = onLongPressDeepSeek
            )
        }
        if (!sparkText.isNullOrBlank()) {
            val collapsed = expandedSection != 3
            ExamAnalysisSection(
                text = sparkText,
                collapsed = collapsed,
                scrollState = sparkScroll,
                backgroundColor = analysisSectionColors(AnalysisSectionTone.Spark).container,
                onToggle = { onSectionToggle(if (collapsed) 3 else -1) },
                onDoubleTap = onDoubleTapSpark,
                onLongPress = onLongPressSpark
            )
        }
        if (!baiduText.isNullOrBlank()) {
            val collapsed = expandedSection != 4
            ExamAnalysisSection(
                text = baiduText,
                collapsed = collapsed,
                scrollState = baiduScroll,
                backgroundColor = analysisSectionColors(AnalysisSectionTone.Baidu).container,
                onToggle = { onSectionToggle(if (collapsed) 4 else -1) },
                onDoubleTap = onDoubleTapBaidu,
                onLongPress = onLongPressBaidu
            )
        }
    } else if (!showResult) {
        Spacer(modifier = Modifier.height(16.dp))
    }
}
