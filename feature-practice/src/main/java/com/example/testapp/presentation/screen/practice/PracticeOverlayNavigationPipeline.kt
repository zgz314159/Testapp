package com.example.testapp.presentation.screen.practice

/** 练习页打开 AI/笔记等 overlay 路由时的锚点与回退判定。 */
object PracticeOverlayNavigationPipeline {

    data class Anchor(val index: Int, val questionId: Int)

    fun capture(index: Int, questionId: Int): Anchor = Anchor(index, questionId)

    /** overlay 关闭后若题号漂移则返回应恢复的 index，否则 null。 */
    fun restoreIndex(anchor: Anchor?, currentIndex: Int): Int? {
        if (anchor == null) return null
        return anchor.index.takeIf { it != currentIndex }
    }
}
