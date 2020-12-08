package com.example.voicedrone.pages

import KTello
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.voicedrone.*

class RecordResultPage : AppCompatActivity() {
    private var tello : KTello? = null
    private var orderIsRight = false
    private var orderValid = true

    private var backButton: Button? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_result_page)

        backButton = findViewById(R.id.backButton)

        val intent = intent
        val voiceResult = findViewById<TextView>(R.id.voiceResult)
        val rightRecognition = findViewById<TextView>(R.id.rightRecognition)
        val leftRecognition = findViewById<TextView>(R.id.leftRecognition)

        val rightRecognitionRate = intent.getStringExtra("rightRecognitionRate")
        val leftRecognitionRate = intent.getStringExtra("leftRecognitionRate")

        val connection = Connection(this, WiFiData.droneWiFiID, WiFiData.droneWiFiPass)
        connection.disable()

        if (rightRecognitionRate != null && leftRecognitionRate != null) {
            val rightRecognitionRateFloat = rightRecognitionRate.toFloat()
            when {
                rightRecognitionRateFloat > 0.6 -> {
                    Log.i("RecordResultPage", "orderIsRight is true")
                    orderIsRight = true
                }
                rightRecognitionRateFloat > 0.4 -> {
                    Log.i("RecordResultPage", "orderValid is false")
                    orderValid = false
                    val yourOrderSentence = findViewById<TextView>(R.id.yourOrderSentence)
                    yourOrderSentence.text = "has Not been carrying out "
                }
                else -> {
                    Log.v("RecordResultPage", "orderIsRight is false")
                }
            }

            voiceResult.text = intent.getStringExtra("result")
            rightRecognition.text = "$rightRecognitionRate%"
            leftRecognition.text = "$leftRecognitionRate%"
        }

        if (orderValid) {
            connection.connect{
                initTello()
            }

        }
    }

    private fun initTello() {
        Log.i("RecordResultPage", "initTello")
        tello = KTello()
        KTelloHandler.tello = tello
        Thread{
            // ドローンWiFiに接続切り替えするまで待っている
            Thread.sleep(5000)
            try {
                tello?.connect()
                Thread.sleep(3000)
                if (tello?.isConnected!!) {
                    tello?.takeOff()
                    Thread.sleep(3000)
                    if (orderIsRight) {
                        tello?.cw(360)
                        Thread.sleep(10000)
                        tello?.up(50)
                        Thread.sleep(2000)
                        tello?.flip("r")
                        Thread.sleep(2000)
                    } else {
                        tello?.ccw(360)
                        Thread.sleep(10000)
                        tello?.forward(50)
                        Thread.sleep(2000)
                        tello?.flip("l")
                        Thread.sleep(2000)
                    }
                    tello?.land()
                }
            } catch (e: Exception) {
                runOnUiThread{
                    Log.i("RecordResultPage", e.toString())
                    Toast.makeText(applicationContext, "Telloとの通信に失敗しました", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun onClickBackButton(view: View?) {
        Thread{
            tello?.close()
        }.start()
        val intent = Intent(application, RecordPage::class.java)
        intent.putExtra("activity", EnumActivity.RecordResult)

        val wiFiConnected: (Intent) -> Unit = {activityIntent -> startActivity(activityIntent)}
        val connection = Connection(this, WiFiData.internetWiFiID, WiFiData.internetWiFiPass)
        connection.disable()
        connection.connect{wiFiConnected(intent)}
    }

    fun onClickTouchButton(view: View?) {
        val intent = Intent(application, TouchPage::class.java)
        intent.putExtra("activity", EnumActivity.RecordResult)
        startActivity(intent)
    }

    fun onClickHomeButton(view: View?) {
        Thread{
            tello?.close()
        }.start()
        val intent = Intent(application, HomePage::class.java)
        startActivity(intent)
    }
}