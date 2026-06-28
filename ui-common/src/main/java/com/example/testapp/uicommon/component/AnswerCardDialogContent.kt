package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question

private data class AnswerCardTypeSection(
    val key: String,
    val label: String,
    val indices: List<Int>
)

@Composable
fun AnswerCardDialogContent(
    questions: List<Question>,
    selectedOptions: List<List<Int>>,
    textAnswers: List<String>,
    showResultList: List<Boolean>,
    displayInfoByQuestionId: Map<Int, AnswerCardDisplayInfo>,
    entryGrouped: Boolean,
    currentIndex: Int,
    collapsedSections: Set<String>,
    onToggleSection: (String) -> Unit,
    onSelect: (Int) -> Unit,
    typeLabels: AnswerCardTypeLabels,
    modifier: Modifier = Modifier,
    sortIndices: (List<Int>) -> List<Int> = { it }
) {
    var expandedEntryOrder by remember(entryGrouped, questions) { mutableIntStateOf(-1) }

    val entryRows = remember(
        questions,
        displayInfoByQuestionId,
        selectedOptions,
        textAnswers,
        showResultList,
        currentIndex,
        entryGrouped
    ) {
        if (!entryGrouped) emptyList()
        else AnswerCardEntryCompactLayout.buildRows(
            questions,
            displayInfoByQuestionId,
            selectedOptions,
            textAnswers,
            showResultList,
            currentIndex
        )
    }

    val typeSections = remember(questions, typeLabels, entryGrouped) {
        if (entryGrouped) emptyList()
        else buildTypeSections(questions, typeLabels)
    }

    if (entryGrouped) {
        AnswerCardCompactEntryGrid(
            rows = entryRows,
            expandedEntryOrder = expandedEntryOrder.takeIf { it > 0 },
            onEntrySingleClick = { row ->
                if (row.rounds.size > 1) {
                    expandedEntryOrder = if (expandedEntryOrder == row.section.entryOrder) {
                        -1
                    } else {
                        row.section.entryOrder
                    }
                }
            },
            onEntryDoubleClick = { row -> onSelect(row.collapsed.index) },
            onRoundDoubleClick = onSelect,
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(typeSections, key = { it.key }) { section ->
            val cardItems = AnswerCardStateBuilder.build(
                sortIndices(section.indices),
                questions,
                selectedOptions,
                textAnswers,
                showResultList,
                displayInfoByQuestionId,
                currentIndex = currentIndex
            )
            if (cardItems.isNotEmpty()) {
                CollapsibleAnswerCardSection(
                    label = section.label,
                    collapsed = section.key in collapsedSections,
                    onToggle = { onToggleSection(section.key) },
                    items = cardItems,
                    onClick = onSelect
                )
            }
        }
    }
}

private fun buildTypeSections(
    questions: List<Question>,
    typeLabels: AnswerCardTypeLabels
): List<AnswerCardTypeSection> = listOfNotNull(
    AnswerCardTypeSection(
        key = "single",
        label = typeLabels.single,
        indices = questions.mapIndexedNotNull { index, question ->
            if (QuestionTypes.isSingle(question.type)) index else null
        }
    ).takeIf { it.indices.isNotEmpty() },
    AnswerCardTypeSection(
        key = "multi",
        label = typeLabels.multi,
        indices = questions.mapIndexedNotNull { index, question ->
            if (QuestionTypes.isMulti(question.type)) index else null
        }
    ).takeIf { it.indices.isNotEmpty() },
    AnswerCardTypeSection(
        key = "judge",
        label = typeLabels.judge,
        indices = questions.mapIndexedNotNull { index, question ->
            if (QuestionTypes.isJudge(question.type)) index else null
        }
    ).takeIf { it.indices.isNotEmpty() },
    AnswerCardTypeSection(
        key = "fill",
        label = typeLabels.fill,
        indices = questions.mapIndexedNotNull { index, question ->
            if (QuestionTypes.isFill(question.type) || QuestionTypes.isInlineBlank(question.type)) index else null
        }
    ).takeIf { it.indices.isNotEmpty() },
    typeLabels.text?.let { textLabel ->
        AnswerCardTypeSection(
            key = "text",
            label = textLabel,
            indices = questions.mapIndexedNotNull { index, question ->
                if (QuestionTypes.isTextResponse(question.type)) index else null
            }
        ).takeIf { it.indices.isNotEmpty() }
    }
)
