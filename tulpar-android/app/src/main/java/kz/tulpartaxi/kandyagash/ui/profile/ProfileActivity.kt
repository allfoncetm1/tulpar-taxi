package kz.tulpartaxi.kandyagash.ui.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import kz.tulpartaxi.kandyagash.ui.start.ProfileScreen
import kz.tulpartaxi.kandyagash.ui.theme.TulparTaxiTheme

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TulparTaxiTheme(darkTheme = false) {
                ProfileScreen(onBack = { finish() })
            }
        }
    }
}
