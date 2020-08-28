package com.CVproject.wifi_solution

import android.content.Context
import android.graphics.Camera
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_fragment_scan.*

class Fragment_scan : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.activity_fragment_scan, container, false)
        return view
    }



}