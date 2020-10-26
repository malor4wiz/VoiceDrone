package com.example.voicedrone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import KTello
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.touch_page.*

class TouchPage : AppCompatActivity() {
    private var tello : KTello? = null
    private var connectionLabel: TextView? = null
    private var batteryLabel: TextView? = null
    private var timeLabel: TextView? = null

    val MOVEMENT_RANGE = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.touch_page)

        connectionLabel = findViewById(R.id.connectButton)
        batteryLabel = findViewById(R.id.batteryLabel)
        timeLabel = findViewById(R.id.timeLabel)

        tello = KTello()

        Thread{
            tello?.connect()
        }.start()

        Thread{
            askTello()
        }.start()
    }

    private fun askTello(){
        while (true){
            var isConnected: String? = "No"
            var battery: String? = "0%"
            var time: String? = "0s"

            Thread{
                if (tello?.isConnected!!){
                    isConnected = "OK"
                }
            }.start()

            connectionLabel?.text = isConnected

            Thread.sleep(500)

            Thread{
                battery = tello?.battery
            }.start()

            batteryLabel?.text = battery

            Thread.sleep(500)

            Thread{
                time = tello?.time
            }.start()

            timeLabel?.text = time

            Thread.sleep(500)
        }
    }

    fun takeoffBtnClicked(view: View?) {
        Log.w("takeoffBtnClicked", "View")
        Thread{
            tello?.takeOff()
        }.start()
    }

    fun landBtnClicked(view: View?) {
        Log.w("landBtnClicked", "View")
        Thread{
            tello?.land()
        }.start()
    }

    fun endBtnClicked(view: View?) {
        Log.w("endBtnClicked", "View")
        Thread{
            tello?.close()
        }.start()
    }

    fun upBtnClicked(view: View?) {
        Log.i("upBtnClicked", "View")
        Thread{
            tello?.up(MOVEMENT_RANGE)
        }.start()
    }

    fun ccwBtnClicked(view: View?) {
        Log.i("ccwBtnClicked", "View")
        Thread{
            tello?.ccw(MOVEMENT_RANGE)
        }.start()
    }

    fun downBtnClicked(view: View?) {
        Log.i("downBtnClicked", "View")
        Thread{
            tello?.down(MOVEMENT_RANGE)
        }.start()
    }

    fun cwBtnClicked(view: View?) {
        Log.i("cwBtnClicked", "View")
        Thread{
            tello?.cw(MOVEMENT_RANGE)
        }.start()
    }

    fun forwardBtnClicked(view: View?) {
        Log.i("forwardBtnClicked", "View")
        Thread{
            tello?.forward(MOVEMENT_RANGE)
        }.start()
    }

    fun leftBtnClicked(view: View?) {
        Log.i("leftBtnClicked", "View")
        Thread{
            tello?.left(MOVEMENT_RANGE)
        }.start()
    }

    fun backBtnClicked(view: View?) {
        Log.i("backBtnClicked", "View")
        Thread{
            tello?.back(MOVEMENT_RANGE)
        }.start()
    }

    fun rightBtnClicked(view: View?) {
        Log.i("rightBtnClicked", "View")
        Thread{
            tello?.right(MOVEMENT_RANGE)
        }.start()
    }
}