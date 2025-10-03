package org.example.project.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinprojecttest.composeapp.generated.resources.Res
import kotlinprojecttest.composeapp.generated.resources.add
import kotlinx.coroutines.launch
import org.example.project.model.Food
import org.example.project.model.Model
import org.example.project.view.components.FoodTable
import org.example.project.view.dialogs.FoodInputDialog
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.service.tr
import org.example.project.service.TranslationService
import org.jetbrains.compose.resources.painterResource

data class FoodsViewState(
    val foods: List<Food> = emptyList(),
    val searchQuery: String = "",
    val activeSortColumn: String? = null,
    val sortAscending: Map<String, Boolean> = emptyMap(),
    val showInputDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedFood: Food? = null,
    val foodToDelete: Food? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Single source of truth for foods state
    var viewState by remember { 
        mutableStateOf(FoodsViewState(foods = Model.filterFoods(""))) 
    }

    // Update foods when search or sort changes
    fun updateFoods(
        searchQuery: String = viewState.searchQuery,
        sortColumn: String? = viewState.activeSortColumn,
        sortAscending: Map<String, Boolean> = viewState.sortAscending
    ) {
        val filtered = Model.filterFoods(searchQuery)
        val sorted = if (searchQuery.isBlank() && sortColumn != null) {
            // Only allow column sorting when not searching
            val ascending = sortAscending[sortColumn] ?: true
            Model.sortFoods(
                foods = filtered,
                column = sortColumn,
                ascending = ascending
            )
        } else if (searchQuery.isBlank()) {
            // Default alphabetical sort when no search and no column sort
            filtered.sortedBy { it.name }
        } else {
            // Use relevance sorting when searching (already sorted by Model.filterFoods)
            filtered
        }
        
        viewState = viewState.copy(
            foods = sorted,
            searchQuery = searchQuery,
            activeSortColumn = if (searchQuery.isNotBlank()) null else sortColumn,
            sortAscending = sortAscending
        )
    }
    
    // Update view state helper
    fun updateViewState(update: FoodsViewState.() -> FoodsViewState) {
        viewState = viewState.update()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewState.showInputDialog) {
            FoodInputDialog(
                onDismiss = { 
                    updateViewState { 
                        copy(
                            showInputDialog = false,
                            selectedFood = null
                        )
                    }
                    // Refresh the food list after dialog closes (food may have been added/edited)
                    updateFoods()
                },
                showSnackbar = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                },
                food = viewState.selectedFood
            )
        }

        if (viewState.showDeleteDialog && viewState.foodToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    updateViewState { 
                        copy(
                            showDeleteDialog = false,
                            foodToDelete = null
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val success = Model.deleteFood(viewState.foodToDelete!!.name)
                        updateViewState { 
                            copy(
                                showDeleteDialog = false,
                                foodToDelete = null
                            )
                        }
                        if (success) {
                            // Refresh the food list after deletion
                            updateFoods()
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) TranslationService.getString("food_deleted")
                                else TranslationService.getString("cannot_delete_food_in_use")
                            )
                        }
                    }) {
                        Text(tr("delete"))
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        updateViewState { 
                            copy(
                                showDeleteDialog = false,
                                foodToDelete = null
                            )
                        }
                    }) {
                        Text(tr("cancel"))
                    }
                },
                title = { Text(tr("delete")) },
                text = { Text(tr("confirm_delete_food")) }
            )
        }

        Column {
            OutlinedTextField(
                value = viewState.searchQuery,
                onValueChange = { newQuery ->
                    updateFoods(searchQuery = newQuery)
                },
                label = { Text(tr("search_foods"), style = AccessibilityTypography.bodyLarge) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = AccessibilityTypography.bodyLarge,
                singleLine = true
            )

            // Results count display (only visible when searching)
            if (viewState.searchQuery.isNotBlank()) {
                val exactMatches = viewState.foods.count { food ->
                    food.name.lowercase().startsWith(viewState.searchQuery.lowercase())
                }
                if (exactMatches > 0) {
                    Text(
                        text = tr("results_found", exactMatches),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = AccessibilityTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FoodTable(
                foods = viewState.foods,
                activeSortColumn = viewState.activeSortColumn,
                sortAscending = viewState.sortAscending,
                searchActive = viewState.searchQuery.isNotBlank(),
                onHeaderClick = { columnName ->
                    // Only handle clicks when not searching
                    if (viewState.searchQuery.isBlank()) {
                        val currentAscending = viewState.sortAscending[columnName] ?: true
                        val newAscending = !currentAscending
                        
                        val newSortAscending = viewState.sortAscending.toMutableMap()
                        newSortAscending[columnName] = newAscending
                        
                        updateFoods(
                            sortColumn = columnName,
                            sortAscending = newSortAscending
                        )
                    }
                },
                onEditClick = { food ->
                    updateViewState { 
                        copy(
                            selectedFood = food,
                            showInputDialog = true
                        )
                    }
                },
                onDeleteClick = { food ->
                    updateViewState { 
                        copy(
                            foodToDelete = food,
                            showDeleteDialog = true
                        )
                    }
                }
            )
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            SnackbarHost(hostState = snackbarHostState)
        }

        FloatingActionButton(
            onClick = {
                updateViewState { copy(showInputDialog = true) }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(112.dp)
        ) {
            Icon(
                painterResource(Res.drawable.add),
                contentDescription = tr("add_food_button"),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

