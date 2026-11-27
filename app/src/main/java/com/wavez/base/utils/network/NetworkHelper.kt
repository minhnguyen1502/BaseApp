package com.wavez.base.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager

class NetworkHelper(private val context: Context) {

    val netWorkStateHolder by lazy { NetWorkStateHolder() }

    private fun updateState() {
        netWorkStateHolder.updateNetworkState(isConnected())
    }

    init {
        updateState()
    }

    fun startNetworkCallback() {
        fun func() {
            val cm =
                (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?)
                    ?: return
            val builder: NetworkRequest.Builder = NetworkRequest.Builder()
            cm.registerNetworkCallback(
                builder.build(),
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        updateState()
                    }

                    override fun onLost(network: Network) {
                        updateState()
                    }
                },
            )
        }
        func()
    }

    private fun isConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiManager =
            context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        val isWifiConnected =
            wifiManager?.isWifiEnabled == true && actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && isWifiConnected -> true

            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true

            // for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true

            else -> false
        }
    }
}
