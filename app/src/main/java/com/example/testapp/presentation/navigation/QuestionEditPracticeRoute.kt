package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import com.example.testapp.presentation.screen.questionbank.DrawerQuestionEditHost

@Composable
fun QuestionEditPracticeRoute(
    quizId: String,
    questionId: Int,
    onDismiss: () -> Unit,
) {
    DrawerQuestionEditHost(
        fileName = quizId,
        questionId = questionId,
        onDismiss = onDismiss,
    )
}
