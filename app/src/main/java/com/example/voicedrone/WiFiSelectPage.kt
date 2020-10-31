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
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity

var internetWiFiID = ""
var droneWiFiID = ""
var internetWiFiPass = ""
var droneWiFiPass = ""

class WiFiSelectPage : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0
    private var wifis = arrayOf<String>()
    private var adapter : ArrayAdapter<String>? = null
    private var internetWiFi : TextView? = null
    private var droneWiFi : TextView? = null
    private var internetPassword : EditText? = null
    private var dronePassword : EditText? = null

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

        dronePassword = findViewById(R.id.DronePassword)
        internetPassword = findViewById(R.id.InternetPassword)

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
            Log.w("connectionButton", "Clicked")
            internetWiFiID = internetWiFi?.text.toString()
            droneWiFiID = droneWiFi?.text.toString()

            internetWiFiPass = internetPassword?.text.toString()
            droneWiFiPass = dronePassword?.text.toString()

            if((internetWiFiID != "選択してください") && (droneWiFiID != "選択してください")){
                val connection = Connection(this, internetWiFi?.text.toString(), "326824658235a")
                connection.invoke()

                Thread.sleep(1000)

                val intent = Intent(application, RecordPage::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "WiFiが選択されていません", Toast.LENGTH_SHORT).show()
            }
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
            scanWifi()
        } else {
            Toast.makeText(applicationContext, "permissionが許可されていません", Toast.LENGTH_SHORT).show()
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

        adapter?.notifyDataSetChanged()
        wifis += newWifis
    }
}