package kz.tulpartaxi.driver.ui.start

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kz.tulpartaxi.driver.data.repository.AuthRepository
import kz.tulpartaxi.driver.ui.auth.AuthActivity
import kz.tulpartaxi.driver.ui.theme.TulparTaxiTheme
import javax.inject.Inject

@AndroidEntryPoint
class StartActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository

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
            TulparTaxiTheme(darkTheme = true) {
                DriverScreen()
            }
        }
    }
}
