package com.example.testapp.presentation.screen.practice

/** 练习进度 id 规范化 — 保持 `practice_` 前缀契约不变。 */
object PracticeProgressIdPipeline {
    const val PREFIX = "practice_"

    fun ensurePrefix(id: String): String =
        if (id.startsWith(PREFIX)) id else "$PREFIX$id"
}
