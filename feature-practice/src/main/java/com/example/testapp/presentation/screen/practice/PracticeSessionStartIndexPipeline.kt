package com.example.testapp.presentation.screen.practice

/** 练习会话初始题号：重载时保留当前题，新会话才随机/归零。 */
object PracticeSessionStartIndexPipeline {

    fun resolve(
        questionCount: Int,
        restoreFromMap: Boolean,
        savedCurrentIndex: Int?,
        randomPracticeEnabled: Boolean,
        sessionStartTime: Long,
        preserveCurrentIndex: Int?
    ): Int {
        val lastIndex = (questionCount - 1).coerceAtLeast(0)
        preserveCurrentIndex?.let { return it.coerceIn(0, lastIndex) }
        if (restoreFromMap && savedCurrentIndex != null) {
            return savedCurrentIndex.coerceIn(0, lastIndex)
        }
        if (randomPracticeEnabled && questionCount > 0) {
            return (0 until questionCount).random(kotlin.random.Random(sessionStartTime))
        }
        return 0
    }
}
