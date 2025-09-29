package org.example.project.service

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.example.project.model.MultiDayMenu
import org.example.project.model.Model
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MenuPrintService {
    
    private val signatureManager = SignatureManager()
    
    fun generateMenuPdf(menu: MultiDayMenu, includeSignature: Boolean = false): String? {
        return try {
            // Simple: load template, replace placeholders, generate PDF
            val html = generateHtmlContent(menu, includeSignature)
            
            // Create output directory in a more appropriate location
            val outputDir = File(System.getProperty("user.home"), "NutritionApp/MenuPDFs")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            val fileName = "menu_${menu.description.replace(Regex("[^a-zA-Z0-9]"), "_")}_${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            }.pdf"
            val file = File(outputDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                PdfRendererBuilder()
                    .useFastMode()
                    .withHtmlContent(html, null)
                    .toStream(outputStream)
                    .run()
            }
            
            // Open PDF with system default viewer
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file)
            }
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun generateHtmlContent(menu: MultiDayMenu, includeSignature: Boolean = false): String {
        val template = loadTemplate()
        return processTemplate(template, menu, includeSignature)
    }

    fun processTemplate(template: String, menu: MultiDayMenu, includeSignature: Boolean = false): String {
        val content = generateTableContent(menu)
        val averages = calculateAverages(menu)

        val signatureContent = if (includeSignature) {
            signatureManager.getSignatureTemplate() ?: ""
        } else {
            ""
        }

        var html = template

        // Replace mandatory placeholders
        html = html.replace("{{MENU_TITLE}}", escapeHtml(menu.description))
        html = html.replace("{{DAYS_COUNT}}", menu.days.toString())
        html = html.replace("{{GENERATION_DATE}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        html = html.replace("{{CONTENT}}", content)

        // Replace optional placeholders only if they exist in the template
        if (html.contains("{{AVG_PROTEINS}}")) {
            html = html.replace("{{AVG_PROTEINS}}", averages.proteins.toString())
        }
        if (html.contains("{{AVG_FATS}}")) {
            html = html.replace("{{AVG_FATS}}", averages.fats.toString())
        }
        if (html.contains("{{AVG_CARBS}}")) {
            html = html.replace("{{AVG_CARBS}}", averages.carbs.toString())
        }
        if (html.contains("{{AVG_CALORIES}}")) {
            html = html.replace("{{AVG_CALORIES}}", averages.calories.toString())
        }
        if (html.contains("{{SIGNATURE}}")) {
            html = html.replace("{{SIGNATURE}}", signatureContent)
        }

        return ensureXhtmlCompliance(html)
    }
    
    private fun ensureXhtmlCompliance(html: String): String {
        return html
            .replace("<br>", "<br />")
            .replace("<meta charset=\"UTF-8\">", "<meta charset=\"UTF-8\" />")
            .replace("<meta name=\"viewport\"", "<meta name=\"viewport\"")
            .replace("scale=1.0\">", "scale=1.0\" />")
            .replace("<hr>", "<hr />")
            .replace("<img ", "<img ")
            .replace(" />", " />") // Ensure no double spaces before />
            .replace("  />", " />")
    }
    
    private fun loadTemplate(): String {
        val templateFile = File(System.getProperty("user.home"), "NutritionApp/menu_template.html")
        
        return try {
            templateFile.readText()
        } catch (e: Exception) {
            throw RuntimeException("Could not load menu template from ${templateFile.absolutePath}. Please ensure the template file exists.", e)
        }
    }
    
    
    fun generateTableContent(menu: MultiDayMenu): String {
        val mealNames = listOf(
            TranslationService.getString("breakfast"),
            TranslationService.getString("snack_1"),
            TranslationService.getString("lunch"),
            TranslationService.getString("snack_2"),
            TranslationService.getString("dinner")
        )
        val maxDaysPerPage = 7
        val pages = (menu.days + maxDaysPerPage - 1) / maxDaysPerPage
        
        val content = StringBuilder()
        
        for (pageIndex in 0 until pages) {
            val startDay = pageIndex * maxDaysPerPage
            val endDay = minOf(startDay + maxDaysPerPage, menu.days)
            val daysInPage = endDay - startDay
            
            if (pageIndex > 0) {
                content.append("<div class=\"page-break\"></div>")
            }
            
            content.append("<table class=\"menu-table\">")
            
            // Header row
            content.append("<tr>")
            content.append("<th class=\"day-header\">${TranslationService.getString("pdf_day_header")}</th>")
            for (mealName in mealNames) {
                content.append("<th class=\"meal-cell\">$mealName</th>")
            }
            content.append("<th class=\"total-cell\">${TranslationService.getString("pdf_daily_total")}</th>")
            content.append("</tr>")
            
            // Data rows
            for (dayOffset in 0 until daysInPage) {
                val dayIndex = startDay + dayOffset
                val dailyMenu = menu.dailyMenus.getOrNull(dayIndex)
                
                content.append("<tr>")
                
                // Day header cell
                content.append("<td class=\"day-header\">${TranslationService.getString("day_number", dayIndex + 1)}</td>")
                
                // Meal cells
                for (mealIndex in 0 until 5) {
                    val mealId = when (mealIndex) {
                        0 -> dailyMenu?.breakfastId
                        1 -> dailyMenu?.snack1Id
                        2 -> dailyMenu?.lunchId
                        3 -> dailyMenu?.snack2Id
                        4 -> dailyMenu?.dinnerId
                        else -> null
                    }
                    val meal = mealId?.let { Model.getMealById(it) }
                    
                    content.append("<td class=\"meal-cell\">")
                    if (meal != null) {
                        for (sizedFood in meal.foods) {
                            val food = Model.getFoodByName(sizedFood.foodName)
                            val foodNameFormatted = if (food != null && AllergenService.isAllergenFood(food)) {
                                "<strong>${escapeHtml(sizedFood.foodName)}</strong>"
                            } else {
                                escapeHtml(sizedFood.foodName)
                            }
                            content.append("<div class=\"food-item\">${sizedFood.grams.toInt()}g $foodNameFormatted</div>")
                        }
                    }
                    content.append("</td>")
                }
                
                // Daily totals cell
                content.append("<td class=\"total-cell\">")
                if (dailyMenu != null) {
                    val totalCalories = dailyMenu.calories
                    val proteinPercent = if (totalCalories > 0) (dailyMenu.proteins * 4) / totalCalories * 100 else 0f
                    val fatPercent = if (totalCalories > 0) (dailyMenu.fats * 9) / totalCalories * 100 else 0f
                    val carbPercent = if (totalCalories > 0) (dailyMenu.carbs * 4) / totalCalories * 100 else 0f
                    
                    content.append("P: ${proteinPercent.toInt()}%<br />")
                    content.append("F: ${fatPercent.toInt()}%<br />")
                    content.append("G: ${carbPercent.toInt()}%<br />")
                    content.append("Cal: ${totalCalories.toInt()}")
                }
                content.append("</td>")
                
                content.append("</tr>")
            }
            
            content.append("</table>")
        }
        
        return content.toString()
    }
    
    data class MenuAverages(
        val proteins: Int,
        val fats: Int, 
        val carbs: Int,
        val calories: Int
    )
    
    fun calculateAverages(menu: MultiDayMenu): MenuAverages {
        var totalProteins = 0f
        var totalFats = 0f
        var totalCarbs = 0f
        var totalCalories = 0f
        var validDays = 0
        
        for (dailyMenu in menu.dailyMenus) {
            val dayCalories = dailyMenu.calories
            if (dayCalories > 0) {
                totalCalories += dayCalories
                totalProteins += (dailyMenu.proteins * 4) / dayCalories * 100
                totalFats += (dailyMenu.fats * 9) / dayCalories * 100
                totalCarbs += (dailyMenu.carbs * 4) / dayCalories * 100
                validDays++
            }
        }
        
        return if (validDays > 0) {
            MenuAverages(
                proteins = (totalProteins / validDays).toInt(),
                fats = (totalFats / validDays).toInt(),
                carbs = (totalCarbs / validDays).toInt(),
                calories = (totalCalories / validDays).toInt()
            )
        } else {
            MenuAverages(0, 0, 0, 0)
        }
    }
    
    fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }
}