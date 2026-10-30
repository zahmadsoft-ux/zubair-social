package com.zubair.social.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Indigo600,
    onPrimary = Surface0,
    primaryContainer = Indigo100,
    background = Surface0,
    onBackground = Ink900,
    surface = Surface0,
    onSurface = Ink900,
    surfaceVariant = Surface1,
    onSurfaceVariant = Ink500,
    error = Error500,
)

private val DarkColors = darkColorScheme(
    primary = Indigo600,
    onPrimary = Surface0,
    error = Error500,
)

@Composable
fun ZubairSocialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
