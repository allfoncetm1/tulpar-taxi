package kz.tulpartaxi.kandyagash.ui.start

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.tulpartaxi.kandyagash.R
import kz.tulpartaxi.kandyagash.ui.theme.TulparGray
import kz.tulpartaxi.kandyagash.ui.theme.TulparGrayLight
import kz.tulpartaxi.kandyagash.ui.theme.TulparLime

@Composable
fun LoadingScreen() {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(0.92f, animationSpec = tween(2400, easing = LinearEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Логотип с плавной пульсацией
            Image(
                painter = painterResource(R.drawable.tulpar_logo_black),
                contentDescription = "Tulpar Taxi",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .scale(pulseScale),
            )

            Spacer(Modifier.height(48.dp))

            // Прогресс-бар
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TulparGrayLight),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TulparLime),
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Загружаем карту...",
                fontSize = 13.sp,
                color = TulparGray,
            )
        }

        // Слоган внизу
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Кандыагаш • Tulpar Taxi",
                fontSize = 11.sp,
                color = TulparGray,
                letterSpacing = 2.sp,
            )
        }
    }
}
