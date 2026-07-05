package com.example.testapp.data.repository.parser

import android.database.sqlite.SQLiteDatabase
import com.example.testapp.data.repository.ImportedQuestionPayload
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SqliteQuestionParser @Inject constructor() : QuestionFileParser {

    override fun parse(file: File, originFileName: String): List<ImportedQuestionPayload> {
        if (!file.exists() || file.length() == 0L) {
            throw LocalizedException(IOConstants.IMPORT_FAILED_FILE_EMPTY_KEY, listOf(file.name))
        }

        val database = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        try {
            val articleRows = mutableListOf<AtomicArticlePayload>()
            database.rawQuery(
                "SELECT item_id, article_no, source, original_text FROM articles ORDER BY article_no ASC, item_id ASC",
                null
            ).use { cursor ->
                val itemIdIndex = cursor.getColumnIndexOrThrow("item_id")
                val articleNoIndex = cursor.getColumnIndexOrThrow("article_no")
                val sourceIndex = cursor.getColumnIndexOrThrow("source")
                val originalTextIndex = cursor.getColumnIndexOrThrow("original_text")

                while (cursor.moveToNext()) {
                    articleRows += AtomicArticlePayload(
                        item_id = cursor.getString(itemIdIndex).orEmpty(),
                        article_no = cursor.getInt(articleNoIndex),
                        source = cursor.getString(sourceIndex).orEmpty(),
                        original_text = cursor.getString(originalTextIndex).orEmpty()
                    )
                }
            }

            if (articleRows.isEmpty()) {
                throw LocalizedException(IOConstants.IMPORT_FAILED_PARSE_KEY, listOf("SQLite 题库未找到 articles 数据"))
            }

            val segmentsByItemId = linkedMapOf<String, MutableList<AtomicSegmentPayload>>()
            database.rawQuery(
                "SELECT item_id, content, tag, weight_score FROM segment_details ORDER BY item_id ASC, sequence_order ASC",
                null
            ).use { cursor ->
                val itemIdIndex = cursor.getColumnIndexOrThrow("item_id")
                val contentIndex = cursor.getColumnIndexOrThrow("content")
                val tagIndex = cursor.getColumnIndexOrThrow("tag")
                val scoreIndex = cursor.getColumnIndexOrThrow("weight_score")

                while (cursor.moveToNext()) {
                    val itemId = cursor.getString(itemIdIndex).orEmpty()
                    if (itemId.isBlank()) continue
                    val bucket = segmentsByItemId.getOrPut(itemId) { mutableListOf() }
                    bucket += AtomicSegmentPayload(
                        t = cursor.getString(contentIndex).orEmpty(),
                        tag = cursor.getString(tagIndex).orEmpty(),
                        s = cursor.getInt(scoreIndex)
                    )
                }
            }

            return articleRows.mapNotNull { article ->
                article.copy(segments = segmentsByItemId[article.item_id].orEmpty())
                    .toImportedQuestionPayload(originFileName)
            }.takeIf { it.isNotEmpty() }
                ?: throw LocalizedException(IOConstants.IMPORT_FAILED_PARSE_KEY, listOf("SQLite 题库未找到可用的 segment_details 数据"))
        } finally {
            database.close()
        }
    }
}
