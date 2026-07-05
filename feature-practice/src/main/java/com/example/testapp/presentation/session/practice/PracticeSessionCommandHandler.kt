package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.practice.PracticeSessionGradeSnapshot
import com.example.testapp.presentation.screen.practice.PracticeSubmitRevealPipeline

object PracticeSessionCommandHandler {
    fun dispatch(
        bindings: PracticeScreenBindings,
        command: SessionCommand,
    ): PracticeCommandOutcome? =
        when (command) {
            SessionCommand.NavPrevIcon ->
                PracticeCommandOutcome.UnansweredNav(bindings.prevQuestionViaIcon())
            SessionCommand.NavNextIcon ->
                PracticeCommandOutcome.UnansweredNav(bindings.nextQuestionViaIcon())
            SessionCommand.NavPrevIconDoubleClick -> {
                bindings.prevQuestionViaIconDoubleClick()
                null
            }
            SessionCommand.NavNextIconDoubleClick -> {
                bindings.nextQuestionViaIconDoubleClick()
                null
            }
            SessionCommand.BrowseAnsweredHistoryOlder ->
                PracticeCommandOutcome.HistoryOlder(bindings.browseAnsweredHistoryOlder())
            SessionCommand.BrowseAnsweredHistoryNewer ->
                PracticeCommandOutcome.HistoryNewer(bindings.browseAnsweredHistoryNewer())
            else -> {
                handle(bindings, command)
                null
            }
        }

    fun handle(
        bindings: PracticeScreenBindings,
        command: SessionCommand,
    ) {
        when (command) {
            SessionCommand.Back -> Unit
            SessionCommand.NextQuestion -> bindings.nextQuestion()
            SessionCommand.PrevQuestion -> bindings.prevQuestion()
            is SessionCommand.GoToQuestion -> bindings.goToQuestion(command.index, command.source)
            is SessionCommand.GoToQuestionById -> bindings.goToQuestionById(command.questionId, command.source)
            is SessionCommand.SelectOption -> bindings.answerQuestion(command.option)
            is SessionCommand.ToggleOption -> bindings.toggleOption(command.option)
            is SessionCommand.UpdateTextAnswer -> bindings.updateTextAnswer(command.text)
            is SessionCommand.RevealAnswer ->
                PracticeSubmitRevealPipeline.revealImmediately(
                    command.index,
                    bindings::revealShowResult,
                )
            is SessionCommand.SetShowResult -> bindings.updateShowResult(command.index, command.value)
            is SessionCommand.UpdateAnalysis -> bindings.updateAnalysis(command.index, command.text)
            is SessionCommand.UpdateSparkAnalysis -> bindings.updateSparkAnalysis(command.index, command.text)
            is SessionCommand.UpdateBaiduAnalysis -> bindings.updateBaiduAnalysis(command.index, command.text)
            is SessionCommand.SaveNote -> bindings.saveNote(command.questionId, command.index, command.text)
            is SessionCommand.AppendNote -> bindings.appendNote(command.questionId, command.index, command.text)
            is SessionCommand.ClearExplanation -> bindings.clearExplanation(command.index, command.question)
            SessionCommand.ClearEditableQuestion -> bindings.clearEditableQuestion()
            is SessionCommand.PrepareEditableQuestion -> bindings.prepareEditableQuestion(command.questionId)
            is SessionCommand.UpdateQuestionAllFields ->
                bindings.updateQuestionAllFields(
                    command.index,
                    command.content,
                    command.options,
                    command.answer,
                    command.explanation,
                )
            is SessionCommand.AddHistoryRecord ->
                bindings.addHistoryRecord(
                    command.score,
                    command.total,
                    command.unanswered,
                )
            is SessionCommand.RetryCurrentQuestion -> bindings.retryCurrentQuestion(command.index)
            is SessionCommand.RetryWrongBlanks -> bindings.retryWrongBlanks(command.index)
            is SessionCommand.EnterReviewSession -> bindings.enterReviewSession(command.targetProgressId)
            SessionCommand.LeaveReviewSession -> bindings.leaveReviewSession()
            is SessionCommand.SetRandomPractice -> bindings.setRandomPractice(command.enabled)
            is SessionCommand.ReloadForFillConfig ->
                bindings.reloadForFillConfig(
                    command.questionCount,
                    command.initKey,
                )
            is SessionCommand.SetProgressId -> {
                bindings.setProgressId(
                    id = command.id,
                    questionsId = command.questionsId,
                    loadQuestions = command.loadQuestions,
                    questionCount = command.questionCount,
                    random = command.random,
                )
            }
            is SessionCommand.LoadWrongQuestions -> bindings.loadWrongQuestions(command.fileName)
            is SessionCommand.LoadFavoriteQuestions -> bindings.loadFavoriteQuestions(command.fileName)
            SessionCommand.NavPrevIcon,
            SessionCommand.NavNextIcon,
            SessionCommand.NavPrevIconDoubleClick,
            SessionCommand.NavNextIconDoubleClick,
            SessionCommand.BrowseAnsweredHistoryOlder,
            SessionCommand.BrowseAnsweredHistoryNewer,
            SessionCommand.NavPrevSequential,
            SessionCommand.NavNextSequential,
            SessionCommand.SubmitCurrent,
            SessionCommand.SubmitSession,
            is SessionCommand.PrepareEditableAtIndex,
            is SessionCommand.SelectOptionWithSkip,
            is SessionCommand.SaveEditedQuestionFields,
            is SessionCommand.SaveEditedQuestion,
            SessionCommand.GradeSession,
            SessionCommand.GradeSessionOnSubmit,
            is SessionCommand.EnterExamReviewSession,
            is SessionCommand.SetRandomExam,
            is SessionCommand.SetMemoryModeConfig,
            is SessionCommand.LoadQuestions,
            -> Unit
        }
    }

    suspend fun suspendHandle(
        bindings: PracticeScreenBindings,
        command: SessionCommand,
    ): PracticeSessionGradeSnapshot? =
        when (command) {
            SessionCommand.GradeSessionOnSubmit -> bindings.gradeSessionOnSubmit()
            is SessionCommand.SaveEditedQuestion -> {
                bindings.saveEditedQuestion(command.edited)
                null
            }
            else -> {
                handle(bindings, command)
                null
            }
        }
}
