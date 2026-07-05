package com.example.testapp.data.repository.parser

import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import com.example.testapp.domain.model.Question
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TxtQuestionParser @Inject constructor() : SimpleQuestionFileParser {

    override fun parse(file: File, originFileName: String): List<Question> {
        try {
            if (file.length() == 0L) throw LocalizedException(IOConstants.IMPORT_FAILED_TXT_EMPTY_KEY, listOf(file.name))
            val lines = file.readLines()
            if (lines.isEmpty()) throw LocalizedException(IOConstants.IMPORT_FAILED_TXT_NO_CONTENT_KEY, listOf(file.name))
            val questions = lines.mapNotNull { line ->
                try {
                    val parts = line.split("|")
                    if (parts.size >= 11) {
                        Question(
                            id = 0,
                            content = parts[0],
                            type = parts[1],
                            options = parts.slice(2..8).filter { it.isNotBlank() },
                            explanation = parts[9],
                            answer = parts[10],
                            isFavorite = false,
                            isWrong = false,
                            fileName = originFileName
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            if (questions.isEmpty()) throw LocalizedException(IOConstants.IMPORT_FAILED_TXT_PARSE_KEY, listOf(file.name))
            return questions
        } catch (e: LocalizedException) {
            throw e
        } catch (e: Exception) {
            throw LocalizedException(IOConstants.IMPORT_FAILED_TXT_PARSE_KEY, listOf(e.message ?: ""))
        }
    }
}
