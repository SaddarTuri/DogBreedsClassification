package com.saddar.dogbreeds.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DogBreedsColorScheme = lightColorScheme(
    primary             = DarkNavy,
    onPrimary           = Color.White,
    primaryContainer    = ChipBlue,
    onPrimaryContainer  = DarkNavy,
    secondary           = DeepBlue,
    onSecondary         = Color.White,
    secondaryContainer  = ChipBlue,
    onSecondaryContainer = DarkNavy,
    background          = LightLavender,
    onBackground        = DarkNavy,
    surface             = Color.White,
    onSurface           = DarkNavy,
    surfaceVariant      = ChipBlue,
    onSurfaceVariant    = DarkNavy,
    outline             = DividerColor,
    error               = ErrorRed,
    onError             = Color.White,
)

@Composable
fun DogBreedsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DogBreedsColorScheme,
        typography  = DogBreedsTypography,
        content     = content
    )
}
