package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState

object ExamFullAnswerNavigation {

    fun listSourceEntryIndices(questions: List<Question>): List<Int> {
        val indices = mutableListOf<Int>()
        val seen = mutableSetOf<Int>()
        questions.forEachIndexed { index, question ->
            val sourceId = extractSourceQuestionId(question.id)
            if (sourceId !in seen) {
                seen.add(sourceId)
                indices.add(index)
            }
        }
        return indices
    }

    /**
     * 双击上一题/下一题：跳过当前词条各轮，跳到相邻词条的首个出现位置。
     * [randomOrder] 为 true 时（考试随机）在其它词条中随机选取。
     */
    fun resolveSkipToAdjacentSourceIndex(
        questions: List<Question>,
        currentIndex: Int,
        forward: Boolean,
        randomOrder: Boolean,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): Int? {
        if (questions.isEmpty()) return null
        val currentQuestion = questions.getOrNull(currentIndex) ?: return null
        val currentSourceId = extractSourceQuestionId(currentQuestion.id)
        if (currentSourceId == currentQuestion.id) return null

        val entries = listSourceEntryIndices(questions)
        if (entries.size <= 1) return null

        val currentEntryPos = entries.indexOfFirst {
            extractSourceQuestionId(questions[it].id) == currentSourceId
        }
        if (currentEntryPos < 0) return null

        if (randomOrder) {
            return entries
                .filter { extractSourceQuestionId(questions[it].id) != currentSourceId }
                .randomOrNull(random)
        }

        return if (forward) {
            entries.getOrNull(currentEntryPos + 1)
        } else {
            entries.getOrNull(currentEntryPos - 1)
        }
    }

    fun hasAnswerContent(state: UnifiedQuestionState): Boolean =
        state.selectedOptions.isNotEmpty() || state.textAnswer.isNotBlank()

    fun isVariantPending(
        question: Question,
        state: UnifiedQuestionState,
        fullAnswerModeActive: Boolean,
        requireCorrect: Boolean,
        sessionGraded: Boolean,
        isCorrect: (Question, UnifiedQuestionState) -> Boolean
    ): Boolean {
        if (!fullAnswerModeActive) {
            return if (sessionGraded) !state.showResult else !hasAnswerContent(state)
        }
        if (!sessionGraded) return !hasAnswerContent(state)
        if (!state.showResult) return true
        return requireCorrect && !isCorrect(question, state)
    }

    fun isCurrentSourceComplete(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerModeActive: Boolean,
        requireCorrect: Boolean,
        sessionGraded: Boolean,
        isCorrect: (Question, UnifiedQuestionState) -> Boolean
    ): Boolean {
        if (!fullAnswerModeActive) return true
        val currentQuestion = questions.getOrNull(currentIndex) ?: return true
        val currentSourceId = extractSourceQuestionId(currentQuestion.id)
        return questionsWithState.indices
            .filter { extractSourceQuestionId(questions[it].id) == currentSourceId }
            .none { index ->
                isVariantPending(
                    questions[index],
                    toUnifiedState(questionsWithState[index]),
                    fullAnswerModeActive,
                    requireCorrect,
                    sessionGraded,
                    isCorrect
                )
            }
    }

    fun currentSourcePendingIndices(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        eligibleIndices: List<Int>,
        fullAnswerModeActive: Boolean,
        requireCorrect: Boolean,
        sessionGraded: Boolean,
        isCorrect: (Question, UnifiedQuestionState) -> Boolean
    ): List<Int> {
        if (!fullAnswerModeActive) return emptyList()
        val currentQuestion = questions.getOrNull(currentIndex) ?: return emptyList()
        val currentSourceId = extractSourceQuestionId(currentQuestion.id)
        return (eligibleIndices + currentIndex)
            .distinct()
            .filter { extractSourceQuestionId(questions[it].id) == currentSourceId }
            .filter { index ->
                isVariantPending(
                    questions[index],
                    toUnifiedState(questionsWithState[index]),
                    fullAnswerModeActive,
                    requireCorrect,
                    sessionGraded,
                    isCorrect
                )
            }
    }

    fun findNextSourceEntryIndices(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerModeActive: Boolean,
        requireCorrect: Boolean,
        sessionGraded: Boolean,
        isCorrect: (Question, UnifiedQuestionState) -> Boolean
    ): List<Int> {
        if (!fullAnswerModeActive) return emptyList()
        val currentQuestion = questions.getOrNull(currentIndex) ?: return emptyList()
        val currentSourceId = extractSourceQuestionId(currentQuestion.id)

        val sourceEntries = mutableListOf<Pair<Int, Int>>()
        val seen = mutableSetOf<Int>()
        questions.forEachIndexed { index, question ->
            val sourceId = extractSourceQuestionId(question.id)
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
                isVariantPending(
                    questions[index],
                    toUnifiedState(questionsWithState[index]),
                    fullAnswerModeActive,
                    requireCorrect,
                    sessionGraded,
                    isCorrect
                )
            }
    }

