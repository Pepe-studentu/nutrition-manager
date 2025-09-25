package org.example.project.view.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.example.project.model.Food
import org.example.project.model.FoodCategory
import org.example.project.model.Model
import org.example.project.service.tr
import org.example.project.service.TranslationService

data class FoodComponent(
    val foodName: String,
    val grams: Float
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
    var selectedCategory by remember {
        mutableStateOf(
            food?.getEffectiveCategories(Model::getFoodByName)?.firstOrNull() ?: FoodCategory.VEGGIES
        )
    }

    // Tags field (for basic foods only)
    var tagsInput by remember {
        mutableStateOf(
            food?.getEffectiveTags(Model::getFoodByName)?.joinToString(";") ?: ""
        )
    }

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
                // Convert existing percentages back to grams (assume 100g total as base)
                addAll(food.components.map { (name, percentage) ->
                    FoodComponent(name, percentage) // Keep as grams equivalent
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
                        text = if (food == null) tr("add_food") else tr("edit_food"),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.width(100.dp))
                    FilterChip(
                        onClick = { isCompoundFood = false },
                        label = { Text(tr("basic"), style = MaterialTheme.typography.bodyMedium) },
                        selected = !isCompoundFood
                    )
                    FilterChip(
                        onClick = { isCompoundFood = true },
                        label = { Text(tr("compound"), style = MaterialTheme.typography.bodyMedium) },
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
                        label = { Text(tr("title"), style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    // Category dropdown - only for basic foods
                    if (!isCompoundFood) {
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
                                label = { Text(tr("category"), style = MaterialTheme.typography.bodyMedium) },
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
                    } else {
                        // For compound foods, show inherited categories as chips
                            // Calculate inherited categories dynamically
                            val inheritedCategories = remember(selectedComponents.toList()) {
                                selectedComponents.mapNotNull { component ->
                                    Model.getFoodByName(component.foodName)?.getEffectiveCategories(Model::getFoodByName)
                                }.flatten().toSet()
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(inheritedCategories.toList()) { category ->
                                    FilterChip(
                                        onClick = { /* Not selectable */ },
                                        label = {
                                            Text(
                                                category.displayName,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        },
                                        selected = true,
                                        enabled = true,
                                    )
                                }

                                if (inheritedCategories.isEmpty()) {
                                    item {
                                        Text(
                                            text = tr("add_ingredients_to_see_categories"),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                    }
                }

                // Tags input for basic foods, or inherited tags display for compound foods
                if (!isCompoundFood) {
                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = { Text(tr("tags"), style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        placeholder = { Text(tr("tags_placeholder"), style = MaterialTheme.typography.bodyMedium) }
                    )
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
                                label = { Text(tr("search_foods"), style = MaterialTheme.typography.bodyMedium) },
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
                                                    selectedComponents.add(FoodComponent(food.name, 100f))
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
                                text = tr("components"),
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
                                            value = component.grams.toInt().toString(),
                                            onValueChange = { newValue ->
                                                val grams = newValue.toFloatOrNull() ?: 0f
                                                selectedComponents[index] = component.copy(grams = grams)
                                            },
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.End),
                                            modifier = Modifier.weight(1f),
                                            suffix = { Text(tr("grams"), style = MaterialTheme.typography.bodyMedium) },
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
                                                val adjustedProteins = macros.proteins * component.grams / 100
                                                val adjustedFats = macros.fats * component.grams / 100
                                                val adjustedCarbs = macros.carbs * component.grams / 100
                                                val adjustedWater = macros.waterMassPercentage * component.grams / 100

                                                Text(
                                                    text = "${adjustedProteins.toInt()}g",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                                Text(
                                                    text = "${adjustedFats.toInt()}g",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                                Text(
                                                    text = "${adjustedCarbs.toInt()}g",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                                Text(
                                                    text = "${adjustedWater.toInt()}g",
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
                            // Parse tags from semicolon-separated input
                            val parsedTags = tagsInput.split(";")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .toSet()

                            // Create basic food
                            Food(
                                name = name.trim(),
                                categories = setOf(selectedCategory),
                                tags = parsedTags,
                                proteins = proteins.toFloatOrNull(),
                                carbs = carbs.toFloatOrNull(),
                                fats = fats.toFloatOrNull(),
                                waterMassPercentage = water.toFloatOrNull(),
                                usageCount = food?.usageCount ?: 0
                            )
                        } else {
                            // Create compound food - convert grams to percentages
                            val totalGrams = selectedComponents.sumOf { it.grams.toDouble() }.toFloat()
                            val componentsAsPercentages = selectedComponents.associate { component ->
                                component.foodName to (component.grams / totalGrams * 100f)
                            }
                            Food(
                                name = name.trim(),
                                components = componentsAsPercentages,
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
                            showSnackbar(TranslationService.getString(if (food == null) "food_added_successfully" else "food_updated_successfully"))
                        } else {
                            showSnackbar(TranslationService.getString(if (food == null) "failed_to_add_food" else "failed_to_update_food"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && (
                        (!isCompoundFood && listOf(proteins, carbs, fats, water).all { it.toFloatOrNull() != null }) ||
                        (isCompoundFood && selectedComponents.isNotEmpty())
                    )
                ) {
                    Text(if (food == null) tr("add") else tr("update"))
                }
            }
        }
    }
}

@Composable
private fun CompoundFoodMacrosDisplay(
    selectedComponents: List<FoodComponent>
) {
    // Calculate total grams and macros from components
    val totalGrams by remember(selectedComponents) {
        derivedStateOf { selectedComponents.sumOf { it.grams.toDouble() }.toFloat() }
    }

    val totalProteins by remember(selectedComponents, Model.foods) {
        derivedStateOf {
            selectedComponents.sumOf { component ->
                val macros = Model.getFoodMacros(component.foodName)
                macros?.proteins?.toDouble()?.times(component.grams / 100) ?: 0.0
            }.toFloat()
        }
    }

    val totalFats by remember(selectedComponents, Model.foods) {
        derivedStateOf {
            selectedComponents.sumOf { component ->
                val macros = Model.getFoodMacros(component.foodName)
                macros?.fats?.toDouble()?.times(component.grams / 100) ?: 0.0
            }.toFloat()
        }
    }

    val totalCarbs by remember(selectedComponents, Model.foods) {
        derivedStateOf {
            selectedComponents.sumOf { component ->
                val macros = Model.getFoodMacros(component.foodName)
                macros?.carbs?.toDouble()?.times(component.grams / 100) ?: 0.0
            }.toFloat()
        }
    }

    val totalWater by remember(selectedComponents, Model.foods) {
        derivedStateOf {
            selectedComponents.sumOf { component ->
                val macros = Model.getFoodMacros(component.foodName)
                macros?.waterMassPercentage?.toDouble()?.times(component.grams / 100) ?: 0.0
            }.toFloat()
        }
    }

    // Calculate macros per 100g of final compound food
    val proteinsPer100g = if (totalGrams > 0) totalProteins * 100 / totalGrams else 0f
    val fatsPer100g = if (totalGrams > 0) totalFats * 100 / totalGrams else 0f
    val carbsPer100g = if (totalGrams > 0) totalCarbs * 100 / totalGrams else 0f
    val waterPer100g = if (totalGrams > 0) totalWater * 100 / totalGrams else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = tr("total_per_100g"),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = "${totalGrams.toInt()}g",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right
        )
        Text(
            text = "P:${proteinsPer100g.toInt()}g",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right
        )
        Text(
            text = "F:${fatsPer100g.toInt()}g",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right
        )
        Text(
            text = "C:${carbsPer100g.toInt()}g",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right
        )
        Text(
            text = "W:${waterPer100g.toInt()}g",
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
        Text(tr("macronutrients_per_100g"), style = MaterialTheme.typography.bodyMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = proteins,
                onValueChange = onProteinsChange,
                label = { Text(tr("protein"), style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = onCarbsChange,
                label = { Text(tr("carbs"), style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fats,
                onValueChange = onFatsChange,
                label = { Text(tr("fats"), style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = water,
                onValueChange = onWaterChange,
                label = { Text(tr("water_percentage"), style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

