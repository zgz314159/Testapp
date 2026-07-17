package com.example.testapp.presentation.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens

private val SheetBg = Color(0xFFF8FAFD)
private val CardWhite = Color.White
private val TextPrimary = Color(0xFF1B2B4E)
private val TextSecondary = Color(0xFF5F6B7A)
private val BrandBlue = Color(0xFF4F8CFF)
private val BrandBlueSoft = Color(0xFFEAF2FF)
private val AccentExam = Color(0xFF42B883)
private val AccentExamSoft = Color(0xFFE6F7F0)
private val AccentRedo = Color(0xFFE8A838)
private val AccentRedoSoft = Color(0xFFFFF4E0)
private val AccentAdaptive = Color(0xFF7B6CFF)
private val AccentAdaptiveSoft = Color(0xFFF0EDFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeStartQuizSheet(
    visible: Boolean,
    pendingFileName: String,
    hasProgress: Boolean,
    onDismiss: () -> Unit,
    onStartQuiz: (String) -> Unit,
    onStartAdaptive: (String) -> Unit,
    onStartExam: (String) -> Unit,
    onRestart: (String) -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = HomeDesignTokens.bottomNavRadius,
            topEnd = HomeDesignTokens.bottomNavRadius,
        ),
        containerColor = SheetBg,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = pendingFileName,
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = Color(0x291B2B4E),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 4f,
                    ),
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_start_quiz_sheet_subtitle),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(18.dp))

            HomeStartQuizActionCard(
                title = if (hasProgress) {
                    stringResource(R.string.home_continue_practice)
                } else {
                    stringResource(R.string.home_start_practice)
                },
                subtitle = stringResource(R.string.home_start_quiz_practice_hint),
                icon = Icons.Filled.PlayArrow,
                iconTint = BrandBlue,
                iconBg = BrandBlueSoft,
                elevation = 10.dp,
                onClick = {
                    onDismiss()
                    onStartQuiz(pendingFileName)
                },
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (HomeAdaptiveModeEligibilityPipeline.isEligible(pendingFileName)) {
                HomeStartQuizActionCard(
                    title = stringResource(R.string.home_start_adaptive_fading),
                    subtitle = stringResource(R.string.home_start_quiz_adaptive_hint),
                    icon = Icons.Filled.AutoAwesome,
                    iconTint = AccentAdaptive,
                    iconBg = AccentAdaptiveSoft,
                    elevation = 8.dp,
                    onClick = {
                        onDismiss()
                        onStartAdaptive(pendingFileName)
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            HomeStartQuizActionCard(
                title = if (hasProgress) {
                    stringResource(R.string.home_continue_exam)
                } else {
                    stringResource(R.string.home_start_exam)
                },
                subtitle = stringResource(R.string.home_start_quiz_exam_hint),
                icon = Icons.Filled.Quiz,
                iconTint = AccentExam,
                iconBg = AccentExamSoft,
                elevation = 8.dp,
                onClick = {
                    onDismiss()
                    onStartExam(pendingFileName)
                },
            )

            if (hasProgress) {
                Spacer(modifier = Modifier.height(12.dp))
                HomeStartQuizActionCard(
                    title = stringResource(R.string.home_restart_quiz),
                    subtitle = stringResource(R.string.home_start_quiz_restart_hint),
                    icon = Icons.Filled.Refresh,
                    iconTint = AccentRedo,
                    iconBg = AccentRedoSoft,
                    elevation = 8.dp,
                    onClick = { onRestart(pendingFileName) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss),
                shape = RoundedCornerShape(18.dp),
                color = CardWhite,
                tonalElevation = 1.dp,
                shadowElevation = 4.dp,
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun HomeStartQuizActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    elevation: Dp,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        tonalElevation = 2.dp,
        shadowElevation = elevation,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = iconBg,
                tonalElevation = 1.dp,
                shadowElevation = 5.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
