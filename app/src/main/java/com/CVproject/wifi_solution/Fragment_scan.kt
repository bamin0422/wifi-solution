package com.CVproject.wifi_solution

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_fragment_scan.*
import kotlinx.android.synthetic.main.activity_fragment_scan.view.*
import kotlinx.android.synthetic.main.activity_fragment_scan.view.viewFinder
import java.nio.ByteBuffer

class Fragment_scan : Fragment(){

    var imageCapture : ImageCapture? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_fragment_scan, container, false)

        bindCameraUseCase(view)
        view.imageButton.setOnClickListener {
            takePhoto(view)
        }

        return view
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
                    for(line in block.lines){
                        var lineText = line.text
                        view!!.textView.setText(lineText)
                    }
                }
            }
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