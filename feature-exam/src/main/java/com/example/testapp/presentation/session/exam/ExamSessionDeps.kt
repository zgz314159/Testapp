package com.example.testapp.presentation.session.exam

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.session.registry.SessionDeps
import com.example.testapp.domain.usecase.ExamUseCaseFacade
import com.example.testapp.presentation.screen.exam.ExamAnswerRules
import com.example.testapp.presentation.screen.exam.ExamFillTransform
import com.example.testapp.presentation.screen.exam.ExamLoadDelegate
import com.example.testapp.presentation.screen.exam.ExamMemoryModeEngine
import com.example.testapp.presentation.screen.exam.ExamNavigationHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamSessionDeps
    @Inject
    constructor(
        val fontSettings: FontSettingsRepository,
        val sessionEngine: SessionEngine,
        val facade: ExamUseCaseFacade,
        val answerRules: ExamAnswerRules,
        val fillTransform: ExamFillTransform,
        val memoryModeEngine: ExamMemoryModeEngine,
        val navHelper: ExamNavigationHelper,
        val loadDelegate: ExamLoadDelegate,
    ) : SessionDeps
