package com.example.testapp.domain.session

import com.example.testapp.domain.model.Question

/** UI / BackHandler → Session（ADR Command 侧） */
sealed interface SessionCommand {
    data object Back : SessionCommand
    data class GoToQuestion(val index: Int, val source: String = "goToQuestion") : SessionCommand
    data class GoToQuestionById(val questionId: Int, val source: String = "questionId") : SessionCommand
    data class SelectOption(val option: Int) : SessionCommand
    data class ToggleOption(val option: Int) : SessionCommand
    data class UpdateTextAnswer(val text: String) : SessionCommand
    data class RevealAnswer(val index: Int) : SessionCommand
    data class SetShowResult(val index: Int, val value: Boolean) : SessionCommand
    data class UpdateAnalysis(val index: Int, val text: String) : SessionCommand
    data class UpdateSparkAnalysis(val index: Int, val text: String) : SessionCommand
    data class UpdateBaiduAnalysis(val index: Int, val text: String) : SessionCommand
    data class SaveNote(val questionId: Int, val index: Int, val text: String) : SessionCommand
    data class AppendNote(val questionId: Int, val index: Int, val text: String) : SessionCommand
    data class ClearExplanation(val index: Int, val question: Question) : SessionCommand
    data object NavPrevIcon : SessionCommand
    data object NavNextIcon : SessionCommand
    data object NavPrevIconDoubleClick : SessionCommand
    data object NavNextIconDoubleClick : SessionCommand
    data object BrowseAnsweredHistoryOlder : SessionCommand
    data object BrowseAnsweredHistoryNewer : SessionCommand
    data object NavPrevSequential : SessionCommand
    data object NavNextSequential : SessionCommand
    data object ClearEditableQuestion : SessionCommand
    data class PrepareEditableQuestion(val questionId: Int) : SessionCommand
    data class PrepareEditableAtIndex(val index: Int) : SessionCommand
    data class UpdateQuestionAllFields(
        val index: Int,
        val content: String,
        val options: List<String>,
        val answer: String,
        val explanation: String
    ) : SessionCommand
    data class AddHistoryRecord(val score: Int, val total: Int, val unanswered: Int) : SessionCommand
    data class RetryCurrentQuestion(val index: Int) : SessionCommand
    data class RetryWrongBlanks(val index: Int) : SessionCommand
    data class SelectOptionWithSkip(val option: Int, val skipAfterChanged: Boolean) : SessionCommand
    data class SaveEditedQuestionFields(
        val index: Int,
        val content: String,
        val answer: String,
        val options: List<String>
    ) : SessionCommand
    data class SaveEditedQuestion(val edited: Question) : SessionCommand
    data object LeaveReviewSession : SessionCommand
    data object GradeSession : SessionCommand
    data object GradeSessionOnSubmit : SessionCommand
    data object SubmitCurrent : SessionCommand
    data object SubmitSession : SessionCommand
    data object NextQuestion : SessionCommand
    data object PrevQuestion : SessionCommand
    data class EnterReviewSession(val targetProgressId: String) : SessionCommand
    data class EnterExamReviewSession(
        val targetProgressId: String,
        val quizFile: String,
        val questionCount: Int,
        val random: Boolean,
        val wrongBook: Boolean = false,
        val favorite: Boolean = false
    ) : SessionCommand
    data class SetRandomPractice(val enabled: Boolean) : SessionCommand
    data class SetRandomExam(val enabled: Boolean) : SessionCommand
    data class SetMemoryModeConfig(
        val enabled: Boolean,
        val batchSize: Int,
        val wrongMode: Int,
        val poolMode: Int
    ) : SessionCommand
    data class ReloadForFillConfig(val questionCount: Int? = null, val initKey: String? = null) : SessionCommand
    data class SetProgressId(
        val id: String,
        val questionsId: String = id,
        val loadQuestions: Boolean = true,
        val questionCount: Int = 0,
        val random: Boolean = false
    ) : SessionCommand
    data class LoadWrongQuestions(
        val fileName: String,
        val questionCount: Int? = null,
        val random: Boolean? = null
    ) : SessionCommand
    data class LoadFavoriteQuestions(
        val fileName: String,
        val questionCount: Int? = null,
        val random: Boolean? = null
    ) : SessionCommand
    data class LoadQuestions(val quizId: String, val count: Int, val random: Boolean) : SessionCommand
}
