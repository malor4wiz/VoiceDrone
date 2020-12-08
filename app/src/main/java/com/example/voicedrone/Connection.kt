package com.example.voicedrone

import android.content.Context
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi


class Connection(
    private val context: Context,
    private val wifiSSID: String,
    private val wifiPassword: String
) {

    private var wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var flag = true

    @RequiresApi(Build.VERSION_CODES.Q)
    fun connect() {
        connect(null)
    }

    // 新しいWiFiに接続できたかどうか判定するためにBroadcastReceiverを登録し、addNetworkへ
    @RequiresApi(Build.VERSION_CODES.Q)
    fun connect(callback: (() -> Unit?)?) {
//        Log.i("Connection", "connect")
//        val intentFilter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
//        val receiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (isConnectedToCorrectSSID()) {
//                    Log.i("Connection", "Successfully connected to the device.")
//                    if (flag) {
//                        Log.i("Connection", "Connection Callback")
//                        callback()
//                        flag = !flag
//                    }
//                } else {
//                    Log.w(
//                        "Connection",
//                        "Still not connected to ${wifiSSID}. Waiting a little bit more..."
//                    )
//                }
//            }
//        }
//        Log.i("Connection", "Registering connection receiver...")
//        context.registerReceiver(receiver, intentFilter)
//        addNetwork()
        // os10以上はOSのメッセージに制御させる
        val request : NetworkRequest = getNetworkRequest();
        val connectivityManager : ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(request, getNetworkCallback(callback))
    }

//    // 新しいWiFiの情報を入れ、接続する
//    private fun addNetwork() {
//        Log.i("Connection","Connecting to ${wifiSSID}...")
//        val wc = WifiConfiguration()
//        wc.SSID = "\"" + wifiSSID + "\""
//        wc.preSharedKey = "\"" + wifiPassword + "\""
//        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
//        val netId = wifiManager.addNetwork(wc)
//        wifiManager.enableNetwork(netId, true)
//        if (netId != -1) {
//            if (!wifiManager.enableNetwork(netId, true)) {
//                Log.e("Connection","Failed to connect to the device.")
//            }
//        } else {
//            Log.e("Connection","Failed to connect to the device. addNetwork() returned -1")
//        }
//    }

//    private fun isConnectedToCorrectSSID(): Boolean {
//        val info = wifiManager.connectionInfo
//
//        if(info.supplicantState == SupplicantState.COMPLETED) {
//            val currentSSID = info.ssid ?: return false
//            Log.i("Connection","Connected to $currentSSID")
//            return currentSSID == "\"${wifiSSID}\""
//        } else {
//            Log.i("Connection", "SSID not supplied")
//            return false
//        }
//    }

    // WiFiが接続途中などの場合に新しいWiFiに接続をしようとするとエラーが発生するので、一度現在のWiFiの接続を切るために用いる
    fun disable() {
        Log.i("Connection", "Current Network is disabled")
        val netid = wifiManager.connectionInfo.networkId
        wifiManager.disableNetwork(netid)
    }


    /**
     * android 10 以降で使用
     * @return NetworkRequest
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getNetworkRequest(): NetworkRequest {
        val specifier: NetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(wifiSSID)
            .setWpa2Passphrase(wifiPassword)
            .build()
        return NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()
    }
    /**
     * android 10 以降で使用
     * @return NetworkCallback
     */
    private fun getNetworkCallback(callback: (() -> Unit?)?): NetworkCallback? {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return object : NetworkCallback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            override fun onAvailable(@NonNull network: Network) {
                super.onAvailable(network)
                connectivityManager.bindProcessToNetwork(network)
                Log.i("Connection", "onAvailable")
                if (callback != null) {
                    callback()
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.i("Connection", "onUnavailable")
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                Log.i("Connection", "onLosing")
            }

            override fun onLost(network: Network) {
                Log.i("Connection", "onLost")
            }

            override fun onLinkPropertiesChanged(
                network: Network,
                linkProperties: LinkProperties
            ) {
                Log.i("Connection", "onLinkPropertiesChanged")
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Log.i("Connection", "onCapabilitiesChanged")
            }

            override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                Log.i("Connection", "onBlockedStatusChanged")

            }
        }
    }

}