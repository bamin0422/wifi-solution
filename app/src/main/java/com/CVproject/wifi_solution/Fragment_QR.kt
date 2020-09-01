package com.CVproject.wifi_solution


import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_fragment__q_r.*
import kotlinx.android.synthetic.main.activity_fragment__q_r.view.*
import com.CVproject.wifi_solution.Fragment_now

class Fragment_QR : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.activity_fragment__q_r, container, false)

        var fragmentNow = Fragment_now()
        val json = WithFi(fragmentNow.wifiID, fragmentNow.wifiPW, fragmentNow.wifiSecurityType.toSecurityType()).TJ()


        // QR 코드 생성
        var currentWifi = fragmentNow.wifiID

        view.btn_qrmaker.setOnClickListener {
                view -> QR_image.setImageBitmap(QRmaker(json).makeQRBitmap())
                wifi_name_QR.setText(currentWifi)
        }

        return view
    }
}