package kz.tulpartaxi.kandyagash.ui.start

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.tulpartaxi.kandyagash.R
import kz.tulpartaxi.kandyagash.ui.theme.TulparTheme

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val colors = TulparTheme.colors

    val infinite = rememberInfiniteTransition(label = "splash")
    val bob by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob",
    )
    val glow by infinite.animateFloat(
        initialValue = 0.55f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow",
    )
    val progress by infinite.animateFloat(
        initialValue = 0.08f, targetValue = 0.96f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "progress",
    )

    Box(
        modifier = modifier.fillMaxSize().background(colors.bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .scale(1f + 0.08f * glow)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(colors.lime.copy(alpha = glow * 0.5f), Color.Transparent),
                            ),
                        ),
                )
                Image(
                    painter = painterResource(R.drawable.tulpar_mark),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .offset(y = (-bob * 10f).dp),
                )
            }

            Spacer(Modifier.height(30.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append("TULPAR ") }
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("Taxi") }
                },
                fontSize = 34.sp,
                color = colors.fg,
                letterSpacing = (-0.6).sp,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Н А Р О Д Н О Е   Т А К С И",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.fg3,
                letterSpacing = 4.sp,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
                .width(140.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.line),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.lime),
            )
        }
    }
}
