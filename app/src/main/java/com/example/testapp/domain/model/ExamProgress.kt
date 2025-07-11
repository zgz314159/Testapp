package com.example.testapp.domain.model

data class ExamProgress(
    val id: String = "exam_default",
    val currentIndex: Int,
    val selectedOptions: List<List<Int>>,
    val showResultList: List<Boolean>,
    val finished: Boolean,
    val timestamp: Long
)