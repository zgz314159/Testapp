package com.example.testapp.presentation.screen.questionbank

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens

@Composable
fun QuestionBankDrawerHeader(
    title: String,
    closeContentDescription: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B2B4E),
                letterSpacing = (-0.2).sp,
                shadow = Shadow(
                    color = Color(0x331B2B4E),
                    offset = Offset(0f, 2f),
                    blurRadius = 5f,
                ),
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Surface(
            onClick = onClose,
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = HomeDesignTokens.surfaceLight,
            tonalElevation = 2.dp,
            shadowElevation = HomeDesignTokens.elevationHeaderIcon,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = closeContentDescription,
                    tint = HomeDesignTokens.textSecondaryLight,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
