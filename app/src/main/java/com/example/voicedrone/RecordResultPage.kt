package com.example.voicedrone

import KTello
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

    val MOVEMENT_RANGE = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_result_page)

        val intent = intent
        val voiceResult = findViewById<TextView>(R.id.voiceResult)


        val rightRecognition = findViewById<TextView>(R.id.rightRecognition)
        val leftRecognition = findViewById<TextView>(R.id.leftRecognition)

        val rightRecognitionRate = intent.getStringExtra("rightRecognitionRate")
        val leftRecognitionRate = intent.getStringExtra("leftRecognitionRate")

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
            } else {
                Log.v("orderIsRight", "false")
            }

            rightRecognition.text = rightRecognitionRate + "%"
            leftRecognition.text = leftRecognitionRate + "%"
        }

        voiceResult.text = intent.getStringExtra("result")

        if (orderValid) {
            Log.v("Connection", "drone")
            GlobalScope.launch {
                withContext(Dispatchers.Default) {
                    Connection(
                        applicationContext,
                        droneWiFiID,
                        droneWiFiPass
                    ).invoke()
                }
                initTello()
            }
        }
    }

    private fun initTello() {
        tello = KTello()
        Log.v("initTello", "tello")
        Thread{
            try {
                tello?.connect()
                Thread.sleep(3000)
                if (tello?.isConnected!!) {
                    Log.v("takeoff", "tello")
                    tello?.takeOff()
                    Thread.sleep(3000)
                    if (orderIsRight) {
                        repeat(5) {
                            tello?.right(MOVEMENT_RANGE)
                            Thread.sleep(3000)
                        }
                    } else {
                        repeat(5) {
                            tello?.left(MOVEMENT_RANGE)
                            Thread.sleep(3000)
                        }
                    }
                    tello?.land()
                }
            } catch (e: Exception) {
                Log.v("Exception", "tello")
                Log.e("Tello", e.toString())
            }
        }.start()
    }

    fun onClickHomeButton(view: View?) {
        val intent = Intent(application, HomePage::class.java)
        startActivity(intent)
    }
}