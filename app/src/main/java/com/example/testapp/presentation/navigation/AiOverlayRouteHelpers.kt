package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.testapp.data.network.deepseek.DeepSeekExamAnchorPipeline
import com.example.testapp.presentation.session.exam.ExamScreenBindings
import com.example.testapp.presentation.session.practice.PracticeScreenBindings

internal data class AiOverlayParentSessions(
    val examBindings: ExamScreenBindings?,
    val practiceBindings: PracticeScreenBindings?,
)

@Composable
internal fun rememberAiOverlayParentSessions(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
): AiOverlayParentSessions {
    val parentSessions = rememberParentSessionViewModels(navController, backStackEntry)
    return remember(parentSessions) {
        AiOverlayParentSessions(
            examBindings = parentSessions.examBindings,
            practiceBindings = parentSessions.practiceBindings,
        )
    }
}

internal fun NavHostController.navigateAiAsk(
    routePrefix: String,
    questionId: Int,
    index: Int,
    encodedSelection: String,
) {
    navigate("$routePrefix/$questionId/$index/$encodedSelection")
}

internal fun deepSeekExamAnchorFrom(
    examBindings: ExamScreenBindings?,
    practiceBindings: PracticeScreenBindings?,
    index: Int,
) = DeepSeekExamAnchorPipeline.fromQuestion(
    examBindings?.questions?.value?.getOrNull(index)
        ?: practiceBindings?.questions?.value?.getOrNull(index),
)
