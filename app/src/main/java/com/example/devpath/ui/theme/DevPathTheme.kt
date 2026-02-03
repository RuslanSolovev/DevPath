package com.example.devpath.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.devpath.data.repository.LocalThemeRepository
import com.example.devpath.data.repository.ThemeRepository
import com.example.devpath.ui.fffff.Pink40
import com.example.devpath.ui.fffff.Pink80
import com.example.devpath.ui.fffff.Purple40
import com.example.devpath.ui.fffff.Purple80
import com.example.devpath.ui.fffff.PurpleGrey40
import com.example.devpath.ui.fffff.PurpleGrey80

private val LightColors = lightColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val DarkColors = darkColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun DevPathTheme(content: @Composable () -> Unit) {
    val themeRepository = LocalThemeRepository.current
    val appTheme by themeRepository.currentTheme.collectAsState()

    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()

    val useDarkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemDark
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val colorScheme = if (useDarkTheme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context as Activity)
        } else {
            DarkColors
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context as Activity)
        } else {
            LightColors
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}