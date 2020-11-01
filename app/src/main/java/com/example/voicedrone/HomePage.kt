package com.example.voicedrone

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class HomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
    }

    fun onClickVoiceOrder(view: View?) {
        val intent = Intent(application, WiFiSelectPage::class.java)
        startActivity(intent)
    }

    fun onClickTouchOrder(view: View?) {
        val intent = Intent(application, TouchPage::class.java)
        intent.putExtra("activity", EnumActivity.Home)
        startActivity(intent)
    }
}