package com.example.testapp.uicommon.component.stepper

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CapsuleStepperInput(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 0,
    maxValue: Int = 99,
    formatDisplay: (Int) -> String = Int::toString,
    width: Int = CapsuleStepperDefaults.WIDTH_DP,
    contentDescription: String? = null
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(value) { mutableStateOf(formatDisplay(value)) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is FocusInteraction.Focus -> {
                    isEditing = true
                    textValue = formatDisplay(value)
                }
                is FocusInteraction.Unfocus -> {
                    isEditing = false
                    StepperInputParsePipeline.parseDigits(textValue, minValue, maxValue)?.let {
                        onValueChange(it)
                        textValue = formatDisplay(it)
                    } ?: run {
                        textValue = formatDisplay(value)
                    }
                }
            }
        }
    }

    LaunchedEffect(value) {
        if (!isEditing) {
            textValue = formatDisplay(value)
        }
    }

    val editBackground by animateColorAsState(
        targetValue = if (isEditing) MaterialTheme.colorScheme.surface else Color.Transparent,
        label = "stepper_edit_bg"
    )

    val fieldTextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )

    Row(
        modifier = modifier
            .requiredWidth(width.dp)
            .height(CapsuleStepperDefaults.HEIGHT)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (value > minValue) onValueChange(value - 1) },
            enabled = value > minValue,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Remove,
                contentDescription = "Decrease",
                tint = if (value > minValue) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                }
            )
        }

        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(editBackground),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = if (isEditing) textValue else formatDisplay(value),
                onValueChange = { raw ->
                    if (!isEditing) return@BasicTextField
                    val digits = raw.filter { it.isDigit() }
                    textValue = digits
                    StepperInputParsePipeline.parseDigits(digits, minValue, maxValue)?.let(onValueChange)
                },
                textStyle = fieldTextStyle,
                singleLine = true,
                readOnly = !isEditing,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                interactionSource = interactionSource,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .alpha(if (isEditing) 1f else 0f)
                    .focusRequester(focusRequester)
            )
            if (!isEditing) {
                StepperAnimatedValue(
                    value = value,
                    displayText = formatDisplay(value),
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isEditing = true
                    }
                )
            }
        }

        IconButton(
            onClick = { if (value < maxValue) onValueChange(value + 1) },
            enabled = value < maxValue,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Increase",
                tint = if (value < maxValue) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                }
            )
        }
    }

    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }
}
