package kz.tulpartaxi.kandyagash.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.tulpartaxi.kandyagash.ui.theme.TulparBlack
import kz.tulpartaxi.kandyagash.ui.theme.TulparGray
import kz.tulpartaxi.kandyagash.ui.theme.TulparLime
import kz.tulpartaxi.kandyagash.ui.theme.TulparOnLime
import kz.tulpartaxi.kandyagash.ui.theme.TulparSurface
import kz.tulpartaxi.kandyagash.ui.theme.TulparWhite

@Composable
fun RatingScreen(
    orderId: String,
    onSubmit: (orderId: String, rating: Int) -> Unit,
    onSkip: () -> Unit,
) {
    var selectedRating by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TulparBlack.copy(alpha = 0.6f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(TulparSurface)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Ручка
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TulparGray.copy(alpha = 0.3f)),
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Как прошла поездка?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TulparWhite,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Оцените водителя",
                fontSize = 14.sp,
                color = TulparGray,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            // Звёзды
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= selectedRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "$star звезд",
                        tint = if (star <= selectedRating) TulparLime else TulparGray,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { selectedRating = star },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = when (selectedRating) {
                    1 -> "Очень плохо"
                    2 -> "Плохо"
                    3 -> "Нормально"
                    4 -> "Хорошо"
                    5 -> "Отлично!"
                    else -> ""
                },
                fontSize = 14.sp,
                color = if (selectedRating > 0) TulparLime else TulparGray,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { if (selectedRating > 0) onSubmit(orderId, selectedRating) },
                enabled = selectedRating > 0,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TulparLime,
                    contentColor = TulparOnLime,
                    disabledContainerColor = TulparGray.copy(alpha = 0.2f),
                    disabledContentColor = TulparGray,
                ),
            ) {
                Text("Отправить оценку", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("Пропустить", color = TulparGray, fontSize = 14.sp)
            }
        }
    }
}
