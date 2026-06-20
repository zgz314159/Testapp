package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.FileFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileFolderDao {
    @Query("SELECT * FROM file_folders")
    fun getAll(): Flow<List<FileFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FileFolderEntity)

    @Query("DELETE FROM file_folders WHERE fileName = :fileName")
    suspend fun deleteByFile(fileName: String)

    @Query("SELECT folderName FROM file_folders WHERE fileName = :fileName LIMIT 1")
    suspend fun getFolderForFile(fileName: String): String?

    @Query("UPDATE file_folders SET folderName = :newName WHERE folderName = :oldName")
    suspend fun renameFolder(oldName: String, newName: String)

    @Query("DELETE FROM file_folders WHERE folderName = :folderName")
    suspend fun deleteByFolder(folderName: String)
}
