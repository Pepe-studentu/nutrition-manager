package org.example.project.view.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colors chosen for high contrast and distinguishability
val Blue = Color(0xFF0288D1)
val Yellow = Color(0xFFFFD600)
val DeepPurple = Color(0xFF6200EA)
val Teal = Color(0xFF009688)
val ErrorRed = Color(0xFFD32F2F)
val DividerGray = Color(0xFF9E9E9E)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Background = Color(0xFFF5F5F5)
val ProteinRed = Color(0xFFE57373) // Light red for proteins
val FatYellow = Color(0xFFFFD600)  // Same as Yellow for fats
val CarbsTeal = Color(0xFF4DB6AC)  // Teal variant for carbs

val WarmLightBg = Color(0xFFFFF3B0)

// Expanded color set
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val background: Color,
    val onBackground: Color,
    val divider: Color,
    val error: Color,
    val onError: Color,
    val protein: Color = ProteinRed, // Add protein color
    val fat: Color = FatYellow,     // Add fat color
    val carbs: Color = CarbsTeal,
    val variantBackground: Color
)

val highContrastColorScheme = AppColors(
    primary = Blue,
    onPrimary = White,
    secondary = Yellow,
    onSecondary = Black,
    tertiary = DeepPurple,
    onTertiary = White,
    background = Background,
    variantBackground = WarmLightBg,
    onBackground = Black,
    divider = DividerGray,
    error = ErrorRed,
    onError = White,
    protein = ProteinRed,
    fat = FatYellow,
    carbs = CarbsTeal
)

// Super-sized typography
val AccessibilityTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 56.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 56.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 40.sp, lineHeight = 50.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 44.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp)
)

// Creates a scaled version of the typography based on the multiplier
fun createScaledTypography(multiplier: Float): Typography {
    return Typography(
        displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = (56 * multiplier).sp, lineHeight = (64 * multiplier).sp),
        displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = (48 * multiplier).sp, lineHeight = (56 * multiplier).sp),
        headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (40 * multiplier).sp, lineHeight = (50 * multiplier).sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (32 * multiplier).sp, lineHeight = (44 * multiplier).sp),
        titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = (32 * multiplier).sp, lineHeight = (40 * multiplier).sp),
        titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (28 * multiplier).sp, lineHeight = (36 * multiplier).sp),
        bodyLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (28 * multiplier).sp, lineHeight = (36 * multiplier).sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (24 * multiplier).sp, lineHeight = (32 * multiplier).sp),
        labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (24 * multiplier).sp, lineHeight = (32 * multiplier).sp),
        labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (20 * multiplier).sp, lineHeight = (28 * multiplier).sp)
    )
}

// More prominent corner radii
val AccessibilityShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp)
)

@Composable
fun AppTheme(
    textSizeMultiplier: Float = 1.0f,
    content: @Composable () -> Unit
) {
    key(textSizeMultiplier) {
        MaterialTheme(
            colorScheme = highContrastColorScheme.toMaterialColorScheme(),
            typography = createScaledTypography(textSizeMultiplier),
            shapes = AccessibilityShapes,
            content = content
        )
    }
}

// Extended color mapping
private fun AppColors.toMaterialColorScheme() = ColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primary,
    onPrimaryContainer = onPrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondary,
    onSecondaryContainer = onSecondary,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiary,
    onTertiaryContainer = onTertiary,
    background = background,
    onBackground = onBackground,
    surface = background,
    onSurface = onBackground,
    surfaceVariant = variantBackground,
    onSurfaceVariant = onBackground,
    error = error,
    onError = onError,
    errorContainer = error,
    onErrorContainer = onError,
    outline = divider,
    outlineVariant = divider,
    scrim = Color.Black.copy(alpha = 0.5f),

    inversePrimary = onPrimary,
    surfaceTint = primary,
    inverseSurface = onBackground,
    inverseOnSurface = background,

    surfaceBright = Color.White,
    surfaceDim = Color.DarkGray,
    surfaceContainer = background,
    surfaceContainerHigh = background,
    surfaceContainerHighest = background,
    surfaceContainerLow = background,
    surfaceContainerLowest = background
)
