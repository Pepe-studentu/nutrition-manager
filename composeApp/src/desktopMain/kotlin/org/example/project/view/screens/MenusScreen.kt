package org.example.project.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinprojecttest.composeapp.generated.resources.Res
import kotlinprojecttest.composeapp.generated.resources.add
import kotlinx.coroutines.launch
import org.example.project.model.MultiDayMenu
import org.example.project.model.DailyMenu
import org.example.project.model.Meal
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.view.dialogs.AddMealDialog
import org.example.project.service.MenuPrintService
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenusScreen() {
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuToDelete by remember { mutableStateOf<MultiDayMenu?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    val filteredMenus by remember(searchQuery) {
        derivedStateOf { Model.filterMultiDayMenus(searchQuery) }
    }

    // Global selection state for all menus
    var globalSelectedCell by remember { mutableStateOf<Triple<String, Int, Int>?>(null) } // (menuId, dayIndex, mealIndex)
    var showAddMealDialog by remember { mutableStateOf(false) }
    var addMealContext by remember { mutableStateOf<Triple<String, Int, Int>?>(null) } // (menuId, dayIndex, mealIndex)
    var selectedMeal by remember { mutableStateOf<Meal?>(null) } // For editing
    
    var showMealDeleteDialog by remember { mutableStateOf(false) }
    var mealToDelete by remember { mutableStateOf<Meal?>(null) }
    var deleteContext by remember { mutableStateOf<Triple<String, Int, Int>?>(null) } // (menuId, dayIndex, mealIndex)

    Box(modifier = Modifier.fillMaxSize()) {
        if (showDialog) {
            MultiDayMenuInputDialog(
                onDismiss = { showDialog = false },
                showSnackbar = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    menuToDelete = null
                },
                confirmButton = {
                    Button(onClick = {
                        val success = Model.deleteMultiDayMenu(menuToDelete!!.id)
                        showDeleteDialog = false
                        menuToDelete = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "Menu deleted" else "Failed to delete menu"
                            )
                        }
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDeleteDialog = false
                        menuToDelete = null
                    }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete this menu?") }
            )
        }

        Column {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Menus", style = AccessibilityTypography.bodyLarge) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = AccessibilityTypography.bodyLarge,
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredMenus) { menu ->
                    MultiDayMenuCard(
                        menu = menu,
                        globalSelectedCell = globalSelectedCell,
                        onCellClick = { dayIndex, mealIndex, meal ->
                            if (meal == null) {
                                // Empty cell - show add meal dialog
                                addMealContext = Triple(menu.id, dayIndex, mealIndex)
                                showAddMealDialog = true
                            } else {
                                // Populated cell - show/hide toolbar
                                globalSelectedCell = if (globalSelectedCell == Triple(menu.id, dayIndex, mealIndex)) null
                                else Triple(menu.id, dayIndex, mealIndex)
                            }
                        },
                        onDeleteClick = {
                            menuToDelete = menu
                            showDeleteDialog = true
                        },
                        onPrintClick = {
                            scope.launch {
                                val printService = MenuPrintService()
                                val pdfPath = printService.generateMenuPdf(menu)
                                val message = if (pdfPath != null) {
                                    "PDF generated and opened: ${pdfPath.substringAfterLast("/")}"
                                } else {
                                    "Failed to generate PDF"
                                }
                                snackbarHostState.showSnackbar(message)
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
        if (showAddMealDialog) {
            AddMealDialog(
                onDismiss = { 
                    showAddMealDialog = false
                    addMealContext = null
                    selectedMeal = null
                },
                onAddMeal = { mealName, sizedFoods ->
                    val success = Model.insertMeal(mealName, sizedFoods)
                    if (success && addMealContext != null) {
                        val (menuId, dayIndex, mealIndex) = addMealContext!!
                        val newMeal = Model.meals.lastOrNull()
                        if (newMeal != null) {
                            val menu = filteredMenus.find { it.id == menuId }
                            val dailyMenu = menu?.dailyMenus?.getOrNull(dayIndex)
                            if (dailyMenu != null) {
                                when (mealIndex) {
                                    0 -> Model.updateDailyMenuMeal(dailyMenu.id, "breakfast", newMeal.id)
                                    1 -> Model.updateDailyMenuMeal(dailyMenu.id, "snack1", newMeal.id)
                                    2 -> Model.updateDailyMenuMeal(dailyMenu.id, "lunch", newMeal.id)
                                    3 -> Model.updateDailyMenuMeal(dailyMenu.id, "snack2", newMeal.id)
                                    4 -> Model.updateDailyMenuMeal(dailyMenu.id, "dinner", newMeal.id)
                                }
                            }
                        }
                    }
                },
                onEditMeal = { mealId, mealName, sizedFoods ->
                    Model.updateMeal(mealId, mealName, sizedFoods)
                },
                allFoods = Model.foods,
                meal = selectedMeal
            )
        }
        
        // Global Delete Meal Confirmation Dialog
        if (showMealDeleteDialog && mealToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showMealDeleteDialog = false
                    mealToDelete = null
                    deleteContext = null
                },
                confirmButton = {
                    Button(onClick = {
                        deleteContext?.let { (menuId, dayIndex, mealIndex) ->
                            val menu = filteredMenus.find { it.id == menuId }
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
                                mealToDelete?.let { meal ->
                                    Model.deleteMeal(meal.id)
                                }
                            }
                        }
                        showMealDeleteDialog = false
                        mealToDelete = null
                        deleteContext = null
                        globalSelectedCell = null
                    }) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showMealDeleteDialog = false
                        mealToDelete = null
                        deleteContext = null
                    }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Remove Meal from Menu") },
                text = { Text("Are you sure you want to remove '${mealToDelete!!.description}' from this menu slot?") }
            )
        }

        // Global floating toolbar for selected cell (positioned at screen bottom center)
        globalSelectedCell?.let { (menuId, dayIndex, mealIndex) ->
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
                            val menu = filteredMenus.find { it.id == menuId }
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
                                selectedMeal = meal
                                showAddMealDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Edit")
                    }
                    Button(
                        onClick = { 
                            val menu = filteredMenus.find { it.id == menuId }
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
                                mealToDelete = meal
                                deleteContext = globalSelectedCell
                                showMealDeleteDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Remove")
                    }
                    Button(
                        onClick = { /* TODO: Duplicate meal */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Duplicate")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(painterResource(Res.drawable.add), contentDescription = "Add Menu")
        }
    }
}

@Composable
fun MultiDayMenuCard(
    menu: MultiDayMenu,
    globalSelectedCell: Triple<String, Int, Int>?,
    onCellClick: (Int, Int, Meal?) -> Unit,
    onDeleteClick: () -> Unit,
    onPrintClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier.fillMaxWidth().border(width = 2.dp, color = MaterialTheme.colorScheme.primary)
            .clickable(indication = null, interactionSource = interactionSource) { expanded = !expanded },
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = menu.description,
                        style = AccessibilityTypography.headlineSmall
                    )
                    Text(
                        text = "${menu.days} days",
                        style = AccessibilityTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPrintClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Print")
                    }
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                MenuGrid(
                    menu = menu,
                    globalSelectedCell = globalSelectedCell,
                    onCellClick = onCellClick
                )
            }
        }
    }
}

