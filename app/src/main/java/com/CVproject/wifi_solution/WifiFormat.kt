package com.CVproject.wifi_solution

class WifiFormat (ssid: String, password: String){
    public var ssid = ssid
    public var password = password

    override fun toString(): String {
        return "WIFI:T:WPA;S:"+ssid+";P:"+password+";;"
    }
}
