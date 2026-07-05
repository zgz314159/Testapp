package com.example.testapp.core.session.strategy.reveal

import com.example.testapp.domain.session.reveal.SessionRevealConfig

/** 显式 RevealAnswer 命令路由（从 PracticeSubmitRevealPipeline 收编） */
object SessionRevealSubmitPipeline {
    fun revealOnExplicitCommand(
        config: SessionRevealConfig,
        answeredIndex: Int,
        revealShowResult: (Int) -> Unit,
    ): Boolean {
        if (!SessionRevealGate.allowsExplicitReveal(config)) return false
        revealShowResult(answeredIndex)
        return true
    }
}
