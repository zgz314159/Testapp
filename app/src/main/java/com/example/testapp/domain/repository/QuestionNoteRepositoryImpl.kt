package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionNoteDao
import com.example.testapp.data.local.entity.QuestionNoteEntity
import com.example.testapp.domain.repository.QuestionNoteRepository
import javax.inject.Inject

class QuestionNoteRepositoryImpl @Inject constructor(
    private val dao: QuestionNoteDao
) : QuestionNoteRepository {
    private val cache = mutableMapOf<Int, String>()

    override suspend fun getNote(questionId: Int): String? {
        cache[questionId]?.let { return it }
        val result = dao.getNote(questionId)
        if (result != null) cache[questionId] = result
        return result
    }

    override suspend fun saveNote(questionId: Int, note: String) {
        dao.upsert(QuestionNoteEntity(questionId = questionId, note = note))
        cache[questionId] = note
    }
}