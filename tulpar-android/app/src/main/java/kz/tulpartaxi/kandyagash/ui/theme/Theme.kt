package kz.tulpartaxi.kandyagash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TulparColorScheme = lightColorScheme(
    primary = TulparLime,
    onPrimary = TulparOnLime,
    primaryContainer = TulparSurface2,
    onPrimaryContainer = TulparWhite,
    secondary = TulparSurface,
    onSecondary = TulparWhite,
    background = TulparBlack,
    onBackground = TulparWhite,
    surface = TulparSurface,
    onSurface = TulparWhite,
    surfaceVariant = TulparSurface2,
    onSurfaceVariant = TulparGray,
    outline = TulparGrayLight,
    error = Color(0xFFFF4444),
)

@Composable
fun TulparTaxiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TulparColorScheme,
        typography = TulparTypography,
        content = content,
    )
}
