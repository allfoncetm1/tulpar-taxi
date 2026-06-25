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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.graphics.Color
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
    var locationPermissionDenied by remember { mutableStateOf(false) }
    var deniedMapMode by remember { mutableStateOf<String?>(null) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        pendingMapMode?.let { mode ->
            if (granted) {
                openMapWithLocation(context, fusedLocation, mode, viewModel)
            } else {
                deniedMapMode = mode
                locationPermissionDenied = true
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

    fun retryPermissionRequest() {
        deniedMapMode?.let {
            pendingMapMode = it
            deniedMapMode = null
            locationPermissionDenied = false
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

    var mapLoadError by remember { mutableStateOf(false) }

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
            mapLoadError = mapLoadError,
            onMapLoadError = { mapLoadError = true },
            onRetryMap = { mapLoadError = false },
        )

        if (locationPermissionDenied) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { locationPermissionDenied = false },
                title = { Text("Разрешение на геолокацию") },
                text = { Text("Чтобы выбрать адрес на карте, разрешите доступ к геолокации.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { retryPermissionRequest() }) {
                        Text("Разрешить")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { locationPermissionDenied = false }) {
                        Text("Отмена")
                    }
                },
                containerColor = TulparSurface,
                titleContentColor = TulparWhite,
                textContentColor = TulparGray,
            )
        }

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
    mapLoadError: Boolean,
    onMapLoadError: () -> Unit,
    onRetryMap: () -> Unit,
) {
    var mapLoaded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Фоновая карта
        MapWebView(
            url = "${StaticConfig.mapBaseUrl()}/citymap",
            modifier = Modifier.fillMaxSize(),
            onPageLoaded = { mapLoaded = true },
            onError = onMapLoadError,
            stateKey = if (mapLoadError) 1 else 0,
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                onProfile = onProfile,
                onHistory = onHistory,
                onInfo = onInfo,
            )
            Spacer(modifier = Modifier.weight(1f))
            OrderBottomSheet(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                formState = formState,
                onFromTap = onFromTap,
                onToTap = onToTap,
                onPriceChange = onPriceChange,
                onDoorChange = onDoorChange,
                onCommentChange = onCommentChange,
                onOrder = onOrder,
            )
        }

        if (mapLoadError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TulparBlack.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(TulparSurface)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Не удалось загрузить карту",
                        color = TulparWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Проверьте интернет-соединение и попробуйте ещё раз.",
                        color = TulparGray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            onRetryMap()
                            mapLoaded = false
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TulparLime, contentColor = TulparOnLime),
                        modifier = Modifier.fillMaxWidth(0.66f),
                    ) {
                        Text("Повторить", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (!mapLoaded && !mapLoadError) {
            SplashScreen()
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
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(TulparSurface.copy(alpha = 0.92f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TopBarIcon(onClick = onProfile) {
                Icon(Icons.Default.Person, contentDescription = "Профиль", tint = TulparWhite, modifier = Modifier.size(20.dp))
            }
            TopBarIcon(onClick = onHistory, background = TulparLime) {
                Icon(Icons.Default.History, contentDescription = "История", tint = TulparBlack, modifier = Modifier.size(20.dp))
            }
            TopBarIcon(onClick = onInfo) {
                Icon(Icons.Default.Info, contentDescription = "Информация", tint = TulparWhite, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun TopBarIcon(
    onClick: () -> Unit,
    background: Color = TulparSurface.copy(alpha = 0.85f),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) { content() }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    iconTint: Color = TulparBlack,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TulparSurface2,
            contentColor = TulparBlack,
        ),
        contentPadding = PaddingValues(horizontal = 10.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconTint)
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(TulparSurface)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TulparGrayLight),
            )
        }

        if (formState.isOrdering) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(TulparLime.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(TulparLime),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_nontext),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp),
                        )
                    }
                }

                Text(
                    text = "Ищем ближайшего водителя…",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TulparBlack,
                )
                Text(
                    text = "${formState.toAddress.ifBlank { "—" }} · ${formState.offeredPrice.ifBlank { "0 ₸" }}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TulparGray,
                )

                Button(
                    onClick = { /* Отмена поиска */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TulparSurface2,
                        contentColor = TulparGray,
                    ),
                ) {
                    Text("Отменить заказ", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        } else {
            Text(
                text = "Заказать поездку",
                style = MaterialTheme.typography.titleLarge,
                color = TulparBlack,
            )

            AddressTapField(
                value = formState.fromAddress,
                placeholder = "Откуда",
                dotColor = TulparLime,
                onTap = onFromTap,
            )
            Spacer(Modifier.height(12.dp))
            AddressTapField(
                value = formState.toAddress,
                placeholder = "Куда",
                dotColor = TulparWhite,
                onTap = onToTap,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Home,
                    label = "Дом",
                    iconTint = TulparLime,
                    onClick = onFromTap,
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Work,
                    label = "Работа",
                    iconTint = TulparLime,
                    onClick = onFromTap,
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Flight,
                    label = "Порт",
                    iconTint = TulparLime,
                    onClick = onFromTap,
                )
            }

            SimpleInputField(
                value = formState.door,
                onValueChange = onDoorChange,
                placeholder = "Подъезд",
                icon = Icons.Default.LocationOn,
                textColor = TulparBlack,
                placeholderColor = TulparGray,
                backgroundColor = TulparSurface2,
            )

            SimpleInputField(
                value = formState.comment,
                onValueChange = onCommentChange,
                placeholder = "Комментарий водителю",
                icon = Icons.Default.Info,
                textColor = TulparBlack,
                placeholderColor = TulparGray,
                backgroundColor = TulparSurface2,
            )

            PriceInputField(
                value = formState.offeredPrice,
                onValueChange = onPriceChange,
                hasDestination = formState.toAddress.isNotBlank(),
            )

            if (formState.error != null) {
                Text(formState.error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Button(
                onClick = onOrder,
                enabled = !formState.isOrdering &&
                    formState.fromAddress.isNotBlank() &&
                    formState.toAddress.isNotBlank() &&
                    formState.offeredPrice.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TulparLime,
                    contentColor = TulparOnLime,
                    disabledContainerColor = TulparGrayLight,
                    disabledContentColor = TulparGray,
                ),
            ) {
                Text("Заказать", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun OrderCardItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    dotColor: androidx.compose.ui.graphics.Color,
    onTap: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TulparSurface2)
            .clickable(onClick = onTap)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Spacer(Modifier.width(8.dp))
            Text(title, color = TulparGray, fontSize = 13.sp)
        }
        Text(value, color = TulparBlack, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 2)
    }
}

