package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.QuestionNoteEntity

@Dao
interface QuestionNoteDao {
    @Query("SELECT note FROM question_notes WHERE questionId = :id LIMIT 1")
    suspend fun getNote(id: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuestionNoteEntity)
}