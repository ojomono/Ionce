package com.ojomono.ionce.utils.images

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Handles QR-code generating - wrapping zxing.
 */
object QRCodeGenerator {

    // QRCode constants
    const val QRCODE_SIZE = 512

    /**
     * Encode the given [contents], and return as QR-code bitmap.
     */
    fun generateQRCode(contents: String): Bitmap {

        // Make the QR code buffer border narrower
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 }

        // Generate the QR code
        val bits =
            QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints)

        // Return QR code as bitmap
        return Bitmap.createBitmap(QRCODE_SIZE, QRCODE_SIZE, Bitmap.Config.RGB_565).also {
            for (x in 0 until QRCODE_SIZE) {
                for (y in 0 until QRCODE_SIZE) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}