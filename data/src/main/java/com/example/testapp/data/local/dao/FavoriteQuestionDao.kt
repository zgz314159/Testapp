package com.example.testapp.data.local.dao

import androidx.room.*
import com.example.testapp.data.local.entity.FavoriteQuestionEntity
import kotlinx.coroutines.flow.Flow

data class FavoriteFileCountRow(
    val fileName: String,
    val count: Int
)

@Dao
interface FavoriteQuestionDao {
    @Query("SELECT * FROM favorite_questions")
    fun getAll(): Flow<List<FavoriteQuestionEntity>>

    @Query("SELECT q.fileName AS fileName, COUNT(*) AS count FROM favorite_questions f INNER JOIN questions q ON q.id = f.questionId WHERE q.fileName IS NOT NULL AND TRIM(q.fileName) != '' GROUP BY q.fileName")
    fun getCountsByFileName(): Flow<List<FavoriteFileCountRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favorite: FavoriteQuestionEntity)

    @Delete
    suspend fun remove(favorite: FavoriteQuestionEntity)

    @Query("DELETE FROM favorite_questions WHERE questionId = :questionId")
    suspend fun removeById(questionId: Int)
}
