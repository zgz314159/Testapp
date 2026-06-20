package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.updateAt
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PracticeNoteCoordinator(
    private val facade: PracticeUseCaseFacade,
    private val sessionState: MutableStateFlow<PracticeSessionState>
) {
    private val appendNoteMutex = Mutex()

    fun saveNoteLocally(index: Int, text: String) {
        sessionState.value = sessionState.value.updateAt(index) { it.copy(note = text) }
    }

    suspend fun saveNoteAndWait(questionId: Int, index: Int, text: String): Boolean {
        facade.notes.save(questionId, text)
        saveNoteLocally(index, text)
        return true
    }

    suspend fun appendNote(questionId: Int, index: Int, text: String) {
        appendNoteMutex.withLock {
            val current = facade.notes.get(questionId).getOrNull().orEmpty()
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val timestampedText = "[$timestamp]\n$text"
            val newText = if (current.isBlank()) timestampedText else "$current\n\n$timestampedText"
            facade.notes.save(questionId, newText)
            saveNoteLocally(index, newText)
        }
    }

    suspend fun getNote(questionId: Int): String? = facade.notes.get(questionId).getOrNull()
}
