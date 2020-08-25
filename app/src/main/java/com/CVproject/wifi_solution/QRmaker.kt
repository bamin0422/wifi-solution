package com.CVproject.wifi_solution

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

class QRmaker(currentWifi: String) {
    val currentwifi: String

    fun makeQRBitmap(): Bitmap {
        val multiFormatWriter = MultiFormatWriter()
        var bitmatrix: BitMatrix = multiFormatWriter.encode(currentwifi, BarcodeFormat.QR_CODE, 700, 700)
        val barcodeEncoder = BarcodeEncoder()
        val bitmap: Bitmap = barcodeEncoder.createBitmap(bitmatrix)

        return bitmap
    }

    init {
        currentwifi = currentWifi
    }
}
