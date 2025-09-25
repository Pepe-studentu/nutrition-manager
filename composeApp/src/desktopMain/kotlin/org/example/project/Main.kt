package org.example.project

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.model.Model
import org.example.project.service.TranslationService
import org.example.project.view.App
import org.example.project.view.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KotlinProjectTest",
    ) {
        Model.loadFoods()
        Model.loadMeals()
        Model.loadMultiDayMenus()
        Model.loadSettings()

        // Initialize translation service with loaded language
        TranslationService.setLanguage(Model.settings.language)
        AppTheme { App() }
    }
}