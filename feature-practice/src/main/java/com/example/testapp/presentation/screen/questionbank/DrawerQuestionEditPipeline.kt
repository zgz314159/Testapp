package com.example.testapp.presentation.screen.questionbank

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.uicommon.util.normalizeEditableFillAnswers

data class DrawerQuestionEditDraft(
    val content: String,
    val answer: String,
    val answerParts: List<String>,
)

object DrawerQuestionEditPipeline {
    fun shouldPrepareEdit(
        requestedEdit: Boolean,
        progressLoaded: Boolean,
        questionsEmpty: Boolean,
        resolvedIndex: Int,
    ): Boolean = !requestedEdit && progressLoaded && !questionsEmpty && resolvedIndex >= 0

    fun draftFromQuestion(question: Question?): DrawerQuestionEditDraft {
        val content = question?.content.orEmpty()
        val answer = question?.answer.orEmpty()
        val parts = if (question?.let { QuestionTypes.isInlineBlank(it.type) } == true) {
            normalizeEditableFillAnswers(content, answer)
        } else {
            listOf(answer)
        }
        return DrawerQuestionEditDraft(content, answer, parts)
    }

    fun buildSavedQuestion(
        editable: Question?,
        newContent: String,
        newOptions: List<String>,
        finalAnswer: String,
    ): Question? = editable?.copy(
        content = newContent,
        answer = finalAnswer,
        options = newOptions,
        isEdited = true,
    )
}
