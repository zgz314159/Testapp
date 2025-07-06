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
import com.example.testapp.data.local.entity.ExamProgressEntity
import com.example.testapp.data.local.entity.FavoriteQuestionEntity
import com.example.testapp.data.local.entity.HistoryRecordEntity
import com.example.testapp.data.local.entity.PracticeProgressEntity
import com.example.testapp.data.local.entity.QuestionEntity
import com.example.testapp.data.local.entity.WrongQuestionEntity
import com.example.testapp.data.local.entity.converter.BooleanListConverter
import com.example.testapp.data.local.entity.converter.IntListConverter

@Database(
    entities = [QuestionEntity::class, WrongQuestionEntity::class, HistoryRecordEntity::class, FavoriteQuestionEntity::class, PracticeProgressEntity::class,ExamProgressEntity::class],
    version = 5, // 升级版本号，修复 Room schema 校验崩溃
    exportSchema = false
)
@TypeConverters(IntListConverter::class, BooleanListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun wrongQuestionDao(): WrongQuestionDao
    abstract fun historyRecordDao(): HistoryRecordDao
    abstract fun favoriteQuestionDao(): FavoriteQuestionDao
    abstract fun practiceProgressDao(): PracticeProgressDao
    abstract fun examProgressDao(): ExamProgressDao
}
