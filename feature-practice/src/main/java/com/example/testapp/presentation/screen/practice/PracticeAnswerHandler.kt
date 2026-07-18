package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.core.util.isFillAnswerCorrect
import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState

/**
 * Pure answer-evaluation logic extracted from PracticeViewModel.
 * Does NOT mutate session state �?only computes truth values.
 */
class PracticeAnswerHandler {

    fun isQuestionAnswered(questionWithState: QuestionWithState): Boolean {
        return questionWithState.isAnswered
    }

    fun isQuestionAnswered(state: UnifiedQuestionState): Boolean {
        return state.textAnswer.isNotBlank() || state.selectedOptions.isNotEmpty()
    }

    fun hasAnswerContent(questionWithState: QuestionWithState): Boolean {
        return if (QuestionTypes.isFill(questionWithState.question.type)) {
            questionWithState.textAnswer.isNotBlank()
        } else {
            questionWithState.selectedOptions.isNotEmpty()
        }
    }

    fun isQuestionCorrect(question: Question, state: UnifiedQuestionState): Boolean {
        if (!isQuestionAnswered(state)) return false
        return if (QuestionTypes.isFill(question.type)) {
            isFillAnswerCorrect(state.textAnswer, resolveFillCorrectAnswer(question))
        } else {
            state.selectedOptions.sorted() == answerToOptionIndices(question).sorted()
        }
    }

    fun isQuestionPendingForCurrentMode(
        questionWithState: QuestionWithState,
        fullAnswerModeActive: Boolean,
        fullAnswerRequireCorrect: Boolean
    ): Boolean {
        // 「答错需重答」是全答模式的子设置：模式未激活时残留开关不得串扰其他出题模式。
        if (fullAnswerModeActive) {
            if (!questionWithState.showResult) return true
            if (fullAnswerRequireCorrect && questionWithState.isCorrect != true) return true
            return false
        }
        return !isQuestionAnswered(questionWithState) || shouldReopenUnansweredReveal(questionWithState)
    }

    fun shouldReopenUnansweredReveal(questionWithState: QuestionWithState): Boolean {
        return questionWithState.showResult && !hasAnswerContent(questionWithState)
    }

    fun hasPendingQuestions(
        questionsWithState: List<QuestionWithState>,
        fullAnswerModeActive: Boolean,
        fullAnswerRequireCorrect: Boolean
    ): Boolean {
        return questionsWithState.any { isQuestionPendingForCurrentMode(it, fullAnswerModeActive, fullAnswerRequireCorrect) }
    }

    fun findFirstPendingIndex(
        questionsWithState: List<QuestionWithState>,
        fullAnswerModeActive: Boolean,
        fullAnswerRequireCorrect: Boolean
    ): Int {
        return questionsWithState.indexOfFirst { isQuestionPendingForCurrentMode(it, fullAnswerModeActive, fullAnswerRequireCorrect) }
            .takeIf { it >= 0 }
            ?: 0
    }

    fun findResumeIndex(
        questionsWithState: List<QuestionWithState>,
        savedIndex: Int,
        fullAnswerModeActive: Boolean,
        fullAnswerRequireCorrect: Boolean
    ): Int {
        if (questionsWithState.isEmpty()) return 0

        val clampedIndex = savedIndex.coerceIn(0, questionsWithState.lastIndex)
        if (!isQuestionPendingForCurrentMode(questionsWithState[clampedIndex], fullAnswerModeActive, fullAnswerRequireCorrect)) {
            return clampedIndex
        }

        return questionsWithState.indices.firstOrNull { index ->
            index >= clampedIndex && isQuestionPendingForCurrentMode(questionsWithState[index], fullAnswerModeActive, fullAnswerRequireCorrect)
        } ?: questionsWithState.indices.firstOrNull { index ->
            isQuestionPendingForCurrentMode(questionsWithState[index], fullAnswerModeActive, fullAnswerRequireCorrect)
        } ?: clampedIndex
    }

    fun currentSourcePendingIndices(
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        eligibleIndices: List<Int>,
        fullAnswerModeActive: Boolean,
        fullAnswerRequireCorrect: Boolean
    ): List<Int> {
        val currentQuestion = questionsWithState.getOrNull(currentIndex)?.question ?: return emptyList()
        val currentSourceQuestionId = extractSourceQuestionId(currentQuestion.id)

        return (eligibleIndices + currentIndex)
            .distinct()
            .filter { index ->
                extractSourceQuestionId(questionsWithState[index].question.id) == currentSourceQuestionId
            }
            .filter { index -> isQuestionPendingForCurrentMode(questionsWithState[index], fullAnswerModeActive, fullAnswerRequireCorrect) }
    }

