package com.example.testapp.presentation.session.practice

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.session.registry.SessionDeps
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import com.example.testapp.domain.usecase.QuestionFlowCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeSessionDeps
    @Inject
    constructor(
        val sessionEngine: SessionEngine,
        val facade: PracticeUseCaseFacade,
        val questionFlowCache: QuestionFlowCache,
        val fontSettings: FontSettingsRepository,
    ) : SessionDeps
