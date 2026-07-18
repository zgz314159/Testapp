package com.example.testapp.uicommon.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.uicommon.R

/** 出题模式短名，用于答题头部等紧凑位置。 */
@Composable
fun fillModeShortLabel(mode: FillQuestionGenerationMode): String = stringResource(
    when (mode) {
        FillQuestionGenerationMode.SCORE_DESC -> R.string.uicommon_fill_mode_score_desc
        FillQuestionGenerationMode.SCORE_ASC -> R.string.uicommon_fill_mode_score_asc
        FillQuestionGenerationMode.TAG_RANDOM -> R.string.uicommon_fill_mode_tag_random
        FillQuestionGenerationMode.SCORE_RANGE_RANDOM -> R.string.uicommon_fill_mode_score_range_random
        FillQuestionGenerationMode.FULL_ANSWER -> R.string.uicommon_fill_mode_full_answer
    },
)

/** 「自适应渐隐练习」会话短名。 */
@Composable
fun adaptiveFadingModeLabel(): String = stringResource(R.string.uicommon_mode_adaptive_fading)

/**
 * 出题模式胶囊徽标：浅蓝底 + 细蓝描边，答题头部与设置页共用，
 * 保证两处样式一致。
 */
@Composable
fun SessionModeBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    val tokens = AppElevatedActionSheetTokens
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = tokens.brandBlueSoft,
        border = BorderStroke(1.dp, tokens.brandBlue.copy(alpha = 0.35f)),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = tokens.brandBlue,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
