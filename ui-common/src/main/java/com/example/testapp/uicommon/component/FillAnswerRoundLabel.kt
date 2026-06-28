package com.example.testapp.uicommon.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.testapp.core.util.extractDerivedFillQuestionRound
import com.example.testapp.core.util.extractSourceQuestionId

@Composable
fun FillAnswerRoundLabel(
    questionId: Int,
    sessionQuestionIds: List<Int>,
    modifier: Modifier = Modifier,
    content: @Composable (round: Int, total: Int) -> Unit
) {
    val fillRound = remember(questionId) { extractDerivedFillQuestionRound(questionId) }
    val fillRoundTotal = remember(questionId, sessionQuestionIds) {
        if (fillRound == null) null
        else sessionQuestionIds.count { extractSourceQuestionId(it) == extractSourceQuestionId(questionId) }
    }
    if (fillRound != null && (fillRoundTotal ?: 0) > 1) {
        content(fillRound, fillRoundTotal ?: 0)
    }
}
