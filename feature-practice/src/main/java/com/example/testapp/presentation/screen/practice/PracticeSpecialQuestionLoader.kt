package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull

class PracticeSpecialQuestionLoader(
    private val facade: PracticeUseCaseFacade,
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val progressId: () -> String,
    private val randomPracticeEnabled: () -> Boolean,
    private val loadProgress: () -> Unit
) {
    suspend fun loadWrongQuestions(fileName: String) {
        val newSessionStartTime = System.currentTimeMillis()
        facade.wrongFavorite.getWrongBook().collect { wrongList ->
            val questions = wrongList.filter { it.question.fileName == fileName }.map { it.question }
            applyQuestions(orderedQuestions(questions), newSessionStartTime)
        }
    }

    suspend fun loadFavoriteQuestions(fileName: String) {
        val newSessionStartTime = System.currentTimeMillis()
        facade.wrongFavorite.getFavorites().collect { favoriteList ->
            val questions = favoriteList.filter { it.question.fileName == fileName }.map { it.question }
            applyQuestions(orderedQuestions(questions), newSessionStartTime)
        }
    }

    private suspend fun orderedQuestions(questions: List<Question>): List<Question> {
        if (!randomPracticeEnabled()) return questions
        val existingProgress = facade.progress.getFlow(progressId()).firstOrNull()
        return if (existingProgress != null) {
            questions.orderByUnansweredFirst(existingProgress)
        } else {
            questions.shuffled()
        }
    }

    private fun List<Question>.orderByUnansweredFirst(progress: PracticeProgress): List<Question> {
        val answeredIds = mutableSetOf<Int>()
        progress.selectedOptions.forEachIndexed { index, options ->
            val showResult = progress.showResultList.getOrElse(index) { false }
            if (index < size && options.isNotEmpty() && showResult) {
                answeredIds.add(this[index].id)
            }
        }

        val unanswered = filter { it.id !in answeredIds }
        val answered = filter { it.id in answeredIds }
        return if (unanswered.isNotEmpty()) {
            unanswered.shuffled() + answered.shuffled()
        } else {
            shuffled()
        }
    }

    private fun applyQuestions(questions: List<Question>, sessionStartTime: Long) {
        sessionState.value = sessionState.value.copy(
            questionsWithState = questions.map { QuestionWithState(question = it) },
            sessionStartTime = sessionStartTime
        )
        loadProgress()
    }
}
