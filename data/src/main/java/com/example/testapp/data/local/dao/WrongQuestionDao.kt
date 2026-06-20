package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.testapp.data.local.entity.WrongQuestionEntity
import kotlinx.coroutines.flow.Flow

data class FileRelatedCountRow(
    val fileName: String,
    val count: Int
)

@Dao
interface WrongQuestionDao {
    @Query("SELECT * FROM wrong_questions")
    fun getAll(): Flow<List<WrongQuestionEntity>>

    @Query("SELECT q.fileName AS fileName, COUNT(DISTINCT w.questionId) AS count FROM wrong_questions w INNER JOIN questions q ON q.id = w.questionId WHERE q.fileName IS NOT NULL AND TRIM(q.fileName) != '' GROUP BY q.fileName")
    fun getCountsByFileName(): Flow<List<FileRelatedCountRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(entity: WrongQuestionEntity)

    @Query("DELETE FROM wrong_questions")
    suspend fun clear()

    @Query("DELETE FROM wrong_questions WHERE questionId IN (SELECT id FROM questions WHERE fileName = :fileName)")
    suspend fun removeByFileName(fileName: String): Int
}
