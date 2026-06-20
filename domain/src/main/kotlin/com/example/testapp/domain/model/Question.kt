package com.example.testapp.domain.model

import kotlinx.serialization.Serializable

// Serializable domain model; persistence annotations live in `:data` module.
@Serializable
data class Question(
    val id: Int = 0,
    val content: String,
    val type: String, // 判断题、单选题、多选题
    val options: List<String>,
    val answer: String,
    val explanation: String,
    val isFavorite: Boolean = false,
    val isWrong: Boolean = false,
    val isEdited: Boolean = false,
    val fileName: String? = null, // <-- 新增字段，用于关联导入的文件
    val stemImages: List<String> = emptyList() // <-- 题干图片本地路径列表
)
