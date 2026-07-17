package com.example.testapp.domain.repository

import com.example.testapp.domain.model.AdaptiveAtomState

interface AdaptiveAtomRepository {
    suspend fun getStates(bankId: String): List<AdaptiveAtomState>

    suspend fun getState(bankId: String, atomId: Int): AdaptiveAtomState?

    suspend fun upsertStates(states: List<AdaptiveAtomState>)

    suspend fun deleteByBank(bankId: String)
}
