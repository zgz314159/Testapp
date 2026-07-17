package com.example.testapp.uicommon.design

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Home-aligned elevated action sheet (white cards + soft depth). */
object AppElevatedActionSheetTokens {
    val sheetBg: Color = Color(0xFFF8FAFD)
    val cardWhite: Color = Color.White
    val textPrimary: Color = Color(0xFF1B2B4E)
    val textSecondary: Color = Color(0xFF5F6B7A)
    val brandBlue: Color = Color(0xFF4F8CFF)
    val brandBlueSoft: Color = Color(0xFFEAF2FF)
    val accentPurple: Color = Color(0xFF7B6CFF)
    val accentPurpleSoft: Color = Color(0xFFF0EDFF)
    val accentTeal: Color = Color(0xFF42B883)
    val accentTealSoft: Color = Color(0xFFE6F7F0)
    val sheetCorner: Dp = 28.dp
    val cardCorner: Dp = 20.dp
    val cardElevation: Dp = 8.dp
    val iconElevation: Dp = 5.dp
}

data class AppElevatedActionItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val iconTint: Color = AppElevatedActionSheetTokens.brandBlue,
    val iconBg: Color = AppElevatedActionSheetTokens.brandBlueSoft,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppElevatedActionSheet(
    visible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    actions: List<AppElevatedActionItem>,
    subtitle: String? = null,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tokens = AppElevatedActionSheetTokens
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = tokens.sheetCorner, topEnd = tokens.sheetCorner),
        containerColor = tokens.sheetBg,
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
                text = title,
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.textPrimary,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = Color(0x291B2B4E),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 4f,
                    ),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = tokens.textSecondary,
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            actions.forEachIndexed { index, item ->
                if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                AppElevatedActionCard(item = item)
            }
        }
    }
}

@Composable
fun AppElevatedActionCard(
    item: AppElevatedActionItem,
    elevation: Dp = AppElevatedActionSheetTokens.cardElevation,
) {
    val tokens = AppElevatedActionSheetTokens
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick),
        shape = RoundedCornerShape(tokens.cardCorner),
        color = tokens.cardWhite,
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
                color = item.iconBg,
                tonalElevation = 1.dp,
                shadowElevation = tokens.iconElevation,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.iconTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = tokens.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
