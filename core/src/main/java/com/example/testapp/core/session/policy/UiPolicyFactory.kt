package com.example.testapp.core.session.policy

import com.example.testapp.domain.session.BottomBarMode
import com.example.testapp.domain.session.GestureMode
import com.example.testapp.domain.session.SessionCapabilities
import com.example.testapp.domain.session.SessionMenuContract
import com.example.testapp.domain.session.SessionUiContract
import com.example.testapp.domain.session.TopBarMode

/** Capabilities → UiContract 唯一推导（ADR-003） */
object UiPolicyFactory {
    private const val DEFAULT_REVEAL_DELAY_MS = 300L

    fun from(capabilities: SessionCapabilities): SessionUiContract {
        val bottomBar =
            when {
                !capabilities.canSubmit && !capabilities.canShowAnswerCard -> BottomBarMode.NavOnly
                !capabilities.canSubmit -> BottomBarMode.NavOnly
                else -> BottomBarMode.Full
            }
        val gesture =
            when {
                !capabilities.canSwipeAnsweredHistory -> GestureMode.Disabled
                capabilities.canSwipeAnsweredHistory -> GestureMode.HistoryBrowse
                else -> GestureMode.Standard
            }
        val topBar =
            when {
                !capabilities.canSubmit && !capabilities.canPersistProgress -> TopBarMode.Minimal
                else -> TopBarMode.Standard
            }
        return SessionUiContract(
            topBar = topBar,
            bottomBar = bottomBar,
            gesture = gesture,
            menu =
                SessionMenuContract(
                    showAi = capabilities.canUseAiAsk,
                    showNote = capabilities.canUseAiAsk,
                    showFavorite = capabilities.canEditQuestion,
                ),
            resultDisplayDelayMs = if (capabilities.canRevealOnSubmit) DEFAULT_REVEAL_DELAY_MS else 0L,
        )
    }
}
