package com.example.voicedrone.pages

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.example.voicedrone.Connection
import com.example.voicedrone.EnumActivity
import com.example.voicedrone.R
import java.util.*


var internetWiFiID = ""
var droneWiFiID = ""
var internetWiFiPass = ""
var droneWiFiPass = ""

class WiFiSelectPage : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0

    private var adapter : ArrayAdapter<String>? = null
    private var wifiScanTimer: Timer? = null

    private var internetWiFi : TextView? = null
    private var droneWiFi : TextView? = null
    private var internetPassword : EditText? = null
    private var dronePassword : EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wifi_select_page)

        val manager : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info : WifiInfo = manager.connectionInfo

        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                if (manager.wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ),
                            PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
                        return;
                    } else {
                        val scanResult = manager.startScan()
                        runOnUiThread{
                            if (!scanResult) {
                                Toast.makeText(applicationContext, "WiFiのScanに失敗しました", Toast.LENGTH_LONG).show()
                            }
                            scanWifi()
                        }
                    }
                }
            }
        }

        Thread{
            wifiScanTimer = Timer(true)
            wifiScanTimer?.schedule(timerTask, 0, 10000)
        }.start()

        if(info.supplicantState == SupplicantState.COMPLETED) {
            val ssid = info.ssid
            val nowConnectingWiFi = findViewById<TextView>(R.id.nowConnectingWiFi)
            nowConnectingWiFi.text = ssid
        } else {
            Log.i("WiFiSelectPage", "SSID not supplied")
        }

        // リスト項目とListViewを対応付けるArrayAdapterを用意する
        adapter = ArrayAdapter(this, R.layout.scanned_wifi)

        // ListViewにArrayAdapterを設定する
        val listView: ListView = findViewById<View>(R.id.scannedWiFi) as ListView
        listView.adapter = adapter

        internetWiFi = findViewById(R.id.internetWiFi)
        droneWiFi = findViewById(R.id.droneWiFi)

        dronePassword = findViewById(R.id.DronePassword)
        internetPassword = findViewById(R.id.InternetPassword)

        listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val clickedWiFiName = parent.getItemAtPosition(position) as String
            AlertDialog.Builder(this)
                .setTitle("WiFi-setting")
                .setMessage("$clickedWiFiName: どちらか選択してください。")
                .setPositiveButton("Internet-WiFi") { dialog, which ->
                    internetWiFi?.text = adapter?.getItem(position)
                }
                .setNegativeButton("Drone-WiFi") { dialog, which ->
                    droneWiFi?.text = adapter?.getItem(position)
                }
                .show()
        }

        val connectButton = findViewById<Button>(R.id.connectButton)
        connectButton.setOnClickListener { _ ->
            Log.i("WiFiSelectPage", "connectButton Clicked")
            internetWiFiID = internetWiFi?.text.toString()
            droneWiFiID = droneWiFi?.text.toString()

            internetWiFiPass = internetPassword?.text.toString()
            droneWiFiPass = dronePassword?.text.toString()

            if((internetWiFiID != "選択してください") && (droneWiFiID != "選択してください")){
                val intent = Intent(application, RecordPage::class.java)
                intent.putExtra("activity", EnumActivity.WiFiSelect)

                val wiFiConnected: (Intent) -> Unit = {activityIntent -> startActivity(activityIntent)}
                val connection = Connection(this, internetWiFiID, internetWiFiPass)
                connection.connect{wiFiConnected(intent)}

                Thread{
                    wifiScanTimer?.cancel()
                }.start()
            } else {
                Toast.makeText(applicationContext, "WiFiが選択されていません", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // requestPermissions の後の処理
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            scanWifi()
        } else {
            Toast.makeText(applicationContext, "permissionが許可されていません", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scanWifi() {
        val manager : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val array: MutableList<ScanResult> = manager.scanResults

        adapter?.clear()

        for (data in array) {
            adapter?.add(data.SSID)
        }

        adapter?.notifyDataSetChanged()
    }
}