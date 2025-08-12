package org.example.project.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.example.project.model.Food
import org.example.project.model.FoodCategory
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography

data class FoodComponent(
    val foodName: String,
    val percentage: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodInputDialog(
    onDismiss: () -> Unit,
    showSnackbar: (String) -> Unit,
    food: Food? = null // null for new food, non-null for editing
) {
    var isCompoundFood by remember { mutableStateOf(food?.isCompoundFood ?: false) }
    var name by remember { mutableStateOf(food?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(food?.category ?: FoodCategory.VEGGIES) }

    // Basic food fields
    var proteins by remember { mutableStateOf(food?.proteins?.toString() ?: "") }
    var carbs by remember { mutableStateOf(food?.carbs?.toString() ?: "") }
    var fats by remember { mutableStateOf(food?.fats?.toString() ?: "") }
    var water by remember { mutableStateOf(food?.waterMassPercentage?.toString() ?: "") }

    // Compound food fields
    var searchQuery by remember { mutableStateOf("") }
    val availableFoods by remember(searchQuery) {
        derivedStateOf { 
            Model.filterFoods(searchQuery).filter { it.name != name } // Don't allow self-reference
        }
    }
    val selectedComponents = remember {
        mutableStateListOf<FoodComponent>().apply {
            if (food?.isCompoundFood == true) {
                addAll(food.components.map { (name, percentage) -> 
                    FoodComponent(name, percentage) 
                })
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (food == null) "Add Food" else "Edit Food",
                    style = AccessibilityTypography.headlineSmall
                )

                // Basic info (always shown)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name", style = AccessibilityTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = AccessibilityTypography.bodyLarge
                )

                // Category dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category", style = AccessibilityTypography.bodyMedium) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = AccessibilityTypography.bodyLarge
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        FoodCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName, style = AccessibilityTypography.bodyLarge) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Toggle for food type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Food Type:", style = AccessibilityTypography.bodyMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Row {
                        FilterChip(
                            onClick = { isCompoundFood = false },
                            label = { Text("Basic Food") },
                            selected = !isCompoundFood
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            onClick = { isCompoundFood = true },
                            label = { Text("Compound Food") },
                            selected = isCompoundFood
                        )
                    }
                }

                if (!isCompoundFood) {
                    // Basic Food Mode
                    BasicFoodContent(
                        proteins = proteins,
                        onProteinsChange = { proteins = it },
                        carbs = carbs,
                        onCarbsChange = { carbs = it },
                        fats = fats,
                        onFatsChange = { fats = it },
                        water = water,
                        onWaterChange = { water = it }
                    )
                } else {
                    // Compound Food Mode
                    CompoundFoodContent(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        availableFoods = availableFoods,
                        selectedComponents = selectedComponents,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val newFood = if (!isCompoundFood) {
                                // Create basic food
                                Food(
                                    name = name.trim(),
                                    category = selectedCategory,
                                    proteins = proteins.toFloatOrNull(),
                                    carbs = carbs.toFloatOrNull(),
                                    fats = fats.toFloatOrNull(),
                                    waterMassPercentage = water.toFloatOrNull(),
                                    usageCount = food?.usageCount ?: 0
                                )
                            } else {
                                // Create compound food
                                Food(
                                    name = name.trim(),
                                    category = selectedCategory,
                                    components = selectedComponents.associate { it.foodName to it.percentage },
                                    usageCount = food?.usageCount ?: 0
                                )
                            }

                            val success = if (food == null) {
                                Model.insertFood(newFood)
                            } else {
                                Model.updateFood(food.name, newFood)
                            }

                            if (success) {
                                onDismiss()
                                showSnackbar("Food ${if (food == null) "added" else "updated"} successfully")
                            } else {
                                showSnackbar("Failed to ${if (food == null) "add" else "update"} food. Check inputs.")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && (
                            (!isCompoundFood && listOf(proteins, carbs, fats, water).all { it.toFloatOrNull() != null }) ||
                            (isCompoundFood && selectedComponents.isNotEmpty())
                        )
                    ) {
                        Text(if (food == null) "Add" else "Update")
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicFoodContent(
    proteins: String,
    onProteinsChange: (String) -> Unit,
    carbs: String,
    onCarbsChange: (String) -> Unit,
    fats: String,
    onFatsChange: (String) -> Unit,
    water: String,
    onWaterChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Macronutrients (per 100g)", style = AccessibilityTypography.bodyMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = proteins,
                onValueChange = onProteinsChange,
                label = { Text("Protein", style = AccessibilityTypography.bodySmall) },
                modifier = Modifier.weight(1f),
                textStyle = AccessibilityTypography.bodyMedium
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = onCarbsChange,
                label = { Text("Carbs", style = AccessibilityTypography.bodySmall) },
                modifier = Modifier.weight(1f),
                textStyle = AccessibilityTypography.bodyMedium
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fats,
                onValueChange = onFatsChange,
                label = { Text("Fats", style = AccessibilityTypography.bodySmall) },
                modifier = Modifier.weight(1f),
                textStyle = AccessibilityTypography.bodyMedium
            )
            OutlinedTextField(
                value = water,
                onValueChange = onWaterChange,
                label = { Text("Water", style = AccessibilityTypography.bodySmall) },
                modifier = Modifier.weight(1f),
                textStyle = AccessibilityTypography.bodyMedium
            )
        }
    }
}

@Composable
private fun CompoundFoodContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    availableFoods: List<Food>,
    selectedComponents: MutableList<FoodComponent>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        // Left side: Search and available foods
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search Foods") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn {
                items(availableFoods) { food ->
                    Text(
                        text = food.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedComponents.none { it.foodName == food.name }) {
                                    selectedComponents.add(FoodComponent(food.name, 10f))
                                }
                            }
                            .padding(8.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Right side: Selected components
        Column(
            modifier = Modifier
                .weight(1.5f)
                .padding(start = 8.dp)
        ) {
            Text("Components", style = AccessibilityTypography.bodyMedium)
            
            val totalPercentage = selectedComponents.sumOf { it.percentage.toDouble() }.toFloat()
            Text(
                text = "Total: ${totalPercentage}%",
                style = AccessibilityTypography.bodyMedium,
                color = if (totalPercentage <= 100f) MaterialTheme.colorScheme.onSurface
                       else MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn {
                items(selectedComponents.size) { index ->
                    val component = selectedComponents[index]
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = component.foodName,
                                modifier = Modifier.weight(1f),
                                style = AccessibilityTypography.bodyMedium
                            )
                            
                            OutlinedTextField(
                                value = component.percentage.toString(),
                                onValueChange = { newValue ->
                                    val percentage = newValue.toFloatOrNull() ?: 0f
                                    selectedComponents[index] = component.copy(percentage = percentage)
                                },
                                label = { Text("%") },
                                modifier = Modifier.width(80.dp),
                                singleLine = true
                            )
                            
                            Button(
                                onClick = { selectedComponents.removeAt(index) },
                                modifier = Modifier.size(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("×", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}