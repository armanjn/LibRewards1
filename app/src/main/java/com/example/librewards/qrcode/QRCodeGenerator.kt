package com.example.librewards.qrcode

import android.graphics.Bitmap
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetEncoder
import java.util.*
import kotlin.collections.HashMap


class QRCodeGenerator {
    // Function to create the QR code
    @Throws(WriterException::class, IOException::class)
    fun createQR(data : String, height: Int,
                 width: Int): Bitmap {
        var bitmap : Bitmap? = null
        val charset: Charset = Charsets.UTF_8
        val hints : Hashtable<EncodeHintType, String> = Hashtable(2);
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        val matrix : BitMatrix = MultiFormatWriter().encode(
                String(data.toByteArray(charset), charset),
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints)
        try{
            val encoder = BarcodeEncoder()
            bitmap = encoder.createBitmap(matrix)

        }
        catch (e: Exception){
            Log.e("TAG", "Failed to create image")
        }
        return bitmap!!
    }
}

