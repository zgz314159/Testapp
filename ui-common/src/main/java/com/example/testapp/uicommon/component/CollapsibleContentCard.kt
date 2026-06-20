package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * A generic collapsible card with a header row (label + arrow toggle button)
 * and a scrollable content area.
 *
 * @param label       Header text shown on the left
 * @param collapsed   Whether the content is currently collapsed
 * @param onToggle    Called when the arrow button is clicked
 * @param content     The content text to display
 * @param maxLines    Max lines when collapsed (default 1)
 * @param heightInMax Max height of the content area when expanded (default 400.dp)
 * @param backgroundColor Background color of the card
 * @param labelColor  Text color for the label/content
 * @param fontSize    Font size for the text
 * @param fontFamily  Font family for the text
 */
@Composable
fun CollapsibleContentCard(
    label: String,
    collapsed: Boolean,
    onToggle: () -> Unit,
    content: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    heightInMax: Dp = 400.dp,
    backgroundColor: Color = Color.Transparent,
    labelColor: Color = Color.Unspecified,
    fontSize: TextUnit = LocalFontSize.current,
    fontFamily: FontFamily? = LocalFontFamily.current
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        // Header row with label + arrow icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = labelColor,
                fontSize = fontSize,
                fontFamily = fontFamily,
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
                    tint = labelColor
                )
            }
        }

        // Content area
        if (!collapsed) {
            Text(
                text = content,
                color = labelColor,
                fontSize = fontSize,
                fontFamily = fontFamily,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = heightInMax)
                    .verticalScroll(scrollState)
            )
        } else {
            Text(
                text = content,
                color = labelColor,
                fontSize = fontSize,
                fontFamily = fontFamily,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}