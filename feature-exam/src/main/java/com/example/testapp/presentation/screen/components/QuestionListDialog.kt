package com.example.testapp.presentation.screen.exam.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.feature.exam.R
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.uicommon.component.AnswerCardGrid
import com.example.testapp.uicommon.component.AnswerCardStateBuilder
import com.example.testapp.uicommon.component.CollapsibleAnswerCardSection
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun QuestionListDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    questions: List<com.example.testapp.domain.model.Question>,
    selectedOptions: List<List<Int>>,
    textAnswers: List<String>,
    showResultList: List<Boolean>,
    displayInfoByQuestionId: Map<Int, AnswerCardDisplayInfo> = emptyMap(),
    currentIndex: Int = -1,
    onSelect: (Int) -> Unit
) {
    if (!show) return

    var sectionCollapsed by remember { mutableStateOf(emptySet<String>()) }
    fun toggleSection(name: String) {
        sectionCollapsed = if (name in sectionCollapsed) sectionCollapsed - name else sectionCollapsed + name
    }
    AlertDialog(onDismissRequest = { onDismiss() }, confirmButton = {}, text = {
    val singleIndices = remember(questions, showResultList, displayInfoByQuestionId) {
            questions.mapIndexedNotNull { i, q -> if (QuestionTypes.isSingle(q.type)) i else null }
                .sortedWith(compareByDescending<Int> { showResultList.getOrElse(it) { false } }.thenBy { displayInfoByQuestionId[questions[it].id]?.order ?: it })
        }
        val multiIndices = remember(questions, showResultList, displayInfoByQuestionId) {
            questions.mapIndexedNotNull { i, q -> if (QuestionTypes.isMulti(q.type)) i else null }
                .sortedWith(compareByDescending<Int> { showResultList.getOrElse(it) { false } }.thenBy { displayInfoByQuestionId[questions[it].id]?.order ?: it })
        }
        val judgeIndices = remember(questions, showResultList, displayInfoByQuestionId) {
            questions.mapIndexedNotNull { i, q -> if (QuestionTypes.isJudge(q.type)) i else null }
                .sortedWith(compareByDescending<Int> { showResultList.getOrElse(it) { false } }.thenBy { displayInfoByQuestionId[questions[it].id]?.order ?: it })
        }
        val fillIndices = remember(questions, showResultList, displayInfoByQuestionId) {
            questions.mapIndexedNotNull { i, q -> if (QuestionTypes.isFill(q.type)) i else null }
                .sortedWith(compareByDescending<Int> { showResultList.getOrElse(it) { false } }.thenBy { displayInfoByQuestionId[questions[it].id]?.order ?: it })
        }

        val singleItems = remember(singleIndices, questions, selectedOptions, textAnswers, showResultList, displayInfoByQuestionId, currentIndex) {
            AnswerCardStateBuilder.build(singleIndices, questions, selectedOptions, textAnswers, showResultList, displayInfoByQuestionId, currentIndex = currentIndex)
        }
        val multiItems = remember(multiIndices, questions, selectedOptions, textAnswers, showResultList, displayInfoByQuestionId, currentIndex) {
            AnswerCardStateBuilder.build(multiIndices, questions, selectedOptions, textAnswers, showResultList, displayInfoByQuestionId, currentIndex = currentIndex)
        }
        val judgeItems = remember(judgeIndices, questions, selectedOptions, textAnswers, showResultList, displayInfoByQuestionId, currentIndex) {
            AnswerCardStateBuilder.build(judgeIndices, questions, selectedOptions, textAnswers, showResultList, displayInfoByQuestionId, currentIndex = currentIndex)
        }
        val fillItems = remember(fillIndices, questions, selectedOptions, textAnswers, showResultList, displayInfoByQuestionId, currentIndex) {
            AnswerCardStateBuilder.build(fillIndices, questions, selectedOptions, textAnswers, showResultList, displayInfoByQuestionId, currentIndex = currentIndex)
        }

        LazyColumn(
            modifier = Modifier.heightIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (singleItems.isNotEmpty()) {
                item {
                    CollapsibleAnswerCardSection(
                        label = stringResource(R.string.single_choice),
                        collapsed = "single" in sectionCollapsed,
                        onToggle = { toggleSection("single") },
                        items = singleItems,
                        onClick = { onSelect(it); onDismiss() }
                    )
                }
            }
            if (multiItems.isNotEmpty()) {
                item {
                    CollapsibleAnswerCardSection(
                        label = stringResource(R.string.multi_choice),
                        collapsed = "multi" in sectionCollapsed,
                        onToggle = { toggleSection("multi") },
                        items = multiItems,
                        onClick = { onSelect(it); onDismiss() }
                    )
                }
            }
            if (judgeItems.isNotEmpty()) {
                item {
                    CollapsibleAnswerCardSection(
                        label = stringResource(R.string.judge_choice),
                        collapsed = "judge" in sectionCollapsed,
                        onToggle = { toggleSection("judge") },
                        items = judgeItems,
                        onClick = { onSelect(it); onDismiss() }
                    )
                }
            }
            if (fillItems.isNotEmpty()) {
                item {
                    CollapsibleAnswerCardSection(
                        label = stringResource(R.string.fill_blank),
                        collapsed = "fill" in sectionCollapsed,
                        onToggle = { toggleSection("fill") },
                        items = fillItems,
                        onClick = { onSelect(it); onDismiss() }
                    )
                }
            }
        }
    })
}
