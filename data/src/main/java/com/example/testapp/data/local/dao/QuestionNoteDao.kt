package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.QuestionNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionNoteDao {
    @Query("SELECT * FROM question_notes WHERE questionId = :questionId")
    fun getByQuestionId(questionId: Int): Flow<QuestionNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuestionNoteEntity)

    @Query("SELECT note FROM question_notes WHERE questionId = :questionId LIMIT 1")
    suspend fun getNote(questionId: Int): String?

    @Query("DELETE FROM question_notes WHERE questionId = :questionId")
    suspend fun deleteByQuestionId(questionId: Int)
}