    fun currentFullAnswerCandidateIndices(
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        eligibleIndices: List<Int>,
        fullAnswerRequireCorrect: Boolean
    ): List<Int> {
        if (eligibleIndices.isEmpty()) return emptyList()

        val currentQuestion = questionsWithState.getOrNull(currentIndex)?.question ?: return emptyList()
        val currentSourceQuestionId = extractSourceQuestionId(currentQuestion.id)

        val sameSourceIndices = eligibleIndices.filter { index ->
            extractSourceQuestionId(questionsWithState[index].question.id) == currentSourceQuestionId
        }
        val sameSourceUnreviewed = sameSourceIndices.filter { index ->
            !questionsWithState[index].showResult
        }
        if (sameSourceUnreviewed.isNotEmpty()) return sameSourceUnreviewed

        if (fullAnswerRequireCorrect) {
            val sameSourceIncorrect = sameSourceIndices.filter { index ->
                val questionWithState = questionsWithState[index]
                questionWithState.showResult && questionWithState.isCorrect != true
            }
            if (sameSourceIncorrect.isNotEmpty()) return sameSourceIncorrect
        }

        val unreviewedIndices = eligibleIndices.filter { index ->
            !questionsWithState[index].showResult
        }
        if (unreviewedIndices.isNotEmpty()) return unreviewedIndices

        if (fullAnswerRequireCorrect) {
            return eligibleIndices.filter { index ->
                val questionWithState = questionsWithState[index]
                questionWithState.showResult && questionWithState.isCorrect != true
            }
        }

        return emptyList()
    }

    fun nextFullAnswerCandidateIndices(
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        eligibleIndices: List<Int>,
        fullAnswerModeActive: Boolean,
        fullAnswerRequireCorrect: Boolean
    ): List<Int> {
        if (!fullAnswerModeActive) return emptyList()
        if (eligibleIndices.isEmpty()) return emptyList()

        val currentQuestion = questionsWithState.getOrNull(currentIndex)?.question ?: return emptyList()
        val currentSourceQuestionId = extractSourceQuestionId(currentQuestion.id)

        val sameSourceUnreviewed = eligibleIndices.filter { index ->
            index != currentIndex &&
                extractSourceQuestionId(questionsWithState[index].question.id) == currentSourceQuestionId &&
                !questionsWithState[index].showResult
        }
        if (sameSourceUnreviewed.isNotEmpty()) return sameSourceUnreviewed

        val unreviewedIndices = eligibleIndices.filter { index ->
            index != currentIndex && !questionsWithState[index].showResult
        }
        if (unreviewedIndices.isNotEmpty()) return unreviewedIndices

        if (fullAnswerRequireCorrect) {
            return eligibleIndices.filter { index ->
                val questionWithState = questionsWithState[index]
                questionWithState.showResult && questionWithState.isCorrect != true
            }
        }

        return emptyList()
    }

    fun findNextSourceEntryIndices(
        currentState: PracticeSessionState,
        fullAnswerModeActive: Boolean,
        fullAnswerRequireCorrect: Boolean
    ): List<Int> {
        if (!fullAnswerModeActive) return emptyList()
        val questionsWithState = currentState.questionsWithState
        if (questionsWithState.isEmpty()) return emptyList()

        val currentQuestion = questionsWithState.getOrNull(currentState.currentIndex)?.question ?: return emptyList()
        val currentSourceId = extractSourceQuestionId(currentQuestion.id)

        val sourceEntries = mutableListOf<Pair<Int, Int>>()
        val seen = mutableSetOf<Int>()
        questionsWithState.forEachIndexed { index, qws ->
            val sourceId = extractSourceQuestionId(qws.question.id)
            if (sourceId !in seen) {
                seen.add(sourceId)
                sourceEntries.add(sourceId to index)
            }
        }

        val currentSourcePosition = sourceEntries.indexOfFirst { it.first == currentSourceId }
        if (currentSourcePosition < 0) return emptyList()

        return sourceEntries
            .drop(currentSourcePosition + 1)
            .map { it.second }
            .filter { index ->
                isQuestionPendingForCurrentMode(
                    questionsWithState[index],
                    fullAnswerModeActive,
                    fullAnswerRequireCorrect
                )
            }
    }

    fun isCurrentSourceComplete(
        currentState: PracticeSessionState,
        fullAnswerModeActive: Boolean,
        fullAnswerRequireCorrect: Boolean
    ): Boolean {
        val questionsWithState = currentState.questionsWithState
        val currentQuestion = questionsWithState.getOrNull(currentState.currentIndex)?.question ?: return true
        val currentSourceQuestionId = extractSourceQuestionId(currentQuestion.id)

        return questionsWithState.indices
            .filter { index -> extractSourceQuestionId(questionsWithState[index].question.id) == currentSourceQuestionId }
            .none { index -> isQuestionPendingForCurrentMode(questionsWithState[index], fullAnswerModeActive, fullAnswerRequireCorrect) }
    }
}

