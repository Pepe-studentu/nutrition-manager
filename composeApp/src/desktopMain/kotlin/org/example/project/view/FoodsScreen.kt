package org.example.project.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinprojecttest.composeapp.generated.resources.Res
import kotlinprojecttest.composeapp.generated.resources.add
import kotlinx.coroutines.launch
import org.example.project.model.Food
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsScreen() {
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var foodToDelete by remember { mutableStateOf<Food?>(null) }

    var selectedFood by remember { mutableStateOf<Food?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    val filteredFoods by remember(searchQuery) {
        derivedStateOf { Model.filterFoods(searchQuery) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showDialog) {
            FoodInputDialog(
                onDismiss = { 
                    showDialog = false 
                    selectedFood = null
                },
                showSnackbar = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                },
                food = selectedFood
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    foodToDelete = null
                },
                confirmButton = {
                    Button(onClick = {
                        val success = Model.deleteFood(foodToDelete!!.name)
                        showDeleteDialog = false
                        foodToDelete = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "Food deleted"
                                else "Cannot delete: Food used elsewhere"
                            )
                        }
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDeleteDialog = false
                        foodToDelete = null
                    }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete ${foodToDelete?.name}?") }
            )
        }

        Column {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Foods", style = AccessibilityTypography.bodyLarge) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = AccessibilityTypography.bodyLarge,
                singleLine = true
            )

            FoodTable(
                foods = filteredFoods,
                onEditClick = { food ->
                    selectedFood = food
                    showDialog = true
                },
                onDeleteClick = { food ->
                    foodToDelete = food
                    showDeleteDialog = true
                }
            )
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            SnackbarHost(hostState = snackbarHostState)
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(painterResource(Res.drawable.add), contentDescription = "Add Food")
        }
    }
}

