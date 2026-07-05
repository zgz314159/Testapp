package com.example.testapp.presentation.session.exam

import com.example.testapp.domain.model.Question
import com.example.testapp.presentation.screen.exam.ExamFillConfig
import com.example.testapp.presentation.screen.exam.ExamMemoryModeEngine

/** Exam 运行时可变状态（装配 / LoadDelegate 共用） */
internal class ExamSessionRuntimeState {
    var progressId: String = "exam_default"
    var progressSeed: Long = System.currentTimeMillis()
    var fullAnswerRequireCorrect: Boolean = false
    var quizIdInternal: String = ""
    var randomExamEnabled: Boolean = false
    var activeFillConfig: ExamFillConfig = ExamFillConfig.default
    var allSourceQuestions: List<Question> = emptyList()
    var currentMemoryRoundQuestionIds: Set<Int> = emptySet()

    val memory = MemoryState()
    val artifactFlags = ArtifactLoadFlags()

    inner class MemoryState {
        var enabled: Boolean = false
        var active: Boolean = false
        var batchSize: Int = 10
        var wrongMode: Int = ExamMemoryModeEngine.MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS
        var poolMode: Int = ExamMemoryModeEngine.MEMORY_POOL_MODE_IN_OUT
    }

    class ArtifactLoadFlags {
        var analysisLoaded: Boolean = false
        var sparkAnalysisLoaded: Boolean = false
        var baiduAnalysisLoaded: Boolean = false
        var notesLoaded: Boolean = false

        fun reset() {
            analysisLoaded = false
            sparkAnalysisLoaded = false
            baiduAnalysisLoaded = false
            notesLoaded = false
        }
    }

    fun resetArtifactLoadedFlags() = artifactFlags.reset()
}
