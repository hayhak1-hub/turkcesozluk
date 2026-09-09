package com.hayhak.turkcesozluk.ui.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import com.hayhak.turkcesozluk.util.findActivity

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = BackgroundDark,
    surface = SurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    tertiary = TertiaryLight,
    background = BackgroundLight,
    surface = SurfaceLight
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
            val activity = view.context.findActivity() as? ComponentActivity ?: return@SideEffect
            val barStyle = if (darkTheme) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            }
            activity.enableEdgeToEdge(
                statusBarStyle = barStyle,
                navigationBarStyle = barStyle,
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
