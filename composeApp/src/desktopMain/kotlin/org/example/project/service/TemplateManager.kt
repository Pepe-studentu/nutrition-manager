package org.example.project.service

import org.example.project.model.MultiDayMenu
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TemplateManager {
    
    private val templateFile = File(System.getProperty("user.home"), "NutritionApp/menu_template.html")
    
    fun generateEditableTemplate(menu: MultiDayMenu, includeSignature: Boolean = false): String {
        // Read template from file and substitute placeholders with real data
        val template = readTemplateFile()
        val substitutedTemplate = substitutePlaceholders(template, menu, includeSignature)
        return wrapWithEditableMarkers(substitutedTemplate)
    }
    
    private fun readTemplateFile(): String {
        return try {
            templateFile.readText()
        } catch (e: Exception) {
            throw RuntimeException("Could not read template file from ${templateFile.absolutePath}", e)
        }
    }
    
    private fun substitutePlaceholders(template: String, menu: MultiDayMenu, includeSignature: Boolean = false): String {
        val printService = MenuPrintService()
        val content = printService.generateTableContent(menu)
        val averages = printService.calculateAverages(menu)
        
        val signatureManager = SignatureManager()
        val signatureContent = if (includeSignature) {
            signatureManager.getSignatureTemplate() ?: ""
        } else {
            ""
        }
        
        return template
            .replace("{{MENU_TITLE}}", printService.escapeHtml(menu.description))
            .replace("{{DAYS_COUNT}}", menu.days.toString())
            .replace("{{GENERATION_DATE}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            .replace("{{CONTENT}}", content)
            .replace("{{AVG_PROTEINS}}", averages.proteins.toString())
            .replace("{{AVG_FATS}}", averages.fats.toString())
            .replace("{{AVG_CARBS}}", averages.carbs.toString())
            .replace("{{AVG_CALORIES}}", averages.calories.toString())
            .replace("{{SIGNATURE}}", signatureContent)
    }
    
    private fun wrapWithEditableMarkers(html: String): String {
        // Find the CSS section and wrap it with editable markers
        val cssStart = html.indexOf("<style>")
        val cssEnd = html.indexOf("</style>") + 8
        
        if (cssStart != -1 && cssEnd != -1) {
            val beforeCss = html.substring(0, cssStart)
            val cssSection = html.substring(cssStart, cssEnd)
            val afterCss = html.substring(cssEnd)
            
            return beforeCss + 
                   "<!-- EDITABLE SECTION START: CSS Styling -->\n" +
                   cssSection + 
                   "\n<!-- EDITABLE SECTION END: CSS Styling -->\n" +
                   afterCss
        }
        
        return html
    }
    
    fun copyToClipboard(content: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val stringSelection = StringSelection(content)
        clipboard.setContents(stringSelection, null)
    }
    
    fun getFromClipboard(): String? {
        return try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val data = clipboard.getData(DataFlavor.stringFlavor)
            data as? String
        } catch (e: Exception) {
            null
        }
    }
    
    fun extractCssFromTemplate(template: String): String? {
        val startMarker = "<!-- EDITABLE SECTION START: CSS Styling -->"
        val endMarker = "<!-- EDITABLE SECTION END: CSS Styling -->"
        
        val startIndex = template.indexOf(startMarker)
        val endIndex = template.indexOf(endMarker)
        
        if (startIndex != -1 && endIndex != -1) {
            val cssSection = template.substring(startIndex + startMarker.length, endIndex).trim()
            
            // Extract just the CSS content between <style> tags
            val styleStart = cssSection.indexOf("<style>")
            val styleEnd = cssSection.indexOf("</style>")
            
            if (styleStart != -1 && styleEnd != -1) {
                return cssSection.substring(styleStart + 7, styleEnd).trim()
            }
        }
        
        return null
    }
    
    fun updateCss(newCss: String) {
        // Read current template, update CSS section, write back to file
        val template = readTemplateFile()
        val updatedTemplate = updateTemplateCss(template, newCss)
        writeTemplateFile(updatedTemplate)
    }
    
    private fun writeTemplateFile(content: String) {
        try {
            templateFile.writeText(content)
        } catch (e: Exception) {
            throw RuntimeException("Could not write template file to ${templateFile.absolutePath}", e)
        }
    }
    
    fun updateTemplateCss(originalTemplate: String, newCss: String): String {
        val styleStart = originalTemplate.indexOf("<style>")
        val styleEnd = originalTemplate.indexOf("</style>")
        
        if (styleStart != -1 && styleEnd != -1) {
            val before = originalTemplate.substring(0, styleStart + 7)
            val after = originalTemplate.substring(styleEnd)
            
            return before + "\n" + newCss + "\n                " + after
        }
        
        return originalTemplate
    }
}