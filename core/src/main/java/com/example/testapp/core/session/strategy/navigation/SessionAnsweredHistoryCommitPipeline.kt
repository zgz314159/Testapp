package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.session.navigation.SessionAnsweredHistoryNavigationUpdate

/** 已答历史 commit / forward-miss 判定（从 NavigationHistory 收编） */
object SessionAnsweredHistoryCommitPipeline {
    fun navigationUpdate(
        originIndex: Int,
        orderedIndices: List<Int>,
        targetIndex: Int,
        anchorPoolIndices: Set<Int>,
    ): SessionAnsweredHistoryNavigationUpdate =
        SessionAnsweredHistoryNavigationUpdate(
            originIndex = originIndex,
            historyPosition =
                SessionAnsweredHistoryBrowsePipeline.historyPositionForTarget(
                    orderedIndices,
                    targetIndex,
                ),
            orderedIndices = orderedIndices,
            anchorPoolIndices = anchorPoolIndices,
        )

    fun shouldResumeLiveOnForwardMiss(inActiveHistoryMode: Boolean): Boolean = inActiveHistoryMode
}
