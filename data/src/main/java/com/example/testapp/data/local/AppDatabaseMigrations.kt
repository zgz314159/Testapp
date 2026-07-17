package com.example.testapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseMigrations {
    val migration26To27 =
        object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `adaptive_atom_states` (
                        `bankId` TEXT NOT NULL,
                        `atomId` INTEGER NOT NULL,
                        `sourceQuestionId` INTEGER NOT NULL,
                        `blankIndex` INTEGER NOT NULL,
                        `tag` TEXT NOT NULL,
                        `weight` INTEGER NOT NULL,
                        `pool` TEXT NOT NULL,
                        `stage` TEXT NOT NULL,
                        `correctStreak` INTEGER NOT NULL,
                        `lapseCount` INTEGER NOT NULL,
                        `reviewCount` INTEGER NOT NULL,
                        `dueAt` INTEGER NOT NULL,
                        `lastReviewedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`bankId`, `atomId`)
                    )
                    """.trimIndent(),
                )
            }
        }
}
