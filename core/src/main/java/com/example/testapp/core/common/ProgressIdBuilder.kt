package com.example.testapp.core.common

private const val PROGRESS_SCOPE_SEPARATOR = "__scope="

fun stripProgressScope(id: String): String =
    id.substringBefore(PROGRESS_SCOPE_SEPARATOR)

fun buildScopedProgressId(
    baseId: String,
    questionCount: Int,
    randomEnabled: Boolean,
    memoryModeEnabled: Boolean,
    memoryBatchSize: Int,
    memoryWrongMode: Int,
    memoryPoolMode: Int
): String {
    if (questionCount <= 0 && !randomEnabled && !memoryModeEnabled) {
        return baseId
    }

    val modeParts = mutableListOf("q=${if (questionCount > 0) questionCount else "all"}")
    if (randomEnabled) {
        modeParts += "r=1"
    }
    if (memoryModeEnabled) {
        modeParts += "m=1"
        modeParts += "mb=$memoryBatchSize"
        modeParts += "mw=$memoryWrongMode"
        modeParts += "mp=$memoryPoolMode"
    }

    return "$baseId$PROGRESS_SCOPE_SEPARATOR${modeParts.joinToString(";")}"
}

fun examProgressBaseId(id: String): String =
    stripProgressScope(if (id.startsWith("exam_")) id else "exam_$id")

fun practiceProgressBaseId(id: String): String =
    stripProgressScope(if (id.startsWith("practice_")) id else "practice_$id")

fun buildExamProgressId(
    id: String,
    questionCount: Int,
    randomEnabled: Boolean,
    memoryModeEnabled: Boolean,
    memoryBatchSize: Int,
    memoryWrongMode: Int,
    memoryPoolMode: Int
): String {
    return buildScopedProgressId(
        baseId = examProgressBaseId(id),
        questionCount = questionCount,
        randomEnabled = randomEnabled,
        memoryModeEnabled = memoryModeEnabled,
        memoryBatchSize = memoryBatchSize,
        memoryWrongMode = memoryWrongMode,
        memoryPoolMode = memoryPoolMode
    )
}

fun buildPracticeProgressId(
    id: String,
    questionCount: Int,
    randomEnabled: Boolean,
    memoryModeEnabled: Boolean,
    memoryBatchSize: Int,
    memoryWrongMode: Int,
    memoryPoolMode: Int
): String {
    return buildScopedProgressId(
        baseId = practiceProgressBaseId(id),
        questionCount = questionCount,
        randomEnabled = false,
        memoryModeEnabled = memoryModeEnabled,
        memoryBatchSize = memoryBatchSize,
        memoryWrongMode = memoryWrongMode,
        memoryPoolMode = memoryPoolMode
    )
}
