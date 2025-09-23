package org.example.project.view.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.model.MultiDayMenu
import org.example.project.service.TemplateManager
import org.example.project.service.SignatureManager
import org.example.project.service.MenuPrintService
import org.example.project.view.theme.AccessibilityTypography

@Composable
fun PrintDialog(
    menu: MultiDayMenu,
    onDismiss: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    var includeSignature by remember { mutableStateOf(true) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var isGeneratingTemplate by remember { mutableStateOf(false) }
    
    // Use singleton TemplateManager
    val signatureManager = remember { SignatureManager() }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .width(600.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Print: ${menu.description}",
                    style = AccessibilityTypography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Signature toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Include signature",
                        style = AccessibilityTypography.bodyMedium
                    )
                    Switch(
                        checked = includeSignature,
                        onCheckedChange = { includeSignature = it }
                    )
                }

                AnimatedVisibility(includeSignature) {

                    Text(
                        text = "Signature file: ${signatureManager.getSignatureFilePath()}",
                        style = AccessibilityTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))

                // Template editing section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Template CSS Customization",
                            style = AccessibilityTypography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Copy -> Edit CSS elsewhere -> Paste",
                            style = AccessibilityTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isGeneratingTemplate = true
                                        try {
                                            withContext(Dispatchers.Default) {
                                                val template = TemplateManager.generateEditableTemplate(menu, includeSignature)
                                                TemplateManager.copyToClipboard(template)
                                            }
                                            showSnackbar("Template copied to clipboard - edit CSS and paste back")
                                        } catch (e: Exception) {
                                            showSnackbar("Failed to generate template: ${e.message}")
                                        } finally {
                                            isGeneratingTemplate = false
                                        }
                                    }
                                },
                                enabled = !isGeneratingTemplate && !isGeneratingPdf,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isGeneratingTemplate) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Copy Template")
                                }
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val clipboardContent = TemplateManager.getFromClipboard()
                                            if (clipboardContent != null) {
                                                val extractedCss = TemplateManager.extractCssFromTemplate(clipboardContent)
                                                if (extractedCss != null) {
                                                    // Store the updated CSS for use in PDF generation
                                                    TemplateManager.updateCss(extractedCss)
                                                    showSnackbar("Template updated successfully")
                                                } else {
                                                    showSnackbar("No valid CSS found in clipboard")
                                                }
                                            } else {
                                                showSnackbar("No content found in clipboard")
                                            }
                                        } catch (e: Exception) {
                                            showSnackbar("Failed to update template: ${e.message}")
                                        }
                                    }
                                },
                                enabled = !isGeneratingTemplate && !isGeneratingPdf,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Update Template")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isGeneratingPdf = true
                                try {
                                    withContext(Dispatchers.Default) {
                                        val printService = MenuPrintService()
                                        val pdfPath = printService.generateMenuPdf(menu, includeSignature)
                                        val message = if (pdfPath != null) {
                                            "PDF generated and opened: ${pdfPath.substringAfterLast("/")}"
                                        } else {
                                            "Failed to generate PDF"
                                        }
                                        showSnackbar(message)
                                    }
                                    onDismiss()
                                } catch (e: Exception) {
                                    showSnackbar("Failed to generate PDF: ${e.message}")
                                } finally {
                                    isGeneratingPdf = false
                                }
                            }
                        },
                        enabled = !isGeneratingTemplate && !isGeneratingPdf,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Generate PDF")
                        }
                    }
                }
            }
        }
    }
}