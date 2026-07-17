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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens

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
    val tokens = AppElevatedActionSheetTokens

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
        targetValue = if (isEditing) tokens.cardWhite else Color.Transparent,
        label = "stepper_edit_bg"
    )

    val fieldTextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        color = tokens.textPrimary
    )

    Surface(
        modifier = modifier
            .requiredWidth(width.dp)
            .height(CapsuleStepperDefaults.HEIGHT)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            ),
        shape = CircleShape,
        color = tokens.brandBlueSoft,
        tonalElevation = 1.dp,
        shadowElevation = 5.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepperIconButton(
                enabled = value > minValue,
                onClick = { if (value > minValue) onValueChange(value - 1) },
                contentDescription = "Decrease",
                icon = Icons.Filled.Remove,
            )

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
                    cursorBrush = SolidColor(tokens.brandBlue),
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

            StepperIconButton(
                enabled = value < maxValue,
                onClick = { if (value < maxValue) onValueChange(value + 1) },
                contentDescription = "Increase",
                icon = Icons.Filled.Add,
            )
        }
    }

    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }
}

@Composable
private fun StepperIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    val tokens = AppElevatedActionSheetTokens
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = if (enabled) tokens.cardWhite else tokens.cardWhite.copy(alpha = 0.55f),
            tonalElevation = if (enabled) 1.dp else 0.dp,
            shadowElevation = if (enabled) 4.dp else 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (enabled) {
                        tokens.brandBlue
                    } else {
                        tokens.textSecondary.copy(alpha = 0.35f)
                    },
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
