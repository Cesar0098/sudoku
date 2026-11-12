package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.icons.MaterialSymbolsOutlined
import com.example.ui.theme.sudokuColors

@Composable
fun GameControlsBar(
    isNotesMode: Boolean,
    canUndo: Boolean,
    hintsUsed: Int,
    onUndoClick: () -> Unit,
    onEraseClick: () -> Unit,
    onNotesToggleClick: () -> Unit,
    onHintClick: () -> Unit,
    canRedo: Boolean = false,
    onRedoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlItem(
            icon = MaterialSymbolsOutlined.ArrowTopLeft,
            label = "Deshacer",
            isEnabled = canUndo,
            isActive = false,
            badgeText = null,
            testTag = "undo_button",
            onClick = onUndoClick
        )

        ControlItem(
            icon = Icons.AutoMirrored.Outlined.Redo, label = "Rehacer",
            isEnabled = canRedo, isActive = false, badgeText = null,
            testTag = "redo_button", onClick = onRedoClick
        )

        ControlItem(
            icon = MaterialSymbolsOutlined.Delete,
            label = "Borrar",
            isEnabled = true,
            isActive = false,
            badgeText = null,
            testTag = "erase_button",
            onClick = onEraseClick
        )

        // Sleek Pill Toggle for Notes
        NotesToggleItem(
            isNotesMode = isNotesMode,
            testTag = "notes_button",
            onClick = onNotesToggleClick
        )

        ControlItem(
            icon = Icons.Outlined.Lightbulb,
            label = "Pista",
            isEnabled = true,
            isActive = false,
            badgeText = if (hintsUsed > 0) "$hintsUsed" else null,
            testTag = "hint_button",
            onClick = onHintClick
        )
    }
}

@Composable
private fun NotesToggleItem(
    isNotesMode: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.sudokuColors
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 32.dp),
                onClick = onClick
            )
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        BadgedBox(
            badge = {
                if (isNotesMode) {
                    Badge(
                        containerColor = colors.controlActiveColor,
                        contentColor = Color.White
                    ) {
                        Text(text = "ON", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isNotesMode) colors.controlActiveColor.copy(alpha = 0.12f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MaterialSymbolsOutlined.Edit,
                    contentDescription = "Notas",
                    tint = if (isNotesMode) colors.controlActiveColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "NOTAS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            softWrap = false,
            color = if (isNotesMode) colors.controlActiveColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ControlItem(
    icon: ImageVector,
    label: String,
    isEnabled: Boolean,
    isActive: Boolean,
    badgeText: String?,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.sudokuColors
    val interactionSource = remember { MutableInteractionSource() }

    val iconTint = when {
        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        isActive -> colors.controlActiveColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                enabled = isEnabled,
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 32.dp),
                onClick = onClick
            )
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        BadgedBox(
            badge = {
                if (badgeText != null) {
                    Badge(
                        containerColor = colors.controlActiveColor,
                        contentColor = Color.White
                    ) {
                        Text(text = badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isActive) colors.controlActiveColor.copy(alpha = 0.15f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            softWrap = false,
            color = if (isActive) colors.controlActiveColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
