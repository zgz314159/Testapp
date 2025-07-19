package com.example.testapp.data.local.dao

import androidx.room.*
import com.example.testapp.data.local.entity.FileFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileFolderDao {
    @Query("SELECT * FROM file_folders")
    fun getAll(): Flow<List<FileFolderEntity>>

    @Query("SELECT folderName FROM file_folders WHERE fileName = :fileName")
    suspend fun getFolderForFile(fileName: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FileFolderEntity)

    @Query("DELETE FROM file_folders WHERE fileName = :fileName")
    suspend fun delete(fileName: String)
}