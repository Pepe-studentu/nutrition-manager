package org.example.project.view.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import org.example.project.model.Screen
import org.example.project.view.theme.AccessibilityShapes
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyNavBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(108.dp) // Wide enough for large icons
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.primary) // Distinct background
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        Screen.entries.forEach { screen ->
            var isClicked by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isClicked) 1.2f else 1f,
                label = "Icon Scale Animation"
            )

            Box(
                modifier = Modifier
                    .size(72.dp) // Large icon size
                    .clip(AccessibilityShapes.medium)
                    .clickable {
                        isClicked = true
                        onScreenSelected(screen)
                    }
                    .background(
                        color = if (currentScreen == screen) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )

                    .scale(scale)
            ) {
                Icon(
                    painter = painterResource(
                        if (currentScreen == screen) screen.filledIcon else screen.icon
                    ),
                    contentDescription = screen.name,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    tint = if (currentScreen == screen) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                )
            }

            // Reset animation after click
            LaunchedEffect(isClicked) {
                if (isClicked) {
                    kotlinx.coroutines.delay(200) // Short animation duration
                    isClicked = false
                }
            }
        }
    }
}