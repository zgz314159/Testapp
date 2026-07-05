package com.example.testapp.data.repository.parser

import com.example.testapp.data.repository.ImportedQuestionPayload
import com.example.testapp.domain.model.Question
import java.io.File

interface QuestionFileParser {
    fun parse(file: File, originFileName: String): List<ImportedQuestionPayload>
}

interface SimpleQuestionFileParser {
    fun parse(file: File, originFileName: String): List<Question>
}
