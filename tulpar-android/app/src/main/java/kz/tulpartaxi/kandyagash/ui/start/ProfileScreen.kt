package kz.tulpartaxi.kandyagash.ui.start

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.tulpartaxi.kandyagash.R
import kz.tulpartaxi.kandyagash.ui.theme.TulparTheme

@Composable
fun ProfileScreen(
    name: String = "Айдос Серіков",
    phone: String = "+7 (777) 123-45-67",
    initials: String = "АС",
    rating: String = "4.9",
    trips: String = "248",
    bonuses: String = "1 250 ₸",
    yearsInApp: String = "2 года",
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onPayment: () -> Unit = {},
    onHistory: () -> Unit = {},
    onAddresses: () -> Unit = {},
    onPromo: () -> Unit = {},
    onSupport: () -> Unit = {},
    onBecomeDriver: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val colors = TulparTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 54.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Header(onBack = onBack, onEdit = onEdit)
        UserCard(name = name, phone = phone, initials = initials, rating = rating)
        StatsRow(trips = trips, bonuses = bonuses, years = yearsInApp)
        MenuCard(
            onPayment = onPayment,
            onHistory = onHistory,
            onAddresses = onAddresses,
            onPromo = onPromo,
            onSupport = onSupport,
        )
        BecomeDriverCard(onClick = onBecomeDriver)
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Выйти из аккаунта",
            color = colors.error,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLogout)
                .padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun Header(onBack: () -> Unit, onEdit: () -> Unit) {
    val colors = TulparTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBackIos, null, tint = colors.fg, modifier = Modifier.size(18.dp))
        }
        Text("Профиль", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = colors.fg, letterSpacing = (-0.2).sp)
        RoundButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, null, tint = colors.fg, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun RoundButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val colors = TulparTheme.colors
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun UserCard(name: String, phone: String, initials: String, rating: String) {
    val colors = TulparTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(colors.lime, Color(0xFFA9CC00)))),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = colors.onLime)
            }
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(colors.lime))
            }
        }
        Spacer(Modifier.width(15.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = colors.fg, maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = (-0.2).sp)
            Spacer(Modifier.height(3.dp))
            Text(phone, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.fg2)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .background(colors.limeGlow)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Star, null, tint = colors.limeDeep, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(rating, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = colors.limeDeep)
        }
    }
}

@Composable
private fun StatsRow(trips: String, bonuses: String, years: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(value = trips, label = "поездок", modifier = Modifier.weight(1f))
        StatTile(value = bonuses, label = "бонусов", highlight = true, modifier = Modifier.weight(1.2f))
        StatTile(value = years, label = "в Tulpar", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    val colors = TulparTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = if (highlight) colors.limeDeep else colors.fg)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.fg3)
    }
}

@Composable
private fun MenuCard(
    onPayment: () -> Unit,
    onHistory: () -> Unit,
    onAddresses: () -> Unit,
    onPromo: () -> Unit,
    onSupport: () -> Unit,
) {
    val colors = TulparTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        MenuRow(Icons.Filled.CreditCard, "Способы оплаты", trailing = "Kaspi Gold", showDivider = true, onClick = onPayment)
        MenuRow(Icons.Filled.History, "История поездок", showDivider = true, onClick = onHistory)
        MenuRow(Icons.Filled.LocationOn, "Сохранённые адреса", trailing = "2", showDivider = true, onClick = onAddresses)
        MenuRow(Icons.Filled.LocalOffer, "Промокоды и бонусы", showDivider = true, onClick = onPromo)
        MenuRow(Icons.AutoMirrored.Filled.Chat, "Поддержка", showDivider = false, onClick = onSupport)
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    trailing: String? = null,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    val colors = TulparTheme.colors
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.field),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = colors.fg, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.fg, modifier = Modifier.weight(1f))
            if (trailing != null) {
                Text(trailing, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.fg3)
                Spacer(Modifier.width(6.dp))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = colors.fg3, modifier = Modifier.size(14.dp))
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
    }
}

@Composable
private fun BecomeDriverCard(onClick: () -> Unit) {
    val colors = TulparTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.lime)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.tulpar_mark_black),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Стать водителем", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.onLime, letterSpacing = (-0.2).sp)
            Spacer(Modifier.height(2.dp))
            Text("Зарабатывайте на Tulpar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.onLime.copy(alpha = 0.7f))
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = colors.onLime, modifier = Modifier.size(16.dp))
    }
}
