package com.CVproject.wifi_solution


import android.content.Context
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_fragment__q_r.*
import kotlinx.android.synthetic.main.activity_fragment__q_r.view.*
import com.CVproject.wifi_solution.Fragment_now
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class Fragment_QR : Fragment(){

    lateinit var mAdView: AdView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.activity_fragment__q_r, container, false)

        var fragmentNow = Fragment_now()

        var myRef = FirebaseDatabase.getInstance().reference

        // QR 코드 생성
        var currentWifi: CurWifi? = null
        var wifiID : String = ""
        var wifiPW : String = ""
        var wifiManager = view.context?.getSystemService(Context.WIFI_SERVICE) as WifiManager


        mAdView = view.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        var e = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children){
                    if(data.key.equals("curWifi")) {
                        if(data.child("ssid").key.equals("ssid")){
                            NetworkConnector(wifiManager, context).getCurSSID()
                            wifiID = data.child("ssid").value as String
                        }
                        if(data.child("password").key.equals("password")){
                            wifiPW = data.child("password").value as String
                        }
                    }
                }
            }
        }
        myRef.addValueEventListener(e)

        view.btn_qrmaker.setOnClickListener {
                if (wifiID == "" || wifiPW == "-"){
                    wifiID = wifiID.replace("\"", "") // remove "" in QR's name
                    wifi_name_QR.setText("wifi를 다시 연결해 주십시오.")
                }
                else {
                    wifiID = wifiID.replace("\"", "") // remove "" in QR's name
                    currentWifi = CurWifi(wifiID, wifiPW, "WPA")
                    QR_image.setImageBitmap(QRmaker(currentWifi.toString()).makeQRBitmap())
                    wifi_name_QR.setText(currentWifi?.ssid)
                }

        }

        return view
    }
}