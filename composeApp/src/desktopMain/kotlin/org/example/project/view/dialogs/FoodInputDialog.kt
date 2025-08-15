package org.example.project.view.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.example.project.model.Food
import org.example.project.model.FoodCategory
import org.example.project.model.Model

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
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Add food (basic) (compound) buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (food == null) "Add food" else "Edit food",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.width(100.dp))
                    FilterChip(
                        onClick = { isCompoundFood = false },
                        label = { Text("basic", style = MaterialTheme.typography.bodyMedium) },
                        selected = !isCompoundFood
                    )
                    FilterChip(
                        onClick = { isCompoundFood = true },
                        label = { Text("compound", style = MaterialTheme.typography.bodyMedium) },
                        selected = isCompoundFood
                    )
                }

                // Title and Category fields in row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Title", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    // Category dropdown
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.displayName,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Category", style = MaterialTheme.typography.bodyMedium) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            FoodCategory.values().forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.displayName, style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Left side: Search and available foods
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Search foods", style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyColumn {
                                items(availableFoods) { food ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (selectedComponents.none { it.foodName == food.name }) {
                                                    selectedComponents.add(FoodComponent(food.name, 10f))
                                                }
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "--",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = food.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }

                        // Vertical divider
                        VerticalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.outline
                        )

                        // Right side: Components with total macros
                        Column(
                            modifier = Modifier
                                .weight(2f)
                                .padding(start = 8.dp)
                        ) {
                            Text(
                                text = "Components:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // Total macros display
                            CompoundFoodMacrosDisplay(selectedComponents = selectedComponents)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyColumn {
                                items(selectedComponents.size) { index ->
                                    val component = selectedComponents[index]
                                    val componentFood = Model.foods.find { it.name == component.foodName }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = component.foodName,
                                            modifier = Modifier.weight(2f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        TextField(
                                            value = component.percentage.toInt().toString(),
                                            onValueChange = { newValue ->
                                                val percentage = newValue.toFloatOrNull() ?: 0f
                                                selectedComponents[index] = component.copy(percentage = percentage)
                                            },
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.End),
                                            modifier = Modifier.weight(1f),
                                            suffix = { Text("%", style = MaterialTheme.typography.bodyMedium) },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                        
                                        // Food info display
                                        if (componentFood != null) {
                                            val macros = Model.getFoodMacros(componentFood.name)
                                            if (macros != null) {
                                                val adjustedProteins = macros.proteins * component.percentage / 100
                                                val adjustedFats = macros.fats * component.percentage / 100
                                                val adjustedCarbs = macros.carbs * component.percentage / 100
                                                val adjustedWater = macros.waterMassPercentage * component.percentage / 100
                                                
                                                Text(
                                                    text = "${adjustedProteins.toInt()}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                                Text(
                                                    text = "${adjustedFats.toInt()}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                                Text(
                                                    text = "${adjustedCarbs.toInt()}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                                Text(
                                                    text = "${adjustedWater.toInt()}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                            }
                                        }
                                        
                                        Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                                            Button(
                                                onClick = { selectedComponents.removeAt(index) },
                                                modifier = Modifier.size(24.dp),
                                                contentPadding = PaddingValues(0.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Text(
                                                    "×",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onError
                                                )
                                            }
                                        }
                                    }
                                    
                                    if (index < selectedComponents.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add button (no cancel button as requested)
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
                    modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun CompoundFoodMacrosDisplay(
    selectedComponents: List<FoodComponent>
) {
    // Calculate total macros from components
    val totalPercentage by remember(selectedComponents) {
        derivedStateOf { selectedComponents.sumOf { it.percentage.toDouble() }.toFloat() }
    }
    
    val totalProteins by remember(selectedComponents, Model.foods) {
        derivedStateOf {
            selectedComponents.sumOf { component ->
                val macros = Model.getFoodMacros(component.foodName)
                macros?.proteins?.toDouble()?.times(component.percentage / 100) ?: 0.0
            }.toFloat()
        }
    }
    
    val totalFats by remember(selectedComponents, Model.foods) {
        derivedStateOf {
            selectedComponents.sumOf { component ->
                val macros = Model.getFoodMacros(component.foodName)
                macros?.fats?.toDouble()?.times(component.percentage / 100) ?: 0.0
            }.toFloat()
        }
    }
    
    val totalCarbs by remember(selectedComponents, Model.foods) {
        derivedStateOf {
            selectedComponents.sumOf { component ->
                val macros = Model.getFoodMacros(component.foodName)
                macros?.carbs?.toDouble()?.times(component.percentage / 100) ?: 0.0
            }.toFloat()
        }
    }
    
    val totalWater by remember(selectedComponents, Model.foods) {
        derivedStateOf {
            selectedComponents.sumOf { component ->
                val macros = Model.getFoodMacros(component.foodName)
                macros?.waterMassPercentage?.toDouble()?.times(component.percentage / 100) ?: 0.0
            }.toFloat()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Total macros:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = "${totalPercentage.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right,
            color = if (totalPercentage <= 100f) MaterialTheme.colorScheme.onSurface
                   else MaterialTheme.colorScheme.error
        )
        Text(
            text = "P:${totalProteins.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right
        )
        Text(
            text = "F:${totalFats.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right
        )
        Text(
            text = "C:${totalCarbs.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right
        )
        Text(
            text = "W:${totalWater.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right
        )
        Spacer(Modifier.weight(0.5f))
    }
    
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.outline
    )
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
        Text("Macronutrients (per 100g)", style = MaterialTheme.typography.bodyMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = proteins,
                onValueChange = onProteinsChange,
                label = { Text("Protein", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = onCarbsChange,
                label = { Text("Carbs", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fats,
                onValueChange = onFatsChange,
                label = { Text("Fats", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = water,
                onValueChange = onWaterChange,
                label = { Text("Water %", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

