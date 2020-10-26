package com.example.voicedrone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import KTello
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.Button

class TouchPage : AppCompatActivity() {
    private var connectTello : ConnectTello? = null

    val MOVEMENT_RANGE = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.touch_page)

        val takeoffBtn = findViewById<Button>(R.id.takeoffBtn)

        val landBtn = findViewById<Button>(R.id.landBtn)

        connectTello = ConnectTello(takeoffBtn, landBtn)

        connectTello!!.execute()
    }

    fun takeoffBtnClicked(view: View?) {
        Log.w("takeoffBtnClicked", "View")
        connectTello?.takeoffBtnClicked()
//        tello?.takeOff()
    }

    fun landBtnClicked(view: View?) {
        Log.w("landBtnClicked", "View")
        connectTello?.landBtnClicked()
//        tello?.land()
    }

//    fun endBtnClicked(view: View?) {
////        Log.w("BtnClicked", "View")
////        connectTello.landBtnClicked()
////        tello?.close()
//    }
//
//    fun upBtnClicked(view: View?) {
////        tello?.up(MOVEMENT_RANGE)
//    }
//
//    fun ccwBtnClicked(view: View?) {
////        tello?.ccw(MOVEMENT_RANGE)
//    }
//
//    fun downBtnClicked(view: View?) {
////        tello?.down(MOVEMENT_RANGE)
//    }
//
//    fun cwBtnClicked(view: View?) {
////        tello?.cw(MOVEMENT_RANGE)
//    }
//
//    fun forwardBtnClicked(view: View?) {
////        tello?.forward(MOVEMENT_RANGE)
//    }
//
//    fun leftBtnClicked(view: View?) {
////        tello?.left(MOVEMENT_RANGE)
//    }
//
//    fun backBtnClicked(view: View?) {
////        tello?.back(MOVEMENT_RANGE)
//    }
//
//    fun rightBtnClicked(view: View?) {
////        tello?.right(MOVEMENT_RANGE)
//    }

    inner class ConnectTello : AsyncTask<String, Void, String> {
        private var takeoffBtn: Button? = null
        private var landBtn: Button? = null


        private var tello: KTello? = null
        private var flags = mapOf(
            "takeoff" to false,
            "land" to false
        )

        constructor(takeoffBtn: Button, landBtn: Button) {
            this.takeoffBtn = takeoffBtn
            this.landBtn = landBtn
        }

        override fun doInBackground(vararg params: String): String? {
            tello = KTello()
            tello?.connect()

//            takeoffBtn?.setOnClickListener { view ->
//                Log.w("takeoffBtnClicked", "Async")
//                tello?.takeOff()
//            }
//
//            landBtn?.setOnClickListener { view ->
//                Log.w("BtnClicked", "Async")
//                tello?.land()
//            }



            return ""
        }

        override fun onPostExecute(string: String?) {
        }

        fun takeoffBtnClicked() {
            Log.w("takeoffBtnClicked", "Async")
            val t = Thread {
                Log.w("takeoffBtnClicked", "Async")
                tello?.takeOff()
            }
            t.start()
//            if (!(flags["takeoff"] ?: error("non flag"))){
//                tello?.takeOff()
//            }
        }

        fun landBtnClicked() {
            val t = Thread{
                Log.w("landBtnClicked", "Async")
                tello?.land()
            }
            t.start()
        }
    }
}