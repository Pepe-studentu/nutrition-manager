package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.model.Model
import org.example.project.service.DataManager
import org.example.project.service.TranslationService
import org.example.project.view.App

fun main() {
    // Initialize user data directory and seed initial data if needed - BEFORE composable
    DataManager.initializeUserData()

    Model.loadFoods()
    Model.loadMeals()
    Model.loadMultiDayMenus()
    Model.loadSettings()

    // Initialize translation service with loaded language
    TranslationService.setLanguage(Model.settings.language)

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KotlinProjectTest",
        ) {
            App()
        }
    }
}