package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.mutableStateOf
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.session.exam.ExamScreenBindings

/**
 * 考试结束编排 — 去重提分 (hasGradedExam)
 * 消除 ExamScreen 中 4 处重复的 gradeExam + onExamEnd 分支
 */
class ExamEndFlow {
    val graded = mutableStateOf(false)

    suspend fun tryGradeThen(
        bindings: ExamScreenBindings,
        onSuccess: () -> Unit
    ) {
        if (graded.value) { onSuccess(); return }
        graded.value = true
        val score = suspendExamCommand(bindings, SessionCommand.GradeSession) ?: run {
            graded.value = false
            return
        }
        if (score < 0) { graded.value = false; return }
        onSuccess()
    }

    fun reset() { graded.value = false }
}
