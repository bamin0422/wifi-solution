package com.CVproject.wifi_solution


import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_fragment__q_r.*
import kotlinx.android.synthetic.main.activity_fragment__q_r.view.*

class Fragment_QR : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.activity_fragment__q_r, container, false)

        // QR 코드 생성
        var currentWifi = "TP-LINK-98"

        view.btn_qrmaker.setOnClickListener {
                view -> QR_image.setImageBitmap(QRmaker(currentWifi).makeQRBitmap())
                wifi_name_QR.setText(currentWifi.toString())
        }

        return view
    }
}