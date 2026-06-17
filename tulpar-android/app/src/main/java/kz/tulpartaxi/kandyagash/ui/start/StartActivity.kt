package kz.tulpartaxi.kandyagash.ui.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kz.tulpartaxi.kandyagash.data.api.TulparApi
import kz.tulpartaxi.kandyagash.data.repository.AuthRepository
import kz.tulpartaxi.kandyagash.ui.auth.AuthActivity
import kz.tulpartaxi.kandyagash.ui.theme.TulparTaxiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StartActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var api: TulparApi

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (!authRepository.isLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            TulparTaxiTheme(darkTheme = false) {
                StartScreen()
            }
        }

        sendFcmTokenToServer()
    }

    private fun sendFcmTokenToServer() {
        val prefs = getSharedPreferences("fcm", Context.MODE_PRIVATE)
        val savedToken = prefs.getString("token", null)
        if (savedToken != null) {
            uploadToken(savedToken)
        } else {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                prefs.edit().putString("token", token).apply()
                uploadToken(token)
            }
        }
    }

    private fun uploadToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { api.updateDeviceId(mapOf("firebaseToken" to token)) }
        }
    }
}
