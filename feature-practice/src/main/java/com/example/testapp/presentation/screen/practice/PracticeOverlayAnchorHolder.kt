package com.example.testapp.presentation.screen.practice

/**
 * 同步 overlay 锚点（不依赖 Compose recomposition），避免 ON_PAUSE 早于 anchor 状态提交。
 */
class PracticeOverlayAnchorHolder {
    @Volatile var openIndex: Int = -1
        private set

    @Volatile var openQuestionId: Int = -1
        private set

    @Volatile var isOverlayOpen: Boolean = false
        private set

    fun open(index: Int, questionId: Int) {
        openIndex = index
        openQuestionId = questionId
        isOverlayOpen = true
    }

    /** overlay 关闭；返回打开时的 index（供恢复）。 */
    fun close(): Int? {
        val pinned = openIndex.takeIf { isOverlayOpen && it >= 0 }
        isOverlayOpen = false
        openIndex = -1
        openQuestionId = -1
        return pinned
    }

    fun shouldPin(currentIndex: Int): Int? {
        if (!isOverlayOpen || openIndex < 0) return null
        return openIndex.takeIf { it != currentIndex }
    }
}
