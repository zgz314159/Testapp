package com.example.testapp.data.repository

import com.example.testapp.domain.model.Question

data class ImportedQuestionPayload(
    val question: Question,
    val deepSeekAnalysis: String = "",
    val sparkAnalysis: String = "",
    val baiduAnalysis: String = "",
    val note: String = ""
) {
    val hasSupplementalData: Boolean
        get() = deepSeekAnalysis.isNotBlank() || sparkAnalysis.isNotBlank() || baiduAnalysis.isNotBlank() || note.isNotBlank()
}

class DuplicateFileImportException(val duplicates: List<String>) : Exception()

class ImportFailedException(val reason: String?) : Exception(reason)
