package com.CVproject.wifi_solution

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_fragment_scan.*
import kotlinx.android.synthetic.main.activity_fragment_scan.view.*

class Fragment_scan : Fragment(), Fragment_now.OnResultListener{

    var imageCapture : ImageCapture? = null
    var lineText: String? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // View가 Recreate 시 처리
        val restoreValue = arguments?.getString(keyRestore)
        if(restoreValue != null){
            // action to do something
        }

        val view = inflater.inflate(R.layout.activity_fragment_scan, container, false)

        bindCameraUseCase(view)
        view.imageButton.setOnClickListener {
            takePhoto(view)
        }


        return view
    }

    private fun showFragment_now(){
        parentFragmentManager.commit {
            replace(R.id.scan_layout,Fragment_now().apply {
                // FragmentB 표시할 때 Listener을 전달
                setListener(this@Fragment_scan)
            })
            addToBackStack(null)
        }
    }

    // Fragment_now.onResultListener을 실행
    override fun onResult(value: List<ScanResult>) {
        // Fragment가 Visible중일 때 처리
        if(isVisible){
            Toast.makeText(context,"Visible 중...", Toast.LENGTH_SHORT).show()
        }
        // Visible이 아닌 경우, Fragment#Argument에 데이터 저장
        else{
            arguments = (arguments?:Bundle()).also {
                it.putString(keyRestore, value.toString())
            }

        }
    }

    companion object{
        private const val keyRestore = "resultRestore"
    }

    fun takePhoto(view: View){
        imageCapture?.takePicture(ContextCompat.getMainExecutor(view.context), object : ImageCapture.OnImageCapturedCallback(){
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = imageProxyToBitmap(image)
                textRecognize(bitmap)
                super.onCaptureSuccess(image)
            }
        })
    }
    fun textRecognize(bitmap: Bitmap){
        FirebaseVision.getInstance().onDeviceTextRecognizer.processImage(FirebaseVisionImage.fromBitmap(bitmap))
            .addOnSuccessListener {firebaseVisionText ->
                for(block in firebaseVisionText.textBlocks){

                    lineText = block.text
                    view?.textView?.setText(lineText)
                    showPasswordPopup()
                    break

                }
            }
    }


    private fun showPasswordPopup() {
     val inflater = view?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.password_popup, null)
        var password: TextView = view.findViewById(R.id.editText)
        password.text = lineText

        val alertDialog = AlertDialog.Builder(view.context)
            .setTitle("비밀번호 확인")
            .setPositiveButton("확인"){ dialog, which ->
                textView.text = "${password.text}"
                startScan(textView.text.toString())
            }
            .setNeutralButton("취소",null)
            .create()

        alertDialog.setView(view)
        alertDialog.show()
    }



    private fun startScan(password: String) {
    }


    fun imageProxyToBitmap(imageProxy: ImageProxy) : Bitmap{
        val buffer = imageProxy.planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // Rotate bitmap
        val matrix = Matrix()
        matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

        return Bitmap.createBitmap(bitmap,0,0,bitmap.width, bitmap.height,matrix,true)
    }

    fun bindCameraUseCase(view: View){
        val rotation = 0
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(view.context)
        cameraProviderFuture?.addListener(Runnable {

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetRotation(rotation)
                .build()

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(960,1280))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(rotation)
                .build()

            cameraProvider.unbindAll()

            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.cameraInfo))

        },ContextCompat.getMainExecutor(view.context))
    }


}