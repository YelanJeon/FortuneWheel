package com.lanhee.fortunewheel.inter

interface OnRouletteListener {
    fun onRouletteClick(position: Int)
    fun onRouletteStop(position: Int)
}