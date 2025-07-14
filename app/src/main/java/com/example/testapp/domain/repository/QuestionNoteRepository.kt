package com.example.testapp.domain.repository

interface QuestionNoteRepository {
    suspend fun getNote(questionId: Int): String?
    suspend fun saveNote(questionId: Int, note: String)
}