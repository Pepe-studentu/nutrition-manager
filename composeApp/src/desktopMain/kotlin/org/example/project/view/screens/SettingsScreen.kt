package org.example.project.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.example.project.model.Model
import org.example.project.model.Settings
import org.example.project.service.Language
import org.example.project.service.TranslationService
import org.example.project.service.tr
import org.example.project.view.theme.AccessibilityTypography

@Composable
fun SettingsScreen() {
    var currentSettings by remember { mutableStateOf(Model.settings) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Settings header
        Text(
            text = tr("settings"),
            style = AccessibilityTypography.headlineMedium
        )

        HorizontalDivider(thickness = 2.dp)

        // Language Setting Section
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = tr("language"),
                style = AccessibilityTypography.titleMedium
            )

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Language.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (currentSettings.language == language),
                                onClick = {
                                    val newSettings = currentSettings.copy(language = language)
                                    currentSettings = newSettings
                                    Model.updateSettings(newSettings)
                                    TranslationService.setLanguage(language)
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = (currentSettings.language == language),
                            onClick = null
                        )
                        Text(
                            text = language.displayName,
                            style = AccessibilityTypography.bodyLarge
                        )
                    }
                }
            }
        }

        HorizontalDivider(thickness = 2.dp)

        // Future settings can be added here with additional dividers
    }
}