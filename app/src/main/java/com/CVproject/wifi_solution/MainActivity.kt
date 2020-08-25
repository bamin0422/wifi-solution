package com.CVproject.wifi_solution

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setFrag(0)

        btn_wifiList.setOnClickListener {
            setFrag(0)
        }

        btn_scan.setOnClickListener {
            setFrag(1)
        }

        btn_QR.setOnClickListener {
            setFrag(2)
        }
    }

    private fun setFrag(fragNum: Int) {
        val ft = supportFragmentManager.beginTransaction()
        when (fragNum) {
            0 -> ft.replace(R.id.main_frame, Fragment_now()).commit()

            1 -> ft.replace(R.id.main_frame, Fragment_scan()).commit()

            2 -> ft.replace(R.id.main_frame, Fragment_QR()).commit()

        }
    }
}