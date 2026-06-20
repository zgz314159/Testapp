package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.component.AnswerCardItemState

/**
 * A collapsible section for answer card per question type group.
 *
 * @param label       Type label (e.g. "单选题", "多选题")
 * @param collapsed   Whether this section is collapsed
 * @param onToggle    Called when the arrow button is clicked
 * @param items       List of AnswerCardItemState to show in the AnswerCardGrid
 * @param onClick     Called when an item in the grid is clicked
 * @param labelPaddingTop Top padding above the label (default 8.dp)
 */
@Composable
fun CollapsibleAnswerCardSection(
    label: String,
    collapsed: Boolean,
    onToggle: () -> Unit,
    items: List<AnswerCardItemState>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    labelPaddingTop: Dp = 8.dp
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header row with label + arrow toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = labelPaddingTop),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onToggle,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = if (collapsed) Icons.Filled.KeyboardArrowDown
                                  else Icons.Filled.KeyboardArrowUp,
                    contentDescription = if (collapsed) "展开" else "折叠",
    tint = Color.Unspecified
                )
            }
        }
        // Grid content
        if (!collapsed) {
            AnswerCardGrid(items = items, onClick = onClick)
        }
    }
}