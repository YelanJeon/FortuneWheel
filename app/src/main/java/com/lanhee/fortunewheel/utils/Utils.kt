package com.lanhee.fortunewheel.utils

import android.content.Context
import android.util.TypedValue

class Utils {
    companion object {
        fun dimensionToPixel(context: Context, dp: Float) : Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        }

        fun getScreenWidth(context: Context) : Int{
            return context.resources.displayMetrics.widthPixels
        }

        fun getScreenHeight(context: Context) : Int{
            return context.resources.displayMetrics.heightPixels
        }


    }

}