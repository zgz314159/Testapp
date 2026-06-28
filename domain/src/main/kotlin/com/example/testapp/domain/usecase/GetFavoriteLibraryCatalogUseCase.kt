package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.LibraryCatalog
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteLibraryCatalogUseCase @Inject constructor(
    private val repository: FavoriteQuestionRepository
) {
    operator fun invoke(): Flow<LibraryCatalog> = repository.observeLibraryCatalog()
}
