package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.testapp.presentation.session.exam.AbstractExamQuestionSession
import com.example.testapp.presentation.session.exam.ExamScreenBindings
import com.example.testapp.presentation.session.host.QuestionSessionHostViewModel
import com.example.testapp.presentation.session.practice.AbstractPracticeQuestionSession
import com.example.testapp.presentation.session.practice.PracticeScreenBindings

data class ParentSessionViewModels(
    val practiceBindings: PracticeScreenBindings?,
    val examBindings: ExamScreenBindings?,
)

@Composable
fun rememberParentSessionViewModels(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
): ParentSessionViewModels {
    val parentEntry = remember(backStackEntry) { navController.previousBackStackEntry }

    val parentRoute = parentEntry?.destination?.route.orEmpty()

    val isExam = parentRoute.startsWith("exam")

    val practiceBindings =
        if (!isExam) {
            parentEntry?.let { entry ->

                val hostVm: QuestionSessionHostViewModel = hiltViewModel(entry)

                val session by hostVm.session.collectAsState()

                (session as? AbstractPracticeQuestionSession)?.bindings
            }
        } else {
            null
        }

    val examBindings =
        if (isExam) {
            parentEntry?.let { entry ->

                val hostVm: QuestionSessionHostViewModel = hiltViewModel(entry)

                val session by hostVm.session.collectAsState()

                (session as? AbstractExamQuestionSession)?.bindings
            }
        } else {
            null
        }

    return ParentSessionViewModels(
        practiceBindings = practiceBindings,
        examBindings = examBindings,
    )
}
