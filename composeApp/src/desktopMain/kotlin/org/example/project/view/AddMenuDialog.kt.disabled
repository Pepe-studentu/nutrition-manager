package view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.example.project.model.Meal
import org.example.project.model.Model

@Composable
fun AddMenuDialog(
    onDismiss: () -> Unit,
    onAddMenu: (String, String, String, String, String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.7f),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                var searchQuery by remember { mutableStateOf("") }
                val filteredMeals by remember(searchQuery) {
                    derivedStateOf { Model.filterMeals(searchQuery) }
                }
                var breakfastId by remember { mutableStateOf("") }
                var snack1Id by remember { mutableStateOf("") }
                var lunchId by remember { mutableStateOf("") }
                var snack2Id by remember { mutableStateOf("") }
                var dinnerId by remember { mutableStateOf("") }

                // Meal selection dropdowns
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Meals") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp)
                ) {
                    items(filteredMeals) { meal ->
                        Text(
                            text = meal.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Simple selection: assign to first empty slot
                                    when {
                                        breakfastId.isEmpty() -> breakfastId = meal.id
                                        snack1Id.isEmpty() -> snack1Id = meal.id
                                        lunchId.isEmpty() -> lunchId = meal.id
                                        snack2Id.isEmpty() -> snack2Id = meal.id
                                        dinnerId.isEmpty() -> dinnerId = meal.id
                                    }
                                }
                                .padding(8.dp),
                            fontSize = 14.sp
                        )
                    }
                }

                // Display selected meals
                Text("Breakfast: ${Model.getMealById(breakfastId)?.name ?: "Not selected"}")
                Text("Snack 1: ${Model.getMealById(snack1Id)?.name ?: "Not selected"}")
                Text("Lunch: ${Model.getMealById(lunchId)?.name ?: "Not selected"}")
                Text("Snack 2: ${Model.getMealById(snack2Id)?.name ?: "Not selected"}")
                Text("Dinner: ${Model.getMealById(dinnerId)?.name ?: "Not selected"}")

                // Add button
                Button(
                    onClick = {
                        if (listOf(breakfastId, snack1Id, lunchId, snack2Id, dinnerId).all { it.isNotEmpty() }) {
                            onAddMenu(breakfastId, snack1Id, lunchId, snack2Id, dinnerId)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = listOf(breakfastId, snack1Id, lunchId, snack2Id, dinnerId).all { it.isNotEmpty() }
                ) {
                    Text("Add Menu")
                }
            }
        }
    }
}