package com.example.testapp.presentation.screen.home.components

/** Home 文件列表拖拽边缘自动滚动常量与判定 */
object HomeFileListDragScroll {
    const val autoScrollEdge = 96f
    const val autoScrollAmount = 36f

    fun scrollDelta(
        dragY: Float,
        boundsTop: Float,
        boundsBottom: Float,
        edge: Float = autoScrollEdge,
        amount: Float = autoScrollAmount,
    ): Float =
        when {
            dragY < boundsTop + edge -> -amount
            dragY > boundsBottom - edge -> amount
            else -> 0f
        }
}
