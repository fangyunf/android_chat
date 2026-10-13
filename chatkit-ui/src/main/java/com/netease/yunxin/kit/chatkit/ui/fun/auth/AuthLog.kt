package com.netease.yunxin.kit.chatkit.ui.`fun`.auth

import android.util.Log

object AuthLog {
    private const val TAG = "YxAuth"

    fun i(msg: String) = Log.i(TAG, msg)

    fun w(msg: String) = Log.w(TAG, msg)

    fun maskCode(code: String?): String {
        val c = code?.trim().orEmpty()
        if (c.length <= 4) return "****"
        return c.take(2) + "****" + c.takeLast(2)
    }

    fun maskMac(mac: String?): String {
        val m = mac?.trim().orEmpty()
        if (m.length <= 6) return "****"
        return m.take(3) + "****" + m.takeLast(3)
    }

    fun maskStatus(status: String?): String {
        val s = status?.trim().orEmpty()
        if (s.length <= 6) return s.ifEmpty { "-" }
        return s.take(4) + "…"
    }

    fun snap(code: String?, expire: String?, status: String?, authorized: Boolean): String =
        "code=${maskCode(code)} exp=${expire ?: "-"} sc=${maskStatus(status)} ok=$authorized"
}
