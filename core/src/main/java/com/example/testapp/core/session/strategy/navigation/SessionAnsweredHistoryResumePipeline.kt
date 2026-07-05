package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.model.PracticeSessionState

/** 已答历史退出浏览恢复现场（从 NavigationHistory 收编） */
object SessionAnsweredHistoryResumePipeline {
    fun resolveOriginIndex(
        originIndex: Int,
        questionCount: Int,
        fallbackIndex: Int,
    ): Int = originIndex.takeIf { it in 0 until questionCount } ?: fallbackIndex

    fun sessionAfterResume(
        restoredState: PracticeSessionState,
        originIndex: Int,
    ): PracticeSessionState =
        if (originIndex != restoredState.currentIndex) {
            restoredState.copy(currentIndex = originIndex)
        } else {
            restoredState
        }
}
