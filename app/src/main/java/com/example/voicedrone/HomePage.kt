package com.example.voicedrone

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.text.method.Touch
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
        startActivity(intent)
    }
}