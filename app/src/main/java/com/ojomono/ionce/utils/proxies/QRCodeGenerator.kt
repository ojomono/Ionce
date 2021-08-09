package com.ojomono.ionce.utils.proxies

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
    const val QR_CODE_SIZE = 512
    const val QR_CODE_PREFIX = "ionce:" // TODO find cleaner solution for identifying relevant QRs

    /**
     * Encode the given [contents], and return as QR-code bitmap.
     */
    fun generate(contents: String): Bitmap {

        // Make the QR code buffer border narrower
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 }

        // Generate the QR code
        val bits = QRCodeWriter().encode(
            QR_CODE_PREFIX + contents,
            BarcodeFormat.QR_CODE,
            QR_CODE_SIZE,
            QR_CODE_SIZE,
            hints
        )

        // Return QR code as bitmap
        return Bitmap.createBitmap(QR_CODE_SIZE, QR_CODE_SIZE, Bitmap.Config.RGB_565).also {
            for (x in 0 until QR_CODE_SIZE) {
                for (y in 0 until QR_CODE_SIZE) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}