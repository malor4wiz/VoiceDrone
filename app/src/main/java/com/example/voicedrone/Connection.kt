package com.example.voicedrone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import io.reactivex.rxjava3.core.Single

class Connection(private val context: Context, private val wifiSSID: String, private val wifiPassword: String) {

    private var wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    operator fun invoke() {
        Log.v("v", "invoke")
        connect()
    }

    private fun connect() {
        Log.v("v", "connect")
        val intentFilter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (isConnectedToCorrectSSID()) {
                    Log.v("v","Successfully connected to the device.")
                } else {
                    Log.w("w","Still not connected to ${wifiSSID}. Waiting a little bit more...")
                }
            }
        }
        Log.v("v","Registering connection receiver...")
        context.registerReceiver(receiver, intentFilter)
        addNetwork()
    }

    private fun addNetwork() {
        Log.v("v","Connecting to ${wifiSSID}...")
        val wc = WifiConfiguration()
        wc.SSID = "\"" + wifiSSID + "\""
        wc.preSharedKey = "\"" + wifiPassword + "\""
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        val netId = wifiManager.addNetwork(wc)
        if (netId != -1) {
            if (!wifiManager.enableNetwork(netId, true)) {
                Log.e("e","Failed to connect to the device.")
            }
        } else {
            Log.e("e","Failed to connect to the device. addNetwork() returned -1")
        }
    }

    private fun isConnectedToCorrectSSID(): Boolean {
        val currentSSID = wifiManager.connectionInfo.ssid ?: return false
        Log.v("v","Connected to $currentSSID")
        return currentSSID == "\"${wifiSSID}\""
    }

    fun disable() {
        Log.v("v", "Current Network is disabled")
        val netid = wifiManager.connectionInfo.networkId
        wifiManager.disableNetwork(netid)
    }
}