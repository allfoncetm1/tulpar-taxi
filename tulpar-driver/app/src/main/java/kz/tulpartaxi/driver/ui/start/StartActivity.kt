package kz.tulpartaxi.driver.ui.start

import android.Manifest
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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kz.tulpartaxi.driver.data.api.TulparApi
import kz.tulpartaxi.driver.data.local.TokenStorage
import kz.tulpartaxi.driver.data.repository.AuthRepository
import kz.tulpartaxi.driver.ui.auth.AuthActivity
import kz.tulpartaxi.driver.ui.theme.TulparTaxiTheme
import javax.inject.Inject

@AndroidEntryPoint
class StartActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var tokenStorage: TokenStorage
    @Inject lateinit var api: TulparApi
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* no-op for now */ }

        if (!authRepository.isLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        requestNotificationPermissionIfNeeded()
        uploadFcmTokenIfNeeded()

        enableEdgeToEdge()
        setContent {
            TulparTaxiTheme(darkTheme = true) {
                DriverScreen()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun uploadFcmTokenIfNeeded() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            if (token == tokenStorage.deviceId) return@addOnCompleteListener

            lifecycleScope.launch {
                runCatching {
                    api.updateDeviceId(mapOf("firebaseToken" to token))
                    tokenStorage.deviceId = token
                }
            }
        }
    }
}
