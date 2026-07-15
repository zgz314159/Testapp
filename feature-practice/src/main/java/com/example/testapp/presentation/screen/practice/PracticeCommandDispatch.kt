package com.example.testapp.presentation.screen.practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.session.practice.PracticeCommandOutcome
import com.example.testapp.presentation.session.practice.PracticeScreenBindings
import com.example.testapp.presentation.session.practice.PracticeSessionCommandHandler

fun dispatchPracticeCommand(
    bindings: PracticeScreenBindings,
    command: SessionCommand,
): PracticeCommandOutcome? = PracticeSessionCommandHandler.dispatch(bindings, command)

@Composable
fun rememberPracticeCommandDispatcher(
    bindings: PracticeScreenBindings,
): (SessionCommand) -> PracticeCommandOutcome? =
    remember(bindings) { { command -> dispatchPracticeCommand(bindings, command) } }

suspend fun suspendPracticeCommand(
    bindings: PracticeScreenBindings,
    command: SessionCommand,
): PracticeSessionGradeSnapshot? = PracticeSessionCommandHandler.suspendHandle(bindings, command)
