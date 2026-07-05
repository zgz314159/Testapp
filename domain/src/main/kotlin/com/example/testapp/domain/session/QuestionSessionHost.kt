package com.example.testapp.domain.session

import kotlinx.coroutines.flow.StateFlow

/** 薄 Host 契约（P2b 落地）；仅 enter / leave / session 流 */
interface QuestionSessionHost {
    val session: StateFlow<QuestionSession?>

    suspend fun enter(kind: QuestionSessionKind)
    suspend fun leave()
}
