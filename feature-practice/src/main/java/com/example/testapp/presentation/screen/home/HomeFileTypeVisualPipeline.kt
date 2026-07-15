package com.example.testapp.presentation.screen.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 首页题库文件类型视觉 Pipeline。
 * 根据文件名后缀返回图标和颜色，不依赖外部资源。
 */
object HomeFileTypeVisualPipeline {

    data class FileTypeVisual(
        val icon: ImageVector,
        val gradientStart: Color,
        val gradientEnd: Color,
    )

    fun resolve(fileName: String): FileTypeVisual {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        val lower = fileName.lowercase()
        return when {
            // XLS/XLSX → 紫色文档
            ext == "xlsx" || ext == "xls" || lower.contains("excel") || lower.contains("sheet") ->
                FileTypeVisual(Icons.Default.Description, Color(0xFF6366F1), Color(0xFF818CF8))
            // Word 文档 → 蓝色文档
            ext == "docx" || ext == "doc" || lower.contains("word") || lower.contains("doc") ->
                FileTypeVisual(Icons.Default.Description, Color(0xFF4F8CFF), Color(0xFF79C9FF))
            // 填空/练习 → 青绿编辑
            lower.contains("填空") || lower.contains("fill") || lower.contains("blank") ->
                FileTypeVisual(Icons.Default.EditNote, Color(0xFF10B981), Color(0xFF34D399))
            // 图片 → 粉色图片
            ext == "png" || ext == "jpg" || ext == "jpeg" || ext == "webp" || lower.contains("图片") ->
                FileTypeVisual(Icons.Default.Image, Color(0xFFEC4899), Color(0xFFF472B6))
            // 判断/选择 → 紫色/靛蓝
            lower.contains("判断") || lower.contains("judge") || lower.contains("选择") || lower.contains("select") ->
                FileTypeVisual(Icons.Default.Quiz, Color(0xFF8B5CF6), Color(0xFFA78BFA))
            // 默认 → 蓝紫色文档
            else -> FileTypeVisual(Icons.AutoMirrored.Filled.Assignment, Color(0xFF4F8CFF), Color(0xFF79C9FF))
        }
    }
}
