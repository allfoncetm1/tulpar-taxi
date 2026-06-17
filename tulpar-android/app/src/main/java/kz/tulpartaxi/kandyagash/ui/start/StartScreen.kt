package kz.tulpartaxi.kandyagash.ui.start

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kz.tulpartaxi.kandyagash.R
import kz.tulpartaxi.kandyagash.ui.history.HistoryActivity
import kz.tulpartaxi.kandyagash.ui.info.InfoActivity
import kz.tulpartaxi.kandyagash.ui.map.MapWebView
import kz.tulpartaxi.kandyagash.ui.profile.ProfileActivity
import kz.tulpartaxi.kandyagash.ui.theme.TulparBlack
import kz.tulpartaxi.kandyagash.ui.theme.TulparGray
import kz.tulpartaxi.kandyagash.ui.theme.TulparGrayLight
import kz.tulpartaxi.kandyagash.ui.theme.TulparLime
import kz.tulpartaxi.kandyagash.ui.theme.TulparOnLime
import kz.tulpartaxi.kandyagash.ui.theme.TulparSurface
import kz.tulpartaxi.kandyagash.ui.theme.TulparSurface2
import kz.tulpartaxi.kandyagash.ui.theme.TulparWhite
import kz.tulpartaxi.kandyagash.utils.StaticConfig

@Composable
fun StartScreen(
    viewModel: StartViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    var pendingMapMode by remember { mutableStateOf<String?>(null) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        pendingMapMode?.let { mode ->
            if (granted) {
                openMapWithLocation(context, fusedLocation, mode, viewModel)
            } else {
                if (mode == "from") viewModel.openMapForFrom() else viewModel.openMapForTo()
            }
            pendingMapMode = null
        }
    }

    fun openMapWithPermission(mode: String) {
        val ok = android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasLocation = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == ok ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == ok
        if (hasLocation) {
            openMapWithLocation(context, fusedLocation, mode, viewModel)
        } else {
            pendingMapMode = mode
            locationLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ))
        }
    }

    // Диалог успешного заказа
    if (formState.orderSuccess) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = viewModel::dismissOrder,
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = viewModel::dismissOrder) {
                    Text("OK", color = TulparLime, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Заказ создан!", fontWeight = FontWeight.Bold) },
            text = { Text("Ищем водителя поблизости...\n#${formState.orderId?.take(8)?.uppercase()}") },
            containerColor = TulparSurface,
            titleContentColor = TulparWhite,
            textContentColor = TulparGray,
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(TulparBlack)) {

        // Основной экран с картой и панелью заказа
        MainOrderView(
            formState = formState,
            onFromTap = { openMapWithPermission("from") },
            onToTap = { openMapWithPermission("to") },
            onPriceChange = viewModel::updatePrice,
            onDoorChange = viewModel::updateDoor,
            onCommentChange = viewModel::updateComment,
            onOrder = viewModel::createOrder,
            onProfile = { context.startActivity(Intent(context, ProfileActivity::class.java)) },
            onHistory = { context.startActivity(Intent(context, HistoryActivity::class.java)) },
            onInfo = { context.startActivity(Intent(context, InfoActivity::class.java)) },
        )

        // Полноэкранная карта для выбора точки
        val mapMode = formState.mapMode
        if (mapMode != null) {
            BackHandler { viewModel.closeMap() }
            MapSelectionOverlay(
                mode = mapMode,
                baseUrl = StaticConfig.mapBaseUrl(),
                userLat = formState.userLat,
                userLng = formState.userLng,
                onPointSelected = viewModel::onPointSelected,
                onBack = viewModel::closeMap,
            )
        }

        // Оценка водителя после завершения поездки
        val ratingId = formState.ratingOrderId
        if (formState.showRating && ratingId != null) {
            BackHandler { viewModel.skipRating() }
            RatingScreen(
                orderId = ratingId,
                onSubmit = viewModel::submitRating,
                onSkip = viewModel::skipRating,
            )
        }
    }
}

@Composable
private fun MainOrderView(
    formState: OrderFormState,
    onFromTap: () -> Unit,
    onToTap: () -> Unit,
    onPriceChange: (String) -> Unit,
    onDoorChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onOrder: () -> Unit,
    onProfile: () -> Unit,
    onHistory: () -> Unit,
    onInfo: () -> Unit,
) {
    var mapLoaded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Фоновая карта
        MapWebView(
            url = "${StaticConfig.mapBaseUrl()}/citymap",
            modifier = Modifier.fillMaxSize(),
            onPageLoaded = { mapLoaded = true },
        )

        // Топ-бар поверх карты
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            onProfile = onProfile,
            onHistory = onHistory,
            onInfo = onInfo,
        )

        // Нижняя панель заказа
        OrderBottomSheet(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .imePadding(),
            formState = formState,
            onFromTap = onFromTap,
            onToTap = onToTap,
            onPriceChange = onPriceChange,
            onDoorChange = onDoorChange,
            onCommentChange = onCommentChange,
            onOrder = onOrder,
        )

        // Поверх всего: загрузочный экран пока карта не готова
        if (!mapLoaded) {
            LoadingScreen()
        }
    }
}

