package com.CVproject.wifi_solution

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class WifiDialog(context: Context?) {
    private val dlg = Dialog(context!!) // 부모 액티비티의 context가 들어감
    private lateinit var lblDesc: TextView
    private lateinit var wifiPW: EditText
    private lateinit var btnOK: Button
    private lateinit var btnCancel: Button
    private lateinit var listener: WifiDialogOKClickedListener

    fun start(content: String){
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀바 제거
        dlg.setContentView(R.layout.wifi_dialog) // 다이얼로그에 사용할 xml 파일 부르기
        dlg.setCancelable(false) // 다이얼로그 바깥 화면 눌렀을 때 다이얼로그가 닫히지 않도록 함.

        // TextView 설정
        lblDesc = dlg.findViewById(R.id.wifi_connect_tv)
        lblDesc.text = content

        // EditText 설정
        wifiPW = dlg.findViewById(R.id.wifi_pw)

        // ok button 설정
        btnOK = dlg.findViewById(R.id.ok)
        btnOK.setOnClickListener{
            listener.onOkClicked(wifiPW.text.toString())
            dlg.dismiss()
        }

        btnCancel = dlg.findViewById(R.id.cancel)
        btnCancel.setOnClickListener {
            dlg.dismiss()
        }
        dlg.show()
    }

    fun setOnOKClickedListener(listener: (String) -> Unit){
        this.listener = object : WifiDialogOKClickedListener{
            override fun onOkClicked(content: String) {
                listener(content)
            }
        }
    }

    interface WifiDialogOKClickedListener {
        fun onOkClicked(content: String)
    }
}