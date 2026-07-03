package com.example.testapp.presentation.screen.practice

/** 按 currentIndex 解析题目 UI 快照，避免延时为 0 自动跳题时 index 与快照错位。 */
object PracticeQuestionUiResolvePipeline {

    fun selectedOptions(
        ui: PracticeCurrentQuestionUi?,
        index: Int,
        all: List<List<Int>>
    ): List<Int> = if (ui?.index == index) ui.selectedOptions else all.getOrNull(index).orEmpty()

    fun textAnswer(
        ui: PracticeCurrentQuestionUi?,
        index: Int,
        all: List<String>
    ): String = if (ui?.index == index) ui.textAnswer else all.getOrNull(index).orEmpty()

    fun showResult(
        ui: PracticeCurrentQuestionUi?,
        index: Int,
        all: List<Boolean>
    ): Boolean = if (ui?.index == index) ui.showResult else all.getOrNull(index) == true
}
