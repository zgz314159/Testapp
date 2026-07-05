package com.example.testapp.domain.session.navigation

/** 已答历史滑动上下文（Strategy 层快照） */
data class SessionAnsweredHistoryBrowseContext(
    val originIndex: Int,
    val anchorPoolIndices: Set<Int>,
    val orderedIndices: List<Int>,
    val activeHistoryPosition: Int?,
    val inActiveHistoryMode: Boolean
)

/** 已答历史 Active 模式更新计划 */
data class SessionAnsweredHistoryNavigationUpdate(
    val originIndex: Int,
    val historyPosition: Int,
    val orderedIndices: List<Int>,
    val anchorPoolIndices: Set<Int>
)
