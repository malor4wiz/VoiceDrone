package com.example.voicedrone

// WiFiの切り替えをする際に利用
object WiFiData {
    @get:Synchronized
    @set:Synchronized
    var internetWiFiID = ""
    var droneWiFiID = ""
    var internetWiFiPass = ""
    var droneWiFiPass = ""
}