    fun resolveSequentialNextIndex(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerModeActive: Boolean,
        requireCorrect: Boolean,
        sessionGraded: Boolean,
        isCorrect: (Question, UnifiedQuestionState) -> Boolean
    ): Int? {
        if (!fullAnswerModeActive) return null
        val eligible = questions.indices.toList()
        val sameSourcePending = currentSourcePendingIndices(
            questions, questionsWithState, currentIndex, eligible,
            fullAnswerModeActive, requireCorrect, sessionGraded, isCorrect
        )
        val otherPending = sameSourcePending.filter { it != currentIndex }
        otherPending.firstOrNull { it > currentIndex }?.let { return it }
        otherPending.firstOrNull()?.let { return it }

        if (isCurrentSourceComplete(
                questions, questionsWithState, currentIndex,
                fullAnswerModeActive, requireCorrect, sessionGraded, isCorrect
            )
        ) {
            findNextSourceEntryIndices(
                questions, questionsWithState, currentIndex,
                fullAnswerModeActive, requireCorrect, sessionGraded, isCorrect
            ).firstOrNull()?.let { return it }
        }

        if (!isCurrentSourceComplete(
                questions, questionsWithState, currentIndex,
                fullAnswerModeActive, requireCorrect, sessionGraded, isCorrect
            )
        ) {
            return null
        }

        return questions.indices.firstOrNull { index ->
            index > currentIndex && isVariantPending(
                questions[index],
                toUnifiedState(questionsWithState[index]),
                fullAnswerModeActive,
                requireCorrect,
                sessionGraded,
                isCorrect
            )
        }
    }

    fun resolveCandidateIndices(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        eligibleIndices: List<Int>,
        fullAnswerModeActive: Boolean,
        requireCorrect: Boolean,
        sessionGraded: Boolean,
        isCorrect: (Question, UnifiedQuestionState) -> Boolean
    ): List<Int> {
        if (!fullAnswerModeActive) {
            return eligibleIndices.filter { index ->
                isVariantPending(
                    questions[index],
                    toUnifiedState(questionsWithState[index]),
                    fullAnswerModeActive = false,
                    requireCorrect = false,
                    sessionGraded = sessionGraded,
                    isCorrect = isCorrect
                )
            }.ifEmpty { eligibleIndices }
        }
        if (eligibleIndices.isEmpty()) return emptyList()

        val roundNavigable = ExamFullAnswerRoundNavigablePipeline.navigableIndicesInPool(
            questions = questions,
            questionsWithState = questionsWithState,
            currentIndex = currentIndex,
            fullAnswerRequireCorrect = requireCorrect
        ).filter { index -> index == currentIndex || index in eligibleIndices }
        if (roundNavigable.isNotEmpty()) return roundNavigable.distinct()

        val currentQuestion = questions.getOrNull(currentIndex) ?: return eligibleIndices
        val currentSourceId = extractSourceQuestionId(currentQuestion.id)

        val sameSource = eligibleIndices.filter {
            extractSourceQuestionId(questions[it].id) == currentSourceId
        }
        val sameSourcePending = sameSource.filter { index ->
            isVariantPending(
                questions[index],
                toUnifiedState(questionsWithState[index]),
                fullAnswerModeActive,
                requireCorrect,
                sessionGraded,
                isCorrect
            )
        }
        if (sameSourcePending.isNotEmpty()) return sameSourcePending

        val allPending = eligibleIndices.filter { index ->
            isVariantPending(
                questions[index],
                toUnifiedState(questionsWithState[index]),
                fullAnswerModeActive,
                requireCorrect,
                sessionGraded,
                isCorrect
            )
        }
        return allPending.ifEmpty { eligibleIndices }
    }

    fun toUnifiedState(qws: QuestionWithState): UnifiedQuestionState = UnifiedQuestionState(
        questionId = qws.question.id,
        selectedOptions = qws.selectedOptions,
        textAnswer = qws.textAnswer,
        showResult = qws.showResult,
        analysis = qws.analysis,
        sparkAnalysis = qws.sparkAnalysis,
        baiduAnalysis = qws.baiduAnalysis,
        note = qws.note,
        answerTime = qws.sessionAnswerTime
    )
}
