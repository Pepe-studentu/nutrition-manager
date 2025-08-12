package org.example.project.view

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
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDayMenusScreen() {
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuToDelete by remember { mutableStateOf<MultiDayMenu?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    val filteredMenus by remember(searchQuery) {
        derivedStateOf { Model.filterMultiDayMenus(searchQuery) }
    }

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
                        onDeleteClick = {
                            menuToDelete = menu
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            SnackbarHost(hostState = snackbarHostState)
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
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        text = "${menu.days} days - ${menu.averageCalories.toInt()} kcal avg",
                        style = AccessibilityTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    Button(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Collapse" else "Expand")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
                Text(
                    text = "Menu details would go here - grid of days × meals",
                    style = AccessibilityTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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