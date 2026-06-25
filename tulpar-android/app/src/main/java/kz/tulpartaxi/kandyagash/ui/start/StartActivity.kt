package kz.tulpartaxi.kandyagash.ui.start

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                sendFcmTokenToServer()
            } else {
                sendFcmTokenToServer()
            }
        }

        if (!authRepository.isLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            TulparTaxiTheme(darkTheme = true) {
                StartScreen()
            }
        }

        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (permissionGranted) {
                sendFcmTokenToServer()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            sendFcmTokenToServer()
        }
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
