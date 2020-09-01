package com.CVproject.wifi_solution

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import org.json.JSONObject


data class WithFi(var wifiID: String, var wifiPW: String, var wifiST: SecurityType) {
    companion object{
        fun JToS(json: String?): WithFi? {
            val jsonObject: JsonObject
            try {
                jsonObject = JsonParser().parse(json).asJsonObject
            } catch (e: JsonParseException) {
                return null
            }
            return WithFi(jsonObject.get("ssid").asString,
                jsonObject.get("password").asString,
                jsonObject.get("securityType").asString.toSecurityType())
        }
    }

    fun TJ(): String = JSONObject().apply {
        put("ssid", wifiID)
        put("password", wifiPW)
        put("securityType", wifiST)
    }.toString()
}

fun String.toSecurityType(): SecurityType = when(this){
    "WPA" -> SecurityType.WPA
    "WPA2" -> SecurityType.WPA2
    "WEP" -> SecurityType.WEP
    else -> SecurityType.NONE

}

enum class SecurityType(val Name: String) {
    WPA("WPA"),
    WPA2("WPA2"),
    WEP("WEP"),
    NONE("NONE");

    override fun toString(): String = Name
}