@Composable
fun MultiDayMenuInputDialog(
    onDismiss: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Multi-Day Menu", style = AccessibilityTypography.headlineSmall) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", style = AccessibilityTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = AccessibilityTypography.bodyLarge
                )

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Number of Days", style = AccessibilityTypography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = AccessibilityTypography.bodyLarge
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val daysInt = days.toIntOrNull()
                    val success = if (daysInt != null && daysInt > 0) {
                        Model.insertMultiDayMenu(description.trim(), daysInt)
                    } else false

                    if (success) {
                        onDismiss()
                        showSnackbar("Menu created successfully")
                    } else {
                        showSnackbar("Failed to create menu. Check inputs.")
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MenuGrid(
    menu: MultiDayMenu,
    globalSelectedCell: Triple<String, Int, Int>?,
    onCellClick: (Int, Int, Meal?) -> Unit
) {

    val mealNames = listOf("Breakfast", "Snack 1", "Lunch", "Snack 2", "Dinner")
    val cellWidth = 200.dp
    val cellHeight = 120.dp

    Box(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // Column headers
            item {
                Column {
                    // Empty cell for day header position
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(cellHeight)
                            .border(1.dp, MaterialTheme.colorScheme.outline)
                    )

                    // Day headers (rows)
                    repeat(menu.days) { dayIndex ->
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(cellHeight)
                                .border(1.dp, MaterialTheme.colorScheme.outline),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Day ${dayIndex + 1}",
                                style = AccessibilityTypography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Meal columns
            items(mealNames.size) { mealIndex ->
                Column {
                    // Meal name header
                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .height(cellHeight)
                            .border(1.dp, MaterialTheme.colorScheme.outline),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mealNames[mealIndex],
                            style = AccessibilityTypography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Meal cells for each day
                    repeat(menu.days) { dayIndex ->
                        val dailyMenu = menu.dailyMenus.getOrNull(dayIndex)
                        val mealId = when (mealIndex) {
                            0 -> dailyMenu?.breakfastId
                            1 -> dailyMenu?.snack1Id
                            2 -> dailyMenu?.lunchId
                            3 -> dailyMenu?.snack2Id
                            4 -> dailyMenu?.dinnerId
                            else -> null
                        }
                        val meal = mealId?.let { Model.getMealById(it) }

                        MealEntryCell(
                            meal = meal,
                            isSelected = globalSelectedCell == Triple(menu.id, dayIndex, mealIndex),
                            onClick = {
                                onCellClick(dayIndex, mealIndex, meal)
                            },
                            modifier = Modifier
                                .width(cellWidth)
                                .height(cellHeight)
                        )
                    }
                }
            }

            // Daily totals column
            item {
                Column {
                    // "Daily Total" header
                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .height(cellHeight)
                            .border(1.dp, MaterialTheme.colorScheme.outline),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Daily Total",
                            style = AccessibilityTypography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Daily total cells
                    repeat(menu.days) { dayIndex ->
                        val dailyMenu = menu.dailyMenus.getOrNull(dayIndex)
                        DailyTotalsCell(
                            dailyMenu = dailyMenu,
                            modifier = Modifier
                                .width(cellWidth)
                                .height(cellHeight)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MealEntryCell(
    meal: Meal?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.outline
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (meal == null) {
            // Empty state
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.add),
                    contentDescription = "Add meal",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add a meal",
                    style = AccessibilityTypography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Populated state
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(meal.foods) { sizedFood ->
                    Text(
                        text = "${sizedFood.grams.toInt()}g ${sizedFood.foodName}",
                        style = AccessibilityTypography.labelMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTotalsCell(
    dailyMenu: DailyMenu?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (dailyMenu == null) {
            Text(
                text = "No data",
                style = AccessibilityTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            val totalCalories = dailyMenu.calories
            val proteinPercent = if (totalCalories > 0) (dailyMenu.proteins * 4) / totalCalories * 100 else 0f
            val fatPercent = if (totalCalories > 0) (dailyMenu.fats * 9) / totalCalories * 100 else 0f
            val carbPercent = if (totalCalories > 0) (dailyMenu.carbs * 4) / totalCalories * 100 else 0f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "P: ${proteinPercent.toInt()}%",
                    style = AccessibilityTypography.labelMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "F: ${fatPercent.toInt()}%",
                    style = AccessibilityTypography.labelMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "G: ${carbPercent.toInt()}%",
                    style = AccessibilityTypography.labelMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Cal: ${totalCalories.toInt()}",
                    style = AccessibilityTypography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}