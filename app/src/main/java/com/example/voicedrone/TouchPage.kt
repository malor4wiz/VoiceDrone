package com.example.voicedrone

import KTello
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception


class TouchPage : AppCompatActivity() {
    private var tello : KTello? = null
    private var connectionLabel: TextView? = null
    private var batteryLabel: TextView? = null
    private var timeLabel: TextView? = null

    private var askFlag = true

    val MOVEMENT_RANGE = 20

    enum class TelloActions{
        TAKEOFF,
        LAND,
        END,
        UP,
        DOWN,
        CW,
        CCW,
        FORWARD,
        BACK,
        RIGHT,
        LEFT
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.touch_page)

        connectionLabel = findViewById(R.id.connectionLabel)
        batteryLabel = findViewById(R.id.batteryLabel)
        timeLabel = findViewById(R.id.timeLabel)

        tello = KTello()

        Thread{
            try {
                tello?.connect()
            } catch (e: Exception) {
                print(e)
            }
            if (tello?.isConnected!!) {
                runOnUiThread {
                    connectionLabel?.text = "OK"
                }
                askTello()
            }
        }.start()
    }

    private fun askTello(){
        while (askFlag){
            try {
                val battery: String? = tello?.battery + "%"

                runOnUiThread{
                    if (battery != "ok") {
                        batteryLabel?.text = battery
                    }
                }

                Thread.sleep(500)

                val time: String? = tello?.time

                runOnUiThread{
                    if (battery != "ok") {
                        timeLabel?.text = time
                    }
                }

                Thread.sleep(500)
            } catch (e: Exception) {
                print(e)
            }
        }
    }

    fun takeoffBtnClicked(view: View?) {
        Log.w("takeoffBtnClicked", "View")
        telloAction(TelloActions.TAKEOFF)
    }

    fun landBtnClicked(view: View?) {
        Log.w("landBtnClicked", "View")
        telloAction(TelloActions.LAND)
    }

    fun endBtnClicked(view: View?) {
        Log.w("endBtnClicked", "View")
        telloAction(TelloActions.END)
    }

    fun upBtnClicked(view: View?) {
        Log.i("upBtnClicked", "View")
        telloAction(TelloActions.UP)
    }

    fun ccwBtnClicked(view: View?) {
        Log.i("ccwBtnClicked", "View")
        telloAction(TelloActions.CCW)
    }

    fun downBtnClicked(view: View?) {
        Log.i("downBtnClicked", "View")
        telloAction(TelloActions.DOWN)
    }

    fun cwBtnClicked(view: View?) {
        Log.i("cwBtnClicked", "View")
        telloAction(TelloActions.CW)
    }

    fun forwardBtnClicked(view: View?) {
        Log.i("forwardBtnClicked", "View")
        telloAction(TelloActions.FORWARD)
    }

    fun leftBtnClicked(view: View?) {
        Log.i("leftBtnClicked", "View")
        telloAction(TelloActions.LEFT)
    }

    fun backBtnClicked(view: View?) {
        Log.i("backBtnClicked", "View")
        telloAction(TelloActions.BACK)
    }

    fun rightBtnClicked(view: View?) {
        Log.i("rightBtnClicked", "View")
        telloAction(TelloActions.RIGHT)
    }

    private fun telloAction(action: TelloActions){
        Thread{
            try {
                if (tello?.isConnected!!){
                    when(action){
                        TelloActions.TAKEOFF -> tello?.takeOff()
                        TelloActions.LAND -> tello?.land()
                        TelloActions.END -> {
                            askFlag = false
                            tello?.close()
                        }
                        TelloActions.UP -> tello?.up(MOVEMENT_RANGE)
                        TelloActions.CCW -> tello?.ccw(MOVEMENT_RANGE)
                        TelloActions.DOWN -> tello?.down(MOVEMENT_RANGE)
                        TelloActions.CW -> tello?.cw(MOVEMENT_RANGE)
                        TelloActions.FORWARD -> tello?.forward(MOVEMENT_RANGE)
                        TelloActions.LEFT -> tello?.left(MOVEMENT_RANGE)
                        TelloActions.BACK -> tello?.back(MOVEMENT_RANGE)
                        TelloActions.RIGHT -> tello?.right(MOVEMENT_RANGE)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Please connect to Tello", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                print(e)
            }
        }.start()
    }
}