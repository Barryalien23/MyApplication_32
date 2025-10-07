package com.raux.myapplication_32.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.White,
    secondary = AppColors.White40,
    tertiary = AppColors.White20,
    background = AppColors.Background,
    surface = AppColors.Surface,
    onPrimary = AppColors.Black,
    onSecondary = AppColors.White,
    onTertiary = AppColors.White,
    onBackground = AppColors.OnBackground,
    onSurface = AppColors.OnSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Black,
    secondary = AppColors.MainGrey,
    tertiary = AppColors.BackgroundDark,
    background = AppColors.White,
    surface = AppColors.White,
    onPrimary = AppColors.White,
    onSecondary = AppColors.White,
    onTertiary = AppColors.White,
    onBackground = AppColors.Black,
    onSurface = AppColors.Black,
)

@Composable
fun MyApplication_32Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypographyMaterial,
        content = content
    )
}