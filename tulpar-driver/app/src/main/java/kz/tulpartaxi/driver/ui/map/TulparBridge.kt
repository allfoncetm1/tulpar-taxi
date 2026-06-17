package kz.tulpartaxi.driver.ui.map

import android.webkit.JavascriptInterface

class TulparBridge(
    private val onPointSelected: (mode: String, lat: Double, lng: Double, address: String) -> Unit,
    private val onRequestLocation: (() -> Unit)? = null,
) {
    @JavascriptInterface
    fun onPointSelected(mode: String, lat: Double, lng: Double, address: String) {
        onPointSelected.invoke(mode, lat, lng, address)
    }

    @JavascriptInterface
    fun requestLocation() {
        onRequestLocation?.invoke()
    }
}
