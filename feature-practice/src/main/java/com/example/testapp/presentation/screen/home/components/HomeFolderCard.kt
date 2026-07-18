package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.home.HomeFolderVisualPipeline
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens

/**
 * 文件夹剪影 Shape：左上凸出标签页（tab）+ 圆角主体。
 * 作为整卡外轮廓，让阴影与裁剪都是"文件夹"形。
 */
private fun folderSilhouetteShape(tabHeightPx: Float, cornerPx: Float): Shape =
    GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val t = tabHeightPx
        val r = cornerPx
        val tabWidth = w * 0.32f
        val slant = t * 1.4f

        moveTo(0f, h - r)
        lineTo(0f, r * 0.75f)
        quadraticBezierTo(0f, 0f, r * 0.75f, 0f)
        lineTo(tabWidth - slant, 0f)
        // 标签页平滑斜边过渡到主体顶边
        cubicTo(tabWidth - slant * 0.35f, 0f, tabWidth + slant * 0.35f, t, tabWidth + slant, t)
        lineTo(w - r, t)
        quadraticBezierTo(w, t, w, t + r)
        lineTo(w, h - r)
        quadraticBezierTo(w, h, w - r, h)
        lineTo(r, h)
        quadraticBezierTo(0f, h, 0f, h - r)
        close()
    }

/**
 * 首页分组文件夹卡片：真实纸质文件夹的双层结构——
 * 彩色渐变「夹背 + 标签页」打底，上覆浅色「纸张前层」承载内容。
 * 与 [HomeQuestionBankCard] 的纯白圆角卡形成形状与材质双重区分；拖拽悬停时高亮为放置目标。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeFolderCard(
    folderName: String,
    itemCount: Int,
    isDropTarget: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onReportBounds: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isCompact = screenWidthDp < 380
    val tabHeight = 11.dp
    val bodyHeight = if (isCompact) 66.dp else 72.dp
    val visual = remember(folderName) { HomeFolderVisualPipeline.resolve(folderName) }
    // 夹背：类型色渐变（拖拽悬停时更饱和）
    val backBrush = remember(visual.gradientStart, visual.gradientEnd, isDropTarget) {
        val boost = if (isDropTarget) 0f else 0.18f
        Brush.linearGradient(
            listOf(
                lerp(visual.gradientStart, Color.White, boost),
                lerp(visual.gradientEnd, Color.White, boost),
            ),
        )
    }
    // 纸张前层：接近白的类型色浅底，盖住主体，只露出顶部夹背与标签页
    val paperColor = remember(visual.gradientStart, isDropTarget) {
        if (isDropTarget) {
            lerp(Color.White, visual.gradientStart, 0.16f)
        } else {
            lerp(Color.White, visual.gradientStart, 0.055f)
        }
    }
    val density = LocalDensity.current
    val folderShape = remember(density, tabHeight) {
        with(density) { folderSilhouetteShape(tabHeight.toPx(), 16.dp.toPx()) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 5.dp)
            .onGloballyPositioned { coords -> onReportBounds(coords.boundsInRoot()) }
            .height(tabHeight + bodyHeight)
            .shadow(
                elevation = if (isDropTarget) HomeDesignTokens.elevationHigh else HomeDesignTokens.questionCardElevation,
                shape = folderShape,
            )
            .clip(folderShape)
            .background(backBrush)
            .then(
                if (isDropTarget) {
                    Modifier.border(BorderStroke(2.dp, HomeDesignTokens.primary), folderShape)
                } else {
                    Modifier
                },
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        // 纸张前层：顶部圆角小、底部随外轮廓，形成"folder 里夹着纸"的层次
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bodyHeight - 3.dp)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(paperColor),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = tabHeight)
                .padding(start = if (isCompact) 14.dp else 18.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 类型色大号文件夹图标；纯 Folder 时在腹部叠加题型 glyph 镂空徽标
            val iconSize = if (isCompact) 30.dp else 34.dp
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    visual.icon,
                    contentDescription = null,
                    tint = visual.gradientStart,
                    modifier = Modifier.size(iconSize),
                )
                visual.badge?.let { badge ->
                    Icon(
                        badge,
                        contentDescription = null,
                        tint = paperColor,
                        modifier = Modifier
                            .padding(top = iconSize * 0.12f)
                            .size(iconSize * 0.42f),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folderName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B2B4E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.home_folder_item_count, itemCount),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeDesignTokens.textSecondaryLight,
                    maxLines = 1,
                )
            }
            // 数量胶囊徽章：容器语义，替代题库卡的进度/CTA
            Surface(
                shape = RoundedCornerShape(50),
                color = visual.gradientStart.copy(alpha = 0.14f),
            ) {
                Text(
                    text = itemCount.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = visual.gradientStart,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = HomeDesignTokens.textTertiaryLight,
            )
        }
    }
}
