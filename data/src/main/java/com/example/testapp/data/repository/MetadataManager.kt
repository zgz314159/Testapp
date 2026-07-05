package com.example.testapp.data.repository

import com.example.testapp.domain.model.Question
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataManager @Inject constructor() {

    fun quizStorageDir(): File {
        return File("/data/data/com.example.testapp/files/quiz/").apply {
            if (!exists()) mkdirs()
        }
    }

    fun editedMetadataFile(fileName: String): File {
        return File(quizStorageDir(), "$fileName.edited.json")
    }

    fun loadEditedQuestionIds(fileName: String?): Set<Int> {
        if (fileName.isNullOrBlank()) return emptySet()
        val metadataFile = editedMetadataFile(fileName)
        if (!metadataFile.exists()) return emptySet()
        return runCatching {
            Json.decodeFromString<List<Int>>(metadataFile.readText()).toSet()
        }.getOrDefault(emptySet())
    }

    fun writeEditedQuestionIds(fileName: String, questions: List<Question>) {
        val editedIds = questions.filter { it.isEdited }.map { it.id }
        val metadataFile = editedMetadataFile(fileName)
        if (editedIds.isEmpty()) {
            if (metadataFile.exists()) metadataFile.delete()
            return
        }
        metadataFile.writeText(Json.encodeToString(editedIds))
    }

    fun overlayEditedFlags(questions: List<Question>): List<Question> {
        if (questions.isEmpty()) return questions
        val editedIdsByFileName = mutableMapOf<String, Set<Int>>()
        return questions.map { question ->
            val fileName = question.fileName
            if (fileName.isNullOrBlank()) {
                question
            } else {
                val editedIds = editedIdsByFileName.getOrPut(fileName) { loadEditedQuestionIds(fileName) }
                val isEdited = question.id in editedIds
                if (question.isEdited == isEdited) question else question.copy(isEdited = isEdited)
            }
        }
    }
}
