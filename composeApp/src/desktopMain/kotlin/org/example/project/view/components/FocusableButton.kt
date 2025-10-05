package org.example.project.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun FocusableButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    // Determine border color: use secondary (yellow) if button is primary (blue), otherwise use primary
    val borderColor = if (colors.containerColor == MaterialTheme.colorScheme.primary) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Button(
        onClick = onClick,
        modifier = modifier.onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        },
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = if (isFocused) BorderStroke(3.dp, borderColor) else null,
        contentPadding = contentPadding,
        content = content
    )
}
