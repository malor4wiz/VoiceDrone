package com.example.voicedrone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.TextView

class Connection(private val context: Context, private val wifiSSID: String, private val wifiPassword: String) {

    private var wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var flag = true

    // 新しいWiFiに接続できたかどうか判定するためにBroadcastReceiverを登録し、addNetworkへ
    fun connect() {
        Log.i("Connection", "connect")
        val intentFilter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (isConnectedToCorrectSSID()) {
                    Log.i("Connection","Successfully connected to the device.")
                } else {
                    Log.w("Connection","Still not connected to ${wifiSSID}. Waiting a little bit more...")
                }
            }
        }
        Log.i("Connection","Registering connection receiver...")
        context.registerReceiver(receiver, intentFilter)
        addNetwork()
    }

    // 新しいWiFiに接続できたかどうか判定するためにBroadcastReceiverを登録し、addNetworkへ
    fun connect(callback: () -> Unit?) {
        Log.i("Connection", "connect")
        val intentFilter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (isConnectedToCorrectSSID()) {
                    Log.i("Connection","Successfully connected to the device.")
                    if (flag) {
                        Log.i("Connection", "Connection Callback")
                        callback()
                        flag = !flag
                    }
                } else {
                    Log.w("Connection","Still not connected to ${wifiSSID}. Waiting a little bit more...")
                }
            }
        }
        Log.i("Connection","Registering connection receiver...")
        context.registerReceiver(receiver, intentFilter)
        addNetwork()
    }

    // 新しいWiFiの情報を入れ、接続する
    private fun addNetwork() {
        Log.i("Connection","Connecting to ${wifiSSID}...")
        val wc = WifiConfiguration()
        wc.SSID = "\"" + wifiSSID + "\""
        wc.preSharedKey = "\"" + wifiPassword + "\""
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        val netId = wifiManager.addNetwork(wc)
        wifiManager.enableNetwork(netId, true)
        if (netId != -1) {
            if (!wifiManager.enableNetwork(netId, true)) {
                Log.e("Connection","Failed to connect to the device.")
            }
        } else {
            Log.e("Connection","Failed to connect to the device. addNetwork() returned -1")
        }
    }

    private fun isConnectedToCorrectSSID(): Boolean {
        val info = wifiManager.connectionInfo

        if(info.supplicantState == SupplicantState.COMPLETED) {
            val currentSSID = info.ssid ?: return false
            Log.i("Connection","Connected to $currentSSID")
            return currentSSID == "\"${wifiSSID}\""
        } else {
            Log.i("Connection", "SSID not supplied")
            return false
        }
    }

    // WiFiが接続途中などの場合に新しいWiFiに接続をしようとするとエラーが発生するので、一度現在のWiFiの接続を切るために用いる
    fun disable() {
        Log.i("Connection", "Current Network is disabled")
        val netid = wifiManager.connectionInfo.networkId
        wifiManager.disableNetwork(netid)
    }
}