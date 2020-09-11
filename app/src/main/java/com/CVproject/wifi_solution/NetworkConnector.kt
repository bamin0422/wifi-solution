package com.CVproject.wifi_solution

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import com.google.firebase.database.FirebaseDatabase

class NetworkConnector(private val wifiManager: WifiManager, private val context: Context?) {

    var myRef = FirebaseDatabase.getInstance().reference

    fun connectWifi(ssid: String, pw: String){
        when{
            // WifiNetworkSuggestion 버전 29이상일 때만 사용
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {

                // 네트워크 Suggestion 삭제
                wifiManager.removeNetworkSuggestions(
                    listOf(
                        WifiNetworkSuggestion.Builder()
                            .setSsid(ssid)
                            .build()
                        ,WifiNetworkSuggestion.Builder()
                            .setSsid(ssid)
                            .setWpa2Passphrase(pw)
                            .build()
                        ,WifiNetworkSuggestion.Builder()
                            .setSsid(ssid)
                            .setWpa3Passphrase(pw)
                            .build()
                    )
                )

                // 네트워크 Suggestion 추가
                var status = wifiManager.addNetworkSuggestions(
                    listOf(
                        WifiNetworkSuggestion.Builder()
                            .setSsid(ssid)
                            .build()
                        ,WifiNetworkSuggestion.Builder()
                            .setSsid(ssid)
                            .setWpa2Passphrase(pw)
                            .build()
                        ,WifiNetworkSuggestion.Builder()
                            .setSsid(ssid)
                            .setWpa3Passphrase(pw)
                            .build()
                    )
                )
                if(status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS){
                    myRef.child("curWifi").setValue(CurWifi(ssid, pw, "WPA"))
                }

            }

            else -> {
                val wifiConfig = WifiConfiguration().apply {
                    SSID = String.format("\"%s\"", ssid)
                    preSharedKey = String.format("\"%s\"", pw)
                }
                with(wifiManager) {
                    val netId = addNetwork(wifiConfig)
                    disconnect()
                    enableNetwork(netId, true)
                    reconnect()
                }
                if(isWifiConnected(context)){
                    myRef.child("curWifi").setValue(CurWifi(ssid, pw, "WPA"))
                }
            }
        }
    }

    private fun isWifiConnected(context: Context?): Boolean {

        var result = false
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        if (capabilities != null){
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            {
                result = true
            }
            else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                result = false
            }
        }
        return result
    }
}
