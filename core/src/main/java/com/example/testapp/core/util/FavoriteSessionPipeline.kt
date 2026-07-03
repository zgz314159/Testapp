package com.example.testapp.core.util

import com.example.testapp.domain.model.FavoriteQuestion

object FavoriteSessionPipeline {
    fun isFavorite(questionId: Int, favorites: List<FavoriteQuestion>): Boolean =
        favorites.any { it.question.id == questionId }

    fun favoriteIds(favorites: List<FavoriteQuestion>): Set<Int> =
        favorites.asSequence().map { it.question.id }.toSet()
}
