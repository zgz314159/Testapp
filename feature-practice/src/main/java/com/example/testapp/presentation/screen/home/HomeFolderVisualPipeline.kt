package com.example.testapp.presentation.screen.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.RuleFolder
import androidx.compose.material.icons.filled.SnippetFolder
import androidx.compose.material.icons.filled.Topic
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class HomeFolderVisualKind {
    Archive,
    Regulation,
    Learning,
    Work,
    QuestionType,
    StableVariant,
}

/**
 * @property badge 当 [icon] 是无内部标记的纯 Folder 时，叠加在文件夹腹部的小徽标
 *（如题型 glyph），保证每个文件夹图标内部都有可辨识的标记。
 */
data class HomeFolderVisual(
    val kind: HomeFolderVisualKind,
    val icon: ImageVector,
    val gradientStart: Color,
    val gradientEnd: Color,
    val badge: ImageVector? = null,
)

/**
 * Material 3 文件夹视觉：图标一律取自"文件夹家族"（Folder/RuleFolder/Topic…），
 * 与题库卡的文档/题型图标形成形状区分；类型信息主要靠配色表达。
 * 名称可识别时用语义文件夹图标 + 语义色；题型名文件夹保留该题型配色但换文件夹形状；
 * 其余按名称哈希取稳定变体，跨重组与重启一致。
 */
object HomeFolderVisualPipeline {
    fun resolve(folderName: String): HomeFolderVisual {
        val normalized = folderName.lowercase()
        structuralVisual(normalized)?.let { return it }

        val questionVisual = HomeFileTypeVisualPipeline.resolve(folderName)
        if (questionVisual.kind != HomeQuestionBankVisualKind.Generic) {
            // 沿用题型配色与题型 glyph（叠加为文件夹腹部徽标），外形保持文件夹
            return HomeFolderVisual(
                kind = HomeFolderVisualKind.QuestionType,
                icon = Icons.Default.Folder,
                gradientStart = questionVisual.gradientStart,
                gradientEnd = questionVisual.gradientEnd,
                badge = questionVisual.icon,
            )
        }
        contextualVisual(normalized)?.let { return it }
        return stableVariants[Math.floorMod(folderName.hashCode(), stableVariants.size)]
    }

    private fun structuralVisual(name: String): HomeFolderVisual? = when {
        containsAny(name, "旧版", "归档", "历史", "archive") ->
            visual(HomeFolderVisualKind.Archive, Icons.Default.SnippetFolder, 0xFF64748B, 0xFF94A3B8)
        containsAny(name, "法规", "安规", "规程", "规范", "regulation") ->
            visual(HomeFolderVisualKind.Regulation, Icons.Default.RuleFolder, 0xFF0F766E, 0xFF2DD4BF)
        else -> null
    }

    private fun contextualVisual(name: String): HomeFolderVisual? = when {
        containsAny(name, "培训", "学习", "教材", "课程", "training") ->
            visual(HomeFolderVisualKind.Learning, Icons.Default.Topic, 0xFF4F46E5, 0xFF818CF8)
        containsAny(name, "技师", "工种", "职业", "服务员", "work") ->
            visual(HomeFolderVisualKind.Work, Icons.Default.FolderSpecial, 0xFFD97706, 0xFFFBBF24)
        else -> null
    }

    // 全部使用自带内部标记的文件夹变体，避免出现"空白文件夹"图标
    private val stableVariants = listOf(
        visual(HomeFolderVisualKind.StableVariant, Icons.Default.FolderCopy, 0xFF2563EB, 0xFF60A5FA),
        visual(HomeFolderVisualKind.StableVariant, Icons.Default.Topic, 0xFF7C3AED, 0xFFC084FC),
        visual(HomeFolderVisualKind.StableVariant, Icons.Default.FolderShared, 0xFF0D9488, 0xFF2DD4BF),
        visual(HomeFolderVisualKind.StableVariant, Icons.Default.FolderZip, 0xFF0891B2, 0xFF67E8F9),
        visual(HomeFolderVisualKind.StableVariant, Icons.Default.FolderSpecial, 0xFFEA580C, 0xFFFB923C),
    )

    private fun visual(
        kind: HomeFolderVisualKind,
        icon: ImageVector,
        start: Long,
        end: Long,
    ) = HomeFolderVisual(kind, icon, Color(start), Color(end))

    private fun containsAny(value: String, vararg candidates: String): Boolean =
        candidates.any(value::contains)
}
