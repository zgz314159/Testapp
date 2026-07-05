package com.example.testapp.presentation.screen.questionbank

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawerQuestionEditPipelineTest {
    @Test
    fun shouldPrepareEdit_whenReadyAndNotYetRequested() {
        assertTrue(
            DrawerQuestionEditPipeline.shouldPrepareEdit(
                requestedEdit = false,
                progressLoaded = true,
                questionsEmpty = false,
                resolvedIndex = 2,
            )
        )
    }

    @Test
    fun shouldNotPrepareEdit_whenAlreadyRequested() {
        assertFalse(
            DrawerQuestionEditPipeline.shouldPrepareEdit(
                requestedEdit = true,
                progressLoaded = true,
                questionsEmpty = false,
                resolvedIndex = 2,
            )
        )
    }

    @Test
    fun draftFromQuestion_inlineBlank_splitsAnswerParts() {
        val question = Question(
            id = 1,
            content = "A___B",
            answer = "x|y",
            type = QuestionTypes.BLANK,
            options = emptyList(),
            explanation = "",
        )
        val draft = DrawerQuestionEditPipeline.draftFromQuestion(question)
        assertEquals("A___B", draft.content)
        assertEquals("x|y", draft.answer)
        assertEquals(2, draft.answerParts.size)
    }

    @Test
    fun buildSavedQuestion_returnsEditedCopy() {
        val original = Question(
            id = 1,
            content = "old",
            answer = "a",
            type = QuestionTypes.SINGLE,
            options = listOf("A", "B"),
            explanation = "",
        )
        val saved = DrawerQuestionEditPipeline.buildSavedQuestion(
            editable = original,
            newContent = "new",
            newOptions = listOf("C", "D"),
            finalAnswer = "b",
        )
        requireNotNull(saved)
        assertEquals("new", saved.content)
        assertEquals("b", saved.answer)
        assertEquals(listOf("C", "D"), saved.options)
        assertTrue(saved.isEdited)
    }

    @Test
    fun buildSavedQuestion_nullEditable_returnsNull() {
        assertNull(
            DrawerQuestionEditPipeline.buildSavedQuestion(
                editable = null,
                newContent = "new",
                newOptions = emptyList(),
                finalAnswer = "b",
            )
        )
    }
}
