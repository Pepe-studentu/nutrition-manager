package view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.example.project.model.Ingredient
import org.example.project.model.Model

@Composable
fun IngredientInputDialog(
    onDismiss: () -> Unit,
    showSnackbar: (String) -> Unit,
    ingredient: Ingredient? = null
) {
    val isEdit = ingredient != null
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var proteins by remember { mutableStateOf(ingredient?.proteins?.toString() ?: "") }
    var fats by remember { mutableStateOf(ingredient?.fats?.toString() ?: "") }
    var carbs by remember { mutableStateOf(ingredient?.carbs?.toString() ?: "") }
    var water by remember { mutableStateOf(ingredient?.waterMassPercentage?.toString() ?: "") }

    val focusManager = LocalFocusManager.current

    // Create focus requesters for each field
    val nameFocusRequester = remember { FocusRequester() }
    val proteinsFocusRequester = remember { FocusRequester() }
    val fatsFocusRequester = remember { FocusRequester() }
    val carbsFocusRequester = remember { FocusRequester() }
    val waterFocusRequester = remember { FocusRequester() }
    val submitFocusRequester = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .wrapContentHeight()
                .width(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(if (isEdit) "Edit Ingredient" else "Add Ingredient", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .focusRequester(nameFocusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                proteinsFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                )

                OutlinedTextField(
                    value = proteins,
                    onValueChange = { proteins = it },
                    label = { Text("Proteins (g)") },
                    modifier = Modifier
                        .focusRequester(proteinsFocusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                fatsFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                )

                OutlinedTextField(
                    value = fats,
                    onValueChange = { fats = it },
                    label = { Text("Fats (g)") },
                    modifier = Modifier
                        .focusRequester(fatsFocusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                carbsFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                )

                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs (g)") },
                    modifier = Modifier
                        .focusRequester(carbsFocusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                waterFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                )

                OutlinedTextField(
                    value = water,
                    onValueChange = { water = it },
                    label = { Text("Water (%)") },
                    modifier = Modifier
                        .focusRequester(waterFocusRequester)
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                                submitFocusRequester.requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val success = if (isEdit) {
                                Model.updateIngredient(ingredient!!.id, name, proteins, fats, carbs, water)
                            } else {
                                Model.insertIngredient(name, proteins, fats, carbs, water)
                            }
                            if (success) {
                                showSnackbar(if (isEdit) "Ingredient updated successfully." else "Ingredient added successfully.")
                                onDismiss()
                            } else {
                                showSnackbar("Invalid input. Please check values.")
                            }
                        },
                        modifier = Modifier.focusRequester(submitFocusRequester)
                    ) {
                        Text(if (isEdit) "Update" else "Submit")
                    }
                }
            }
        }
    }
}