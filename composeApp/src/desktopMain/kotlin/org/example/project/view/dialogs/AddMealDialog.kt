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
import org.example.project.model.Meal
import org.example.project.model.Model
import org.example.project.model.SizedFood
import org.example.project.service.tr
import org.example.project.service.TranslationService

@Composable
fun AddMealDialog(
    onDismiss: () -> Unit,
    onAddMeal: (String, List<SizedFood>) -> Unit,
    onEditMeal: (String, String, List<SizedFood>) -> Unit,
    allFoods: List<Food>,
    meal: Meal? = null
) {
    val isEdit = meal != null
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
                var mealDescription by remember { mutableStateOf(meal?.description ?: "") }
                var searchQuery by remember { mutableStateOf("") }
                val filteredFoods by remember(searchQuery) {
                    derivedStateOf { 
                        val frequencySortedFoods = allFoods.sortedByDescending { it.usageCount }
                        Model.filterFoods(searchQuery, frequencySortedFoods) 
                    }
                }
                val selectedFoods = remember {
                    mutableStateListOf<SizedFood>().apply {
                        if (meal != null) {
                            addAll(meal.foods)
                        }
                    }
                }

                // Title and description in Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) tr("edit_meal") else tr("add_meal_to_slot"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextField(
                        value = mealDescription,
                        onValueChange = { mealDescription = it },
                        placeholder = { Text(tr("description")) },
                        modifier = Modifier.width(200.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Left side: Search and food suggestions
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text(tr("search_foods")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        LazyColumn {
                            items(filteredFoods) { food ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedFoods.add(SizedFood(food.name, 100f))
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

                    // Right side: Tabletop with selected foods and total macros
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(2f)
                            .padding(start = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.medium
                            )
                            .padding(12.dp)

                    ) {
                        Column {
                            // Calculate totals
                            val totalGrams by remember(selectedFoods) {
                                derivedStateOf { selectedFoods.sumOf { it.grams.toDouble() }.toFloat() }
                            }
                            val totalCalories by remember(selectedFoods, Model.foods) {
                                derivedStateOf {
                                    selectedFoods.sumOf { sized ->
                                        val macros = Model.getFoodMacros(sized.foodName)
                                        if (macros != null) {
                                            val calories = macros.proteins * 4 + macros.carbs * 4 + macros.fats * 9
                                            calories.toDouble() * sized.grams / 100
                                        } else 0.0
                                    }.toFloat()
                                }
                            }
                            val totalCarbs by remember(selectedFoods, Model.foods) {
                                derivedStateOf {
                                    selectedFoods.sumOf { sized ->
                                        val macros = Model.getFoodMacros(sized.foodName)
                                        macros?.carbs?.toDouble()?.times(sized.grams / 100) ?: 0.0
                                    }.toFloat()
                                }
                            }
                            val totalProteins by remember(selectedFoods, Model.foods) {
                                derivedStateOf {
                                    selectedFoods.sumOf { sized ->
                                        val macros = Model.getFoodMacros(sized.foodName)
                                        macros?.proteins?.toDouble()?.times(sized.grams / 100) ?: 0.0
                                    }.toFloat()
                                }
                            }
                            val totalFats by remember(selectedFoods, Model.foods) {
                                derivedStateOf {
                                    selectedFoods.sumOf { sized ->
                                        val macros = Model.getFoodMacros(sized.foodName)
                                        macros?.fats?.toDouble()?.times(sized.grams / 100) ?: 0.0
                                    }.toFloat()
                                }
                            }

                            // Header row with proper alignment
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tr("total"),
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
                                    text = "P:${totalProteins.toInt()}g",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Right
                                )
                                Text(
                                    text = "F:${totalFats.toInt()}g",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Right
                                )
                                Text(
                                    text = "C:${totalCarbs.toInt()}g",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Right
                                )
                                Text(
                                    text = "Cal:${totalCalories.toInt()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Right
                                )
                                Spacer(Modifier.weight(0.5f)) // ??
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 2.dp,
                                color = MaterialTheme.colorScheme.outline
                            )

                            // Selected foods entries
                            LazyColumn {
                                items(selectedFoods) { sizedFood ->
                                    val macros = Model.getFoodMacros(sizedFood.foodName)
                                    
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = sizedFood.foodName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(2f)
                                            )
                                            TextField(
                                                value = sizedFood.grams.toInt().toString(),
                                                onValueChange = { newValue ->
                                                    val grams = newValue.toFloatOrNull() ?: 0f
                                                    val index = selectedFoods.indexOf(sizedFood)
                                                    selectedFoods[index] = sizedFood.copy(grams = grams)
                                                },
                                                textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.End),                                                modifier = Modifier.weight(1f),
                                                suffix = { Text(tr("grams"), style = MaterialTheme.typography.bodyMedium)},
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent
                                                )
                                            )
                                            if (macros != null) {
                                                val proteins = macros.proteins * sizedFood.grams / 100
                                                val fats = macros.fats * sizedFood.grams / 100
                                                val carbs = macros.carbs * sizedFood.grams / 100
                                                val calories = (macros.proteins * 4 + macros.carbs * 4 + macros.fats * 9) * sizedFood.grams / 100
                                                Text(
                                                    text = "${proteins.toInt()}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                                Text(
                                                    text = "${fats.toInt()}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                                Text(
                                                    text = "${carbs.toInt()}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                                Text(
                                                    text = "${calories.toInt()}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Right
                                                )
                                            }
                                            Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                                                Button(
                                                    onClick = { selectedFoods.remove(sizedFood) },
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
                                        
                                        if (selectedFoods.indexOf(sizedFood) < selectedFoods.size - 1) {
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add/Update button
                Button(
                    onClick = {
                        val finalMealDescription = mealDescription.ifBlank { TranslationService.getString("none") }
                        if (isEdit) {
                            onEditMeal(meal!!.id, finalMealDescription, selectedFoods.toList())
                        } else {
                            onAddMeal(finalMealDescription, selectedFoods.toList())
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = selectedFoods.isNotEmpty()
                ) {
                    Text(if (isEdit) tr("update_meal") else tr("add_meal"))
                }
            }
        }
    }
}