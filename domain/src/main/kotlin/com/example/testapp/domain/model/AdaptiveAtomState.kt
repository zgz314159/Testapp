package com.example.testapp.domain.model

enum class AdaptiveAtomStage {
    CHOICE,
    HINTED,
    RECALL,
    MATURE,
}

enum class AdaptiveAtomPool {
    CORE,
    PROBE,
}

data class AdaptiveAtomState(
    val bankId: String,
    val atomId: Int,
    val sourceQuestionId: Int,
    val blankIndex: Int,
    val tag: String,
    val weight: Int,
    val pool: AdaptiveAtomPool,
    val stage: AdaptiveAtomStage = AdaptiveAtomStage.CHOICE,
    val correctStreak: Int = 0,
    val lapseCount: Int = 0,
    val reviewCount: Int = 0,
    val dueAt: Long = 0L,
    val lastReviewedAt: Long = 0L,
)
