package org.example.project.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.example.project.model.Food
import org.example.project.model.Meal
import org.example.project.model.Model
import org.example.project.model.SizedFood

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
                Text(
                    text = if (isEdit) "Edit Meal" else "Add Meal",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                var mealName by remember { mutableStateOf(meal?.name ?: "") }
                var searchQuery by remember { mutableStateOf("") }
                val filteredFoods by remember(searchQuery) {
                    derivedStateOf { Model.filterFoods(searchQuery, allFoods) }
                }
                val selectedFoods = remember {
                    mutableStateListOf<SizedFood>().apply {
                        if (meal != null) {
                            addAll(meal.foods)
                        }
                    }
                }

                // Meal name field
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Meal Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Left side: Drawer with search and food suggestions
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search Foods") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        LazyColumn {
                            items(filteredFoods) { food ->
                                Text(
                                    text = food.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedFoods.add(SizedFood(food.name, 100f))
                                        }
                                        .padding(8.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Right side: Tabletop with selected foods and total macros
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .padding(start = 8.dp)
                    ) {
                        // Total macros
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
                        val totalWater by remember(selectedFoods, Model.foods) {
                            derivedStateOf {
                                selectedFoods.sumOf { sized ->
                                    val macros = Model.getFoodMacros(sized.foodName)
                                    macros?.waterMassPercentage?.toDouble()?.times(sized.grams / 100) ?: 0.0
                                }.toFloat()
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total: Cal: %.1f".format(totalCalories), fontSize = 16.sp)
                            Text("Carbs: %.1f".format(totalCarbs), fontSize = 16.sp)
                            Text("Prot: %.1f".format(totalProteins), fontSize = 16.sp)
                            Text("Fats: %.1f".format(totalFats), fontSize = 16.sp)
                            Text("Water: %.1f".format(totalWater), fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Selected foods
                        LazyColumn {
                            items(selectedFoods) { sizedFood ->
                                val macros = Model.getFoodMacros(sizedFood.foodName)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = sizedFood.foodName,
                                            modifier = Modifier.weight(1f),
                                            fontSize = 16.sp
                                        )
                                        OutlinedTextField(
                                            value = sizedFood.grams.toString(),
                                            onValueChange = { newValue ->
                                                val grams = newValue.toFloatOrNull() ?: 0f
                                                val index = selectedFoods.indexOf(sizedFood)
                                                selectedFoods[index] = sizedFood.copy(grams = grams)
                                            },
                                            label = { Text("Grams") },
                                            modifier = Modifier.width(100.dp)
                                        )
                                        if (macros != null) {
                                            val calories = (macros.proteins * 4 + macros.carbs * 4 + macros.fats * 9) * sizedFood.grams / 100
                                            Text("Cal: %.1f".format(calories), fontSize = 16.sp)
                                            Text("C: %.1f".format(macros.carbs * sizedFood.grams / 100), fontSize = 16.sp)
                                            Text("P: %.1f".format(macros.proteins * sizedFood.grams / 100), fontSize = 16.sp)
                                            Text("F: %.1f".format(macros.fats * sizedFood.grams / 100), fontSize = 16.sp)
                                        }
                                        Button(
                                            onClick = { selectedFoods.remove(sizedFood) },
                                            modifier = Modifier.size(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("×", fontSize = 18.sp)
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
                        if (isEdit) {
                            onEditMeal(meal!!.id, mealName, selectedFoods.toList())
                        } else {
                            onAddMeal(mealName, selectedFoods.toList())
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = mealName.isNotBlank() && selectedFoods.isNotEmpty()
                ) {
                    Text(if (isEdit) "Update Meal" else "Add Meal")
                }
            }
        }
    }
}