package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.core.util.splitFillAnswerDescriptors
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question

class PracticeFullAnswerCoordinator(
    private val modeCoordinator: PracticeModeCoordinator
) {

    fun shouldReuseSavedSourceOrder(
        savedSourceOrder: List<Int>,
        expectedQuestionCount: Int,
        expectedSequentialOrder: List<Int>,
        randomEnabled: Boolean
    ): Boolean {
        if (savedSourceOrder.isEmpty() || savedSourceOrder.size != expectedQuestionCount) return false
        return if (randomEnabled) {
            savedSourceOrder != expectedSequentialOrder
        } else {
            savedSourceOrder == expectedSequentialOrder
        }
    }

    fun restoreConfiguredQuestionsForProgress(
        sourceQuestions: List<Question>,
        configuredQuestions: List<Question>,
        existingProgress: PracticeProgress?
    ): List<Question> {
        val progress = existingProgress ?: return configuredQuestions
        if (progress.questionStateMap.isEmpty()) return configuredQuestions
        if (progress.fixedQuestionOrder.isEmpty()) return configuredQuestions
        if (progress.questionStateMap.values.none { modeCoordinator.hasConfiguredQuestionSnapshot(it) }) {
            return configuredQuestions
        }

        val configuredBySourceId: Map<Int, MutableList<Question>> = configuredQuestions
            .groupBy { extractSourceQuestionId(it.id) }
            .mapValues { (_, variants) -> variants.toMutableList() }
            .toMutableMap()
        val savedQuestionIdsBySourceId: Map<Int, List<Int>> =
            progress.fixedQuestionOrder.groupBy(::extractSourceQuestionId)
        val restoredQuestions = mutableListOf<Question>()
        val seenQuestionIds = mutableSetOf<Int>()

        sourceQuestions.forEach { sourceQuestion ->
            val sourceQuestionId = sourceQuestion.id
            val currentVariants = configuredBySourceId[sourceQuestionId]
            val savedQuestionIds = savedQuestionIdsBySourceId[sourceQuestionId].orEmpty()

            savedQuestionIds.forEach { savedQuestionId ->
                val savedState = progress.questionStateMap[savedQuestionId]

                val snapshotMatchedIndex = currentVariants?.indexOfFirst {
                    modeCoordinator.isConfiguredSnapshotCompatible(it, savedState!!)
                } ?: -1
                val idMatchedIndex = currentVariants?.indexOfFirst { it.id == savedQuestionId } ?: -1

                val restoredQuestion: Question? = when {
                    snapshotMatchedIndex >= 0 -> currentVariants?.removeAt(snapshotMatchedIndex)
                    idMatchedIndex >= 0 -> currentVariants?.removeAt(idMatchedIndex)
                    else -> null
                }

                if (restoredQuestion != null && seenQuestionIds.add(restoredQuestion.id)) {
                    restoredQuestions += restoredQuestion
                }
            }

            currentVariants.orEmpty().forEach { currentVariant ->
                if (seenQuestionIds.add(currentVariant.id)) {
                    restoredQuestions += currentVariant
                }
            }
        }

        configuredQuestions.forEach { configuredQuestion ->
            if (seenQuestionIds.add(configuredQuestion.id)) {
                restoredQuestions += configuredQuestion
            }
        }

        return restoredQuestions
    }

    fun shouldApplyDynamicFillTransform(
        questions: List<Question>,
        sourceId: String,
        dynamicFillSensitivityCache: MutableMap<String, Boolean>
    ): Boolean {
        if (questions.isEmpty()) return false
        dynamicFillSensitivityCache[sourceId]?.let { return it }
        if (!sourceId.endsWith(".json", ignoreCase = true) &&
            !sourceId.endsWith(".sqlite", ignoreCase = true) &&
            !sourceId.endsWith(".db", ignoreCase = true)
        ) {
            return false
        }
        if (!questions.all { QuestionTypes.isInlineBlank(it.type) }) return false

        val result = questions.any { question ->
            splitFillAnswerDescriptors(question.answer).any { descriptor ->
                descriptor.score != null || !descriptor.category.isNullOrBlank()
            }
        }
        dynamicFillSensitivityCache[sourceId] = result
        return result
    }
}

