package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: Modifier = Modifier.padding(22.dp),
    containerColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .questionSessionSoftCard(
                shape = RoundedCornerShape(28.dp),
                elevation = 10.dp,
                containerColor = containerColor ?: questionSessionSurfaceColor(),
            )
            .then(contentPadding),
        content = content,
    )
}
