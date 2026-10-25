package com.example.insulia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Light scheme used across the app. The brand colours are fixed so the app
 * always looks the same, regardless of the device wallpaper.
 */
private val InsuliaLightColorScheme = lightColorScheme(
    primary = InsuliaSkyBlue,
    onPrimary = Color.White,
    primaryContainer = InsuliaLightBlue,
    onPrimaryContainer = InsuliaOnSurface,
    secondary = InsuliaSkyBlue,
    onSecondary = Color.White,
    secondaryContainer = InsuliaLightBlue,
    onSecondaryContainer = InsuliaOnSurface,
    background = InsuliaSurface,
    onBackground = InsuliaOnSurface,
    surface = InsuliaSurface,
    onSurface = InsuliaOnSurface,
    surfaceVariant = InsuliaSurfaceVariant,
    onSurfaceVariant = InsuliaOnSurfaceVariant,
    outline = InsuliaBorderBlue,
    outlineVariant = InsuliaBorderBlue,
    error = InsuliaHigh,
    onError = Color.White
)

private val InsuliaDarkColorScheme = darkColorScheme(
    primary = InsuliaSkyBlue,
    onPrimary = Color(0xFF00283D),
    primaryContainer = Color(0xFF00344C),
    onPrimaryContainer = InsuliaLightBlue,
    secondary = InsuliaSkyBlue,
    onSecondary = Color(0xFF00283D),
    secondaryContainer = Color(0xFF00344C),
    onSecondaryContainer = InsuliaLightBlue,
    background = Color(0xFF10161C),
    onBackground = Color(0xFFE3E8EE),
    surface = Color(0xFF10161C),
    onSurface = Color(0xFFE3E8EE),
    surfaceVariant = Color(0xFF16242F),
    onSurfaceVariant = Color(0xFFC7D2DC),
    outline = Color(0xFF2E4A5C),
    outlineVariant = Color(0xFF2E4A5C),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF3B0906)
)

/**
 * Theme for the whole app.
 *
 * Dynamic (wallpaper) colour is deliberately not used: the Insulia brand
 * colours are part of the product design and must stay consistent on every
 * device.
 */
@Composable
fun InsuliaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) InsuliaDarkColorScheme else InsuliaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = InsuliaTypography,
        content = content
    )
}
