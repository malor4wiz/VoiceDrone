package com.example.voicedrone.pages

import KTello
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voicedrone.EnumActivity
import com.example.voicedrone.KTelloHandler
import com.example.voicedrone.R
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

        when(intent.getSerializableExtra("activity")) {
            EnumActivity.Home -> {
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
            EnumActivity.RecordResult -> {
                tello = KTelloHandler.tello

                Thread{
                    try {
                        if (!(tello?.isConnected!!)){
                            tello?.connect()
                        }
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
        }
    }

    private fun askTello(){
        while (askFlag){
            try {
                val battery: String? = tello?.battery + "%"

                runOnUiThread{
                    if (battery?.substring(0, 2) != "ok") {
                        batteryLabel?.text = battery
                    }
                }

                Thread.sleep(500)

                val time: String? = tello?.time?.substring(0, 3)

                runOnUiThread{
                    if (time?.substring(0, 2) != "ok") {
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
        Log.i("TouchPage", "takeoffBtnClicked")
        telloAction(TelloActions.TAKEOFF)
    }

    fun landBtnClicked(view: View?) {
        Log.i("TouchPage", "landBtnClicked")
        telloAction(TelloActions.LAND)
    }

    fun endBtnClicked(view: View?) {
        Log.i("TouchPage", "endBtnClicked")
        telloAction(TelloActions.END)
    }

    fun upBtnClicked(view: View?) {
        Log.i("TouchPage", "upBtnClicked")
        telloAction(TelloActions.UP)
    }

    fun ccwBtnClicked(view: View?) {
        Log.i("TouchPage", "ccwBtnClicked")
        telloAction(TelloActions.CCW)
    }

    fun downBtnClicked(view: View?) {
        Log.i("TouchPage", "downBtnClicked")
        telloAction(TelloActions.DOWN)
    }

    fun cwBtnClicked(view: View?) {
        Log.i("TouchPage", "cwBtnClicked")
        telloAction(TelloActions.CW)
    }

    fun forwardBtnClicked(view: View?) {
        Log.i("TouchPage", "forwardBtnClicked")
        telloAction(TelloActions.FORWARD)
    }

    fun leftBtnClicked(view: View?) {
        Log.i("TouchPage", "leftBtnClicked")
        telloAction(TelloActions.LEFT)
    }

    fun backBtnClicked(view: View?) {
        Log.i("TouchPage", "backBtnClicked")
        telloAction(TelloActions.BACK)
    }

    fun rightBtnClicked(view: View?) {
        Log.i("TouchPage", "rightBtnClicked")
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
                            runOnUiThread{
                                val intent = Intent(applicationContext, HomePage::class.java)
                                startActivity(intent)
                            }
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
                runOnUiThread{
                    Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}