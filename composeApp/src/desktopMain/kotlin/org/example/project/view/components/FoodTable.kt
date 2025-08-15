package org.example.project.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.model.Food
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.view.theme.Black

@Composable
fun ShowFoodComponents(food: Food, indentLevel: Int) {
    food.components.forEach { (componentName, percentage) ->
        val componentFood = Model.getFoodByName(componentName)
        if (componentFood != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 16.dp)
                    .padding(start = (indentLevel * 20).dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "${percentage}% $componentName",
                    modifier = Modifier.weight(2.5f),
                    style = AccessibilityTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Empty cells for component rows
                repeat(5) {
                    Text(
                        text = "",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // Recursively show nested components if this component is also compound
            if (componentFood.isCompoundFood) {
                ShowFoodComponents(componentFood, indentLevel + 1)
            }
        }
    }
}

@Composable
fun FoodsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background) 
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "Food",
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
            text = "Water %",
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

@Composable
fun FoodRow(
    food: Food,
    onDeleteClick: (Food) -> Unit,
    onEditClick: (Food) -> Unit,
    indentLevel: Int = 0
) {
    var expanded by remember { mutableStateOf(false) }
    val macros = Model.getFoodMacros(food.name)
    
    Column(modifier = Modifier.clickable { expanded = !expanded }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .padding(start = (indentLevel * 20).dp) // Indent for tree structure
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = food.name,
                modifier = Modifier.weight(2.5f),
                style = AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (macros != null) {
                Text(
                    text = macros.proteins.toString(),
                    modifier = Modifier.weight(1f),
                    style = AccessibilityTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = macros.fats.toString(),
                    modifier = Modifier.weight(1f),
                    style = AccessibilityTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = macros.carbs.toString(),
                    modifier = Modifier.weight(1f),
                    style = AccessibilityTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = macros.waterMassPercentage.toString(),
                    modifier = Modifier.weight(1f),
                    style = AccessibilityTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val calories = macros.proteins * 4 + macros.carbs * 4 + macros.fats * 9
                Text(
                    text = calories.toString(),
                    modifier = Modifier.weight(1f),
                    style = AccessibilityTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                // Empty cells if no macros available
                repeat(5) {
                    Text(
                        text = "",
                        modifier = Modifier.weight(1f),
                        style = AccessibilityTypography.bodyLarge
                    )
                }
            }
        }

        // Show components recursively if it's a compound food
        if (food.isCompoundFood) {
            ShowFoodComponents(food, indentLevel + 1)
        }

        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(onClick = { onEditClick(food) }, modifier = Modifier.padding(end = 24.dp)) {
                    Text("Edit")
                }
                Button(onClick = { onDeleteClick(food) }) {
                    Text("Delete")
                }
            }
        }
    }

    HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f))
}

@Composable
fun FoodTable(
    foods: List<Food>, 
    onDeleteClick: (Food) -> Unit, 
    onEditClick: (Food) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            FoodsHeader()
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(foods) { food ->
                    FoodRow(
                        food = food, 
                        onEditClick = onEditClick, 
                        onDeleteClick = onDeleteClick
                    )
                }
            }
        }
    }
}