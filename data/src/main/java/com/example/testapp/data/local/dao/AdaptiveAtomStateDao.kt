package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.AdaptiveAtomStateEntity

@Dao
interface AdaptiveAtomStateDao {
    @Query("SELECT * FROM adaptive_atom_states WHERE bankId = :bankId")
    suspend fun getByBank(bankId: String): List<AdaptiveAtomStateEntity>

    @Query("SELECT * FROM adaptive_atom_states WHERE bankId = :bankId AND atomId = :atomId LIMIT 1")
    suspend fun get(bankId: String, atomId: Int): AdaptiveAtomStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(states: List<AdaptiveAtomStateEntity>)

    @Query("DELETE FROM adaptive_atom_states WHERE bankId = :bankId")
    suspend fun deleteByBank(bankId: String)
}
