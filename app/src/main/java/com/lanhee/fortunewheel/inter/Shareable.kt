package com.lanhee.fortunewheel.inter

import java.io.File

interface Shareable {
    fun share(file: File): Unit
}