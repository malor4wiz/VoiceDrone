package com.example.voicedrone

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class WiFiSelectPage : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0
    private var wifis = arrayOf<String>()
    private var adapter : ArrayAdapter<String>? = null
    private var internetWiFi : TextView? = null
    private var droneWiFi : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wifi_select_page)

        var manager : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info : WifiInfo = manager.connectionInfo

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    Log.w("Success", "yeah!")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // 既に許可されているか確認
                        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                            // 許可されていなかったらリクエストする
                            // ダイアログが表示される
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ),
                                PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
                            return;
                        } else {
                            // 許可されていた場合
                            scanWifi()
                        }
                    }

                } else {
                    Log.w("Failure", "Oh,my")
                }
            }
        }

        applicationContext.registerReceiver(wifiScanReceiver, IntentFilter(SCAN_RESULTS_AVAILABLE_ACTION))
        val scanResult = manager.startScan()

        Log.w("scanResult", scanResult.toString())

        if(info.supplicantState == SupplicantState.COMPLETED) {
            val ssid = info.ssid
            val nowConnectingWiFi = findViewById<TextView>(R.id.nowConnectingWiFi)
            nowConnectingWiFi.text = ssid
            Log.w("SSID", ssid)
        } else {
            Log.w("SSID", "not supplied")
        }

        // リスト項目とListViewを対応付けるArrayAdapterを用意する
        adapter = ArrayAdapter(this, R.layout.scanned_wifi)

        // ListViewにArrayAdapterを設定する
        val listView: ListView = findViewById<View>(R.id.scannedWiFi) as ListView
        listView.adapter = adapter

        internetWiFi = findViewById(R.id.internetWiFi)
        droneWiFi = findViewById(R.id.droneWiFi)

        listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            AlertDialog.Builder(this)
                .setTitle("WiFi-setting")
                .setMessage("どちらか選択してください。")
                .setPositiveButton("Internet-WiFi",
                    DialogInterface.OnClickListener { dialog, which ->
                        internetWiFi?.text = adapter?.getItem(position)
                    })
                .setNegativeButton("Drone-WiFi",
                    DialogInterface.OnClickListener { dialog, which ->
                        droneWiFi?.text = adapter?.getItem(position)
                    })
                .show()
        }

        val connectButton = findViewById<Button>(R.id.connectButton)
        connectButton.setOnClickListener { _ ->
            val intent = Intent(application, RecordPage::class.java)
            startActivity(intent)
//            Log.w("connectionButton", "Clicked")
//            val connection = Connection(this, internetWiFi?.text.toString(), "326824658235a")
//            connection.invoke()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // 許可された場合
            scanWifi()
        } else {
            // 許可されなかった場合
            // 何らかの対処が必要
        }
    }

    private fun scanWifi() {
        var manager : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val array: MutableList<ScanResult> = manager.scanResults
        Log.w("scanResults", array.toString())
        var newWifis = arrayOf<String>()
        for (data in array) {
            if(!wifis.contains(data.SSID)) {
                newWifis += data.SSID
            }
            Log.w("result", data.SSID)
        }
        for(a in wifis){
            Log.w("wifis", a)
        }
        for(a in newWifis){
            Log.w("newWifis", a)
        }

        if(newWifis.isNotEmpty()) {
            adapter?.addAll(*newWifis)
        }

//        for(wifi in wifis) {
//            if(!newWifis.contains(wifi)) {
//                adapter?.remove(wifi)
//            }
//        }
        adapter?.notifyDataSetChanged()
        wifis += newWifis
    }
}