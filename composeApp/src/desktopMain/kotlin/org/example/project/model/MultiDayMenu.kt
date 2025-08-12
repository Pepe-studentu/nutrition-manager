package org.example.project.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class MultiDayMenu(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val days: Int,
    val dailyMenus: List<DailyMenu>
) {
    val averageProteins: Float by lazy {
        if (dailyMenus.isEmpty()) 0f
        else dailyMenus.map { it.proteins }.average().toFloat()
    }
    
    val averageFats: Float by lazy {
        if (dailyMenus.isEmpty()) 0f
        else dailyMenus.map { it.fats }.average().toFloat()
    }
    
    val averageCarbs: Float by lazy {
        if (dailyMenus.isEmpty()) 0f
        else dailyMenus.map { it.carbs }.average().toFloat()
    }
    
    val averageCalories: Float by lazy {
        if (dailyMenus.isEmpty()) 0f
        else dailyMenus.map { it.calories }.average().toFloat()
    }
    
    fun validate(): Boolean {
        return description.isNotBlank() && 
               days > 0 && 
               dailyMenus.size <= days
    }
}