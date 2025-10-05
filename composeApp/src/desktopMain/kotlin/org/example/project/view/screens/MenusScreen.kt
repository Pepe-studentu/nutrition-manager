package org.example.project.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinprojecttest.composeapp.generated.resources.Res
import kotlinprojecttest.composeapp.generated.resources.add
import kotlinx.coroutines.launch
import org.example.project.model.MultiDayMenu
import org.example.project.model.Meal
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.view.dialogs.AddMealDialog
import org.example.project.view.dialogs.PrintDialog
import org.example.project.view.components.menus.MultiDayMenuInputDialog
import org.example.project.view.components.menus.MultiDayMenuCard
import org.example.project.service.tr
import org.example.project.service.TranslationService
import org.jetbrains.compose.resources.painterResource

data class MenusViewState(
    val menus: List<MultiDayMenu> = emptyList(),
    val searchQuery: String = "",
    val selectedCell: Triple<String, Int, Int>? = null, // (menuId, dayIndex, mealIndex)
    val selectedMeal: Meal? = null,
    val showAddMealDialog: Boolean = false,
    val showDeleteMealDialog: Boolean = false,
    val showDeleteMenuDialog: Boolean = false,
    val showCreateMenuDialog: Boolean = false,
    val showPrintDialog: Boolean = false,
    val menuToPrint: MultiDayMenu? = null,
    val menuToDelete: MultiDayMenu? = null,
    val mealToDelete: Meal? = null,
    val addMealContext: Triple<String, Int, Int>? = null,
    val deleteContext: Triple<String, Int, Int>? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenusScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Single source of truth for menus state
    var viewState by remember { 
        mutableStateOf(MenusViewState(menus = Model.filterMultiDayMenus(""))) 
    }

    // Update menus when search changes
    fun updateMenus(searchQuery: String = viewState.searchQuery) {
        val filtered = Model.filterMultiDayMenus(searchQuery)
        viewState = viewState.copy(
            menus = filtered,
            searchQuery = searchQuery
        )
    }
    
    // Update view state helper
    fun updateViewState(update: MenusViewState.() -> MenusViewState) {
        viewState = viewState.update()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewState.showCreateMenuDialog) {
            MultiDayMenuInputDialog(
                onDismiss = { 
                    updateViewState { copy(showCreateMenuDialog = false) }
                    // Refresh menu list after creating new menu
                    updateMenus()
                },
                showSnackbar = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            )
        }

        if (viewState.showDeleteMenuDialog && viewState.menuToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    updateViewState { copy(showDeleteMenuDialog = false, menuToDelete = null) }
                },
                confirmButton = {
                    Button(onClick = {
                        val success = Model.deleteMultiDayMenu(viewState.menuToDelete!!.id)
                        updateViewState { copy(showDeleteMenuDialog = false, menuToDelete = null) }
                        if (success) {
                            updateMenus() // Refresh list after deletion
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) TranslationService.getString("menu_deleted") else TranslationService.getString("failed_to_delete_menu")
                            )
                        }
                    }) {
                        Text(tr("delete"))
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        updateViewState { copy(showDeleteMenuDialog = false, menuToDelete = null) }
                    }) {
                        Text(tr("cancel"))
                    }
                },
                title = { Text(tr("confirm_deletion")) },
                text = { Text(tr("confirm_delete_menu")) }
            )
        }

        if (viewState.showPrintDialog && viewState.menuToPrint != null) {
            PrintDialog(
                menu = viewState.menuToPrint!!,
                onDismiss = {
                    updateViewState { copy(showPrintDialog = false, menuToPrint = null) }
                },
                showSnackbar = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            )
        }

        Column {
            OutlinedTextField(
                value = viewState.searchQuery,
                onValueChange = { newQuery ->
                    updateMenus(searchQuery = newQuery)
                },
                label = { Text(tr("search_menus"), style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewState.menus) { menu ->
                    MultiDayMenuCard(
                        menu = menu,
                        globalSelectedCell = viewState.selectedCell,
                        onCellClick = { dayIndex, mealIndex, meal ->
                            if (meal == null) {
                                // Empty cell - show add meal dialog
                                updateViewState { 
                                    copy(
                                        addMealContext = Triple(menu.id, dayIndex, mealIndex),
                                        showAddMealDialog = true
                                    )
                                }
                            } else {
                                // Populated cell - show/hide toolbar
                                val newSelectedCell = if (viewState.selectedCell == Triple(menu.id, dayIndex, mealIndex)) null
                                else Triple(menu.id, dayIndex, mealIndex)
                                updateViewState { copy(selectedCell = newSelectedCell) }
                            }
                        },
                        onDeleteClick = {
                            updateViewState { 
                                copy(
                                    menuToDelete = menu,
                                    showDeleteMenuDialog = true
                                )
                            }
                        },
                        onPrintClick = {
                            updateViewState {
                                copy(
                                    showPrintDialog = true,
                                    menuToPrint = menu
                                )
                            }
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            SnackbarHost(hostState = snackbarHostState)
        }

        // Global Add/Edit Meal Dialog
        if (viewState.showAddMealDialog) {
            AddMealDialog(
                onDismiss = { 
                    updateViewState { 
                        copy(
                            showAddMealDialog = false,
                            addMealContext = null,
                            selectedMeal = null
                        )
                    }
                },
                onAddMeal = { mealName, sizedFoods ->
                    val success = Model.insertMeal(mealName, sizedFoods)
                    if (success && viewState.addMealContext != null) {
                        val (menuId, dayIndex, mealIndex) = viewState.addMealContext!!
                        val newMeal = Model.meals.lastOrNull()
                        if (newMeal != null) {
                            val menu = viewState.menus.find { it.id == menuId }
                            val dailyMenu = menu?.dailyMenus?.getOrNull(dayIndex)
                            if (dailyMenu != null) {
                                when (mealIndex) {
                                    0 -> Model.updateDailyMenuMeal(dailyMenu.id, "breakfast", newMeal.id)
                                    1 -> Model.updateDailyMenuMeal(dailyMenu.id, "snack1", newMeal.id)
                                    2 -> Model.updateDailyMenuMeal(dailyMenu.id, "lunch", newMeal.id)
                                    3 -> Model.updateDailyMenuMeal(dailyMenu.id, "snack2", newMeal.id)
                                    4 -> Model.updateDailyMenuMeal(dailyMenu.id, "dinner", newMeal.id)
                                }
                                updateMenus() // Refresh menus to show new meal
                            }
                        }
                    }
                },
                onEditMeal = { mealId, mealName, sizedFoods ->
                    Model.updateMeal(mealId, mealName, sizedFoods)
                    updateMenus() // Refresh menus to show updated meal
                },
                allFoods = Model.foods,
                meal = viewState.selectedMeal
            )
        }
        
        // Global Delete Meal Confirmation Dialog
        if (viewState.showDeleteMealDialog && viewState.mealToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    updateViewState { 
                        copy(
                            showDeleteMealDialog = false,
                            mealToDelete = null,
                            deleteContext = null
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewState.deleteContext?.let { (menuId, dayIndex, mealIndex) ->
                            val menu = viewState.menus.find { it.id == menuId }
                            val dailyMenu = menu?.dailyMenus?.getOrNull(dayIndex)
                            if (dailyMenu != null) {
                                when (mealIndex) {
                                    0 -> Model.updateDailyMenuMeal(dailyMenu.id, "breakfast", "")
                                    1 -> Model.updateDailyMenuMeal(dailyMenu.id, "snack1", "")
                                    2 -> Model.updateDailyMenuMeal(dailyMenu.id, "lunch", "")
                                    3 -> Model.updateDailyMenuMeal(dailyMenu.id, "snack2", "")
                                    4 -> Model.updateDailyMenuMeal(dailyMenu.id, "dinner", "")
                                }
                                // Delete the orphaned meal after removing it from the menu
                                viewState.mealToDelete?.let { meal ->
                                    Model.deleteMeal(meal.id)
                                }
                                updateMenus() // Refresh menus
                            }
                        }
                        updateViewState { 
                            copy(
                                showDeleteMealDialog = false,
                                mealToDelete = null,
                                deleteContext = null,
                                selectedCell = null
                            )
                        }
                    }) {
                        Text(tr("remove"))
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        updateViewState { 
                            copy(
                                showDeleteMealDialog = false,
                                mealToDelete = null,
                                deleteContext = null
                            )
                        }
                    }) {
                        Text(tr("cancel"))
                    }
                },
                title = { Text(tr("remove_meal_from_menu")) },
                text = { Text(tr("confirm_remove_meal")) }
            )
        }

        // Global floating toolbar for selected cell (positioned at screen bottom center)
        viewState.selectedCell?.let { (menuId, dayIndex, mealIndex) ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            val menu = viewState.menus.find { it.id == menuId }
                            val dailyMenu = menu?.dailyMenus?.getOrNull(dayIndex)
                            val mealId = when (mealIndex) {
                                0 -> dailyMenu?.breakfastId
                                1 -> dailyMenu?.snack1Id
                                2 -> dailyMenu?.lunchId
                                3 -> dailyMenu?.snack2Id
                                4 -> dailyMenu?.dinnerId
                                else -> null
                            }
                            val meal = mealId?.let { Model.getMealById(it) }
                            if (meal != null) {
                                updateViewState { 
                                    copy(
                                        selectedMeal = meal,
                                        showAddMealDialog = true
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(tr("edit"))
                    }
                    Button(
                        onClick = { 
                            val menu = viewState.menus.find { it.id == menuId }
                            val dailyMenu = menu?.dailyMenus?.getOrNull(dayIndex)
                            val mealId = when (mealIndex) {
                                0 -> dailyMenu?.breakfastId
                                1 -> dailyMenu?.snack1Id
                                2 -> dailyMenu?.lunchId
                                3 -> dailyMenu?.snack2Id
                                4 -> dailyMenu?.dinnerId
                                else -> null
                            }
                            val meal = mealId?.let { Model.getMealById(it) }
                            if (meal != null) {
                                updateViewState { 
                                    copy(
                                        mealToDelete = meal,
                                        deleteContext = viewState.selectedCell,
                                        showDeleteMealDialog = true
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(tr("remove"))
                    }
                    Button(
                        onClick = { /* TODO: Duplicate meal */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(tr("duplicate"))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                updateViewState { copy(showCreateMenuDialog = true) }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(112.dp)
        ) {
            Icon(
                painterResource(Res.drawable.add),
                contentDescription = tr("add_menu_button"),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}