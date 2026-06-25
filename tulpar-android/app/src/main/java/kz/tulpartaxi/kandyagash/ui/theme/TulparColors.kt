package kz.tulpartaxi.kandyagash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TulparColors(
    val bg: Color,
    val bg2: Color,
    val surface: Color,
    val fg: Color,
    val fg2: Color,
    val fg3: Color,
    val line: Color,
    val field: Color,
    val lime: Color,
    val onLime: Color,
    val limeDeep: Color,
    val limeGlow: Color,
    val error: Color,
)

val LightTulparColors = TulparColors(
    bg = LightBg, bg2 = LightBg2, surface = LightSurface,
    fg = LightFg, fg2 = LightFg2, fg3 = LightFg3,
    line = LightLine, field = LightField,
    lime = TulparLime, onLime = TulparOnLime,
    limeDeep = LightLimeDeep, limeGlow = LightLimeGlow,
    error = TulparError,
)

val DarkTulparColors = TulparColors(
    bg = DarkBg, bg2 = DarkBg2, surface = DarkSurface,
    fg = DarkFg, fg2 = DarkFg2, fg3 = DarkFg3,
    line = DarkLine, field = DarkField,
    lime = TulparLime, onLime = TulparOnLime,
    limeDeep = DarkLimeDeep, limeGlow = DarkLimeGlow,
    error = TulparError,
)

val LocalTulparColors = staticCompositionLocalOf { LightTulparColors }

object TulparTheme {
    val colors: TulparColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTulparColors.current
}
