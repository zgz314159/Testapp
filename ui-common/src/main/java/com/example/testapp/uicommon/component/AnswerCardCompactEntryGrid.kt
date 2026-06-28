package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnswerCardCompactEntryGrid(
    rows: List<AnswerCardEntryCompactLayout.EntryRow>,
    expandedEntryOrder: Int?,
    onEntrySingleClick: (AnswerCardEntryCompactLayout.EntryRow) -> Unit,
    onEntryDoubleClick: (AnswerCardEntryCompactLayout.EntryRow) -> Unit,
    onRoundDoubleClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayLines = remember(rows, expandedEntryOrder) {
        AnswerCardEntryGridLines.build(rows, expandedEntryOrder)
    }

    LazyColumn(
        modifier = modifier.heightIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(displayLines, key = { line ->
            when (line) {
                is AnswerCardEntryGridLines.DisplayLine.EntryLine ->
                    line.entries.joinToString("_") { it.section.sectionKey }
                is AnswerCardEntryGridLines.DisplayLine.RoundLine ->
                    "rounds_${line.entryOrder}_${line.startColumn}_${line.rounds.firstOrNull()?.index}"
            }
        }) { line ->
            when (line) {
                is AnswerCardEntryGridLines.DisplayLine.EntryLine -> {
                    AnswerCardEntryLineRow(
                        entries = line.entries,
                        expandedEntryOrder = expandedEntryOrder,
                        onEntrySingleClick = onEntrySingleClick,
                        onEntryDoubleClick = onEntryDoubleClick
                    )
                }
                is AnswerCardEntryGridLines.DisplayLine.RoundLine -> {
                    AnswerCardRoundLineRow(
                        startColumn = line.startColumn,
                        rounds = line.rounds,
                        onRoundDoubleClick = onRoundDoubleClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerCardEntryLineRow(
    entries: List<AnswerCardEntryCompactLayout.EntryRow>,
    expandedEntryOrder: Int?,
    onEntrySingleClick: (AnswerCardEntryCompactLayout.EntryRow) -> Unit,
    onEntryDoubleClick: (AnswerCardEntryCompactLayout.EntryRow) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        entries.forEach { entry ->
            val multiRound = entry.rounds.size > 1
            val expanded = expandedEntryOrder == entry.section.entryOrder && multiRound
            AnswerCardEntryCell(
                item = entry.collapsed,
                multiRound = multiRound,
                expanded = expanded,
                onSingleClick = {
                    if (multiRound) onEntrySingleClick(entry)
                },
                onDoubleClick = { onEntryDoubleClick(entry) },
                modifier = Modifier.weight(1f)
            )
        }
        repeat(AnswerCardEntryGridLines.COLUMNS - entries.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AnswerCardRoundLineRow(
    startColumn: Int,
    rounds: List<AnswerCardItemState>,
    onRoundDoubleClick: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        repeat(AnswerCardEntryGridLines.COLUMNS) { column ->
            val roundIndex = column - startColumn
            if (roundIndex in rounds.indices) {
                val round = rounds[roundIndex]
                AnswerCardRoundCell(
                    item = round,
                    onDoubleClick = { onRoundDoubleClick(round.index) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
