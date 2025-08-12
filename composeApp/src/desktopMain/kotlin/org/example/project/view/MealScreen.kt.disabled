package org.example.project.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinprojecttest.composeapp.generated.resources.Res
import kotlinprojecttest.composeapp.generated.resources.add
import kotlinx.coroutines.launch
import org.example.project.model.Meal
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityShapes
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.view.theme.Black
import org.example.project.view.theme.highContrastColorScheme
import org.jetbrains.compose.resources.painterResource

val boxHeight = 32.dp

@Composable
fun MealTableHeader() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "Meal",
                modifier = Modifier.weight(2.5f),
                style = AccessibilityTypography.headlineLarge,
                color = Black
            )
            Text(
                text = "Protein",
                modifier = Modifier.weight(1f),
                style = AccessibilityTypography.headlineLarge,
                color = Black
            )
            Text(
                text = "Fat",
                modifier = Modifier.weight(1f),
                style = AccessibilityTypography.headlineLarge,
                color = Black
            )
            Text(
                text = "Carbs",
                modifier = Modifier.weight(1f),
                style = AccessibilityTypography.headlineLarge,
                color = Black
            )
            Text(
                text = "Calories",
                modifier = Modifier.weight(1f),
                style = AccessibilityTypography.headlineLarge,
                color = Black
            )
        }
        HorizontalDivider(thickness = 4.dp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun MealRow(meal: Meal, modifier: Modifier, onEdit: (Meal) -> Unit, onDelete: (Meal) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AccessibilityShapes.medium)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = AccessibilityShapes.medium
            )
            .clickable { isExpanded = !isExpanded }
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Macronutrient visualization boxes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            if (meal.proteins > 1)
                Box(
                    modifier = Modifier
                        .weight(meal.proteins * 4 / meal.calories)
                        .height(boxHeight)
                        .background(highContrastColorScheme.protein)
                ) {
                    Text(
                        text = "${(meal.proteins * 4 / meal.calories * 100).toInt()}%",
                        style = AccessibilityTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                    )
                }
            if (meal.fats > 1)
                Box(
                    modifier = Modifier
                        .weight(meal.fats * 9 / meal.calories)
                        .height(boxHeight)
                        .background(highContrastColorScheme.fat)
                ) {
                    Text(
                        text = "${(meal.fats * 9 / meal.calories * 100).toInt()}%",
                        style = AccessibilityTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                    )
                }
            if (meal.carbs > 1)
                Box(
                    modifier = Modifier
                        .weight(meal.carbs * 4 / meal.calories)
                        .height(boxHeight)
                        .background(highContrastColorScheme.carbs)
                ) {
                    Text(
                        text = "${(meal.carbs * 4 / meal.calories * 100).toInt()}%",
                        style = AccessibilityTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                    )
                }
        }

        // Main row with meal details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = meal.name,
                modifier = Modifier.weight(2.5f),
                style = AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "%.1f".format(meal.proteins),
                modifier = Modifier.weight(1f),
                style = AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "%.1f".format(meal.fats),
                modifier = Modifier.weight(1f),
                style = AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "%.1f".format(meal.carbs),
                modifier = Modifier.weight(1f),
                style = AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "%.1f".format(meal.calories),
                modifier = Modifier.weight(1f),
                style = AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Expanded content: only buttons

            Column {
                // Ingredients list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    meal.ingredients.forEach { sizedIngredient ->
                        val ingredient = Model.getIngredientById(sizedIngredient.ingredientId)
                        if (ingredient != null) {
                            Text(
                                text = "${sizedIngredient.grams}g ${ingredient.name}",
                                style = AccessibilityTypography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
                // Edit and Delete Buttons
                AnimatedVisibility(visible = isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = { onEdit(meal) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = "Edit",
                            style = AccessibilityTypography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Button(
                        onClick = { onDelete(meal) },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(
                            text = "Delete",
                            style = AccessibilityTypography.labelLarge,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MealTable(filteredMeals: List<Meal>, onEdit: (Meal) -> Unit, onDelete: (Meal) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        MealTableHeader()
        LazyColumn(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            items(filteredMeals) { meal ->
                MealRow(
                    meal = meal,
                    modifier = Modifier.padding(8.dp),
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
@Preview
fun MealScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var mealToDelete by remember { mutableStateOf<Meal?>(null) }
    var showNewMealDialog by remember { mutableStateOf(false) }
    var showEditMealDialog by remember { mutableStateOf(false) }
    var mealToEdit by remember { mutableStateOf<Meal?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredMeals by remember(searchQuery) {
        derivedStateOf { Model.filterMeals(searchQuery) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.fillMaxWidth().padding(24.dp).align(Alignment.BottomCenter)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 24.dp, end = 16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Meals") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textStyle = AccessibilityTypography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
                )
            )
            MealTable(
                filteredMeals = filteredMeals,
                onEdit = { meal ->
                    mealToEdit = meal
                    showEditMealDialog = true
                },
                onDelete = { meal ->
                    mealToDelete = meal
                    showDeleteDialog = true
                }
            )
        }

        FloatingActionButton(
            onClick = { showNewMealDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp),
            shape = MaterialTheme.shapes.medium,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                painter = painterResource(Res.drawable.add),
                contentDescription = "Add meal",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (showNewMealDialog) {
            AddMealDialog(
                onDismiss = { showNewMealDialog = false },
                onAddMeal = { name, sizedIngredients ->
                    val success = Model.insertMeal(name, sizedIngredients)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (success) "Meal added: $name"
                            else "Failed to add meal: Invalid data"
                        )
                    }
                },
                onEditMeal = { id, name, sizedIngredients ->
                    val success = Model.updateMeal(id, name, sizedIngredients)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (success) "Meal updated: $name"
                            else "Failed to update meal: Invalid data"
                        )
                    }
                },
                allIngredients = Model.ingredients
            )
        }

        if (showEditMealDialog && mealToEdit != null) {
            AddMealDialog(
                onDismiss = { showEditMealDialog = false },
                onAddMeal = { _, _ -> }, // Not used in edit mode
                onEditMeal = { id, name, sizedIngredients ->
                    val success = Model.updateMeal(id, name, sizedIngredients)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (success) "Meal updated: $name"
                            else "Failed to update meal: Invalid data"
                        )
                    }
                },
                allIngredients = Model.ingredients,
                meal = mealToEdit
            )
        }

        if (showDeleteDialog && mealToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Meal", style = AccessibilityTypography.titleLarge) },
                text = { Text("Are you sure you want to delete ${mealToDelete?.name}?", style = AccessibilityTypography.bodyLarge) },
                confirmButton = {
                    TextButton(onClick = {
                        val success = Model.deleteMeal(mealToDelete!!.id)
                        showDeleteDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "Meal deleted"
                                else "Cannot delete: Meal used in a menu"
                            )
                        }
                    }) {
                        Text("Delete", style = AccessibilityTypography.labelLarge, color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", style = AccessibilityTypography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                },
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                textContentColor = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}