@Composable
private fun MapSelectionOverlay(
    mode: String,
    baseUrl: String,
    userLat: Double?,
    userLng: Double?,
    onPointSelected: (String, Double, Double, String) -> Unit,
    onBack: () -> Unit,
) {
    val locationParams = if (userLat != null && userLng != null) "&lat=$userLat&lng=$userLng" else ""
    Box(modifier = Modifier.fillMaxSize()) {
        MapWebView(
            url = "$baseUrl/citymap?mode=$mode$locationParams",
            modifier = Modifier.fillMaxSize(),
            onPointSelected = onPointSelected,
        )

        // Кнопка назад поверх карты
        Box(
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(TulparSurface.copy(alpha = 0.9f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = TulparWhite,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    onProfile: () -> Unit,
    onHistory: () -> Unit,
    onInfo: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.logo_nontext),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text("TULPAR", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TulparWhite, letterSpacing = 1.sp)
                Text("Кандыагаш", fontSize = 10.sp, color = TulparLime, letterSpacing = 0.5.sp)
            }
        }

        Row {
            TopBarIcon(onClick = onProfile) { Icon(Icons.Default.Person, contentDescription = null, tint = TulparWhite, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(8.dp))
            TopBarIcon(onClick = onHistory) { Icon(Icons.Default.History, contentDescription = null, tint = TulparWhite, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(8.dp))
            TopBarIcon(onClick = onInfo) { Icon(Icons.Default.Info, contentDescription = null, tint = TulparWhite, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun TopBarIcon(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(TulparSurface.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) { content() }
    }
}

@Composable
private fun OrderBottomSheet(
    modifier: Modifier = Modifier,
    formState: OrderFormState,
    onFromTap: () -> Unit,
    onToTap: () -> Unit,
    onPriceChange: (String) -> Unit,
    onDoorChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onOrder: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(TulparSurface)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Ручка
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(TulparGrayLight)
                .align(Alignment.CenterHorizontally),
        )

        // Откуда
        AddressTapField(value = formState.fromAddress, placeholder = "Откуда", dotColor = TulparLime, onTap = onFromTap)

        Box(modifier = Modifier.padding(start = 22.dp).fillMaxWidth().height(1.dp).background(TulparGrayLight))

        // Куда
        AddressTapField(value = formState.toAddress, placeholder = "Куда", dotColor = TulparWhite, onTap = onToTap)

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TulparGrayLight))

        // Подъезд
        if (formState.fromAddress.isNotBlank()) {
            SimpleInputField(
                value = formState.door,
                onValueChange = onDoorChange,
                placeholder = "Подъезд / квартира (необяз.)",
                icon = Icons.Default.LocationOn,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            )
        }

        // Комментарий
        SimpleInputField(
            value = formState.comment,
            onValueChange = onCommentChange,
            placeholder = "Комментарий водителю (необяз.)",
            icon = Icons.Default.Info,
        )

        // Поле цены
        PriceInputField(
            value = formState.offeredPrice,
            onValueChange = onPriceChange,
        )

        if (formState.error != null) {
            Text(formState.error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        // Кнопка заказа
        Button(
            onClick = onOrder,
            enabled = !formState.isOrdering &&
                formState.fromAddress.isNotBlank() &&
                formState.toAddress.isNotBlank() &&
                formState.offeredPrice.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TulparLime,
                contentColor = TulparOnLime,
                disabledContainerColor = TulparGrayLight,
                disabledContentColor = TulparGray,
            ),
        ) {
            if (formState.isOrdering) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TulparOnLime, strokeWidth = 2.dp)
            } else {
                Text("Заказать такси", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun SimpleInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TulparSurface2),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = TulparGray, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(color = TulparWhite, fontSize = 14.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(TulparLime),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                ),
                decorationBox = { inner ->
                    if (value.isBlank()) Text(placeholder, color = TulparGray, fontSize = 14.sp)
                    inner()
                },
            )
        }
    }
}

@Composable
private fun PriceInputField(value: String, onValueChange: (String) -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TulparSurface2),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Иконка монеты
            Text("₸", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = if (value.isBlank()) TulparGray else TulparLime)
            Spacer(Modifier.width(10.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TulparWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(TulparLime),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                ),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text("Ваша цена", color = TulparGray, fontSize = 16.sp, fontWeight = FontWeight.Normal)
                    }
                    inner()
                },
            )
            if (value.isNotBlank()) {
                Text(" ₸", fontSize = 16.sp, color = TulparGray)
            }
        }
    }
}

@Composable
private fun AddressTapField(
    value: String,
    placeholder: String,
    dotColor: androidx.compose.ui.graphics.Color,
    onTap: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onTap)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value.ifBlank { placeholder },
            color = if (value.isBlank()) TulparGray else TulparWhite,
            fontSize = 15.sp,
            fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = TulparGray,
            modifier = Modifier.size(18.dp),
        )
    }
}

@SuppressLint("MissingPermission")
private fun openMapWithLocation(
    context: android.content.Context,
    fusedLocation: com.google.android.gms.location.FusedLocationProviderClient,
    mode: String,
    viewModel: StartViewModel,
) {
    val open = { lat: Double?, lng: Double? ->
        if (mode == "from") viewModel.openMapForFrom(lat, lng)
        else viewModel.openMapForTo(lat, lng)
    }
    // Сначала пробуем кешированную локацию (мгновенно)
    fusedLocation.lastLocation
        .addOnSuccessListener { loc ->
            if (loc != null) open(loc.latitude, loc.longitude)
            else {
                // Нет кеша — запрашиваем свежую
                fusedLocation.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { fresh -> open(fresh?.latitude, fresh?.longitude) }
                    .addOnFailureListener { open(null, null) }
            }
        }
        .addOnFailureListener { open(null, null) }
}
