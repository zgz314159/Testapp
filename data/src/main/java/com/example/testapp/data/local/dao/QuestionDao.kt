package com.example.testapp.data.local.dao

import androidx.room.*
import com.example.testapp.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

data class FileQuestionCountRow(
    val fileName: String,
    val questionCount: Int
)

data class FileQuestionTypeCountRow(
    val fileName: String,
    val type: String,
    val count: Int
)

data class FileQuestionAnswerRow(
    val fileName: String,
    val type: String,
    val answer: String
)

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id")
    fun getAll(): Flow<List<QuestionEntity>>

    @Query("SELECT fileName AS fileName, COUNT(*) AS questionCount FROM questions WHERE fileName IS NOT NULL AND TRIM(fileName) != '' GROUP BY fileName")
    fun getFileQuestionCounts(): Flow<List<FileQuestionCountRow>>

    @Query("SELECT fileName AS fileName, type AS type, COUNT(*) AS count FROM questions WHERE fileName IS NOT NULL AND TRIM(fileName) != '' GROUP BY fileName, type")
    fun getFileQuestionTypeCounts(): Flow<List<FileQuestionTypeCountRow>>

    @Query("SELECT fileName AS fileName, type AS type, answer AS answer FROM questions WHERE fileName IS NOT NULL AND TRIM(fileName) != ''")
    fun getFileQuestionAnswers(): Flow<List<FileQuestionAnswerRow>>

    @Query("SELECT fileName FROM questions WHERE fileName IS NOT NULL AND TRIM(fileName) != '' GROUP BY fileName ORDER BY MIN(id)")
    fun getOrderedFileNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<QuestionEntity>)

    @Query("DELETE FROM questions")
    suspend fun clear()

    @Query("SELECT * FROM questions WHERE fileName = :fileName ORDER BY id")
    fun getQuestionsByFileName(fileName: String): Flow<List<QuestionEntity>>

    @Query("SELECT id FROM questions WHERE fileName = :fileName ORDER BY id")
    suspend fun getQuestionIdsByFileName(fileName: String): List<Int>

    @Query("DELETE FROM questions WHERE fileName = :fileName")
    suspend fun deleteByFileName(fileName: String): Int

    @Transaction
    suspend fun replaceQuestionsByFileName(fileName: String, list: List<QuestionEntity>) {
        deleteByFileName(fileName)
        insertAll(list)
    }
}
