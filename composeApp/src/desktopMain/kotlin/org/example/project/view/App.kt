package org.example.project.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.model.Model
import org.example.project.model.Screen
import org.example.project.view.components.MyNavBar
import org.example.project.view.screens.FoodsScreen
import org.example.project.view.screens.MenusScreen
import org.example.project.view.theme.AppTheme
@Composable
fun App() {
    AppTheme {
        Row(Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.primary)) {
            // Navigation bar on the left
            MyNavBar(
                currentScreen = Model.currentScreen,
                onScreenSelected = { screen -> Model.currentScreen = screen }
            )

            // Content area with rounded top-left corner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 36.dp)
                    )
            ) {
                when (Model.currentScreen) {
                    Screen.Foods -> FoodsScreen()
                    Screen.Menus -> MenusScreen()
                }
            }
        }
    }
}