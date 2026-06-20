package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.mutableStateOf

/**
 * 考试结束编排 — 去重提分 (hasGradedExam)
 * 消除 ExamScreen 中 4 处重复的 gradeExam + onExamEnd 分支
 */
class ExamEndFlow {
    val graded = mutableStateOf(false)

    suspend fun tryGradeThen(
        viewModel: ExamViewModel,
        onSuccess: () -> Unit
    ) {
        if (graded.value) { onSuccess(); return }
        graded.value = true
        val score = viewModel.gradeExam()
        if (score < 0) { graded.value = false; return }
        onSuccess()
    }

    fun reset() { graded.value = false }
}
