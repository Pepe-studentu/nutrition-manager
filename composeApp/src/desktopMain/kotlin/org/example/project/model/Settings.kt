package org.example.project.model

import kotlinx.serialization.Serializable
import org.example.project.service.Language

@Serializable
data class Settings(
    val language: Language = Language.ENGLISH,
    val textSizeMultiplier: Float = 1.0f
)