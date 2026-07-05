package com.example.testapp.presentation.session.exam

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal object ExamSessionLoadDelegateWiring {
    fun attach(
        deps: ExamSessionDeps,
        scope: CoroutineScope,
        sessionState: MutableStateFlow<PracticeSessionState>,
        runtime: ExamSessionRuntimeState,
        persistentQuestionStateMap: MutableMap<Int, UnifiedQuestionState>,
        initializeMemoryModeIfNeeded: suspend (Long) -> Boolean,
        loadProgress: () -> Unit,
    ) {
        val memory = runtime.memory
        deps.loadDelegate.init(
            vmScope = scope,
            progressIdRef = { runtime.progressId },
            setProgressId = { runtime.progressId = it },
            progressSeedRef = { runtime.progressSeed },
            setProgressSeed = { runtime.progressSeed = it },
            setFullAnswerRequireCorrect = { runtime.fullAnswerRequireCorrect = it },
            onFillConfigApplied = { runtime.activeFillConfig = it },
            memoryModeActiveRef = { memory.active },
            setMemoryModeActive = { memory.active = it },
            memoryModeEnabledRef = { memory.enabled },
            memoryModeBatchSizeRef = { memory.batchSize },
            memoryWrongModeRef = { memory.wrongMode },
            memoryPoolModeRef = { memory.poolMode },
            allSourceQuestionsRef = { runtime.allSourceQuestions },
            setAllSourceQuestions = { runtime.allSourceQuestions = it },
            setCurrentMemoryRoundQuestionIds = { runtime.currentMemoryRoundQuestionIds = it },
            persistentQuestionStateMap = persistentQuestionStateMap,
            onQuestions = { qs ->
                sessionState.update {
                    it.copy(questionsWithState = qs.map { q -> QuestionWithState(question = q) })
                }
            },
            onProgressLoaded = { loaded -> sessionState.update { it.copy(progressLoaded = loaded) } },
            onPostArtifacts = { arts ->
                sessionState.update { s ->
                    val upd =
                        s.questionsWithState.mapIndexed { i, qws ->
                            qws.copy(
                                analysis = arts.analysis.getOrElse(i) { qws.analysis },
                                sparkAnalysis = arts.sparkAnalysis.getOrElse(i) { qws.sparkAnalysis },
                                baiduAnalysis = arts.baiduAnalysis.getOrElse(i) { qws.baiduAnalysis },
                                note = arts.notes.getOrElse(i) { qws.note },
                            )
                        }
                    s.copy(questionsWithState = upd)
                }
            },
            onInitMemoryMode = { seed -> initializeMemoryModeIfNeeded(seed) },
            onLoadProgress = { loadProgress() },
        )
    }
}
