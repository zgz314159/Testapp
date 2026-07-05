package com.example.testapp.data.repository.parser

import com.example.testapp.data.repository.ImportedQuestionPayload
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import com.example.testapp.domain.model.Question
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonQuestionParser @Inject constructor() : QuestionFileParser {

    companion object {
        const val INLINE_BLANK_PLACEHOLDER = "【   】"
    }

    private val importJson = Json {
        ignoreUnknownKeys = true
    }

    override fun parse(file: File, originFileName: String): List<ImportedQuestionPayload> {
        val content = file.readText()
        if (content.isBlank()) throw LocalizedException(IOConstants.IMPORT_FAILED_FILE_EMPTY_KEY, listOf(file.name))

        runCatching {
            importJson.decodeFromString<List<Question>>(content)
                .map { importedQuestion ->
                    ImportedQuestionPayload(
                        question = importedQuestion.copy(
                            id = 0,
                            isFavorite = false,
                            isWrong = false,
                            isEdited = false,
                            fileName = originFileName
                        )
                    )
                }
        }.getOrNull()?.takeIf { it.isNotEmpty() }?.let { return it }

        runCatching {
            importJson.decodeFromString<List<AtomicArticlePayload>>(content)
                .mapNotNull { article -> article.toImportedQuestionPayload(originFileName) }
        }.getOrNull()?.takeIf { it.isNotEmpty() }?.let { return it }

        runCatching {
            importJson.decodeFromString<AtomicArticleIndexPayload>(content)
        }.getOrNull()?.let {
            throw LocalizedException(
                IOConstants.IMPORT_FAILED_PARSE_KEY,
                listOf("检测到原子题库索引 JSON，请改为导入包含 segments 的全量原子题库 JSON 文件")
            )
        }

        throw LocalizedException(IOConstants.IMPORT_FAILED_PARSE_KEY, listOf("不支持的 JSON 题库格式"))
    }
}
