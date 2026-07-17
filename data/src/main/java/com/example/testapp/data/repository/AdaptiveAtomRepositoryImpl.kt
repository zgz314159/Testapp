package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.AdaptiveAtomStateDao
import com.example.testapp.data.local.entity.AdaptiveAtomStateEntity
import com.example.testapp.domain.model.AdaptiveAtomPool
import com.example.testapp.domain.model.AdaptiveAtomStage
import com.example.testapp.domain.model.AdaptiveAtomState
import com.example.testapp.domain.repository.AdaptiveAtomRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdaptiveAtomRepositoryImpl
    @Inject
    constructor(
        private val dao: AdaptiveAtomStateDao,
    ) : AdaptiveAtomRepository {
        override suspend fun getStates(bankId: String): List<AdaptiveAtomState> =
            dao.getByBank(bankId).map(AdaptiveAtomStateEntity::toDomain)

        override suspend fun getState(bankId: String, atomId: Int): AdaptiveAtomState? =
            dao.get(bankId, atomId)?.toDomain()

        override suspend fun upsertStates(states: List<AdaptiveAtomState>) {
            if (states.isNotEmpty()) dao.upsertAll(states.map(AdaptiveAtomState::toEntity))
        }

        override suspend fun deleteByBank(bankId: String) = dao.deleteByBank(bankId)
    }

private fun AdaptiveAtomStateEntity.toDomain(): AdaptiveAtomState =
    AdaptiveAtomState(
        bankId = bankId,
        atomId = atomId,
        sourceQuestionId = sourceQuestionId,
        blankIndex = blankIndex,
        tag = tag,
        weight = weight,
        pool = enumValueOrDefault(pool, AdaptiveAtomPool.PROBE),
        stage = enumValueOrDefault(stage, AdaptiveAtomStage.CHOICE),
        correctStreak = correctStreak,
        lapseCount = lapseCount,
        reviewCount = reviewCount,
        dueAt = dueAt,
        lastReviewedAt = lastReviewedAt,
    )

private fun AdaptiveAtomState.toEntity(): AdaptiveAtomStateEntity =
    AdaptiveAtomStateEntity(
        bankId = bankId,
        atomId = atomId,
        sourceQuestionId = sourceQuestionId,
        blankIndex = blankIndex,
        tag = tag,
        weight = weight,
        pool = pool.name,
        stage = stage.name,
        correctStreak = correctStreak,
        lapseCount = lapseCount,
        reviewCount = reviewCount,
        dueAt = dueAt,
        lastReviewedAt = lastReviewedAt,
    )

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback
