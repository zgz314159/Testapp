package com.example.testapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.dao.HistoryRecordDao
import com.example.testapp.data.local.dao.PracticeProgressDao
import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.dao.WrongQuestionDao
import com.example.testapp.data.local.dao.ExamProgressDao
import com.example.testapp.data.local.dao.QuestionAnalysisDao
import com.example.testapp.data.local.dao.QuestionNoteDao
import com.example.testapp.data.local.dao.QuestionAskDao
import com.example.testapp.data.local.entity.ExamProgressEntity
import com.example.testapp.data.local.entity.FavoriteQuestionEntity
import com.example.testapp.data.local.entity.HistoryRecordEntity
import com.example.testapp.data.local.entity.PracticeProgressEntity
import com.example.testapp.data.local.entity.QuestionEntity
import com.example.testapp.data.local.entity.WrongQuestionEntity
import com.example.testapp.data.local.entity.QuestionAnalysisEntity
import com.example.testapp.data.local.entity.QuestionNoteEntity
import com.example.testapp.data.local.entity.QuestionAskEntity
import com.example.testapp.data.local.entity.FileFolderEntity
import com.example.testapp.data.local.entity.FolderEntity
import com.example.testapp.data.local.entity.converter.BooleanListConverter
import com.example.testapp.data.local.entity.converter.IntListConverter
import com.example.testapp.data.local.entity.converter.NestedIntListConverter
import com.example.testapp.data.local.entity.converter.StringListConverter

@Database(
    entities = [QuestionEntity::class, WrongQuestionEntity::class, HistoryRecordEntity::class, FavoriteQuestionEntity::class, PracticeProgressEntity::class, ExamProgressEntity::class, QuestionAnalysisEntity::class, QuestionNoteEntity::class, QuestionAskEntity::class, FileFolderEntity::class, FolderEntity::class],
    version = 21,
    exportSchema = false
)
@TypeConverters(IntListConverter::class, BooleanListConverter::class, NestedIntListConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun wrongQuestionDao(): WrongQuestionDao
    abstract fun historyRecordDao(): HistoryRecordDao
    abstract fun favoriteQuestionDao(): FavoriteQuestionDao
    abstract fun practiceProgressDao(): PracticeProgressDao
    abstract fun examProgressDao(): ExamProgressDao
    abstract fun questionAnalysisDao(): QuestionAnalysisDao
    abstract fun questionNoteDao(): QuestionNoteDao
    abstract fun questionAskDao(): QuestionAskDao
    abstract fun fileFolderDao(): com.example.testapp.data.local.dao.FileFolderDao
    abstract fun folderDao(): com.example.testapp.data.local.dao.FolderDao
}

