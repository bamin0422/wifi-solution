package com.CVproject.wifi_solution

import android.hardware.Camera
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_fragment_scan.view.*

class Fragment_scan : Fragment(){

    var mCamera : Camera? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_fragment_scan, container, false)
        setupCamera(view)
        return view
    }



    private fun setupCamera(view: View?) {
        if(mCamera == null){
            mCamera = Camera.open()
        }
        var cameraPreview = CameraPreview(view?.context, mCamera!!)
        view?.camera_frameLayout?.addView(cameraPreview)
    }


}