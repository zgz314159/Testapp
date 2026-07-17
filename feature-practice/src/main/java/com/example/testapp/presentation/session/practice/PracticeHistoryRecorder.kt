package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun recordPracticeHistory(
    scope: CoroutineScope,
    facade: PracticeUseCaseFacade,
    sourceId: String,
    score: Int,
    total: Int,
    unanswered: Int,
    enabled: Boolean,
) {
    if (!enabled || total <= unanswered) return
    scope.launch {
        facade.history.add(HistoryRecord(score, total, unanswered, "practice_$sourceId"))
    }
}
