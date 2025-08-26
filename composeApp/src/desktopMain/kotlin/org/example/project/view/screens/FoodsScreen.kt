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
import org.example.project.view.components.SortState
import org.example.project.view.dialogs.FoodInputDialog
import org.example.project.view.theme.AccessibilityTypography
import org.jetbrains.compose.resources.painterResource

data class FoodsViewState(
    val foods: List<Food> = emptyList(),
    val searchQuery: String = "",
    val activeSortColumn: String? = null,
    val sortStates: Map<String, SortState> = emptyMap(),
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
        sortStates: Map<String, SortState> = viewState.sortStates
    ) {
        val currentSortState = sortStates[sortColumn] ?: SortState.NONE
        val filtered = Model.filterFoods(searchQuery)
        val sorted = if (sortColumn != null && currentSortState != SortState.NONE) {
            Model.sortFoods(
                foods = filtered,
                column = sortColumn,
                ascending = currentSortState == SortState.ASCENDING
            )
        } else {
            Model.sortFoods(foods = filtered) // Default usage sort
        }
        
        viewState = viewState.copy(
            foods = sorted,
            searchQuery = searchQuery,
            activeSortColumn = sortColumn,
            sortStates = sortStates
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
                                if (success) "Food deleted"
                                else "Cannot delete: Food used elsewhere"
                            )
                        }
                    }) {
                        Text("Delete")
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
                        Text("Cancel")
                    }
                },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete ${viewState.foodToDelete!!.name}?") }
            )
        }

        Column {
            OutlinedTextField(
                value = viewState.searchQuery,
                onValueChange = { newQuery ->
                    updateFoods(searchQuery = newQuery)
                },
                label = { Text("Search Foods", style = AccessibilityTypography.bodyLarge) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = AccessibilityTypography.bodyLarge,
                singleLine = true
            )

            FoodTable(
                foods = viewState.foods,
                activeSortColumn = viewState.activeSortColumn,
                sortStates = viewState.sortStates,
                onHeaderClick = { columnName ->
                    val currentState = viewState.sortStates[columnName] ?: SortState.NONE
                    val nextState = when (currentState) {
                        SortState.NONE -> SortState.ASCENDING
                        SortState.ASCENDING -> SortState.DESCENDING
                        SortState.DESCENDING -> SortState.NONE
                    }
                    
                    val newSortStates = viewState.sortStates.toMutableMap()
                    newSortStates[columnName] = nextState
                    
                    val newActiveSortColumn = if (nextState == SortState.NONE) null else columnName
                    
                    updateFoods(
                        sortColumn = newActiveSortColumn,
                        sortStates = newSortStates
                    )
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
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(painterResource(Res.drawable.add), contentDescription = "Add Food")
        }
    }
}

