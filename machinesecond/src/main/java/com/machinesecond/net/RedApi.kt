package com.machinesecond.net

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.machinesecond.title.MineTitleParser
import kotlin.math.round

object RedApi {

    const val GRAB = "/red/grab"
    const val RECIVE_EXCLUSIVE = "/red/reciveExclusiveRedpacket"
    const val RECIVE_PERSON = "/red/recivePersonRedpacket"
    const val SEND_GROUP = "/red/sendGroupRedpacket"
    const val DETAIL = "/red/redpacketDetail"
    const val SEND_EXCLUSIVE = "/red/sendExclusiveRedPacket"

    fun yuanToFenString(yuan: Double): String =
        round(yuan * 100.0).toLong().toString()

    fun grabPathByType(type: Int): String? = when (type) {
        21 -> RECIVE_EXCLUSIVE
        22 -> RECIVE_PERSON
        23 -> GRAB
        else -> null
    }

    fun isSuccessCode(response: JsonObject): Boolean {
        val codeEl = response.get("code") ?: return false
        return when {
            codeEl.isJsonPrimitive && codeEl.asJsonPrimitive.isNumber ->
                codeEl.asInt == 200
            codeEl.isJsonPrimitive && codeEl.asJsonPrimitive.isString ->
                codeEl.asString == "200" || codeEl.asString.toIntOrNull() == 200
            else -> false
        }
    }

    fun responseMessage(response: JsonObject): String =
        response.get("msg")?.takeIf { it.isJsonPrimitive }?.asString ?: ""

    fun parsePersonalAmount(response: JsonObject, meUserId: String, type: Int? = null): Double {
        val data = response.getAsJsonObject("data") ?: response
        val listKeys = if (type == 23) listOf("vos") else listOf(
            "vos", "list", "records", "receiveList", "detailList", "users"
        )
        for (key in listKeys) {
            val arr = data.get(key)?.takeIf { it.isJsonArray }?.asJsonArray ?: continue
            for (item in arr) {
                if (!item.isJsonObject) continue
                val obj = item.asJsonObject
                val uid = firstString(obj, "userId", "id", "receiveUserId", "reciveUserId", "user_id")
                    ?: continue
                if (uid != meUserId) continue
                val yuan = amountFrom(obj, "amount", "reciveAmount", "receiveAmount", "money", "grabAmount")
                if (yuan > 0) return yuan
            }
        }
        val fromPersonal = amountFrom(
            data,
            "reciveAmount", "receiveAmount", "grabAmount", "money",
            "amount", "sendAmount", "redpacketAmount", "packetAmount"
        )
        if (fromPersonal > 0) return fromPersonal
        return 0.0
    }

    private fun firstString(obj: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            val el = obj.get(key) ?: continue
            if (el.isJsonPrimitive) {
                val s = el.asString
                if (s.isNotBlank()) return s
            }
        }
        return null
    }

    private fun amountFrom(obj: JsonObject, vararg keys: String): Double {
        for (key in keys) {
            val el = obj.get(key) ?: continue
            if (!el.isJsonPrimitive) continue
            val yuan = try {
                MineTitleParser.yuanFromServer(el.asDouble)
            } catch (_: Exception) {
                0.0
            }
            if (yuan > 0) return yuan
        }
        return 0.0
    }

    fun parseTotalNum(response: JsonObject): Int {
        val data = response.getAsJsonObject("data") ?: response
        val keys = listOf("totalNum", "total", "num", "packetCount")
        for (key in keys) {
            val el = data.get(key) ?: continue
            if (el.isJsonPrimitive && el.asJsonPrimitive.isNumber) {
                return el.asInt
            }
        }
        return 0
    }

    fun appearedDigitsFromDetail(response: JsonObject): Set<Int> {
        val data = response.getAsJsonObject("data") ?: response
        val digits = mutableSetOf<Int>()
        val listKeys = listOf("vos", "list", "records", "receiveList", "detailList", "users")
        for (key in listKeys) {
            val arr = data.get(key)?.takeIf { it.isJsonArray }?.asJsonArray ?: continue
            collectDigitsFromArray(arr, digits)
        }
        return digits
    }

    private fun collectDigitsFromArray(arr: JsonArray, digits: MutableSet<Int>) {
        for (item in arr) {
            if (!item.isJsonObject) continue
            val obj = item.asJsonObject
            val amt = obj.get("amount")?.asDouble
                ?: obj.get("reciveAmount")?.asDouble
                ?: obj.get("receiveAmount")?.asDouble
                ?: obj.get("money")?.asDouble
                ?: obj.get("grabAmount")?.asDouble
                ?: continue
            val yuan = MineTitleParser.yuanFromServer(amt)
            if (yuan > 0) {
                val digit = MineTitleParser.lastDigitOfAmountYuan(yuan)
                if (digit >= 0) digits.add(digit)
            }
        }
    }

    fun firstDataObject(response: JsonObject): JsonObject =
        response.getAsJsonObject("data") ?: response
}
