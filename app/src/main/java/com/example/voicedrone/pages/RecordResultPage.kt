package com.example.voicedrone.pages

import KTello
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voicedrone.*

class RecordResultPage : AppCompatActivity() {
    private var tello : KTello? = null
    private var orderIsRight = false
    private var orderValid = true

    private var voiceButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_result_page)

        voiceButton = findViewById(R.id.voiceButton)
        voiceButton?.isEnabled = false

        val intent = intent
        val voiceResult = findViewById<TextView>(R.id.voiceResult)
        val rightRecognition = findViewById<TextView>(R.id.rightRecognition)
        val leftRecognition = findViewById<TextView>(R.id.leftRecognition)

        val rightRecognitionRate = intent.getStringExtra("rightRecognitionRate")
        val leftRecognitionRate = intent.getStringExtra("leftRecognitionRate")

        val connection = Connection(this, WiFiData.droneWiFiID, WiFiData.droneWiFiPass)
        connection.disable()
        connection.connect()

        if (rightRecognitionRate != null && leftRecognitionRate != null) {
            val rightRecognitnionRateFloat = rightRecognitionRate.toFloat()
            when {
                rightRecognitnionRateFloat > 0.6 -> {
                    Log.i("RecordResultPage", "orderIsRight is true")
                    orderIsRight = true
                }
                rightRecognitnionRateFloat > 0.4 -> {
                    Log.i("RecordResultPage", "orderValid is false")
                    orderValid = false
                    val yourOrderSentence = findViewById<TextView>(R.id.yourOrderSentence)
                    yourOrderSentence.text = "has Not been carrying out "
                    voiceButton?.isEnabled = true
                }
                else -> {
                    Log.v("RecordResultPage", "orderIsRight is false")
                }
            }

            voiceResult.text = intent.getStringExtra("result")
            rightRecognition.text = rightRecognitionRate + "%"
            leftRecognition.text = leftRecognitionRate + "%"
        }

        if (orderValid) {
            initTello()
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
                    runOnUiThread{
                        voiceButton?.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                runOnUiThread{
                    Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    fun onClickVoiceButton(view: View?) {
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