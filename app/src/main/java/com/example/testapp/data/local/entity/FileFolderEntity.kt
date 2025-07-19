package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file_folders")
data class FileFolderEntity(
    @PrimaryKey val fileName: String,
    val folderName: String
)