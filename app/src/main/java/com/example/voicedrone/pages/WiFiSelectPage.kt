package com.example.voicedrone.pages

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.voicedrone.Connection
import com.example.voicedrone.EnumActivity
import com.example.voicedrone.R
import com.example.voicedrone.WiFiData
import java.util.*

class WiFiSelectPage : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 0

    private var adapter : ArrayAdapter<String>? = null
    private var wifiScanTimer: Timer? = null

    private var internetWiFi : TextView? = null
    private var droneWiFi : TextView? = null
    private var internetPassword : EditText? = null
    private var dronePassword : EditText? = null
    private var wifiManager : WifiManager? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wifi_select_page)

        confirmRetainedWiFiSetting()

        // CONNECTボタンへのイベント紐付け
        val connectButton = findViewById<Button>(R.id.connectButton)
        connectButton.setOnClickListener { _ ->
            Log.i("WiFiSelectPage", "connectButton Clicked")
            WiFiData.internetWiFiID = internetWiFi?.text.toString()
            WiFiData.droneWiFiID = droneWiFi?.text.toString()

            WiFiData.internetWiFiPass = internetPassword?.text.toString()
            WiFiData.droneWiFiPass = dronePassword?.text.toString()

            if((WiFiData.internetWiFiID != "選択してください") && (WiFiData.droneWiFiID != "選択してください")){
                val intent = Intent(application, RecordPage::class.java)
                intent.putExtra("activity", EnumActivity.WiFiSelect)

                connectInternetWiFi(intent)

            } else {
                Toast.makeText(applicationContext, "WiFiが選択されていません", Toast.LENGTH_SHORT).show()
            }
        }

        // リスト項目とListViewを対応付けるArrayAdapterを用意する
        adapter = ArrayAdapter(this, R.layout.scanned_wifi)

        // ListViewにArrayAdapterを設定する
        val listView: ListView = findViewById<View>(R.id.scannedWiFi) as ListView
        listView.adapter = adapter

        internetWiFi = findViewById(R.id.internetWiFi)
        droneWiFi = findViewById(R.id.droneWiFi)

        internetPassword = findViewById(R.id.InternetPassword)
        dronePassword = findViewById(R.id.DronePassword)

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

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun confirmRetainedWiFiSetting() {
        // WiFi情報を保持している場合はその情報を使用するか確認
        if(WiFiData.droneWiFiID != "" && WiFiData.internetWiFiID != "") {

            AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Saved-WiFi")
                .setMessage("保存されているWiFIデータを使いますか？\n\n" +
                        "InternetWiFiID: ${WiFiData.internetWiFiID}\n" +
                        "DroneWiFiID: ${WiFiData.droneWiFiID}")
                .setPositiveButton("Yes") { _ , _ ->
                    val intent = Intent(application, RecordPage::class.java)
                    intent.putExtra("activity", EnumActivity.WiFiSelect)
                    connectInternetWiFi(intent)
                }
                .setNegativeButton("No") { _ , _ ->
                    // Wi-Fiをスキャン
                    wifiScan()
                }
                .show()
        } else {
            // Wi-Fiをスキャン
            wifiScan()
        }
    }

    // Wi-Fiスキャン
    private fun wifiScan() {

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // 権限チェック
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE),
                PERMISSIONS_REQUEST_CODE);
        }

        val success = wifiManager!!.startScan()
        if (!success) {
            // scan failure handling
            scanFailure()
        }

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        this.registerReceiver(wifiScanReceiver, intentFilter)

    }

    private fun scanSuccess() {
        val array: MutableList<ScanResult> = wifiManager?.scanResults as MutableList<ScanResult>

        val info : WifiInfo? = wifiManager?.connectionInfo

        if(info?.supplicantState == SupplicantState.COMPLETED) {
            val nowConnectingWiFi = findViewById<TextView>(R.id.nowConnectingWiFi)
            nowConnectingWiFi.text = info?.ssid
        } else {
            Log.i("WiFiSelectPage", "SSID not supplied")
        }

        adapter?.clear()
        for (data in array) {
            if (data.SSID != "" && adapter?.getPosition(data.SSID) == -1) {
                adapter?.add(data.SSID)
            }
        }
        adapter?.notifyDataSetChanged()
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        Log.i("WiFiSelectPage", "Wi-Fiのスキャンに失敗しました")
    }

    // requestPermissions の後の処理
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            wifiScan()
        } else {
            Toast.makeText(applicationContext, "permissionが許可されていません", Toast.LENGTH_SHORT).show()
        }
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectInternetWiFi(intent : Intent) {
        val wiFiConnected: (Intent) -> Unit = {activityIntent -> startActivity(activityIntent)}
        val connection = Connection(this, WiFiData.internetWiFiID, WiFiData.internetWiFiPass)
        connection.disable()
        connection.connect {
            wiFiConnected(intent)
        }
    }
}