package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    /** rowid 即插入序；按建立时间倒序排列，新建文件夹位于文件夹区首端。 */
    @Query("SELECT * FROM folders ORDER BY rowid DESC")
    fun getAll(): Flow<List<FolderEntity>>

    // IGNORE：重复 addFolder（重名新建/拖拽建组竞态）不得 REPLACE 重插，
    // 否则旧文件夹 rowid 被顶到最大，会被误当成最新创建并移到首端。
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FolderEntity)

    @Query("UPDATE folders SET name = :newName WHERE name = :oldName")
    suspend fun rename(oldName: String, newName: String)

    @Query("DELETE FROM folders WHERE name = :name")
    suspend fun delete(name: String)
}
