package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.common.practiceProgressBaseId
import com.example.testapp.domain.model.PracticeProgress

fun practiceProgressMatchesFile(progressId: String, fileName: String): Boolean {
    val baseId = practiceProgressBaseId(fileName)
    return progressId == baseId || progressId.startsWith(baseId + "__scope=")
}

fun practiceProgressAnsweredCount(progress: PracticeProgress): Int {
    if (progress.questionStateMap.isNotEmpty()) {
        return progress.questionStateMap.values.count { state ->
            state.textAnswer.isNotBlank() || state.selectedOptions.isNotEmpty()
        }
    }
    return progress.answeredList.size
}

fun preferredHomePracticeProgress(
    fileName: String,
    progressById: Map<String, PracticeProgress>
): PracticeProgress? {
    val matchingProgress = progressById.values.filter { progress ->
        practiceProgressMatchesFile(progress.id, fileName)
    }
    if (matchingProgress.isEmpty()) return null
    val baseId = practiceProgressBaseId(fileName)
    return matchingProgress.firstOrNull { progress -> progress.id == baseId }
        ?: matchingProgress.maxByOrNull { progress -> progress.timestamp }
}

fun homePracticeProgressCount(
    fileName: String,
    progressById: Map<String, PracticeProgress>
): Int = progressById.values
    .filter { progress -> practiceProgressMatchesFile(progress.id, fileName) }
    .maxOfOrNull(::practiceProgressAnsweredCount)
    ?: 0

fun buildHomePracticeProgressMap(
    fileNames: List<String>,
    progressById: Map<String, PracticeProgress>
): Map<String, Int> = fileNames.mapNotNull { fileName ->
    val count = homePracticeProgressCount(fileName, progressById)
    fileName.takeIf { count > 0 }?.let { it to count }
}.toMap()
