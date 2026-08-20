package com.machinesecond.title

import kotlin.math.floor

object MineTitleParser {

    val DEFAULT_SEPS = listOf(
        "-", "/", "\\", "：", "；", "¥", "@", "。", "，", "、",
        "？", "！", ".", "【", "】", "｛", "｝", "#", "%", "^",
        "*", "+", "=", "_", "—", "\\", "｜", "～", "$", "·"
    )

    fun digitsOnly(s: String): String = s.filter { it in '0'..'9' }

    fun separatedIndex(title: String, userSep: String): Int {
        if (title.length < 2) return -1
        val set = (listOfNotNull(userSep.takeIf { it.isNotEmpty() }) + DEFAULT_SEPS).toSet()
        for (i in 0 until title.length - 1) {
            if (title[i].toString() in set) return i
        }
        return -1
    }

    fun mineSegment(title: String, userSep: String, amountHint: String): String? {
        if (title.isEmpty()) return null
        val idx = separatedIndex(title, userSep)
        if (idx >= 0) {
            val mine = title.substring(idx + 1)
            if (digitsOnly(mine).isNotEmpty()) return mine
        }
        val td = digitsOnly(title)
        val ad = digitsOnly(amountHint)
        if (ad.isNotEmpty() && td.length > ad.length && td.startsWith(ad)) {
            return td.substring(ad.length)
        }
        return td.takeIf { it.isNotEmpty() }
    }

    fun mineCount(title: String, userSep: String, amountHint: String): Int =
        digitsOnly(mineSegment(title, userSep, amountHint) ?: "").length

    fun mineDigits(title: String, userSep: String, amountHint: String): Set<Int> =
        digitsOnly(mineSegment(title, userSep, amountHint) ?: "")
            .map { it - '0' }.toSet()

    fun isCombinedGreeting(title: String, userSep: String, amountHint: String): Boolean {
        if (title.isEmpty()) return false
        val idx = separatedIndex(title, userSep)
        if (idx > 0) {
            val before = title.substring(0, idx)
            val after = title.substring(idx + 1)
            if (digitsOnly(before).isNotEmpty() && digitsOnly(after).isNotEmpty()) return true
        }
        val td = digitsOnly(title)
        val ad = digitsOnly(amountHint)
        if (ad.isNotEmpty() && td.length > ad.length && td.startsWith(ad)) return true
        if (td.isNotEmpty() && td.length == title.length) return true
        return false
    }

    fun containsChinese(title: String): Boolean =
        title.any { it.code in 0x4E00..0x9FFF }

    fun amountSegment(title: String, userSep: String, amountHint: String): String? {
        if (title.isEmpty()) return null
        val idx = separatedIndex(title, userSep)
        if (idx > 0) {
            val before = title.substring(0, idx)
            val after = title.substring(idx + 1)
            if (digitsOnly(before).isNotEmpty() && digitsOnly(after).isNotEmpty()) {
                return digitsOnly(before)
            }
        }
        val td = digitsOnly(title)
        val ad = digitsOnly(amountHint)
        if (ad.isNotEmpty() && td.length > ad.length && td.startsWith(ad)) return ad
        return null
    }

    fun faceValue(title: String, userSep: String, amountHint: String, greetingAmount: String): Double {
        val idx = separatedIndex(title, userSep)
        if (idx > 0) {
            val before = title.substring(0, idx)
            val after = title.substring(idx + 1)
            if (digitsOnly(before).isNotEmpty() && digitsOnly(after).isNotEmpty() && '.' in before) {
                return before.toDoubleOrNull() ?: 0.0
            }
        }
        val seg = amountSegment(title, userSep, amountHint)
        if (!seg.isNullOrEmpty()) return seg.toDoubleOrNull() ?: 0.0
        greetingAmount.toDoubleOrNull()?.let { if (it > 0) return it }
        amountHint.toDoubleOrNull()?.let { if (it > 0) return it }
        return 0.0
    }

    fun lastDigitOfAmountYuan(yuan: Double): Int {
        if (yuan < 0) return -1
        val fen = kotlin.math.round(yuan * 100.0).toLong()
        return (fen % 10).toInt()
    }

    fun yuanFromServer(raw: Double): Double {
        if (raw <= 0) return 0.0
        return if (raw == floor(raw)) raw / 100.0 else raw
    }
}
