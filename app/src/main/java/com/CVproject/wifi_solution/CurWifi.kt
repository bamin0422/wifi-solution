package com.CVproject.wifi_solution

class CurWifi (ssid: String, password: String, securityType: String){
    public var ssid = ssid
    public var password = password
    public var securityType = securityType

    override fun toString(): String {
        return "WIFI:T:"+securityType+";S:"+ssid+";P:"+password+";;"
    }
}
