package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.LibraryCatalog
import com.example.testapp.domain.repository.WrongBookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWrongBookLibraryCatalogUseCase @Inject constructor(
    private val repository: WrongBookRepository
) {
    operator fun invoke(): Flow<LibraryCatalog> = repository.observeLibraryCatalog()
}
