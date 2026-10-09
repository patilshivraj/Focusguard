package com.focusguard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = Color.White,
    primaryContainer = SageMuted,
    onPrimaryContainer = SagePrimaryDark,
    secondary = SagePrimaryDark,
    onSecondary = Color.White,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = CreamSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE8E6DE),
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = SageLight,
    onPrimary = SagePrimaryDark,
    primaryContainer = SagePrimaryDark,
    background = Color(0xFF121412),
    surface = Color(0xFF1C1F1D),
)

@Composable
fun FocusGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = FocusGuardTypography,
        content = content,
    )
}
