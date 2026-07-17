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

    /** 正确答案全屏编辑写回：练习走 UpdateQuestionAllFields（含落盘），考试走 SaveEditedQuestionFields。 */
    fun updateQuestionAnswer(
        sessions: AiOverlayParentSessions,
        index: Int,
        answer: String,
    ) {
        sessions.practiceBindings?.let { bindings ->
            val question = bindings.questions.value.getOrNull(index) ?: return@let
            dispatchPracticeCommand(
                bindings,
                SessionCommand.UpdateQuestionAllFields(
                    index = index,
                    content = question.content,
                    options = question.options,
                    answer = answer,
                    explanation = question.explanation,
                ),
            )
        }
        sessions.examBindings?.let { bindings ->
            val question = bindings.questions.value.getOrNull(index) ?: return@let
            dispatchExamCommand(
                bindings,
                SessionCommand.SaveEditedQuestionFields(
                    index = index,
                    content = question.content,
                    answer = answer,
                    options = question.options,
                ),
            )
        }
    }
}
