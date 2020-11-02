package com.example.voicedrone

import KTello

// KTelloインスタンスを保持するためのクラス (Voiceでドローンを動かした後にTouchで操作する際に利用)
object KTelloHandler {
    @get:Synchronized
    @set:Synchronized
    var tello: KTello? = null
}