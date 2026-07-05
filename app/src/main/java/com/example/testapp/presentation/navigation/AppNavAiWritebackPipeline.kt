package com.example.testapp.presentation.navigation

import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.exam.dispatchExamCommand
import com.example.testapp.presentation.screen.practice.dispatchPracticeCommand

/** AI 叠层保存写回 — 统一经 `SessionCommand` 而非 bindings 直调。 */
internal object AppNavAiWritebackPipeline {
    fun updateAnalysis(
        sessions: AiOverlayParentSessions,
        index: Int,
        text: String,
    ) {
        sessions.examBindings?.let {
            dispatchExamCommand(it, SessionCommand.UpdateAnalysis(index, text))
        }
        sessions.practiceBindings?.let {
            dispatchPracticeCommand(it, SessionCommand.UpdateAnalysis(index, text))
        }
    }

    fun updateSparkAnalysis(
        sessions: AiOverlayParentSessions,
        index: Int,
        text: String,
    ) {
        sessions.examBindings?.let {
            dispatchExamCommand(it, SessionCommand.UpdateSparkAnalysis(index, text))
        }
        sessions.practiceBindings?.let {
            dispatchPracticeCommand(it, SessionCommand.UpdateSparkAnalysis(index, text))
        }
    }

    fun updateBaiduAnalysis(
        sessions: AiOverlayParentSessions,
        index: Int,
        text: String,
    ) {
        sessions.examBindings?.let {
            dispatchExamCommand(it, SessionCommand.UpdateBaiduAnalysis(index, text))
        }
        sessions.practiceBindings?.let {
            dispatchPracticeCommand(it, SessionCommand.UpdateBaiduAnalysis(index, text))
        }
    }

    fun saveNote(
        sessions: AiOverlayParentSessions,
        questionId: Int,
        index: Int,
        text: String,
    ) {
        sessions.examBindings?.let {
            dispatchExamCommand(it, SessionCommand.SaveNote(questionId, index, text))
        }
        sessions.practiceBindings?.let {
            dispatchPracticeCommand(it, SessionCommand.SaveNote(questionId, index, text))
        }
    }

    fun appendNote(
        sessions: AiOverlayParentSessions,
        questionId: Int,
        index: Int,
        text: String,
    ) {
        sessions.examBindings?.let {
            dispatchExamCommand(it, SessionCommand.AppendNote(questionId, index, text))
        }
        sessions.practiceBindings?.let {
            dispatchPracticeCommand(it, SessionCommand.AppendNote(questionId, index, text))
        }
    }
}
