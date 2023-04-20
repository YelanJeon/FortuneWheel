package com.lanhee.fortunewheel.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

class ScreenCaptureHelper(private val captureView: View) {

    fun capture(listener: OnCaptureView? = null) {
        var bitmap = Bitmap.createBitmap(captureView.width, captureView.height, Bitmap.Config.ARGB_8888)

        var canvas = Canvas(bitmap)
        captureView.draw(canvas)

        listener?.onCapture(bitmap)
    }

    interface OnCaptureView {
        fun onCapture(bitmap: Bitmap)
    }

}