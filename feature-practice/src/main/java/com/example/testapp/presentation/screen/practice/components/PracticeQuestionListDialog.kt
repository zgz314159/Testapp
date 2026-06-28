package com.example.testapp.presentation.screen.practice.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.model.Question
import com.example.testapp.feature.practice.R
import com.example.testapp.uicommon.component.AnswerCardDialogContent
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.uicommon.component.AnswerCardTypeLabels

@Composable
fun PracticeQuestionListDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    questions: List<Question>,
    selectedOptions: List<List<Int>>,
    textAnswers: List<String>,
    showResultList: List<Boolean>,
    displayInfoByQuestionId: Map<Int, AnswerCardDisplayInfo>,
    entryGrouped: Boolean,
    currentIndex: Int,
    onSelect: (Int) -> Unit
) {
    if (!show) return

    var sectionCollapsed by remember { mutableStateOf(emptySet<String>()) }
    fun toggleSection(name: String) {
        sectionCollapsed = if (name in sectionCollapsed) sectionCollapsed - name else sectionCollapsed + name
    }

    AlertDialog(onDismissRequest = onDismiss, confirmButton = {}, text = {
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
                fill = stringResource(R.string.fill_blank),
                text = stringResource(R.string.short_answer)
            ),
            modifier = Modifier.heightIn(max = 500.dp)
        )
    })
}
