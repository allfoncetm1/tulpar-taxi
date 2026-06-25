package kz.tulpartaxi.kandyagash.ui.theme

import androidx.compose.ui.graphics.Color

// Lime accent — same in dark/light
val TulparLime = Color(0xFFDCFB3D)
val TulparOnLime = Color(0xFF16170C)

// Light theme tokens
val LightBg = Color(0xFFF1F1EE)
val LightBg2 = Color(0xFFE7E7E1)
val LightSurface = Color(0xFFFFFFFF)
val LightFg = Color(0xFF15150F)
val LightFg2 = Color(0xFF6E6E66)
val LightFg3 = Color(0xFFAEAEA4)
val LightLine = Color(0xFFECECE6)
val LightField = Color(0xFFF1F1EC)
val LightLimeDeep = Color(0xFF9DBF00)
val LightLimeGlow = Color(0x47DCFB3D) // ~28% alpha

// Dark theme tokens
val DarkBg = Color(0xFF0C0E08)
val DarkBg2 = Color(0xFF0A0B06)
val DarkSurface = Color(0xFF15180E)
val DarkFg = Color(0xFFF2F4E8)
val DarkFg2 = Color(0xFF9A9D8C)
val DarkFg3 = Color(0xFF62655A)
val DarkLine = Color(0xFF272B1B)
val DarkField = Color(0xFF1E2214)
val DarkLimeDeep = TulparLime
val DarkLimeGlow = Color(0x29DCFB3D) // ~16% alpha

val TulparError = Color(0xFFE5604D)

// Legacy aliases — point at dark tokens since app defaults to dark theme.
// New screens use TulparTheme.colors and respect the active theme.
val TulparBlack = DarkBg
val TulparSurface = DarkSurface
val TulparSurface2 = DarkField
val TulparWhite = DarkFg
val TulparGray = DarkFg2
val TulparGrayLight = DarkLine
val TulparOrange = TulparLime
val TulparDark = DarkBg
val TulparDarkSurface = DarkSurface
val TulparLight = DarkBg
val TulparOnDark = DarkFg
val TulparOnOrange = TulparOnLime
