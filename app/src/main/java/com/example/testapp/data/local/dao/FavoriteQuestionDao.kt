package com.example.testapp.data.local.dao

import androidx.room.*
import com.example.testapp.data.local.entity.FavoriteQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteQuestionDao {
    @Query("SELECT * FROM favorite_questions")
    fun getAll(): Flow<List<FavoriteQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favorite: FavoriteQuestionEntity)

    @Delete
    suspend fun remove(favorite: FavoriteQuestionEntity)

    @Query("DELETE FROM favorite_questions WHERE questionId = :questionId")
    suspend fun removeById(questionId: Int)
}
