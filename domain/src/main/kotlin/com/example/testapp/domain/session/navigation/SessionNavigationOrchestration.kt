package com.example.testapp.domain.session.navigation

/** NavigationController / Coordinator 编排决策（由 SessionNavigationPolicy 推导） */
data class SessionNavigationOrchestration(
    val behavior: SessionNavigationBehavior,
    /** Icon ←/→ 进入未答导航前，若处于已答历史浏览态则先退出 */
    val exitAnsweredHistoryBeforeIconNav: Boolean,
    /** 答题卡 / 手动跳题时清空导航历史栈 */
    val clearNavigationHistoryOnManualJump: Boolean,
    /** 双击跨源跳转需全答模式激活 */
    val doubleClickRequiresFullAnswerMode: Boolean,
    /** 答后 advance：退出已答历史后，非随机模式下可停留在 restored pending 题 */
    val resumePendingAfterExitingAnsweredHistory: Boolean,
    /** 答后 advance：全答模式下优先在同源 pending 间跳转 */
    val usesFullAnswerSourceStayAdvance: Boolean,
    /** 答后 advance：当前源完成后跳下一源入口 */
    val usesNextSourceEntryAdvance: Boolean,
    /** 答后 advance：尝试相邻衍生题 direct index 跳转 */
    val usesAdjacentDerivedAdvance: Boolean,
    /** 答后 advance：未答 reopen / 全答错题 retry */
    val usesReopenOnPostAnswerAdvance: Boolean
)
