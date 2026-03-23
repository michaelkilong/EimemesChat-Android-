package com.eimemes.chat.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    fun now(): String = SimpleDateFormat("h:mm a", Locale.US).format(Date())
    fun timestamp(): Long = System.currentTimeMillis()
}
