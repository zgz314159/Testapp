package com.example.testapp.presentation.session.exam

import com.example.testapp.core.session.strategy.reveal.SessionRevealSubmitPipeline
import com.example.testapp.domain.session.SessionCommand

object ExamSessionCommandHandler {
    fun dispatch(
        bindings: ExamScreenBindings,
        command: SessionCommand,
    ): ExamCommandOutcome? =
        when (command) {
            SessionCommand.BrowseAnsweredHistoryOlder ->
                ExamCommandOutcome.ReviewHistoryOlder(bindings.browseReviewAnsweredOlder())
            SessionCommand.BrowseAnsweredHistoryNewer ->
                ExamCommandOutcome.ReviewHistoryNewer(bindings.browseReviewAnsweredNewer())
            else -> {
                handle(bindings, command)
                null
            }
        }

    fun handle(
        bindings: ExamScreenBindings,
        command: SessionCommand,
    ) {
        when (command) {
            SessionCommand.Back -> Unit
            SessionCommand.NextQuestion -> bindings.nextQuestion()
            SessionCommand.PrevQuestion -> bindings.prevQuestion()
            SessionCommand.NavPrevIcon -> bindings.prevQuestionViaIcon()
            SessionCommand.NavNextIcon -> bindings.nextQuestionViaIcon()
            SessionCommand.NavPrevIconDoubleClick -> bindings.prevQuestionViaIconDoubleClick()
            SessionCommand.NavNextIconDoubleClick -> bindings.nextQuestionViaIconDoubleClick()
            SessionCommand.NavPrevSequential -> bindings.prevQuestionSequential()
            SessionCommand.NavNextSequential -> bindings.nextQuestionSequential()
            is SessionCommand.GoToQuestion -> bindings.goToQuestion(command.index)
            is SessionCommand.SelectOption -> bindings.selectOption(command.option)
            is SessionCommand.ToggleOption -> bindings.selectOption(command.option)
            is SessionCommand.UpdateTextAnswer -> bindings.updateTextAnswer(command.text)
            is SessionCommand.RevealAnswer -> {
                if (SessionRevealSubmitPipeline.revealOnExplicitCommand(
                        bindings.revealConfig(),
                        command.index,
                    ) { bindings.updateShowResult(command.index, true) }
                ) {
                    Unit
                }
            }
            is SessionCommand.SetShowResult -> bindings.updateShowResult(command.index, command.value)
            is SessionCommand.UpdateAnalysis -> bindings.updateAnalysis(command.index, command.text)
            is SessionCommand.UpdateSparkAnalysis -> bindings.updateSparkAnalysis(command.index, command.text)
            is SessionCommand.UpdateBaiduAnalysis -> bindings.updateBaiduAnalysis(command.index, command.text)
            is SessionCommand.SaveNote -> bindings.saveNote(command.questionId, command.index, command.text)
            is SessionCommand.AppendNote -> bindings.appendNote(command.questionId, command.index, command.text)
            is SessionCommand.GoToQuestionById,
            is SessionCommand.ClearExplanation,
            SessionCommand.BrowseAnsweredHistoryOlder,
            SessionCommand.BrowseAnsweredHistoryNewer,
            SessionCommand.SubmitCurrent,
            SessionCommand.SubmitSession,
            is SessionCommand.PrepareEditableQuestion,
            is SessionCommand.UpdateQuestionAllFields,
            is SessionCommand.AddHistoryRecord,
            is SessionCommand.SaveEditedQuestion,
            SessionCommand.GradeSession,
            -> Unit
            SessionCommand.ClearEditableQuestion -> bindings.clearEditableQuestion()
            is SessionCommand.PrepareEditableAtIndex -> bindings.prepareEditableQuestion(command.index)
            is SessionCommand.SaveEditedQuestionFields ->
                bindings.saveEditedQuestion(
                    command.index,
                    command.content,
                    command.answer,
                    command.options,
                )
            is SessionCommand.SelectOptionWithSkip ->
                bindings.selectOption(
                    command.option,
                    skipAfterChanged = command.skipAfterChanged,
                )
            is SessionCommand.RetryCurrentQuestion -> bindings.retryCurrentQuestion(command.index)
            is SessionCommand.RetryWrongBlanks -> bindings.retryWrongFillBlanks(command.index)
            is SessionCommand.EnterExamReviewSession ->
                bindings.enterReviewSession(
                    targetProgressId = command.targetProgressId,
                    quizFile = command.quizFile,
                    questionCount = command.questionCount,
                    random = command.random,
                    wrongBook = command.wrongBook,
                    favorite = command.favorite,
                )
            is SessionCommand.SetRandomExam -> bindings.setRandomExam(command.enabled)
            is SessionCommand.SetMemoryModeConfig ->
                bindings.setMemoryModeConfig(
                    enabled = command.enabled,
                    batchSize = command.batchSize,
                    wrongMode = command.wrongMode,
                    poolMode = command.poolMode,
                )
            is SessionCommand.ReloadForFillConfig -> bindings.reloadForFillConfig()
            is SessionCommand.LoadQuestions ->
                bindings.loadQuestions(
                    command.quizId,
                    command.count,
                    command.random,
                )
            is SessionCommand.LoadWrongQuestions -> {
                val count = command.questionCount
                val random = command.random
                if (count != null && random != null) {
                    bindings.loadWrongQuestions(command.fileName, count, random)
                }
            }
            is SessionCommand.LoadFavoriteQuestions -> {
                val count = command.questionCount
                val random = command.random
                if (count != null && random != null) {
                    bindings.loadFavoriteQuestions(command.fileName, count, random)
                }
            }
            is SessionCommand.EnterReviewSession,
            is SessionCommand.SetRandomPractice,
            is SessionCommand.SetProgressId,
            SessionCommand.GradeSessionOnSubmit,
            SessionCommand.LeaveReviewSession,
            -> Unit
        }
    }

    suspend fun suspendHandle(
        bindings: ExamScreenBindings,
        command: SessionCommand,
    ): Int? =
        when (command) {
            SessionCommand.GradeSession -> bindings.gradeExam()
            else -> {
                handle(bindings, command)
                null
            }
        }
}
