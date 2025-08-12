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
import org.example.project.model.Ingredient
import org.example.project.model.Meal
import org.example.project.model.Model
import org.example.project.model.SizedIngredient

@Composable
fun AddMealDialog(
    onDismiss: () -> Unit,
    onAddMeal: (String, List<SizedIngredient>) -> Unit,
    onEditMeal: (String, String, List<SizedIngredient>) -> Unit,
    allIngredients: List<Ingredient>,
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
                val filteredIngredients by remember(searchQuery) {
                    derivedStateOf { Model.filterIngredients(searchQuery, allIngredients) }
                }
                val selectedIngredients = remember {
                    mutableStateListOf<SizedIngredient>().apply {
                        if (meal != null) {
                            addAll(meal.ingredients)
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
                    // Left side: Drawer with search and ingredient suggestions
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search Ingredients") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        LazyColumn {
                            items(filteredIngredients) { ingredient ->
                                Text(
                                    text = ingredient.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedIngredients.add(SizedIngredient(ingredient.id, 100f))
                                        }
                                        .padding(8.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Right side: Tabletop with selected ingredients and total macros
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .padding(start = 8.dp)
                    ) {
                        // Total macros
                        val totalCalories by remember(selectedIngredients, Model.ingredients) {
                            derivedStateOf {
                                selectedIngredients.sumOf { sized ->
                                    Model.getIngredientById(sized.ingredientId)?.calories?.toDouble()?.times(sized.grams / 100) ?: 0.0
                                }.toFloat()
                            }
                        }
                        val totalCarbs by remember(selectedIngredients, Model.ingredients) {
                            derivedStateOf {
                                selectedIngredients.sumOf { sized ->
                                    Model.getIngredientById(sized.ingredientId)?.carbs?.toDouble()?.times(sized.grams / 100) ?: 0.0
                                }.toFloat()
                            }
                        }
                        val totalProteins by remember(selectedIngredients, Model.ingredients) {
                            derivedStateOf {
                                selectedIngredients.sumOf { sized ->
                                    Model.getIngredientById(sized.ingredientId)?.proteins?.toDouble()?.times(sized.grams / 100) ?: 0.0
                                }.toFloat()
                            }
                        }
                        val totalFats by remember(selectedIngredients, Model.ingredients) {
                            derivedStateOf {
                                selectedIngredients.sumOf { sized ->
                                    Model.getIngredientById(sized.ingredientId)?.fats?.toDouble()?.times(sized.grams / 100) ?: 0.0
                                }.toFloat()
                            }
                        }
                        val totalWater by remember(selectedIngredients, Model.ingredients) {
                            derivedStateOf {
                                selectedIngredients.sumOf { sized ->
                                    Model.getIngredientById(sized.ingredientId)?.waterMassPercentage?.toDouble()?.times(sized.grams / 100) ?: 0.0
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

                        // Selected ingredients
                        LazyColumn {
                            items(selectedIngredients) { sizedIngredient ->
                                val ingredient = Model.getIngredientById(sizedIngredient.ingredientId)
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
                                            text = ingredient?.name ?: "Unknown Ingredient",
                                            modifier = Modifier.weight(1f),
                                            fontSize = 16.sp
                                        )
                                        OutlinedTextField(
                                            value = sizedIngredient.grams.toString(),
                                            onValueChange = { newValue ->
                                                val grams = newValue.toFloatOrNull() ?: 0f
                                                val index = selectedIngredients.indexOf(sizedIngredient)
                                                selectedIngredients[index] = sizedIngredient.copy(grams = grams)
                                            },
                                            label = { Text("Grams") },
                                            modifier = Modifier.width(100.dp)
                                        )
                                        Text(
                                            text = "Cal: %.1f".format(ingredient?.calories?.times(sizedIngredient.grams / 100) ?: 0f),
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "C: %.1f".format(ingredient?.carbs?.times(sizedIngredient.grams / 100) ?: 0f),
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "P: %.1f".format(ingredient?.proteins?.times(sizedIngredient.grams / 100) ?: 0f),
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "F: %.1f".format(ingredient?.fats?.times(sizedIngredient.grams / 100) ?: 0f),
                                            fontSize = 16.sp
                                        )
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
                            onEditMeal(meal!!.id, mealName, selectedIngredients.toList())
                        } else {
                            onAddMeal(mealName, selectedIngredients.toList())
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = mealName.isNotBlank() && selectedIngredients.isNotEmpty()
                ) {
                    Text(if (isEdit) "Update Meal" else "Add Meal")
                }
            }
        }
    }
}