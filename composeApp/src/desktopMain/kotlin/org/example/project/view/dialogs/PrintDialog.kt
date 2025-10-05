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
import org.example.project.view.components.FocusableButton
import org.example.project.view.theme.AccessibilityTypography
import org.example.project.service.tr
import org.example.project.service.TranslationService

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
                    text = tr("print_menu_title", menu.description),
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
                        text = tr("include_signature"),
                        style = AccessibilityTypography.bodyMedium
                    )
                    Switch(
                        checked = includeSignature,
                        onCheckedChange = { includeSignature = it }
                    )
                }

                AnimatedVisibility(includeSignature) {

                    Text(
                        text = tr("signature_file", signatureManager.getSignatureFilePath()),
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
                            text = tr("template_css_customization"),
                            style = AccessibilityTypography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = tr("copy_paste_instruction"),
                            style = AccessibilityTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FocusableButton(
                                onClick = {
                                    scope.launch {
                                        isGeneratingTemplate = true
                                        try {
                                            withContext(Dispatchers.Default) {
                                                val template = TemplateManager.generateEditableTemplate(menu, includeSignature)
                                                TemplateManager.copyToClipboard(template)
                                            }
                                            showSnackbar(TranslationService.getString("template_copied_message"))
                                        } catch (e: Exception) {
                                            showSnackbar(TranslationService.getString("failed_to_generate_template", e.message ?: ""))
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
                                    Text(tr("copy_template"))
                                }
                            }

                            FocusableButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val clipboardContent = TemplateManager.getFromClipboard()
                                            if (clipboardContent != null) {
                                                val extractedCss = TemplateManager.extractCssFromTemplate(clipboardContent)
                                                if (extractedCss != null) {
                                                    // Store the updated CSS for use in PDF generation
                                                    TemplateManager.updateCss(extractedCss)
                                                    showSnackbar(TranslationService.getString("template_updated_successfully"))
                                                } else {
                                                    showSnackbar(TranslationService.getString("no_valid_css_found"))
                                                }
                                            } else {
                                                showSnackbar(TranslationService.getString("no_content_found_in_clipboard"))
                                            }
                                        } catch (e: Exception) {
                                            showSnackbar(TranslationService.getString("failed_to_update_template", e.message ?: ""))
                                        }
                                    }
                                },
                                enabled = !isGeneratingTemplate && !isGeneratingPdf,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(tr("update_template"))
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
                    FocusableButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(tr("cancel"))
                    }

                    FocusableButton(
                        onClick = {
                            scope.launch {
                                isGeneratingPdf = true
                                try {
                                    withContext(Dispatchers.Default) {
                                        val printService = MenuPrintService()
                                        val pdfPath = printService.generateMenuPdf(menu, includeSignature)
                                        val message = if (pdfPath != null) {
                                            TranslationService.getString("pdf_generated_message", pdfPath.substringAfterLast("/"))
                                        } else {
                                            TranslationService.getString("failed_to_generate_pdf")
                                        }
                                        showSnackbar(message)
                                    }
                                    onDismiss()
                                } catch (e: Exception) {
                                    showSnackbar(TranslationService.getString("failed_to_generate_pdf"))
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
                            Text(tr("generate_pdf"))
                        }
                    }
                }
            }
        }
    }
}