package com.example.voicedrone

import KTello

object KTelloHandler {
    @get:Synchronized
    @set:Synchronized
    var tello: KTello? = null

}