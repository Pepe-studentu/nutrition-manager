package org.example.project.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinprojecttest.composeapp.generated.resources.Res
import kotlinprojecttest.composeapp.generated.resources.add
import kotlinx.coroutines.launch
import org.example.project.model.DailyMenu
import org.example.project.model.Meal
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography
import org.jetbrains.compose.resources.painterResource
import view.AddMenuDialog

// New Composable for displaying a single meal entry
@Composable
fun MealEntry(meal: Meal?) {
    val mealName = meal?.name ?: "Unknown Meal"
    val calories = meal?.calories?.let { "$it kcal" } ?: "N/A"
    val proteins = meal?.proteins?.let { "${it}g P" } ?: "N/A"
    val carbs = meal?.carbs?.let { "${it}g C" } ?: "N/A"
    val fats = meal?.fats?.let { "${it}g F" } ?: "N/A"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = mealName,
                style = AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = calories, style = AccessibilityTypography.bodyMedium)
                Text(text = proteins, style = AccessibilityTypography.bodyMedium)
                Text(text = carbs, style = AccessibilityTypography.bodyMedium)
                Text(text = fats, style = AccessibilityTypography.bodyMedium)
            }
        }
    }
}

@Composable
fun MenuCard(menu: DailyMenu) {
    val carbsColor = Color(0xFF8BC34A) // Light Green
    val proteinsColor = Color(0xFF2196F3) // Blue
    val fatsColor = Color(0xFFFFC107) // Amber


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
    ) {
        Column(modifier = Modifier) {
            // Header with proportional macro colors
            Box {
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (menu.carbs > 0) { // Check for > 0 to avoid division by zero or invisible bars
                        Box(
                            modifier = Modifier
                                .weight(menu.carbs * 4f / menu.calories) // Use 'f' for float literal
                                .height(80.dp)
                                .background(carbsColor)
                        )
                    }
                    if (menu.proteins > 0) {
                        Box(
                            modifier = Modifier
                                .weight(menu.proteins * 4f / menu.calories)
                                .height(80.dp)
                                .background(proteinsColor)
                        )
                    }
                    if (menu.fats > 0) {
                        Box(
                            modifier = Modifier
                                .weight(menu.fats * 9f / menu.calories)
                                .height(80.dp)
                                .background(fatsColor)
                        )
                    }
                }
                Text(
                    text = (menu.name ?: "Unnamed Menu") + " - ${menu.calories} kcal",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp),
                    style = AccessibilityTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                MealEntry(meal = Model.getMealById(menu.breakfastId))
                MealEntry(meal = Model.getMealById(menu.snack1Id))
                MealEntry(meal = Model.getMealById(menu.lunchId))
                MealEntry(meal = Model.getMealById(menu.snack2Id))
                MealEntry(meal = Model.getMealById(menu.dinnerId))
            }
        }
    }
}


@Composable
fun MenuScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuToDelete by remember { mutableStateOf<DailyMenu?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewMenuDialog by remember { mutableStateOf(false) }

    val filteredMenus by remember(searchQuery) {
        derivedStateOf { Model.filterMenus(searchQuery) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Menus") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = AccessibilityTypography.bodyLarge
            )
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) { // Apply horizontal padding here
                items(filteredMenus) { menu ->
                    MenuCard(menu = menu)
                }
            }
        }

        FloatingActionButton(
            onClick = { showNewMenuDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(painterResource(Res.drawable.add), contentDescription = "Add")
        }

        if (showNewMenuDialog) {
            AddMenuDialog(
                onDismiss = { showNewMenuDialog = false },
                onAddMenu = { breakfastId, snack1Id, lunchId, snack2Id, dinnerId ->
                    val success = Model.insertMenu(breakfastId, snack1Id, lunchId, snack2Id, dinnerId, "todo")
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (success) "Menu added"
                            else "Failed to add menu: Invalid meal IDs"
                        )
                    }
                },
            )
        }

        if (showDeleteDialog && menuToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Menu", style = AccessibilityTypography.headlineSmall) },
                text = { Text("Are you sure you want to delete this menu?", style = AccessibilityTypography.bodyMedium) },
                confirmButton = {
                    TextButton(onClick = {
                        val success = Model.deleteMenu(menuToDelete!!.id)
                        showDeleteDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "Menu deleted"
                                else "Failed to delete menu"
                            )
                        }
                    }) {
                        Text("Delete", style = AccessibilityTypography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", style = AccessibilityTypography.labelLarge)
                    }
                }
            )
        }
    }
}