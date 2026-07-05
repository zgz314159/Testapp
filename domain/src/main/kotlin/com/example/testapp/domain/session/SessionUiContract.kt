package com.example.testapp.domain.session

/** 由 UiPolicyFactory 从 Capabilities 推导（ADR-003）；Session 子类禁止手写 */
enum class TopBarMode { Standard, Review, Minimal }

enum class BottomBarMode { Full, NavOnly, Hidden }

enum class GestureMode { Standard, HistoryBrowse, Disabled }

data class SessionMenuContract(
    val showAi: Boolean = true,
    val showNote: Boolean = true,
    val showFavorite: Boolean = true
)

data class SessionUiContract(
    val topBar: TopBarMode = TopBarMode.Standard,
    val bottomBar: BottomBarMode = BottomBarMode.Full,
    val gesture: GestureMode = GestureMode.Standard,
    val menu: SessionMenuContract = SessionMenuContract(),
    val resultDisplayDelayMs: Long = 0L
)
