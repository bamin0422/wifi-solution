package com.CVproject.wifi_solution

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_fragment_now.view.*

class Fragment_now : Fragment() {

    // fragment 간의 통신용 Listener 정의
    interface OnResultListener{
        fun onResult(value: List<ScanResult>)
    }

    private var listener: OnResultListener? = null

    // 외부에서 전달할 Setter Listener
    fun setListener(listener: OnResultListener){
        this.listener = listener

    }

    private fun clickDone(){
        listener?.onResult(results)
        parentFragmentManager.popBackStack()
    }

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

        clickDone()
    }

    private fun makeDialog(view: View?, wifiSelected: ScanResult) {
        val dlg = WifiDialog(view?.context)
        wifiID = wifiSelected.SSID
        dlg.setOnOKClickedListener { content ->
            wifiPW = content
            wifiManager = view?.context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
            var curwifi = CurWifi(wifiID, wifiPW, "WPA")

            var suggestionList = suggestion(wifiID, wifiPW)

            val status = wifiManager.addNetworkSuggestions(suggestionList)


            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
            if(isConnected){
                myRef.child("curWifi").setValue(curwifi)
            }
        }
        dlg.start(wifiID+"에 연결하시겠습니까?")

    }

    private fun suggestion(wifiID: String, wifiPW: String): List<WifiNetworkSuggestion> {
        val suggestion1 = WifiNetworkSuggestion.Builder().setSsid(wifiID).setWpa2Passphrase(wifiPW).build()
        val suggestion2 = WifiNetworkSuggestion.Builder().setSsid(wifiID).setWpa3Passphrase(wifiPW).build()

        return listOf(suggestion1, suggestion2)
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
