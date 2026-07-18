package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 主页风格确认弹窗：白底、圆角、加深 elevation，与 [AppElevatedActionSheet] 对齐。
 */
@Composable
fun AppElevatedConfirmDialog(
    onDismiss: () -> Unit,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    confirmDestructive: Boolean = false,
) {
    val tokens = AppElevatedActionSheetTokens
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
            ) {
                Text(
                    text = confirmLabel,
                    color = if (confirmDestructive) Color(0xFFE87461) else tokens.brandBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissLabel,
                    color = tokens.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                )
            }
        },
        title = title?.let {
            {
                Text(
                    text = it,
                    color = tokens.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
            }
        },
        text = {
            Text(
                text = message,
                color = tokens.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        },
        shape = RoundedCornerShape(tokens.cardCorner),
        containerColor = tokens.cardWhite,
        tonalElevation = 4.dp,
    )
}
