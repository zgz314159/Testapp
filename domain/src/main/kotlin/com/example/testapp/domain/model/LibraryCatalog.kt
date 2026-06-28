package com.example.testapp.domain.model

import com.example.testapp.domain.usecase.FileStatistics

data class LibraryCatalog(
    val fileNames: List<String>,
    val fileStatistics: Map<String, FileStatistics>
)
