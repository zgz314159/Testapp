package com.example.testapp.domain.model

data class PracticeProgress(
    val id: String = "practice_default",
    val currentIndex: Int,
    val answeredList: List<Int>,
    val selectedOptions: List<List<Int>>, // 支持多选！
    val showResultList: List<Boolean>, // 新增：每题是否已提交/显示解析
    val analysisList: List<String>,
    val sparkAnalysisList: List<String> = emptyList(),
    val baiduAnalysisList: List<String> = emptyList(),
    val noteList: List<String>,
    val timestamp: Long
)
