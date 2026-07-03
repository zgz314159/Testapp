package com.example.testapp.core.util

import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.model.Question
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteSessionPipelineTest {

    private fun question(id: Int) = Question(
        id = id,
        content = "q$id",
        type = "单选",
        options = listOf("A", "B"),
        answer = "A",
        explanation = "",
        isFavorite = false,
        isWrong = false,
        isEdited = false,
        fileName = "test.txt",
        stemImages = emptyList()
    )

    @Test
    fun isFavorite_returnsTrueWhenIdMatches() {
        val favorites = listOf(FavoriteQuestion(question(1)), FavoriteQuestion(question(2)))
        assertTrue(FavoriteSessionPipeline.isFavorite(2, favorites))
    }

    @Test
    fun isFavorite_returnsFalseWhenNotInList() {
        val favorites = listOf(FavoriteQuestion(question(1)))
        assertFalse(FavoriteSessionPipeline.isFavorite(99, favorites))
    }
}
