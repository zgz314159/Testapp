package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.session.exam.ExamCommandOutcome
import com.example.testapp.presentation.session.exam.ExamScreenBindings
import com.example.testapp.presentation.session.exam.ExamSessionCommandHandler

fun dispatchExamCommand(
    bindings: ExamScreenBindings,
    command: SessionCommand,
): ExamCommandOutcome? = ExamSessionCommandHandler.dispatch(bindings, command)

@Composable
fun rememberExamCommandDispatcher(
    bindings: ExamScreenBindings,
): (SessionCommand) -> ExamCommandOutcome? =
    remember(bindings) { { command -> dispatchExamCommand(bindings, command) } }

suspend fun suspendExamCommand(
    bindings: ExamScreenBindings,
    command: SessionCommand,
): Int? = ExamSessionCommandHandler.suspendHandle(bindings, command)
