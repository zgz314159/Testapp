package com.example.testapp.presentation.screen.home.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * PowerAI 首页设计令牌。
 *
 * 所有首页专用颜色、间距、圆角、字体尺寸集中于此。
 * 全局颜色方案（如 Primary、Background）不在此覆盖以避免破坏其他页面。
 *
 * 参考：设计1.txt §8 Color System、§9 Typography、§6 Shape System
 */
object HomeDesignTokens {

    // ─── Background / Surface ───
    val backgroundLight: Color = Color(0xFFF8FAFD)
    val surfaceLight: Color = Color(0xFFFFFFFF)
    val surfaceVariantLight: Color = Color(0xFFF3F6FB)
    val outlineLight: Color = Color(0xFFD9E2EC)

    val backgroundDark: Color = Color(0xFF111318)
    val surfaceDark: Color = Color(0xFF1A1D23)
    val surfaceVariantDark: Color = Color(0xFF2A2D35)
    val outlineDark: Color = Color(0xFF3A3D45)

    // ─── Primary / Accent ───
    val primary: Color = Color(0xFF4F8CFF)
    val primaryContainer: Color = Color(0xFFDDEBFF)
    val secondary: Color = Color(0xFF79C9FF)

    // ─── Text ───
    val textPrimaryLight: Color = Color(0xFF1B1F24)
    val textSecondaryLight: Color = Color(0xFF5F6B7A)
    val textTertiaryLight: Color = Color(0xFF687484)
    val subtitleLight: Color = Color(0xFF687484)

    val textPrimaryDark: Color = Color(0xFFFFFFFF)
    val textSecondaryDark: Color = Color(0xFFB7C1CF)
    val subtitleDark: Color = Color(0xFFB7C1CF)

    // ─── Semantic ───
    val success: Color = Color(0xFF42C883)
    val warning: Color = Color(0xFFF7B84B)
    val error: Color = Color(0xFFF56C6C)

    // ─── Hero 渐变 ───
    val heroGradientStart: Color = Color(0xFF4F8CFF)
    val heroGradientEnd: Color = Color(0xFF79C9FF)

    // ─── Spacing (8dp grid) ───
    val spacingXs: Dp = 4.dp
    val spacingSm: Dp = 8.dp
    val spacingMd: Dp = 16.dp
    val spacingLg: Dp = 24.dp
    val spacingXl: Dp = 32.dp
    val spacingXxl: Dp = 40.dp

    // ─── Page Padding ───
    val pageHorizontalPadding: Dp = 24.dp
    val pageTopPadding: Dp = 20.dp
    val pageBottomPadding: Dp = 32.dp

    // ─── Card Spacing ───
    val cardGap: Dp = 16.dp
    val sectionGap: Dp = 24.dp
    val insideCardPadding: Dp = 24.dp

    // ─── Rounded Corners ───
    val heroCardRadius: Dp = 32.dp
    val questionCardRadius: Dp = 28.dp
    val buttonRadius: Dp = 24.dp
    val statCardRadius: Dp = 24.dp
    val bottomNavRadius: Dp = 28.dp
    val chipRadius: Dp = 16.dp

    // ─── Elevation ───
    val elevationNone: Dp = 0.dp
    val elevationLow: Dp = 2.dp
    val elevationMedium: Dp = 4.dp
    val elevationHigh: Dp = 8.dp

    // ─── Typography ───
    val greetingFontSize = 30.sp
    val greetingLineHeight = 36.sp
    val greetingFontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    val greetingLetterSpacing = (-0.4).sp

    val subtitleFontSize = 15.sp
    val subtitleLineHeight = 22.sp
    val subtitleFontWeight = androidx.compose.ui.text.font.FontWeight.Medium

    val sectionTitleFontSize = 20.sp
    val sectionTitleFontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold

    val bodyFontSize = 16.sp

    val captionFontSize = 13.sp
    val captionFontWeight = androidx.compose.ui.text.font.FontWeight.Medium

    val statNumberFontSize = 26.sp
    val statNumberFontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    val statLabelFontSize = 13.sp
    val statLabelFontWeight = androidx.compose.ui.text.font.FontWeight.Medium

    // ─── Header ───
    val headerExpandedHeight: Dp = 96.dp
    val headerCollapsedHeight: Dp = 72.dp
    val headerActionSize: Dp = 48.dp
    val headerIconSize: Dp = 24.dp
    val headerActionSpacing: Dp = 12.dp
    val headerTitleSubtitleGap: Dp = 6.dp

    // ─── Hero Card ───
    val heroCardHeight: Dp = 248.dp
    val heroProgressBarHeight: Dp = 8.dp

    // ─── Question Bank Card ───
    val questionCardHeight: Dp = 132.dp
    val questionCardIconAreaWidth: Dp = 88.dp

    // ─── Animations ───
    const val headerEnterDurationMs: Int = 280
}
