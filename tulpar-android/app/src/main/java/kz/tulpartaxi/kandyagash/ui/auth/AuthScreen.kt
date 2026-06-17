package kz.tulpartaxi.kandyagash.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kz.tulpartaxi.kandyagash.R
import kz.tulpartaxi.kandyagash.ui.theme.TulparBlack
import kz.tulpartaxi.kandyagash.ui.theme.TulparGray
import kz.tulpartaxi.kandyagash.ui.theme.TulparGrayLight
import kz.tulpartaxi.kandyagash.ui.theme.TulparLime
import kz.tulpartaxi.kandyagash.ui.theme.TulparOnLime
import kz.tulpartaxi.kandyagash.ui.theme.TulparSurface

@Composable
fun AuthScreen(
    onSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateToMain by viewModel.navigateToMain.collectAsStateWithLifecycle()

    LaunchedEffect(navigateToMain) {
        if (navigateToMain) onSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TulparBlack)
            .imePadding(),
    ) {
        // Верхняя часть — логотип
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.logo_nontext),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "TULPAR Taxi",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = kz.tulpartaxi.kandyagash.ui.theme.TulparWhite,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "НАРОДНОЕ ТАКСИ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TulparGray,
                    letterSpacing = 3.sp,
                )
            }
        }

        // Нижняя карточка — форма
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(TulparSurface)
                .padding(28.dp),
        ) {
            when (uiState.step) {
                AuthStep.PHONE -> PhoneStep(
                    phoneDigits = uiState.phoneDigits,
                    dialCode = uiState.phoneDialCode,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onPhoneDigitsChange = viewModel::onPhoneDigitsChange,
                    onNext = viewModel::sendSms,
                )
                AuthStep.CODE -> CodeStep(
                    phone = uiState.phone,
                    code = uiState.code,
                    name = uiState.name,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onCodeChange = viewModel::onCodeChange,
                    onNameChange = viewModel::onNameChange,
                    onConfirm = viewModel::verifyCode,
                    onBack = viewModel::backToPhone,
                )
            }
        }
    }
}

@Composable
private fun PhoneStep(
    phoneDigits: String,
    dialCode: String,
    isLoading: Boolean,
    error: String?,
    onPhoneDigitsChange: (String, String) -> Unit,
    onNext: () -> Unit,
) {
    // Держим выбранную страну локально чтобы передавать dialCode при изменении цифр
    val context = androidx.compose.ui.platform.LocalContext.current
    val initialCountry = remember { detectCountryFromSim(context) }
    var currentDialCode by remember { mutableStateOf(initialCountry.dialCode) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Введите номер",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = kz.tulpartaxi.kandyagash.ui.theme.TulparWhite,
        )
        Text(
            text = "Отправим код подтверждения",
            fontSize = 14.sp,
            color = TulparGray,
        )
        PhoneInputField(
            digits = phoneDigits,
            onDigitsChange = { digits ->
                onPhoneDigitsChange(digits, currentDialCode)
            },
            onNext = onNext,
            isError = error != null,
        )
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        TulparButton(text = "Получить код", isLoading = isLoading, onClick = onNext)
    }
}

@Composable
private fun CodeStep(
    phone: String,
    code: String,
    name: String,
    isLoading: Boolean,
    error: String?,
    onCodeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = TulparLime)
            }
            Spacer(Modifier.size(4.dp))
            Column {
                Text(
                    text = "Код из SMS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = kz.tulpartaxi.kandyagash.ui.theme.TulparWhite,
                )
                Text(text = phone, fontSize = 13.sp, color = TulparGray)
            }
        }
        TulparTextField(
            value = code,
            onValueChange = { if (it.length <= 6) onCodeChange(it) },
            placeholder = "• • • •",
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Next,
            onImeAction = {},
        )
        TulparTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Ваше имя (необязательно)",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            onImeAction = onConfirm,
        )
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        TulparButton(text = "Войти", isLoading = isLoading, onClick = onConfirm)
    }
}

@Composable
private fun TulparTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TulparGray) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onNext = { onImeAction() },
        ),
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TulparLime,
            unfocusedBorderColor = TulparGrayLight,
            focusedTextColor = kz.tulpartaxi.kandyagash.ui.theme.TulparWhite,
            unfocusedTextColor = kz.tulpartaxi.kandyagash.ui.theme.TulparWhite,
            cursorColor = TulparLime,
            focusedContainerColor = kz.tulpartaxi.kandyagash.ui.theme.TulparSurface2,
            unfocusedContainerColor = kz.tulpartaxi.kandyagash.ui.theme.TulparSurface2,
        ),
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun TulparButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TulparLime,
            contentColor = TulparOnLime,
            disabledContainerColor = TulparGrayLight,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TulparOnLime, strokeWidth = 2.dp)
        } else {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
