package org.example.project.view.components.menus

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinprojecttest.composeapp.generated.resources.Res
import kotlinprojecttest.composeapp.generated.resources.add
import org.example.project.model.DailyMenu
import org.example.project.model.Meal
import org.example.project.model.Model
import org.example.project.model.MultiDayMenu
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.service.tr
import org.jetbrains.compose.resources.painterResource

@Composable
fun MenuGrid(
    menu: MultiDayMenu,
    globalSelectedCell: Triple<String, Int, Int>?,
    onCellClick: (Int, Int, Meal?) -> Unit
) {
    val mealNames = listOf(tr("breakfast"), tr("snack_1"), tr("lunch"), tr("snack_2"), tr("dinner"))
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
                                text = tr("day_number", dayIndex + 1),
                                style = MaterialTheme.typography.bodyMedium,
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
                            style = MaterialTheme.typography.bodyMedium,
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
                            text = tr("daily_total"),
                            style = MaterialTheme.typography.bodyMedium,
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
                    contentDescription = tr("add_meal"),
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tr("add_a_meal"),
                    style = MaterialTheme.typography.bodyMedium,
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
                        style = MaterialTheme.typography.labelMedium,
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
                text = tr("no_data"),
                style = MaterialTheme.typography.bodyMedium,
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
                    text = tr("protein")+": ${proteinPercent.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = tr("fat")+": ${fatPercent.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = tr("carbs")+": ${carbPercent.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = tr("calories")+": ${totalCalories.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}