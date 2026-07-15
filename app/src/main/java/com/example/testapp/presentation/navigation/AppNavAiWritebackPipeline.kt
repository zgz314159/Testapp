package com.example.testapp.presentation.navigation

import com.example.testapp.data.network.deepseek.DeepSeekAskPersistDebugLog
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
        val hasExam = sessions.examBindings != null
        val hasPractice = sessions.practiceBindings != null
        DeepSeekAskPersistDebugLog.d(
            "Nav.writeback",
            "index=$index hasExam=$hasExam hasPractice=$hasPractice ${DeepSeekAskPersistDebugLog.meta(text)} " +
                "preview=${DeepSeekAskPersistDebugLog.preview(text)}",
        )
        if (!hasExam && !hasPractice) {
            DeepSeekAskPersistDebugLog.w("Nav.writeback", "NO parent bindings — session UI will NOT update")
        }
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
