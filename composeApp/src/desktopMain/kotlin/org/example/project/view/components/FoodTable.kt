package org.example.project.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinprojecttest.composeapp.generated.resources.Res
import kotlinprojecttest.composeapp.generated.resources.arrow_drop_down_24px
import kotlinprojecttest.composeapp.generated.resources.arrow_drop_up_24px
import org.example.project.model.Food
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.view.theme.Black
import org.example.project.service.tr
import org.jetbrains.compose.resources.painterResource



@Composable
fun ShowFoodComponents(food: Food, indentLevel: Int) {
    food.components.forEach { (componentName, percentage) ->
        val componentFood = Model.getFoodByName(componentName)
        if (componentFood != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 16.dp)
                    .padding(start = (indentLevel * 25).dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "${"%.1f".format(percentage)}% $componentName",
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
fun SortableHeaderCell(
    text: String,
    isActive: Boolean,
    ascending: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = AccessibilityTypography.headlineMedium,
            color = Black,
            modifier = Modifier.weight(1f)
        )
        if (isActive) {
            if (ascending) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_drop_up_24px),
                    contentDescription = tr("ascending"),
                    modifier = Modifier.size(32.dp),
                    tint = Black
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.arrow_drop_down_24px),
                    contentDescription = tr("descending"), 
                    modifier = Modifier.size(32.dp),
                    tint = Black
                )
            }
        } else {
            Spacer(modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun FoodsHeader(
    activeSortColumn: String?,
    sortAscending: Map<String, Boolean>,
    onHeaderClick: (String) -> Unit,
    searchActive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background) 
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        SortableHeaderCell(
            text = tr("food"),
            isActive = !searchActive && activeSortColumn == "food",
            ascending = sortAscending["food"] ?: true,
            modifier = Modifier.weight(2.5f),
            onClick = { if (!searchActive) onHeaderClick("food") }
        )
        SortableHeaderCell(
            text = tr("protein"),
            isActive = !searchActive && activeSortColumn == "protein",
            ascending = sortAscending["protein"] ?: true,
            modifier = Modifier.weight(1f),
            onClick = { if (!searchActive) onHeaderClick("protein") }
        )
        SortableHeaderCell(
            text = tr("fat"),
            isActive = !searchActive && activeSortColumn == "fat",
            ascending = sortAscending["fat"] ?: true,
            modifier = Modifier.weight(1f),
            onClick = { if (!searchActive) onHeaderClick("fat") }
        )
        SortableHeaderCell(
            text = tr("carbs"),
            isActive = !searchActive && activeSortColumn == "carbs",
            ascending = sortAscending["carbs"] ?: true,
            modifier = Modifier.weight(1f),
            onClick = { if (!searchActive) onHeaderClick("carbs") }
        )
        Text(
            text = tr("water_percentage"),
            modifier = Modifier.weight(1f).padding(4.dp),
            style = AccessibilityTypography.headlineMedium,
            color = Black
        )
        SortableHeaderCell(
            text = tr("calories"),
            isActive = !searchActive && activeSortColumn == "calories",
            ascending = sortAscending["calories"] ?: true,
            modifier = Modifier.weight(1.1f),
            onClick = { if (!searchActive) onHeaderClick("calories") }
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
                    text = "%.1f".format(macros.proteins),
                    modifier = Modifier.weight(1f),
                    style = AccessibilityTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "%.1f".format(macros.fats),
                    modifier = Modifier.weight(1f),
                    style = AccessibilityTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "%.1f".format(macros.carbs),
                    modifier = Modifier.weight(1f),
                    style = AccessibilityTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "%.1f".format(macros.waterMassPercentage),
                    modifier = Modifier.weight(1f),
                    style = AccessibilityTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val calories = macros.proteins * 4 + macros.carbs * 4 + macros.fats * 9
                Text(
                    text = "%.1f".format(calories),
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
                    Text(tr("edit"))
                }
                Button(onClick = { onDeleteClick(food) }) {
                    Text(tr("delete"))
                }
            }
        }
    }

    HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f))
}

@Composable
fun FoodTable(
    foods: List<Food>,
    activeSortColumn: String?,
    sortAscending: Map<String, Boolean>,
    onHeaderClick: (String) -> Unit,
    onDeleteClick: (Food) -> Unit, 
    onEditClick: (Food) -> Unit,
    searchActive: Boolean = false
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
            FoodsHeader(
                activeSortColumn = activeSortColumn,
                sortAscending = sortAscending,
                onHeaderClick = onHeaderClick,
                searchActive = searchActive
            )
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