package kz.tulpartaxi.kandyagash.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPointSelected: (mode: String, lat: Double, lng: Double, address: String) -> Unit = { _, _, _, _ -> },
    onPageLoaded: () -> Unit = {},
    onError: () -> Unit = {},
) {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setGeolocationEnabled(true)
                // Нужно для WebGL (Mapbox GL JS) и загрузки ресурсов с http:// сервера
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                mediaPlaybackRequiresUserGesture = false
            }
            // Убираем белый флэш при загрузке
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false
                override fun onPageFinished(view: WebView?, url: String?) {
                    mainHandler.post { onPageLoaded() }
                }
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    if (request?.isForMainFrame == true) mainHandler.post { onError() }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                    Log.d("TulparMap", "[${msg?.messageLevel()}] ${msg?.message()} @ ${msg?.sourceId()}:${msg?.lineNumber()}")
                    return true
                }
                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?,
                ) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    callback?.invoke(origin, hasPermission, false)
                }
            }
            addJavascriptInterface(
                TulparBridge(
                    onPointSelected = { mode, lat, lng, address ->
                        mainHandler.post { onPointSelected(mode, lat, lng, address) }
                    },
                ),
                "TulparBridge",
            )
        }
    }

    AndroidView(
        factory = { webView.also { it.loadUrl(url) } },
        modifier = modifier,
    )
}
