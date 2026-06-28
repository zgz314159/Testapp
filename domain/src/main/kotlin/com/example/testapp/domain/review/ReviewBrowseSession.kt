package com.example.testapp.domain.review

data class ReviewBrowseSession(
    val displayOrder: List<Int>,
    val position: Int = 0
) {
    val currentIndex: Int
        get() = displayOrder.getOrElse(position) { 0 }

    fun canStepBack(): Boolean = position > 0

    fun canStepForward(): Boolean = displayOrder.isNotEmpty() && position < displayOrder.lastIndex

    fun step(delta: Int): ReviewBrowseSession? {
        val next = position + delta
        if (next !in displayOrder.indices) return null
        return copy(position = next)
    }
}
