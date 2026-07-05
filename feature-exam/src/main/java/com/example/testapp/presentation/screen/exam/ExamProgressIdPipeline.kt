package com.example.testapp.presentation.screen.exam

/** 考试进度 id 规范化 — 保持 `exam_` 前缀契约不变（对称 PracticeProgressIdPipeline）。 */
object ExamProgressIdPipeline {
    const val PREFIX = "exam_"

    fun ensurePrefix(id: String): String =
        if (id.startsWith(PREFIX)) id else "$PREFIX$id"
}
