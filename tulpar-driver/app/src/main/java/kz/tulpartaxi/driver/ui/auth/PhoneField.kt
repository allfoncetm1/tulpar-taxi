package kz.tulpartaxi.driver.ui.auth

import android.content.Context
import android.telephony.TelephonyManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.tulpartaxi.driver.ui.theme.TulparGray
import kz.tulpartaxi.driver.ui.theme.TulparGrayLight
import kz.tulpartaxi.driver.ui.theme.TulparLime
import kz.tulpartaxi.driver.ui.theme.TulparSurface2
import kz.tulpartaxi.driver.ui.theme.TulparWhite

data class PhoneCountry(
    val iso: String,       // "kz"
    val flag: String,      // "🇰🇿"
    val dialCode: String,  // "+7"
    val mask: String,      // "(___) ___-__-__"  только цифры-заполнители
    val digitCount: Int,   // сколько цифр без кода
)

val COUNTRIES = listOf(
    PhoneCountry("kz", "🇰🇿", "+7",  "(___) ___-__-__", 10),
    PhoneCountry("ru", "🇷🇺", "+7",  "(___) ___-__-__", 10),
    PhoneCountry("uz", "🇺🇿", "+998","(__) ___-__-__",  9),
    PhoneCountry("kg", "🇰🇬", "+996","(___) ___-___",   9),
    PhoneCountry("tr", "🇹🇷", "+90", "(___)___-__-__",  10),
)

fun detectCountryFromSim(context: Context): PhoneCountry {
    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    val iso = tm?.simCountryIso?.lowercase() ?: tm?.networkCountryIso?.lowercase() ?: "kz"
    return COUNTRIES.firstOrNull { it.iso == iso } ?: COUNTRIES.first()
}

// Применяет маску к введённым цифрам: "7771234567" → "(777) 123-45-67"
fun applyMask(digits: String, mask: String): String {
    var di = 0
    val sb = StringBuilder()
    for (ch in mask) {
        if (di >= digits.length) break
        if (ch == '_') {
            sb.append(digits[di++])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

// Строит подсказку-плейсхолдер по маске
fun maskPlaceholder(mask: String) = mask  // маска и есть плейсхолдер

@Composable
fun PhoneInputField(
    digits: String,            // только цифры без кода страны
    onDigitsChange: (String) -> Unit,
    onNext: () -> Unit,
    isError: Boolean = false,
) {
    val context = LocalContext.current
    var country by remember { mutableStateOf(detectCountryFromSim(context)) }
    var focused by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }

    val maskedValue = applyMask(digits, country.mask)
    val placeholder = maskPlaceholder(country.mask)
    val borderColor = when {
        isError -> androidx.compose.ui.graphics.Color(0xFFFF5252)
        focused -> TulparLime
        else -> TulparGrayLight
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(TulparSurface2, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Выбор страны — флаг + код
        Box {
            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showPicker = true }
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(country.flag, fontSize = 20.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = country.dialCode,
                    color = TulparWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }

            DropdownMenu(
                expanded = showPicker,
                onDismissRequest = { showPicker = false },
            ) {
                COUNTRIES.forEach { c ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(c.flag, fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text("${c.dialCode}  ${c.iso.uppercase()}", fontSize = 15.sp)
                            }
                        },
                        onClick = {
                            country = c
                            onDigitsChange("")   // сброс при смене страны
                            showPicker = false
                        },
                    )
                }
            }
        }

        // Разделитель
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(TulparGrayLight),
        )
        Spacer(Modifier.width(12.dp))

        // Ввод цифр с маской
        Box(modifier = Modifier.weight(1f)) {
            // Плейсхолдер
            if (digits.isEmpty()) {
                Text(placeholder, color = TulparGray, fontSize = 16.sp)
            }
            // Маскированный текст поверх
            if (digits.isNotEmpty()) {
                Text(maskedValue, color = TulparWhite, fontSize = 16.sp)
            }
            // Невидимое поле для ввода
            BasicTextField(
                value = digits,
                onValueChange = { raw ->
                    val newDigits = raw.filter { it.isDigit() }.take(country.digitCount)
                    onDigitsChange(newDigits)
                },
                modifier = Modifier
                    .matchParentSize()
                    .onFocusChanged { focused = it.isFocused },
                textStyle = TextStyle(color = androidx.compose.ui.graphics.Color.Transparent, fontSize = 16.sp),
                cursorBrush = SolidColor(TulparLime),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onNext() }),
                singleLine = true,
            )
        }
    }
}
