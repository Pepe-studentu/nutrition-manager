package org.example.project.view.components.menus

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.example.project.model.Model
import org.example.project.view.components.FocusableButton
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val focusManager = LocalFocusManager.current

        Surface(
            modifier = Modifier
                .width(400.dp)
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Tab) {
                        focusManager.moveFocus(
                            if (keyEvent.isShiftPressed) FocusDirection.Previous
                            else FocusDirection.Next
                        )
                        true
                    } else false
                }
                .focusable(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = tr("create_multi_day_menu"),
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(tr("description"), style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge
                )

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text(tr("number_of_days"), style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FocusableButton(onClick = onDismiss) {
                        Text(tr("cancel"))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FocusableButton(
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
                }
            }
        }
    }
}