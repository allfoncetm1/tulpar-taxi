package kz.tulpartaxi.kandyagash.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import kz.tulpartaxi.kandyagash.ui.start.StartActivity
import kz.tulpartaxi.kandyagash.ui.theme.TulparTaxiTheme

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TulparTaxiTheme(darkTheme = false) {
                AuthScreen(
                    onSuccess = {
                        startActivity(Intent(this, StartActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}
