package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.UnifiedQuestionState

class PracticeProgressCoordinator {

    fun practiceProgressSeed(progress: PracticeProgress?, fallback: Long): Long {
        return progress?.sessionId
            ?.substringAfterLast('_')
            ?.substringBefore('|')
            ?.toLongOrNull()
            ?: progress?.timestamp
            ?: fallback
    }

    fun defaultFillConfigSignature(): String {
        return listOf(
            FillQuestionGenerationMode.SCORE_RANGE_RANDOM.storageValue,
            "0",
            "true",
            "false",
            "1",
            "10",
            ""
        ).joinToString("|")
    }

    fun buildSessionIdWithFillSignature(baseId: String, seed: Long, fillSignature: String): String {
        return "${baseId}_$seed|fill=$fillSignature"
    }

    fun extractFillConfigSignature(sessionId: String?): String? {
        val parsed = sessionId
            ?.substringAfter("|fill=", missingDelimiterValue = "")
            ?.trim()
            .orEmpty()
        return parsed.takeIf { it.isNotBlank() }
    }

    fun canReuseByFillSignature(
        sessionId: String?,
        currentFillSignature: String,
        fillConfigSensitive: Boolean
    ): Boolean {
        val savedSignature = extractFillConfigSignature(sessionId)
        return if (savedSignature.isNullOrBlank()) !fillConfigSensitive
        else savedSignature == currentFillSignature
    }

    fun fillGenerationModeFromSignature(fillSignature: String): FillQuestionGenerationMode {
        return FillQuestionGenerationMode.fromStorageValue(
            fillSignature.substringBefore('|', missingDelimiterValue = fillSignature)
                .ifBlank { FillQuestionGenerationMode.SCORE_RANGE_RANDOM.storageValue }
        )
    }

    fun buildProgressSnapshotFromState(
        currentState: PracticeSessionState,
        targetProgressId: String,
        fillSignature: String
    ): PracticeProgress {
        val questionStateMap = currentState.questionsWithState.associate { qws ->
            val qid = qws.question.id
            qid to UnifiedQuestionState(
                questionId = qid,
                selectedOptions = qws.selectedOptions,
                textAnswer = qws.textAnswer,
                showResult = qws.showResult,
                analysis = qws.analysis,
                sparkAnalysis = qws.sparkAnalysis,
                baiduAnalysis = qws.baiduAnalysis,
                note = qws.note,
                answerTime = qws.sessionAnswerTime,
                displayedQuestionContent = qws.question.content,
                displayedQuestionAnswer = qws.question.answer
            )
        }
        return PracticeProgress(
            id = targetProgressId,
            currentIndex = currentState.currentIndex,
            answeredList = currentState.answeredIndices,
            selectedOptions = currentState.questionsWithState.map { it.selectedOptions },
            showResultList = currentState.questionsWithState.map { it.showResult },
            analysisList = currentState.questionsWithState.map { it.analysis },
            sparkAnalysisList = currentState.questionsWithState.map { it.sparkAnalysis },
            baiduAnalysisList = currentState.questionsWithState.map { it.baiduAnalysis },
            noteList = currentState.questionsWithState.map { it.note },
            timestamp = System.currentTimeMillis(),
            sessionId = buildSessionIdWithFillSignature(
                targetProgressId, currentState.sessionStartTime, fillSignature
            ),
            fixedQuestionOrder = currentState.questionsWithState.map { it.question.id },
            questionStateMap = questionStateMap
        )
    }
}

