package com.CVproject.wifi_solution

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import kotlinx.android.synthetic.main.activity_fragment_now.view.*

class Fragment_now : Fragment(){


    lateinit var wifiManager: WifiManager
    lateinit var mAdapter: RecyclerAdapter

    private lateinit var recyclerView: RecyclerView
    private val wifiScanReceiver = object: BroadcastReceiver(){
        override fun onReceive(c: Context?, intent: Intent?) { // wifiManager.startscan() 시 발동
            Toast.makeText(context, "제발!!", Toast.LENGTH_SHORT).show()
            var suc = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)

            if(suc == true){
                scanSuccess()
            }else{
                scanFailure()
            }
        }
    }

    // Wifi검색 성공
    private fun scanSuccess() {
        var results: List<ScanResult> = wifiManager.scanResults
        mAdapter = RecyclerAdapter(results)
        recyclerView.adapter = mAdapter
    }

    // Wifi검색 실패
    private fun scanFailure() {
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.activity_fragment_now, container, false)
        recyclerView = view.findViewById(R.id.wifi_list)



        // wifi scan 관련
        wifiManager = view.context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var intentFilter= IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        view.context.registerReceiver(wifiScanReceiver, intentFilter)

        // wifi scan 시작
        var success = wifiManager.startScan()

        // refresh 버튼 클릭 시
        view.reScanWifi.setOnClickListener {
            success = wifiManager.startScan()
            if(!success) Toast.makeText(view.context.applicationContext, "wifi 스캔에 실패하였습니다.", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}