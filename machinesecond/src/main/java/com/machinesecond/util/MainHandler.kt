package com.machinesecond.util

import android.os.Handler
import android.os.Looper

object MainHandler {
    private val handler = Handler(Looper.getMainLooper())

    fun post(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            handler.post(block)
        }
    }

    fun postDelayed(delayMs: Long, block: () -> Unit) {
        handler.postDelayed({ block() }, delayMs)
    }

    fun postDelayed(delayMs: Long, runnable: Runnable) {
        handler.postDelayed(runnable, delayMs)
    }

    fun removeCallbacks(runnable: Runnable) {
        handler.removeCallbacks(runnable)
    }
}
