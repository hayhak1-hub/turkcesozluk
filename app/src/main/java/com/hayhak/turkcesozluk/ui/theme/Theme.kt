package com.hayhak.turkcesozluk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.hayhak.turkcesozluk.util.findActivity

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = CyberDark,
    surface = SurfaceDark,
    onPrimary = Color(0xFF2A1055),
    onSecondary = Color(0xFF1A2744),
    onTertiary = Color(0xFF492532),
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = Color(0xFFB0B8C2),
    surfaceVariant = CardDark,
    outline = Color(0xFF4A5360),
    outlineVariant = Color(0xFF2E3540),
    primaryContainer = PrimaryDark.copy(alpha = 0.22f),
    onPrimaryContainer = Color(0xFFEDE4FF),
    secondaryContainer = SecondaryDark.copy(alpha = 0.18f),
    onSecondaryContainer = Color(0xFFD6EBFF),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE6EDF3),
    inverseOnSurface = Color(0xFF1B1F24),
    inversePrimary = PrimaryLight,
    surfaceTint = PrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    tertiary = TertiaryLight,
    background = SurfaceLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = Color(0xFF4A5563),
    surfaceVariant = Color(0xFFEEF1F8),
    outline = Color(0xFFC5CAD6),
    outlineVariant = Color(0xFFE2E6EF),
    primaryContainer = PrimaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = Color(0xFF1D0060),
    secondaryContainer = SecondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = Color(0xFF001D36),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    surfaceTint = PrimaryLight
)

@Composable
fun TurkceSozlukTheme(
    appTheme: com.hayhak.turkcesozluk.data.db.AppTheme = com.hayhak.turkcesozluk.data.db.AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        com.hayhak.turkcesozluk.data.db.AppTheme.LIGHT -> false
        com.hayhak.turkcesozluk.data.db.AppTheme.DARK -> true
        com.hayhak.turkcesozluk.data.db.AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            WindowInsetsControllerCompat(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
