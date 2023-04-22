package com.lanhee.fortunewheel.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import com.lanhee.fortunewheel.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class ScreenCaptureHelper(private val captureView: View) {

    fun captureAndSave(listener: OnCaptureView? = null) {
        var bitmap = Bitmap.createBitmap(captureView.width, captureView.height, Bitmap.Config.ARGB_8888)

        var canvas = Canvas(bitmap)
        captureView.draw(canvas)

        val imgFile = bitmapToFile(bitmap, getPathToSaveCapture(captureView.context))
        val uri = FileProvider.getUriForFile(captureView.context, BuildConfig.APPLICATION_ID+".fileprovider", imgFile)

        listener?.onCapture(uri)
    }

    private fun bitmapToFile(bitmap: Bitmap, outPath: String): File {
        val imageFile = File(outPath)
        val outputStream = FileOutputStream(imageFile)
        outputStream.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
        }
        return imageFile
    }

    private fun getPathToSaveCapture(context: Context): String {
        val now = SimpleDateFormat("yyyyMMdd_hhmmss").format(Date(System.currentTimeMillis()))
        return context.cacheDir.absolutePath+"/$now.jpg"
    }

    interface OnCaptureView {
        fun onCapture(uri: Uri)
    }

    interface Captureable {
        fun getCaptureView(): View
    }
}