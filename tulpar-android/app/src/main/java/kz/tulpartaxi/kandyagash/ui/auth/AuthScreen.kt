package kz.tulpartaxi.kandyagash.ui.auth

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kz.tulpartaxi.kandyagash.R
import kz.tulpartaxi.kandyagash.ui.theme.TulparTheme

private const val DIAL_CODE_KZ = "+7"
private const val PHONE_LENGTH = 10
private const val PHONE_MASK = "(XXX) XXX-XX-XX"

@Composable
fun AuthScreen(
    onSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateToMain by viewModel.navigateToMain.collectAsStateWithLifecycle()
    val colors = TulparTheme.colors

    LaunchedEffect(navigateToMain) {
        if (navigateToMain) onSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .imePadding(),
    ) {
        AuthHeader()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                .background(colors.surface)
                .padding(horizontal = 26.dp, vertical = 26.dp),
        ) {
            when (uiState.step) {
                AuthStep.PHONE -> PhoneStep(
                    digits = uiState.phoneDigits,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onDigitsChange = { viewModel.onPhoneDigitsChange(it, DIAL_CODE_KZ) },
                    onSubmit = viewModel::sendSms,
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
private fun AuthHeader() {
    val colors = TulparTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 78.dp, bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.tulpar_mark),
            contentDescription = null,
            modifier = Modifier.size(128.dp),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append("TULPAR ") }
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("Taxi") }
            },
            fontSize = 25.sp,
            color = colors.fg,
            letterSpacing = (-0.4).sp,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = "Н А Р О Д Н О Е   Т А К С И",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = colors.fg3,
            letterSpacing = 3.sp,
        )
    }
}

@Composable
private fun PhoneStep(
    digits: String,
    isLoading: Boolean,
    error: String?,
    onDigitsChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val colors = TulparTheme.colors
    val valid = digits.length >= PHONE_LENGTH

    Column {
        Text("Введите номер", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = colors.fg, letterSpacing = (-0.4).sp)
        Spacer(Modifier.height(7.dp))
        Text("Отправим SMS с кодом подтверждения", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.fg2)

        Spacer(Modifier.height(20.dp))
        PhoneField(digits = digits)

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = colors.error, fontSize = 13.sp)
        }

        Spacer(Modifier.height(18.dp))
        Keypad(
            onDigit = { d ->
                if (digits.length < PHONE_LENGTH) onDigitsChange(digits + d)
            },
            onBackspace = { if (digits.isNotEmpty()) onDigitsChange(digits.dropLast(1)) },
        )

        Spacer(Modifier.height(14.dp))
        CtaButton(
            label = "Получить код",
            valid = valid,
            isLoading = isLoading,
            onClick = onSubmit,
        )

        Spacer(Modifier.height(13.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.fg3, fontWeight = FontWeight.Medium)) {
                    append("Нажимая «Получить код», вы соглашаетесь с\n")
                }
                withStyle(SpanStyle(color = colors.fg2, fontWeight = FontWeight.SemiBold)) {
                    append("условиями оферты и политикой конфиденциальности")
                }
            },
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
        )
    }
}

@Composable
private fun PhoneField(digits: String) {
    val colors = TulparTheme.colors

    // Build masked typed/rest strings
    val typedBuilder = StringBuilder()
    val restBuilder = StringBuilder()
    var di = 0
    var filling = true
    for (ch in PHONE_MASK) {
        if (ch == 'X') {
            if (di < digits.length) {
                typedBuilder.append(digits[di]); di++
            } else {
                filling = false
                restBuilder.append('_')
            }
        } else {
            if (filling) typedBuilder.append(ch) else restBuilder.append(ch)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.limeGlow)
            .background(colors.field, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // KZ flag (simplified)
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 17.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF15B5C9)),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(Color(0xFFFFD200)))
        }
        Spacer(Modifier.width(9.dp))
        Text(DIAL_CODE_KZ, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.fg)
        Spacer(Modifier.width(13.dp))
        Box(modifier = Modifier.width(1.dp).height(28.dp).background(colors.line))
        Spacer(Modifier.width(13.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.fg, fontWeight = FontWeight.Bold)) { append(typedBuilder.toString()) }
                withStyle(SpanStyle(color = colors.fg3, fontWeight = FontWeight.SemiBold)) { append(restBuilder.toString()) }
            },
            fontSize = 19.sp,
        )
    }
}

@Composable
private fun Keypad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    val colors = TulparTheme.colors
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "<"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                when (label) {
                                    "" -> Modifier
                                    "<" -> Modifier.clickable(onClick = onBackspace)
                                    else -> Modifier.clickable { onDigit(label) }
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (label) {
                            "" -> {}
                            "<" -> Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Стереть",
                                tint = colors.fg,
                                modifier = Modifier.size(24.dp),
                            )
                            else -> Text(label, fontSize = 25.sp, fontWeight = FontWeight.SemiBold, color = colors.fg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CtaButton(label: String, valid: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    val colors = TulparTheme.colors
    Button(
        onClick = onClick,
        enabled = valid && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.lime,
            contentColor = colors.onLime,
            disabledContainerColor = colors.field,
            disabledContentColor = colors.fg3,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.onLime, strokeWidth = 2.dp)
        } else {
            Text(label, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.2).sp)
        }
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
    val colors = TulparTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = colors.fg)
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text("Код из SMS", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = colors.fg)
                Text(phone, fontSize = 13.sp, color = colors.fg2)
            }
        }
        FilledField(
            value = code,
            onValueChange = { if (it.length <= 6) onCodeChange(it) },
            placeholder = "• • • •",
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Next,
            onImeAction = {},
        )
        FilledField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Ваше имя (необязательно)",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            onImeAction = onConfirm,
        )
        if (error != null) Text(error, color = colors.error, fontSize = 13.sp)
        CtaButton(label = "Войти", valid = code.length >= 4, isLoading = isLoading, onClick = onConfirm)
    }
}

@Composable
private fun FilledField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
) {
    val colors = TulparTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = colors.fg3) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onImeAction() }, onNext = { onImeAction() }),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.lime,
            unfocusedBorderColor = colors.line,
            focusedTextColor = colors.fg,
            unfocusedTextColor = colors.fg,
            cursorColor = colors.lime,
            focusedContainerColor = colors.field,
            unfocusedContainerColor = colors.field,
        ),
    )
}