@Composable
private fun CompactInfoCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    value: String,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TulparSurface2)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = TulparLime, modifier = Modifier.size(20.dp))
        Column {
            Text(text, color = TulparGray, fontSize = 12.sp)
            Text(value, color = TulparBlack, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SimpleInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    textColor: androidx.compose.ui.graphics.Color = TulparWhite,
    placeholderColor: androidx.compose.ui.graphics.Color = TulparGray,
    backgroundColor: androidx.compose.ui.graphics.Color = TulparSurface2,
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = TulparGray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(color = textColor, fontSize = 14.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(TulparLime),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                ),
                decorationBox = { inner ->
                    if (value.isBlank()) Text(placeholder, color = placeholderColor, fontSize = 14.sp)
                    inner()
                },
            )
        }
    }
}

@Composable
private fun PriceInputField(value: String, onValueChange: (String) -> Unit, hasDestination: Boolean) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TulparSurface2)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "₸",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (value.isBlank()) TulparGray else TulparLime,
            )
            Spacer(Modifier.width(12.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TulparBlack,
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
            if (hasDestination) {
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TulparLime.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        "рекомендуем",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TulparBlack,
                    )
                }
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
            .clip(RoundedCornerShape(16.dp))
            .background(TulparSurface2)
            .clickable(onClick = onTap)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = value.ifBlank { placeholder },
            color = if (value.isBlank()) TulparGray else TulparBlack,
            fontSize = 15.sp,
            fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = TulparGray,
            modifier = Modifier.size(20.dp),
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
