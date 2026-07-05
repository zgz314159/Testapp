package com.example.testapp.presentation.screen.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.di.FontSettingsEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

enum class AiFontScope {
    DEEPSEEK,
    SPARK,
    BAIDU,
    PRACTICE,
}

data class AiFontSizeState(
    val size: Float,
    val setSize: (Float) -> Unit,
    val persistSize: suspend (Float) -> Unit,
)

data class AiArtifactTypographyState(
    val fontSize: AiFontSizeState,
    val lineSpacing: Float,
    val letterSpacing: Float,
)

@Composable
fun rememberFontSettingsRepository(): FontSettingsRepository {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        EntryPointAccessors.fromApplication(appContext, FontSettingsEntryPoint::class.java)
            .fontSettingsRepository()
    }
}

@Composable
fun rememberAiFontSize(
    scope: AiFontScope,
    repository: FontSettingsRepository = rememberFontSettingsRepository(),
): AiFontSizeState {
    val globalFontSize by repository.fontSize.collectAsState(initial = 18f)
    val storedFlow = scope.storedFontSizeFlow(repository)
    val storedSize by storedFlow.collectAsState(initial = Float.NaN)
    var screenFontSize by remember { mutableStateOf(globalFontSize) }
    var fontLoaded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(storedSize) {
        if (!storedSize.isNaN()) {
            screenFontSize = storedSize
            fontLoaded = true
        }
    }
    LaunchedEffect(screenFontSize, fontLoaded) {
        if (fontLoaded) {
            coroutineScope.launch { scope.persistFontSize(repository, screenFontSize) }
        }
    }

    val persist: suspend (Float) -> Unit = { size -> scope.persistFontSize(repository, size) }
    return AiFontSizeState(
        size = screenFontSize,
        setSize = { screenFontSize = it },
        persistSize = persist,
    )
}

@Composable
fun rememberAiArtifactTypography(
    fontScope: AiFontScope,
    repository: FontSettingsRepository = rememberFontSettingsRepository(),
): AiArtifactTypographyState {
    val fontSize = rememberAiFontSize(fontScope, repository)
    val storedLineSpacing by repository.practiceLineSpacing.collectAsState(initial = Float.NaN)
    val storedLetterSpacing by repository.practiceLetterSpacing.collectAsState(initial = Float.NaN)
    var lineSpacing by remember { mutableStateOf(1.3f) }
    var letterSpacing by remember { mutableStateOf(0f) }

    LaunchedEffect(storedLineSpacing) {
        if (!storedLineSpacing.isNaN()) lineSpacing = storedLineSpacing
    }
    LaunchedEffect(storedLetterSpacing) {
        if (!storedLetterSpacing.isNaN()) letterSpacing = storedLetterSpacing
    }

    return AiArtifactTypographyState(
        fontSize = fontSize,
        lineSpacing = lineSpacing,
        letterSpacing = letterSpacing,
    )
}

private fun AiFontScope.storedFontSizeFlow(repository: FontSettingsRepository): Flow<Float> =
    when (this) {
        AiFontScope.DEEPSEEK -> repository.deepSeekFontSize
        AiFontScope.SPARK -> repository.sparkFontSize
        AiFontScope.BAIDU -> repository.baiduFontSize
        AiFontScope.PRACTICE -> repository.practiceFontSize
    }

private suspend fun AiFontScope.persistFontSize(repository: FontSettingsRepository, size: Float) {
    when (this) {
        AiFontScope.DEEPSEEK -> repository.setDeepSeekFontSize(size)
        AiFontScope.SPARK -> repository.setSparkFontSize(size)
        AiFontScope.BAIDU -> repository.setBaiduFontSize(size)
        AiFontScope.PRACTICE -> repository.setPracticeFontSize(size)
    }
}
