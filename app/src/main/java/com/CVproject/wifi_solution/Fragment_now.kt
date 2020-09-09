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
import android.view.animation.Animation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_fragment_now.view.*


class Fragment_now : Fragment() {


    // 전역변수로 바꿈
    lateinit var results: MutableList<ScanResult>
    var myRef = FirebaseDatabase.getInstance().reference
    lateinit var wifiManager: WifiManager
    lateinit var mAdapter: RecyclerAdapter
    lateinit var rotateAnimation: Animation
    public var wifiPW = "TESTPW"
    public var wifiID = "TESTID"

    private lateinit var recyclerView: RecyclerView
    private val wifiScanReceiver = object: BroadcastReceiver(){
        override fun onReceive(c: Context?, intent: Intent?) { // wifiManager.startscan() 시 발동

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
        results = wifiManager.scanResults
        mAdapter = RecyclerAdapter(results){scanResult ->
            makeDialog(view, scanResult)
            scanResult.capabilities
        }
        view?.wifi_list?.adapter = mAdapter
        view?.TV_wifiCounter?.setText("총 ${mAdapter.itemCount}개의 wifi가 있습니다.")

    }

    private fun makeDialog(view: View?, wifiSelected: ScanResult) {
        val dlg = WifiDialog(view?.context)
        wifiID = wifiSelected.SSID
        dlg.setOnOKClickedListener { content ->
            wifiPW = content
            wifiManager = view?.context?.getSystemService(Context.WIFI_SERVICE) as WifiManager

            // wifi 연결
            NetworkConnector(wifiManager, context).connectWifi(wifiID, wifiPW)
        }
        dlg.start(wifiID+"에 연결하시겠습니까?")

    }

    // Wifi검색 실패
    private fun scanFailure() {
        view?.TV_wifiCounter?.setText("wifi 탐색에 실패하였습니다.")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        val view = inflater.inflate(R.layout.activity_fragment_now, container, false)


        recyclerView = view.findViewById(R.id.wifi_list)



        // wifi scan 관련
        wifiManager = view.context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.setWifiEnabled(true)
        var intentFilter= IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        view.context.registerReceiver(wifiScanReceiver, intentFilter)

        // wifi scan 시작
        var success = wifiManager.startScan()

        // refresh 버튼 클릭 시 작동
        view.reScanWifi.setOnClickListener {
            success = wifiManager.startScan()
            myRef.child("wifiList").setValue(null)
            if(!success) Toast.makeText(view.context.applicationContext, "wifi 스캔에 실패하였습니다.", Toast.LENGTH_SHORT).show()
        }

        // divider 추가
        val decoration =DividerItemDecoration(view.context?.applicationContext, VERTICAL)
        view.wifi_list.addItemDecoration(decoration)

        return view
    }
}
