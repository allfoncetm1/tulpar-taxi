package kz.tulpartaxi.driver.ui.start

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kz.tulpartaxi.driver.data.api.model.OrderDto
import kz.tulpartaxi.driver.ui.theme.TulparBlack
import kz.tulpartaxi.driver.ui.theme.TulparGray
import kz.tulpartaxi.driver.ui.theme.TulparGrayLight
import kz.tulpartaxi.driver.ui.theme.TulparLime
import kz.tulpartaxi.driver.ui.theme.TulparOnLime
import kz.tulpartaxi.driver.ui.theme.TulparSurface
import kz.tulpartaxi.driver.ui.theme.TulparSurface2
import kz.tulpartaxi.driver.ui.theme.TulparWhite

@Composable
fun DriverScreen(viewModel: DriverViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TulparBlack)
            .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
    ) {
        TopHeader(
            isOnline = state.isOnline,
            balance = state.balance,
            userName = state.userName,
            onToggleOnline = viewModel::toggleOnline,
        )

        Spacer(Modifier.height(20.dp))

        val active = state.activeOrder
        when {
            active != null -> ActiveOrderCard(
                order = active,
                isLoading = state.isLoading,
                onAction = viewModel::setAction,
            )
            !state.isOnline -> OfflineEmpty()
            state.pendingOrders.isEmpty() -> SearchingEmpty()
            else -> PendingOrdersList(
                orders = state.pendingOrders,
                isLoading = state.isLoading,
                onAccept = viewModel::acceptOrder,
            )
        }

        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFCEBEB))
                    .clickable { viewModel.dismissError() }
                    .padding(12.dp),
            ) { Text(err, color = Color(0xFFA32D2D), fontSize = 13.sp) }
        }
    }
}

@Composable
private fun TopHeader(
    isOnline: Boolean,
    balance: Int,
    userName: String?,
    onToggleOnline: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(TulparSurface2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = TulparGray, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(userName ?: "Водитель", color = TulparWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("$balance ₸", color = TulparLime, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (isOnline) "Онлайн" else "Оффлайн",
                color = if (isOnline) TulparLime else TulparGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = isOnline,
                onCheckedChange = { onToggleOnline() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TulparBlack,
                    checkedTrackColor = TulparLime,
                    uncheckedThumbColor = TulparGray,
                    uncheckedTrackColor = TulparSurface2,
                ),
            )
        }
    }
}

@Composable
private fun OfflineEmpty() {
    EmptyState(
        title = "Вы оффлайн",
        subtitle = "Включите режим онлайн, чтобы получать заказы",
    )
}

@Composable
private fun SearchingEmpty() {
    EmptyState(
        title = "Ищем заказы...",
        subtitle = "Новые заказы появятся здесь автоматически",
        showSpinner = true,
    )
}

@Composable
private fun EmptyState(title: String, subtitle: String, showSpinner: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showSpinner) {
                CircularProgressIndicator(color = TulparLime, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(20.dp))
            }
            Text(title, color = TulparWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = TulparGray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PendingOrdersList(
    orders: List<OrderDto>,
    isLoading: Boolean,
    onAccept: (String) -> Unit,
) {
    Column {
        Text(
            "Доступные заказы (${orders.size})",
            color = TulparGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(orders, key = { it.id }) { order ->
                OrderCard(order = order, isLoading = isLoading, onAccept = { onAccept(order.id) })
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderDto, isLoading: Boolean, onAccept: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TulparSurface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TulparLime))
            Spacer(Modifier.width(10.dp))
            Text(
                order.fromAddress ?: "Точка А",
                color = TulparWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TulparWhite))
            Spacer(Modifier.width(10.dp))
            Text(
                order.toAddress ?: "Точка Б",
                color = TulparWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
        }

        if (!order.comment.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text("«${order.comment}»", color = TulparGray, fontSize = 12.sp)
        }
        if (!order.door.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text("Подъезд: ${order.door}", color = TulparGray, fontSize = 12.sp)
        }

        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TulparGrayLight))
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            order.estimatedPrice?.let { price ->
                Text(
                    "${price.toInt()} ₸",
                    color = TulparLime,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onAccept,
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TulparLime,
                    contentColor = TulparOnLime,
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TulparOnLime, strokeWidth = 2.dp)
                } else {
                    Text("Принять", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ActiveOrderCard(
    order: OrderDto,
    isLoading: Boolean,
    onAction: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TulparSurface)
            .padding(20.dp),
    ) {
        Text("Активный заказ", color = TulparLime, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TulparLime, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Откуда", color = TulparGray, fontSize = 10.sp)
                Text(order.fromAddress ?: "—", color = TulparWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TulparWhite, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Куда", color = TulparGray, fontSize = 10.sp)
                Text(order.toAddress ?: "—", color = TulparWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2)
            }
        }

        if (!order.comment.isNullOrBlank() || !order.door.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TulparGrayLight))
            Spacer(Modifier.height(10.dp))
            order.door?.takeIf { it.isNotBlank() }?.let {
                Text("Подъезд: $it", color = TulparGray, fontSize = 12.sp)
            }
            order.comment?.takeIf { it.isNotBlank() }?.let {
                Text("«$it»", color = TulparGray, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        val (primaryLabel, primaryAction) = when (order.status) {
            "accepted" -> "Я приехал" to "arrived"
            "arrived" -> "Начать поездку" to "start"
            "in_progress" -> "Завершить" to "complete"
            else -> "Завершить" to "complete"
        }

        Button(
            onClick = { onAction(primaryAction) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TulparLime, contentColor = TulparOnLime),
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TulparOnLime, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(primaryLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { onAction("cancel") },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("Отменить", color = TulparGray, fontSize = 13.sp)
        }
    }
}
