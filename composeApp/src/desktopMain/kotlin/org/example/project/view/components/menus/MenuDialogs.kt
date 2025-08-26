package org.example.project.view.components.menus

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography

@Composable
fun MultiDayMenuInputDialog(
    onDismiss: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Multi-Day Menu", style = AccessibilityTypography.headlineSmall) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", style = AccessibilityTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = AccessibilityTypography.bodyLarge
                )

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Number of Days", style = AccessibilityTypography.bodyMedium) },
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
                        showSnackbar("Menu created successfully")
                    } else {
                        showSnackbar("Failed to create menu. Check inputs.")
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}