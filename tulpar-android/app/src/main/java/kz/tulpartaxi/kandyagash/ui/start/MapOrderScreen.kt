package kz.tulpartaxi.kandyagash.ui.start

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.tulpartaxi.kandyagash.R
import kz.tulpartaxi.kandyagash.ui.map.MapWebView
import kz.tulpartaxi.kandyagash.ui.theme.TulparTheme
import kz.tulpartaxi.kandyagash.utils.StaticConfig

private data class QuickAddress(val name: String, val addr: String, val price: String)

private val QuickHome = QuickAddress("Дом", "мкр. Жастар, 14", "1 200 ₸")
private val QuickWork = QuickAddress("Работа", "ул. Абая, 52", "900 ₸")
private val QuickAirport = QuickAddress("Порт", "Аэропорт Кандыагаш", "2 400 ₸")

@Composable
fun MapOrderScreen(
    onProfileClick: () -> Unit = {},
    mapContent: @Composable (() -> Unit)? = null,
) {
    val colors = TulparTheme.colors
    var dest by remember { mutableStateOf<QuickAddress?>(null) }
    var ordering by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg2)) {
        if (mapContent != null) {
            mapContent()
        } else {
            MapWebView(
                url = "${StaticConfig.mapBaseUrl()}/citymap",
                modifier = Modifier.fillMaxSize(),
            )
        }

        CenterPin(visible = !ordering)

        TopBar(onProfileClick = onProfileClick)

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(colors.surface)
                .padding(horizontal = 22.dp, vertical = 12.dp)
                .padding(bottom = 18.dp),
        ) {
            Column {
                SheetGrip()
                Spacer(Modifier.height(8.dp))
                if (ordering && dest != null) {
                    OrderingState(
                        dest = dest!!,
                        onCancel = { ordering = false },
                    )
                } else {
                    IdleState(
                        dest = dest,
                        onPick = { dest = it },
                        onOrder = { if (dest != null) ordering = true },
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(onProfileClick: () -> Unit) {
    val colors = TulparTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 54.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .padding(start = 11.dp, end = 15.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.tulpar_mark),
                contentDescription = null,
                modifier = Modifier.size(38.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text("TULPAR", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.fg, letterSpacing = (-0.2).sp)
                Text("Кандыагаш", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.limeDeep)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            CircleIcon(colors.surface, contentTint = colors.fg, icon = Icons.Filled.Person, onClick = onProfileClick)
            CircleIcon(colors.lime, contentTint = colors.onLime, icon = Icons.Filled.MyLocation, onClick = {})
        }
    }
}

@Composable
private fun CircleIcon(bg: Color, contentTint: Color, icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = contentTint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CenterPin(visible: Boolean) {
    if (!visible) return
    val colors = TulparTheme.colors
    val infinite = rememberInfiniteTransition(label = "pin")
    val ring by infinite.animateFloat(
        initialValue = 0.55f, targetValue = 2.1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "ring",
    )
    val ringAlpha by infinite.animateFloat(
        initialValue = 0.55f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "ringAlpha",
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(ring)
                        .clip(CircleShape)
                        .border(2.dp, colors.lime.copy(alpha = ringAlpha), CircleShape),
                )
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = colors.lime,
                    modifier = Modifier.size(50.dp),
                )
            }
        }
    }
}

@Composable
private fun SheetGrip() {
    val colors = TulparTheme.colors
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(width = 42.dp, height = 5.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.line),
        )
    }
}

@Composable
private fun IdleState(
    dest: QuickAddress?,
    onPick: (QuickAddress) -> Unit,
    onOrder: () -> Unit,
) {
    val colors = TulparTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        AddressRow(
            dotColor = colors.lime,
            label = "ОТКУДА",
            text = "Мәңгілік Алау аллеясы",
            textColor = colors.fg,
        )
        Box(
            modifier = Modifier
                .padding(start = 9.dp)
                .size(width = 2.dp, height = 18.dp)
                .background(colors.line),
        )
        AddressRow(
            dotColor = colors.fg,
            label = "КУДА",
            text = dest?.addr ?: "Укажите адрес назначения",
            textColor = if (dest != null) colors.fg else colors.fg3,
        )

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickChip(Icons.Filled.Home, "Дом", Modifier.weight(1f)) { onPick(QuickHome) }
            QuickChip(Icons.Filled.Work, "Работа", Modifier.weight(1f)) { onPick(QuickWork) }
            QuickChip(Icons.Filled.Flight, "Порт", Modifier.weight(1f)) { onPick(QuickAirport) }
        }

        Spacer(Modifier.height(11.dp))
        FieldRow(icon = { Icon(Icons.Outlined.Info, null, tint = colors.fg3, modifier = Modifier.size(19.dp)) }) {
            Text("Комментарий водителю", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.fg3)
        }

        Spacer(Modifier.height(9.dp))
        FieldRow(icon = { Text("₸", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.fg2, modifier = Modifier.width(19.dp)) }) {
            Text(
                text = dest?.price ?: "Ваша цена",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (dest != null) colors.fg else colors.fg3,
                modifier = Modifier.weight(1f),
            )
            if (dest != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.limeGlow)
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                ) {
                    Text("рекомендуем", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.limeDeep)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onOrder,
            enabled = dest != null,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.lime,
                contentColor = colors.onLime,
                disabledContainerColor = colors.field,
                disabledContentColor = colors.fg3,
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(
                text = if (dest != null) "Заказать такси" else "Выберите адрес",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.2).sp,
            )
        }
    }
}

@Composable
private fun AddressRow(dotColor: Color, label: String, text: String, textColor: Color) {
    val colors = TulparTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.fg3, letterSpacing = 0.4.sp)
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1)
        }
        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.fg3, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun QuickChip(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = TulparTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = colors.fg, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.fg)
    }
}

@Composable
private fun FieldRow(icon: @Composable () -> Unit, content: @Composable () -> Unit) {
    val colors = TulparTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.field)
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        icon()
        content()
    }
}

@Composable
private fun OrderingState(dest: QuickAddress, onCancel: () -> Unit) {
    val colors = TulparTheme.colors
    val infinite = rememberInfiniteTransition(label = "radar")
    val r1 by infinite.animateFloat(
        initialValue = 0.2f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "r1",
    )
    val r1Alpha by infinite.animateFloat(
        initialValue = 0.7f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "r1a",
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(r1)
                    .clip(CircleShape)
                    .background(colors.lime.copy(alpha = r1Alpha * 0.3f)),
            )
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(colors.lime),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.tulpar_mark_black),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Ищем ближайшего водителя…", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = colors.fg, letterSpacing = (-0.2).sp)
        Spacer(Modifier.height(6.dp))
        Text("${dest.addr} · ${dest.price}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.fg2)
        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Отменить заказ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.fg2)
        }
    }
}
