package com.example.testapp.domain.model

data class PracticeProgress(
    val id: String = "default",
    val currentIndex: Int,
    val answeredList: List<Int>,
    val selectedOptions: List<Int>,
    val showResultList: List<Boolean>, // 新增：每题是否已提交/显示解析
    val timestamp: Long
)
