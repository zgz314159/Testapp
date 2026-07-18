package com.example.testapp.presentation.screen.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeFolderVisualPipelineTest {
    @Test
    fun `semantic folder names resolve to matching visual kinds`() {
        assertEquals(HomeFolderVisualKind.Archive, HomeFolderVisualPipeline.resolve("旧版题库").kind)
        assertEquals(HomeFolderVisualKind.Regulation, HomeFolderVisualPipeline.resolve("安全法规与规程").kind)
        assertEquals(HomeFolderVisualKind.QuestionType, HomeFolderVisualPipeline.resolve("高级技师判断题").kind)
    }

    @Test
    fun `all folder icons are folder-family shapes`() {
        val names = listOf("旧版题库", "安全法规与规程", "高级技师判断题", "培训教材", "技师工种", "分组甲", "分组乙", "分组丙")
        names.forEach { name ->
            val icon = HomeFolderVisualPipeline.resolve(name).icon
            assertTrue(
                "$name -> ${icon.name} 不是文件夹形状图标",
                icon.name.contains("Folder") || icon.name.contains("Topic"),
            )
        }
    }

    @Test
    fun `every folder icon carries an inner mark or badge`() {
        val names = listOf("旧版题库", "安全法规与规程", "高级技师判断题", "填空题分组", "培训教材", "技师工种", "分组甲", "分组乙", "分组丙", "分组丁")
        names.forEach { name ->
            val resolved = HomeFolderVisualPipeline.resolve(name)
            val plainFolder = resolved.icon.name.endsWith(".Folder")
            assertTrue(
                "$name -> ${resolved.icon.name} 是无标记的空文件夹且没有徽标",
                !plainFolder || resolved.badge != null,
            )
        }
    }

    @Test
    fun `generic folder visual is stable and varied by name`() {
        val first = HomeFolderVisualPipeline.resolve("分组甲")
        val repeated = HomeFolderVisualPipeline.resolve("分组甲")
        val variantColors = listOf("分组甲", "分组乙", "分组丙", "分组丁")
            .map { HomeFolderVisualPipeline.resolve(it).gradientStart }
            .distinct()

        assertEquals(first.icon, repeated.icon)
        assertEquals(first.gradientStart, repeated.gradientStart)
        assertTrue(variantColors.size > 1)
    }
}
