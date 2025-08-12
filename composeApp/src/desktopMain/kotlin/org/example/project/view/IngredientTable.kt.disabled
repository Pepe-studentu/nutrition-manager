package view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import org.example.project.model.Ingredient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.view.theme.Black

@Composable
fun IngredientsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background) 
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        // Each Text composable represents a column header.
        // The 'weight' modifier ensures that columns align with the data rows.
        // Font weight is set to bold for emphasis.
        Text(
            text = "Ingredient",
            modifier = Modifier.weight(2.5f), // Give more space to ingredient name
            style = AccessibilityTypography.headlineLarge,
            color = Black
        )
        Text(
            text = "Protein",
            modifier = Modifier.weight(1f),
            style= AccessibilityTypography.headlineLarge,
            color = Black
        )
        Text(
            text = "Fat",
            modifier = Modifier.weight(1f),
            style= AccessibilityTypography.headlineLarge,
            color = Black
        )

        Text(
            text = "Carbs",
            modifier = Modifier.weight(1f),
            style= AccessibilityTypography.headlineLarge,
            color = Black
        )

        Text(
            text = "Water %",
            modifier = Modifier.weight(1f),
            style= AccessibilityTypography.headlineLarge,
            color = Black
        )
        Text(
            text = "Calories",
            modifier = Modifier.weight(1f),
            style= AccessibilityTypography.headlineLarge,
            color = Black
        )

    }
    // A divider to visually separate the header from the data rows.
    HorizontalDivider(thickness = 4.dp, color = MaterialTheme.colorScheme.onBackground)
}

// 3. Composable for a single Ingredient Row
// This composable represents one row of data in the table.
// It takes an 'Ingredient' object and displays its properties in a horizontal Row layout.
// Similar to the header, 'weight' modifiers are used for column alignment.
@Composable
fun IngredientRow(ingredient: Ingredient,
                  onDeleteClick: (Ingredient)-> Unit,
                  onEditClick: (Ingredient) -> Unit)
{
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Display each property of the Ingredient.
            // The 'weight' modifiers match those in the TableHeader for consistent column widths.
            Text(
                text = ingredient.name,
                modifier = Modifier.weight(2.5f),
                style= AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = ingredient.proteins.toString(),
                modifier = Modifier.weight(1f),
                style= AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = ingredient.fats.toString(),
                modifier = Modifier.weight(1f),
                style= AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = ingredient.carbs.toString(),
                modifier = Modifier.weight(1f),
                style= AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = ingredient.waterMassPercentage.toString(),
                modifier = Modifier.weight(1f),
                style= AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = ingredient.calories.toString(),
                modifier = Modifier.weight(1f),
                style= AccessibilityTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(onClick = { onEditClick(ingredient) }, modifier = Modifier.padding(end=24.dp)) {
                    Text("Edit")
                }
                Button(onClick = { onDeleteClick(ingredient) }) {
                    Text("Delete")
                }
            }
        }
    }

    // A divider to visually separate individual data rows.
    HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f))
}

// 4. Main Composable for the Ingredient Table
// This composable combines the TableHeader and multiple IngredientRow composables
// to form the complete table.
// It uses LazyColumn for efficient rendering of a potentially large list of ingredients,
// as it only renders the items currently visible on screen.
// will change the usage soon
@Composable
fun IngredientTable(ingredients: List<Ingredient>, onDeleteClick: (Ingredient) -> Unit, onEditClick: (Ingredient) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Overall padding for the table
        ) {
            IngredientsHeader()
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ingredients) { ingredient ->
                    // For each ingredient in the list, create an IngredientRow.
                    IngredientRow(ingredient = ingredient, onEditClick = onEditClick, onDeleteClick = onDeleteClick)
                }
            }
        }
    }
}