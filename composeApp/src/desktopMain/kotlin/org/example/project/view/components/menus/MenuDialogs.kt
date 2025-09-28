package org.example.project.view.components.menus

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.service.tr
import org.example.project.service.TranslationService

@Composable
fun MultiDayMenuInputDialog(
    onDismiss: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("7") }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("create_multi_day_menu"), style = AccessibilityTypography.headlineSmall) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Tab) {
                            focusManager.moveFocus(
                                if (keyEvent.isShiftPressed) FocusDirection.Previous
                                else FocusDirection.Next
                            )
                            true
                        } else false
                    }
                    .focusable()
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(tr("description"), style = AccessibilityTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = AccessibilityTypography.bodyLarge
                )

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text(tr("number_of_days"), style = AccessibilityTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = AccessibilityTypography.bodyLarge
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val daysInt = days.toIntOrNull()
                    val success = if (daysInt != null && daysInt > 0) {
                        Model.insertMultiDayMenu(description.trim(), daysInt)
                    } else false

                    if (success) {
                        onDismiss()
                        showSnackbar(TranslationService.getString("menu_created_successfully"))
                    } else {
                        showSnackbar(TranslationService.getString("failed_to_create_menu_check_inputs"))
                    }
                }
            ) {
                Text(tr("create"))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(tr("cancel"))
            }
        }
    )
}