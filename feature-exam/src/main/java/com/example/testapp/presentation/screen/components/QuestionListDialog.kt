package com.example.testapp.presentation.screen.exam.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.model.Question
import com.example.testapp.feature.exam.R
import com.example.testapp.presentation.screen.exam.ExamPipelineLog
import com.example.testapp.uicommon.component.AnswerCardDialogContent
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.uicommon.component.AnswerCardListDialogShell
import com.example.testapp.uicommon.component.AnswerCardTypeLabels

@Composable
fun QuestionListDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    questions: List<Question>,
    selectedOptions: List<List<Int>>,
    textAnswers: List<String>,
    showResultList: List<Boolean>,
    answerTimes: List<Long> = emptyList(),
    displayInfoByQuestionId: Map<Int, AnswerCardDisplayInfo> = emptyMap(),
    entryGrouped: Boolean = false,
    currentIndex: Int = -1,
    onSelect: (Int) -> Unit
) {
    if (!show) return

    var sectionCollapsed by remember { mutableStateOf(emptySet<String>()) }
    fun toggleSection(name: String) {
        sectionCollapsed = if (name in sectionCollapsed) sectionCollapsed - name else sectionCollapsed + name
    }

    val sortIndices: (List<Int>) -> List<Int> = remember(questions, answerTimes, selectedOptions, textAnswers, displayInfoByQuestionId) {
        { indices ->
            indices.sortedWith(
                answeredFirstByTime(questions, answerTimes, selectedOptions, textAnswers, displayInfoByQuestionId)
            ).also { sorted ->
                if (indices.isNotEmpty()) ExamPipelineLog.sort("answerCard", sorted, answerTimes)
            }
        }
    }

    AnswerCardListDialogShell(onDismiss = onDismiss) {
        AnswerCardDialogContent(
            questions = questions,
            selectedOptions = selectedOptions,
            textAnswers = textAnswers,
            showResultList = showResultList,
            displayInfoByQuestionId = displayInfoByQuestionId,
            entryGrouped = entryGrouped,
            currentIndex = currentIndex,
            collapsedSections = sectionCollapsed,
            onToggleSection = ::toggleSection,
            onSelect = { index ->
                onSelect(index)
                onDismiss()
            },
            typeLabels = AnswerCardTypeLabels(
                single = stringResource(R.string.single_choice),
                multi = stringResource(R.string.multi_choice),
                judge = stringResource(R.string.judge_choice),
                fill = stringResource(R.string.fill_blank)
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            sortIndices = sortIndices
        )
    }
}

private fun answeredFirstByTime(
    questions: List<Question>,
    answerTimes: List<Long>,
    selectedOptions: List<List<Int>>,
    textAnswers: List<String>,
    displayInfoByQuestionId: Map<Int, AnswerCardDisplayInfo>
): Comparator<Int> {
    fun isAnswered(idx: Int): Boolean {
        val q = questions.getOrNull(idx) ?: return false
        return if (com.example.testapp.domain.QuestionTypes.isFill(q.type)) {
            textAnswers.getOrElse(idx) { "" }.isNotBlank()
        } else {
            selectedOptions.getOrElse(idx) { emptyList() }.isNotEmpty()
        }
    }
    fun order(idx: Int): Int = displayInfoByQuestionId[questions.getOrNull(idx)?.id]?.order ?: idx
    fun sortKey(idx: Int): Long {
        if (!isAnswered(idx)) return Long.MAX_VALUE - order(idx)
        val t = answerTimes.getOrElse(idx) { 0L }
        return if (t > 0L) t else order(idx).toLong()
    }
    return compareByDescending<Int> { isAnswered(it) }
        .thenByDescending { sortKey(it) }
}
