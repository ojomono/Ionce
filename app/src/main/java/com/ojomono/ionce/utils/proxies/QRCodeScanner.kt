package com.ojomono.ionce.utils.proxies

import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector.*
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.Barcode.QR_CODE
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.ojomono.ionce.utils.TAG

/**
 * Handles QR-code scanning - wrapping play-services-vision.
 */
object QRCodeScanner {

    private val processor = object : Processor<Barcode> {

        override fun receiveDetections(detections: Detections<Barcode>?) {
            detections?.apply {
                if (detectedItems.isNotEmpty()) {
                    val qr = detectedItems.valueAt(0)
                    // Parses the WiFi format for you and gives the field values directly
                    // Similarly you can do qr.sms for SMS QR code etc.
                    qr.wifi.let {
                        Log.d(TAG, "SSID: ${it.ssid}, Password: ${it.password}")
                    }
                }
            }
        }

        override fun release() {}
    }

    fun setupCameraView(context: Context) {

//        BarcodeDetector.Builder(context).setBarcodeFormats(QR_CODE).build().apply {
//            setProcessor(processor)
//            if (!isOperational) {
//                Log.d(TAG, "Native QR detector dependencies not available!")
//                return
//            }
//            val cameraSource =
//                CameraSource.Builder(context, this).setAutoFocusEnabled(true)
//                    .setFacing(CameraSource.CAMERA_FACING_BACK).build()
//        }
    }
}