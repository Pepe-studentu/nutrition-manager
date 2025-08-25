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
    
    fun generateMenuPdf(menu: MultiDayMenu): String? {
        return try {
            val html = generateHtmlContent(menu)
            val fileName = "menu_${menu.description.replace(Regex("[^a-zA-Z0-9]"), "_")}_${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            }.pdf"
            val file = File(fileName)
            
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
    
    private fun generateHtmlContent(menu: MultiDayMenu): String {
        val template = loadTemplate()
        val content = generateTableContent(menu)
        val averages = calculateAverages(menu)
        
        val html = template
            .replace("{{MENU_TITLE}}", escapeHtml(menu.description))
            .replace("{{DAYS_COUNT}}", menu.days.toString())
            .replace("{{GENERATION_DATE}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            .replace("{{CONTENT}}", content)
            .replace("{{AVG_PROTEINS}}", averages.proteins.toString())
            .replace("{{AVG_FATS}}", averages.fats.toString())
            .replace("{{AVG_CARBS}}", averages.carbs.toString())
            .replace("{{AVG_CALORIES}}", averages.calories.toString())
            
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
        // Try multiple possible resource paths
        val possiblePaths = listOf(
            "/menu_template.html",
            "/templates/menu_template.html",
            "menu_template.html"
        )
        
        for (path in possiblePaths) {
            try {
                val stream = this::class.java.getResourceAsStream(path)
                if (stream != null) {
                    return stream.bufferedReader().use { it.readText() }
                }
                println("Template not found at: $path")
            } catch (e: Exception) {
                println("Failed to load template from $path: ${e.message}")
            }
        }
        
        // If all paths fail, return a simple inline template
        println("Using fallback inline template")
        return getFallbackTemplate()
    }
    
    private fun getFallbackTemplate(): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>{{MENU_TITLE}}</title>
            <style>
                @page { size: A4 landscape; margin: 1cm; }
                body { font-family: Arial, sans-serif; font-size: 10pt; }
                .header { margin-bottom: 20px; border-bottom: 2px solid #333; padding-bottom: 15px; }
                .header h1 { margin: 0; font-size: 18pt; }
                .header .meta { margin: 8px 0 0 0; color: #666; }
                table { width: 100%; border-collapse: collapse; table-layout: fixed; }
                th, td { border: 1px solid #333; padding: 8px; vertical-align: top; text-align: center; }
                th { background-color: #f0f0f0; font-weight: bold; font-size: 9pt; }
                .day-header { width: 60px; background-color: #e8e8e8; font-weight: bold; }
                .meal-cell { width: 140px; font-size: 8pt; text-align: left; padding: 6px; }
                .total-cell { width: 80px; font-size: 8pt; background-color: #f9f9f9; }
                .food-item { margin-bottom: 2px; }
                .averages { margin-top: 15px; padding: 10px; background-color: #f5f5f5; border: 1px solid #ddd; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Menu: {{MENU_TITLE}}</h1>
                <div class="meta">{{DAYS_COUNT}} days total • Generated: {{GENERATION_DATE}}</div>
            </div>
            {{CONTENT}}
            <div class="averages">
                <h3>Menu Averages</h3>
                <div>Proteins: {{AVG_PROTEINS}}% • Fats: {{AVG_FATS}}% • Carbs: {{AVG_CARBS}}% • Calories: {{AVG_CALORIES}} per day</div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
    
    private fun generateTableContent(menu: MultiDayMenu): String {
        val mealNames = listOf("Breakfast", "Snack 1", "Lunch", "Snack 2", "Dinner")
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
            content.append("<th class=\"day-header\">Day</th>")
            for (mealName in mealNames) {
                content.append("<th class=\"meal-cell\">$mealName</th>")
            }
            content.append("<th class=\"total-cell\">Daily Total</th>")
            content.append("</tr>")
            
            // Data rows
            for (dayOffset in 0 until daysInPage) {
                val dayIndex = startDay + dayOffset
                val dailyMenu = menu.dailyMenus.getOrNull(dayIndex)
                
                content.append("<tr>")
                
                // Day header cell
                content.append("<td class=\"day-header\">Day ${dayIndex + 1}</td>")
                
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
                            content.append("<div class=\"food-item\">${sizedFood.grams.toInt()}g ${escapeHtml(sizedFood.foodName)}</div>")
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
    
    private data class MenuAverages(
        val proteins: Int,
        val fats: Int, 
        val carbs: Int,
        val calories: Int
    )
    
    private fun calculateAverages(menu: MultiDayMenu): MenuAverages {
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
    
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }
}