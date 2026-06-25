package kz.tulpartaxi.kandyagash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightScheme = lightColorScheme(
    primary = TulparLime,
    onPrimary = TulparOnLime,
    primaryContainer = LightField,
    onPrimaryContainer = LightFg,
    secondary = LightSurface,
    onSecondary = LightFg,
    background = LightBg,
    onBackground = LightFg,
    surface = LightSurface,
    onSurface = LightFg,
    surfaceVariant = LightField,
    onSurfaceVariant = LightFg2,
    outline = LightLine,
    error = TulparError,
)

private val DarkScheme = darkColorScheme(
    primary = TulparLime,
    onPrimary = TulparOnLime,
    primaryContainer = DarkField,
    onPrimaryContainer = DarkFg,
    secondary = DarkSurface,
    onSecondary = DarkFg,
    background = DarkBg,
    onBackground = DarkFg,
    surface = DarkSurface,
    onSurface = DarkFg,
    surfaceVariant = DarkField,
    onSurfaceVariant = DarkFg2,
    outline = DarkLine,
    error = TulparError,
)

@Composable
fun TulparTaxiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val tulparColors = if (darkTheme) DarkTulparColors else LightTulparColors
    CompositionLocalProvider(LocalTulparColors provides tulparColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = TulparTypography,
            content = content,
        )
    }
}
