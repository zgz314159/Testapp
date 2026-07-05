package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.session.navigation.SessionAnsweredHistoryBrowseContext

/** 已答历史 browse 上下文解析（从 NavigationHistory 收编） */
object SessionAnsweredHistoryBrowseContextPipeline {
    fun resolve(
        liveCurrentIndex: Int,
        activeOriginIndex: Int?,
        activeHistoryPosition: Int?,
        activeOrderedIndices: List<Int>?,
        activeAnchorPoolIndices: Set<Int>?,
        fullAnswerModeActive: Boolean,
        idleSourcePoolIndices: Set<Int>,
        idleOrderedIndices: List<Int>,
    ): SessionAnsweredHistoryBrowseContext =
        if (activeOriginIndex != null && activeOrderedIndices != null) {
            SessionAnsweredHistoryBrowseContext(
                originIndex = activeOriginIndex,
                anchorPoolIndices = activeAnchorPoolIndices ?: emptySet(),
                orderedIndices = activeOrderedIndices,
                activeHistoryPosition = activeHistoryPosition,
                inActiveHistoryMode = true,
            )
        } else {
            SessionAnsweredHistoryBrowseContext(
                originIndex = liveCurrentIndex,
                anchorPoolIndices = if (fullAnswerModeActive) idleSourcePoolIndices else emptySet(),
                orderedIndices = idleOrderedIndices,
                activeHistoryPosition = null,
                inActiveHistoryMode = false,
            )
        }
}
