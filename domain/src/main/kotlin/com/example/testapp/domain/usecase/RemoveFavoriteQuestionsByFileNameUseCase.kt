package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.FavoriteQuestionRepository
import javax.inject.Inject

// 新增：按 fileName 批量移除所有收藏
class RemoveFavoriteQuestionsByFileNameUseCase @Inject constructor(
    private val repo: FavoriteQuestionRepository
) {
    suspend operator fun invoke(fileName: String) = repo.removeByFileName(fileName)
}
