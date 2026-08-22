package com.abhii.paprivo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.abhii.paprivo.data.models.ThemeMode

data class PaprivoColors(
    val background: Color,
    val primary: Color,
    val secondary: Color,
    val iconContainer: Color,
    val border: Color,
    val surface: Color,
    val isDark: Boolean
)

val LocalPaprivoColors = staticCompositionLocalOf {
    PaprivoColors(
        background = BlackBackground,
        primary = BlackPrimary,
        secondary = BlackSecondary,
        iconContainer = BlackIconContainer,
        border = BlackBorder,
        surface = BlackSurface,
        isDark = true
    )
}

@Composable
fun PaprivoTheme(
    themeMode: ThemeMode = ThemeMode.BLACK,
    content: @Composable () -> Unit
) {
    val isDark = themeMode == ThemeMode.BLACK

    val customColors = remember(isDark) {
        if (isDark) {
            PaprivoColors(
                background = BlackBackground,
                primary = BlackPrimary,
                secondary = BlackSecondary,
                iconContainer = BlackIconContainer,
                border = BlackBorder,
                surface = BlackSurface,
                isDark = true
            )
        } else {
            PaprivoColors(
                background = WhiteBackground,
                primary = WhitePrimary,
                secondary = WhiteSecondary,
                iconContainer = WhiteIconContainer,
                border = WhiteBorder,
                surface = WhiteSurface,
                isDark = false
            )
        }
    }

    val materialColorScheme = remember(isDark) {
        if (isDark) {
            darkColorScheme(
                background = BlackBackground,
                surface = BlackSurface,
                onBackground = BlackPrimary,
                onSurface = BlackPrimary,
                primary = BlackPrimary,
                secondary = BlackSecondary,
                outline = BlackBorder,
                surfaceVariant = BlackIconContainer
            )
        } else {
            lightColorScheme(
                background = WhiteBackground,
                surface = WhiteSurface,
                onBackground = WhitePrimary,
                onSurface = WhitePrimary,
                primary = WhitePrimary,
                secondary = WhiteSecondary,
                outline = WhiteBorder,
                surfaceVariant = WhiteIconContainer
            )
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalPaprivoColors provides customColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
