package view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinprojecttest.composeapp.generated.resources.Res
import kotlinprojecttest.composeapp.generated.resources.add
import kotlinx.coroutines.launch
import org.example.project.model.Ingredient
import org.example.project.model.Model
import org.example.project.view.theme.AccessibilityTypography
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(
){
    // this is not a stateless composable
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember {mutableStateOf(false)}
    var ingredientToDelete by remember { mutableStateOf<Ingredient?>(null) }

    var selectedIngredient by remember {mutableStateOf<Ingredient?>(null)}

    var searchQuery by remember { mutableStateOf("") }
    val filteredIngredients by remember(searchQuery) {
        derivedStateOf { Model.filterIngredients(searchQuery) }
    }

    // will have: the ingredients row, the floating action button, the snack bar host
    // inside a Box
    Box(modifier = Modifier.fillMaxSize()){
        if (showDialog) {
            IngredientInputDialog(
                onDismiss = { showDialog = false },
                showSnackbar = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                },
                ingredient = selectedIngredient
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    ingredientToDelete = null
                },
                confirmButton = {
                    Button(onClick = {
                        val success = Model.deleteIngredient(ingredientToDelete!!.id)
                        showDeleteDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "Ingredient deleted"
                                else "Cannot delete: Ingredient used in a meal"
                            )
                        }
                    }){
                        Text("Delete")
                    }
                },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete ${ingredientToDelete?.name}?") },
            )
        }

        Column{
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Ingredients", style = AccessibilityTypography.bodyLarge) }, // Applied typography
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = AccessibilityTypography.bodyLarge, // Applied typography
                singleLine = true // Ensures it's a single line input
            )
            IngredientTable(
                filteredIngredients,
                onEditClick = {
                    clickedIngredient ->
                        selectedIngredient = clickedIngredient
                        showDialog = true
                },
                onDeleteClick = {
                        clickedIngredient ->
                    ingredientToDelete = clickedIngredient
                    showDeleteDialog = true
                }
            )
        }

        // might need resizing
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            // move it above the dialogs later
            SnackbarHost(hostState = snackbarHostState)
        }

        // FAB
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(painterResource(Res.drawable.add), contentDescription = "Add")
        }
    }
}