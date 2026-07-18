package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.feature.settings.R
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(
    title: String = stringResource(R.string.settings_title),
    onBack: () -> Unit,
) {
    val tokens = AppElevatedActionSheetTokens
    TopAppBar(
        title = {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.textPrimary,
                    shadow = Shadow(
                        color = Color(0x291B2B4E),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 4f,
                    ),
                ),
            )
        },
        navigationIcon = {
            Surface(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = tokens.cardWhite,
                tonalElevation = 1.dp,
                shadowElevation = 6.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.settings_nav_back),
                        tint = tokens.brandBlue,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = tokens.sheetBg,
            titleContentColor = tokens.textPrimary,
        ),
    )
}
