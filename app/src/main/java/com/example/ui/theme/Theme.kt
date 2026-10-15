package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = NavyMedium,
    onPrimaryContainer = SkyBlueLight,
    secondary = AmberGold,
    onSecondary = Color.Black,
    tertiary = EmeraldGreen,
    background = NavyDark,
    surface = Color(0xFF132238),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = SkyBlueLight,
    onPrimaryContainer = NavyDark,
    secondary = AmberGold,
    onSecondary = Color.Black,
    secondaryContainer = AmberLight,
    onSecondaryContainer = Color(0xFF7A4A00),
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    tertiaryContainer = MintLight,
    onTertiaryContainer = EmeraldGreen,
    background = OffWhite,
    onBackground = TextPrimary,
    surface = SurfacePure,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep NovaMarket brand colors consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
