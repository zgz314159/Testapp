package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    /** rowid 即插入序，等同文件夹建立时间升序（旧 → 新）。 */
    @Query("SELECT * FROM folders ORDER BY rowid ASC")
    fun getAll(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FolderEntity)

    @Query("UPDATE folders SET name = :newName WHERE name = :oldName")
    suspend fun rename(oldName: String, newName: String)

    @Query("DELETE FROM folders WHERE name = :name")
    suspend fun delete(name: String)
}
