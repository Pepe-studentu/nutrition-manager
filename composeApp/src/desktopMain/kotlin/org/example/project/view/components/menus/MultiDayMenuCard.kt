package org.example.project.view.components.menus

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.model.Meal
import org.example.project.model.MultiDayMenu
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.service.tr

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
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "${menu.days} ${tr("days")}",
                        style = MaterialTheme.typography.bodyMedium,
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
                        Text(tr("print"))
                    }
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(tr("delete"))
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