package com.example.voicedrone

import KTello
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordResultPage : AppCompatActivity() {
    private var tello : KTello? = null
    private var orderIsRight = false
    private var orderValid = true

    private var voiceButton: Button? = null

    val MOVEMENT_RANGE = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_result_page)

        voiceButton = findViewById<Button>(R.id.voiceButton)
        voiceButton?.isEnabled = false

        val intent = intent
        val voiceResult = findViewById<TextView>(R.id.voiceResult)

        val rightRecognition = findViewById<TextView>(R.id.rightRecognition)
        val leftRecognition = findViewById<TextView>(R.id.leftRecognition)

        val rightRecognitionRate = intent.getStringExtra("rightRecognitionRate")
        val leftRecognitionRate = intent.getStringExtra("leftRecognitionRate")

        val connection = Connection(this, droneWiFiID, droneWiFiPass)
        connection.disable()
        connection.invoke()

        if (rightRecognitionRate != null && leftRecognitionRate != null) {
            val rightRecognitnionRateFloat = rightRecognitionRate.toFloat()
            if (rightRecognitnionRateFloat > 0.6) {
                Log.v("orderIsRight", "true")
                orderIsRight = true
            } else if (rightRecognitnionRateFloat > 0.4) {
                Log.v("orderValid", "false")
                orderValid = false
                val yourOrderSentence = findViewById<TextView>(R.id.yourOrderSentence)
                yourOrderSentence.text = "has Not been carrying out "
                voiceButton?.isEnabled = true
            } else {
                Log.v("orderIsRight", "false")
            }

            rightRecognition.text = rightRecognitionRate + "%"
            leftRecognition.text = leftRecognitionRate + "%"
        }

        voiceResult.text = intent.getStringExtra("result")

        if (orderValid) {
            Log.v("Connection", "drone")
            initTello()
        }
    }

    private fun initTello() {
        tello = KTello()
        KTelloHandler.tello = tello
        Log.v("initTello", "tello")
        Thread{
            Thread.sleep(5000)
            Log.v("wait", "waited")
            try {
                tello?.connect()
                Thread.sleep(3000)
                if (tello?.isConnected!!) {
                    Log.v("takeoff", "tello")
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
                Log.v("Exception", "tello")
                Log.e("Tello", e.toString())
            }
        }.start()
    }

    fun onClickVoiceButton(view: View?) {
        Thread{
            tello?.close()
        }.start()
        val intent = Intent(application, RecordPage::class.java)
        intent.putExtra("activity", EnumActivity.RecordResult)
        val connection = Connection(this, internetWiFiID, internetWiFiPass)
        connection.disable()
        connection.invoke()
        Thread.sleep(2000)
        startActivity(intent)
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