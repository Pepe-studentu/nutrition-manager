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
            style = MaterialTheme.typography.headlineMedium
        )

        HorizontalDivider(thickness = 2.dp)

        // Language Setting Section
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = tr("language"),
                style = MaterialTheme.typography.titleMedium
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
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        HorizontalDivider(thickness = 2.dp)

        // Text Size Setting Section
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = tr("text_size"),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "70%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(60.dp)
                )

                Slider(
                    value = currentSettings.textSizeMultiplier,
                    onValueChange = { newMultiplier ->
                        val newSettings = currentSettings.copy(textSizeMultiplier = newMultiplier)
                        currentSettings = newSettings
                        Model.updateSettings(newSettings)
                    },
                    valueRange = 0.7f..1.3f,
                    modifier = Modifier.weight(0.6f)
                )

                Text(
                    text = "130%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(60.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${(currentSettings.textSizeMultiplier * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(80.dp)
                )
            }
        }

        HorizontalDivider(thickness = 2.dp)

        // Future settings can be added here with additional dividers
    }
}