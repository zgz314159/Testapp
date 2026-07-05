package com.example.testapp.core.session

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.SessionMode
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.UnifiedSessionState

/**
 * 进度持久化管理器 — 统一 Practice/Exam 的 save/load 逻辑。
 * ViewModel 通过此管理器保存和恢复会话状态。
 */
interface SessionProgressManager {
    suspend fun saveProgress(
        progressId: String,
        state: UnifiedSessionState,
        memoryActive: Boolean,
        allSourceQuestions: List<Question>,
        fillSignature: String,
        extras: Map<String, Any> = emptyMap()
    )

    suspend fun clearProgress(progressId: String)

    suspend fun clearProgressByFileName(pattern: String, mode: SessionMode)

    /**
     * 从持久化进度恢复 QuestionWithState
     */
    fun restoreQuestionsWithState(
        questions: List<Question>,
        savedStateMap: Map<Int, UnifiedQuestionState>,
        sessionStartTime: Long,
        fillSignature: String = ""
    ): List<QuestionWithState>

    /**
     * 完整恢复：从原始持久化进度数据恢复 QuestionWithState（支持 questionStateMap + 旧 flat-list 降级）
     * @param rawProgress 原始持久化对象（PracticeProgress? / ExamProgress?），由 VM 的 facade.progress.getFlow 获取
     * @return RestoreResult，null 表示 rawProgress 为空
     */
    fun restoreFromRawProgress(
        questions: List<Question>,
        rawProgress: Any?,
        sessionStartTime: Long
    ): RestoreResult?

    /**
     * 完整恢复（自动查询）：获取持久化进度 → 恢复 QuestionWithState。
     * 注意：会额外查询一次数据库；若已有进度对象请使用 restoreFromRawProgress。
     */
    suspend fun restoreProgress(
        progressId: String,
        questions: List<Question>,
        sessionStartTime: Long,
        mode: SessionMode
    ): RestoreResult?

    /**
     * 获取持久化进度的 Flow（用于 VM 尾递归/自动保存监听）
     * @deprecated 推荐使用 restoreProgress 替代
     */
    suspend fun loadProgressFlow(
        progressId: String,
        mode: SessionMode
    ): UnifiedSessionState?
}

/**
 * 进度恢复结果
 */
data class RestoreResult(
    val questionsWithState: List<QuestionWithState>,
    val savedCurrentIndex: Int,
    val finished: Boolean
)
