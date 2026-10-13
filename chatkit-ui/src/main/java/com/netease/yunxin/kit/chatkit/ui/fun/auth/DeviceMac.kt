package com.netease.yunxin.kit.chatkit.ui.`fun`.auth

import android.content.Context
import java.util.UUID

/**
 * 易游 Mac（机器码）上限 32 位（-121）。
 * UUID 标准串带连字符是 36，必须去连字符后才合法。
 */
object DeviceMac {
    private const val SP = "com.netease.yunxin.yx.device"
    private const val KEY = "deviceUUID"
    private const val MAX_LEN = 32

    fun forContext(context: Context): String {
        val sp = context.applicationContext.getSharedPreferences(SP, Context.MODE_PRIVATE)
        var raw = sp.getString(KEY, null)
        val created = raw.isNullOrBlank()
        if (created) {
            raw = UUID.randomUUID().toString()
            sp.edit().putString(KEY, raw).apply()
            AuthLog.i("deviceMac NEW rawLen=${raw!!.length}")
        }
        val mac = normalize(raw!!)
        AuthLog.i("deviceMac ${if (created) "created" else "reuse"} ${AuthLog.maskMac(mac)}")
        return mac
    }

    /** 去非字母数字并截断到 32，保证可过 eydata */
    fun normalize(raw: String): String {
        val compact = raw.filter { it.isLetterOrDigit() }
        return if (compact.length <= MAX_LEN) compact else compact.take(MAX_LEN)
    }
}
