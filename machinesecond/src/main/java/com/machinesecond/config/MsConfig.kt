package com.machinesecond.config

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.machinesecond.host.HostBridge
import java.text.SimpleDateFormat
import java.util.Locale

class MsConfig private constructor(
    private val context: Context,
    private var host: HostBridge?
) {

    companion object {
        private const val PREFIX = "MachineSecond_"
        private val gson = Gson()
        private val expireFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        @Volatile
        private var instance: MsConfig? = null

        fun getInstance(context: Context, host: HostBridge? = null): MsConfig {
            return instance ?: synchronized(this) {
                instance ?: MsConfig(context.applicationContext, host).also { instance = it }
            }.also {
                if (host != null) it.host = host
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("${PREFIX}config", Context.MODE_PRIVATE)

    private val securePrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "${PREFIX}secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Secure fields
    var authCode: String = ""
        private set
    var authExpireTime: String? = null
        private set
    var autoSendPassword: String = ""
        private set
    var compensationPassword: String = ""
        private set

    // Boolean fields
    var masterSwitch: Boolean = false
    var secondSwitch: Boolean = false
    var sendSwitch: Boolean = false
    var compensationSwitch: Boolean = false
    var fanSwitch: Boolean = false
    var redPacketReminder: Boolean = false
    var backgroundGrab: Boolean = false
    var onlyPinnedChats: Boolean = false
    var grabNormalRedPacket: Boolean = false
    var grabTargetedRedPacket: Boolean = false
    var grabPrivateRedPacket: Boolean = false
    var autoReceiveTransfer: Boolean = false
    var skipCompensationPacket: Boolean = false
    var autoGrabTailIfNotMine: Boolean = false
    var skipTailIfMine: Boolean = false
    var onlyGrabMineCountEnabled: Boolean = false
    var fixedMineEnabled: Boolean = false
    var onlyMultiMine: Boolean = false
    var grabSpecifiedUsersEnabled: Boolean = false
    var skipSpecifiedUsersEnabled: Boolean = false
    var skipSelf: Boolean = false
    var specifiedAmountEnabled: Boolean = false
    var allMineMode: Boolean = false
    var cloudRedPacketSend: Boolean = false
    var pocketMoneySend: Boolean = false
    var fixedMineValueEnabled: Boolean = false
    var loopMineValueEnabled: Boolean = false
    var randomMineEnabled: Boolean = false
    var addPreciseCrowdEnabled: Boolean = false
    var autoCompensation: Boolean = false
    var hideCompensationBlessing: Boolean = false

    // Int fields
    var onlyGrabMineCount: Int = 0
    var fixedMineIndex: Int = 0
    var grabDelayMs: Int = 0
    var packetCount: Int = 9
    /** 红包个数原文，支持「5/6/7」多值循环 */
    var packetCountText: String = ""
    var randomMineIndex: Int = 0
    var fanIntervalSec: Int = 5
    var compensationDelaySec: Int = 0
    var memoryTimeSec: Int = 0

    // Double fields
    var autoSendInterval: Double = 5.0

    // String fields
    var grabSpecifiedUsers: String = ""
    var grabSpecifiedUsersDisplay: String = ""
    var skipSpecifiedUsers: String = ""
    var skipSpecifiedUsersDisplay: String = ""
    var specifiedAmount: String = ""
    var sendAmount: String = ""
    var greetingAmount: String = ""
    var separator: String = ""
    var fixedMineValue: String = ""
    var loopMineValue: String = ""
    var fanGreeting: String = "加我通过下"
    var skipAmountAbove: String = ""
    var skipPacketCount: String = ""
    var pluginTitle: String = ""

    // Matrix & lists
    private var compensationMatrix: MutableMap<String, String> = mutableMapOf()
    private var discountMatrix: MutableMap<String, Map<String, String>> = mutableMapOf()
    var grabSessionIds: MutableList<String> = mutableListOf()
    var compensationSessionIds: MutableList<String> = mutableListOf()

    /** Process-only, not persisted */
    var loopMineIndex: Int = 0

    fun setHost(bridge: HostBridge) {
        host = bridge
    }

    fun setAuthCredentials(code: String, expireTime: String?) {
        authCode = code
        authExpireTime = expireTime
    }

    fun setAutoSendPassword(value: String) {
        autoSendPassword = value
    }

    fun setCompensationPassword(value: String) {
        compensationPassword = value
    }

    fun reloadFromDisk() {
        authCode = securePrefs.getString("authCode", "") ?: ""
        authExpireTime = securePrefs.getString("authExpireTime", null)
        autoSendPassword = securePrefs.getString("autoSendPassword", "") ?: ""
        compensationPassword = securePrefs.getString("compensationPassword", "") ?: ""

        masterSwitch = prefs.getBoolean("masterSwitch", false)
        secondSwitch = prefs.getBoolean("secondSwitch", false)
        sendSwitch = prefs.getBoolean("sendSwitch", false)
        compensationSwitch = prefs.getBoolean("compensationSwitch", false)
        fanSwitch = prefs.getBoolean("fanSwitch", false)
        redPacketReminder = prefs.getBoolean("redPacketReminder", false)
        backgroundGrab = prefs.getBoolean("backgroundGrab", false)
        onlyPinnedChats = prefs.getBoolean("onlyPinnedChats", false)
        grabNormalRedPacket = prefs.getBoolean("grabNormalRedPacket", false)
        grabTargetedRedPacket = prefs.getBoolean("grabTargetedRedPacket", false)
        grabPrivateRedPacket = prefs.getBoolean("grabPrivateRedPacket", false)
        autoReceiveTransfer = prefs.getBoolean("autoReceiveTransfer", false)
        skipCompensationPacket = prefs.getBoolean("skipCompensationPacket", false)
        autoGrabTailIfNotMine = prefs.getBoolean("autoGrabTailIfNotMine", false)
        skipTailIfMine = prefs.getBoolean("skipTailIfMine", false)
        onlyGrabMineCountEnabled = prefs.getBoolean("onlyGrabMineCountEnabled", false)
        fixedMineEnabled = prefs.getBoolean("fixedMineEnabled", false)
        onlyMultiMine = prefs.getBoolean("onlyMultiMine", false)
        grabSpecifiedUsersEnabled = prefs.getBoolean("grabSpecifiedUsersEnabled", false)
        skipSpecifiedUsersEnabled = prefs.getBoolean("skipSpecifiedUsersEnabled", false)
        skipSelf = prefs.getBoolean("skipSelf", false)
        specifiedAmountEnabled = prefs.getBoolean("specifiedAmountEnabled", false)
        allMineMode = prefs.getBoolean("allMineMode", false)
        cloudRedPacketSend = prefs.getBoolean("cloudRedPacketSend", false)
        pocketMoneySend = prefs.getBoolean("pocketMoneySend", false)
        fixedMineValueEnabled = prefs.getBoolean("fixedMineValueEnabled", false)
        loopMineValueEnabled = prefs.getBoolean("loopMineValueEnabled", false)
        randomMineEnabled = prefs.getBoolean("randomMineEnabled", false)
        addPreciseCrowdEnabled = prefs.getBoolean("addPreciseCrowdEnabled", false)
        autoCompensation = prefs.getBoolean("autoCompensation", false)
        hideCompensationBlessing = prefs.getBoolean("hideCompensationBlessing", false)

        onlyGrabMineCount = prefs.getInt("onlyGrabMineCount", 0)
        fixedMineIndex = prefs.getInt("fixedMineIndex", 0)
        grabDelayMs = prefs.getInt("grabDelayMs", 0)
        packetCount = prefs.getInt("packetCount", 9).let { if (it <= 0) 9 else it }
        packetCountText = prefs.getString("packetCountText", "") ?: ""
        if (packetCountText.isBlank()) packetCountText = packetCount.toString()
        randomMineIndex = prefs.getInt("randomMineIndex", 0)
        fanIntervalSec = prefs.getInt("fanIntervalSec", 5).coerceAtLeast(5)
        compensationDelaySec = prefs.getInt("compensationDelaySec", 0)
        memoryTimeSec = prefs.getInt("memoryTimeSec", 0)

        autoSendInterval = prefs.getFloat("autoSendInterval", 5.0f).toDouble()
            .let { if (it <= 0) 5.0 else it }

        grabSpecifiedUsers = prefs.getString("grabSpecifiedUsers", "") ?: ""
        grabSpecifiedUsersDisplay = prefs.getString("grabSpecifiedUsersDisplay", "") ?: ""
        skipSpecifiedUsers = prefs.getString("skipSpecifiedUsers", "") ?: ""
        skipSpecifiedUsersDisplay = prefs.getString("skipSpecifiedUsersDisplay", "") ?: ""
        specifiedAmount = prefs.getString("specifiedAmount", "") ?: ""
        sendAmount = prefs.getString("sendAmount", "") ?: ""
        greetingAmount = prefs.getString("greetingAmount", "") ?: ""
        separator = prefs.getString("separator", "") ?: ""
        fixedMineValue = prefs.getString("fixedMineValue", "") ?: ""
        loopMineValue = prefs.getString("loopMineValue", "") ?: ""
        fanGreeting = prefs.getString("fanGreeting", "加我通过下") ?: "加我通过下"
        skipAmountAbove = prefs.getString("skipAmountAbove", "") ?: ""
        skipPacketCount = prefs.getString("skipPacketCount", "") ?: ""
        pluginTitle = prefs.getString("pluginTitle", "") ?: ""

        compensationMatrix = readStringMap(prefs.getString("compensationMatrix", null))
        discountMatrix = readDiscountMap(prefs.getString("discountMatrix", null))
        grabSessionIds = readStringList(prefs.getString("grabSessionIds", null)).toMutableList()
        compensationSessionIds =
            readStringList(prefs.getString("compensationSessionIds", null)).toMutableList()
    }

    fun synchronize() {
        securePrefs.edit()
            .putString("authCode", authCode)
            .putString("authExpireTime", authExpireTime)
            .putString("autoSendPassword", autoSendPassword)
            .putString("compensationPassword", compensationPassword)
            .apply()

        val editor = prefs.edit()
        editor.putBoolean("masterSwitch", masterSwitch)
        editor.putBoolean("secondSwitch", secondSwitch)
        editor.putBoolean("sendSwitch", sendSwitch)
        editor.putBoolean("compensationSwitch", compensationSwitch)
        editor.putBoolean("fanSwitch", fanSwitch)
        editor.putBoolean("redPacketReminder", redPacketReminder)
        editor.putBoolean("backgroundGrab", backgroundGrab)
        editor.putBoolean("onlyPinnedChats", onlyPinnedChats)
        editor.putBoolean("grabNormalRedPacket", grabNormalRedPacket)
        editor.putBoolean("grabTargetedRedPacket", grabTargetedRedPacket)
        editor.putBoolean("grabPrivateRedPacket", grabPrivateRedPacket)
        editor.putBoolean("autoReceiveTransfer", autoReceiveTransfer)
        editor.putBoolean("skipCompensationPacket", skipCompensationPacket)
        editor.putBoolean("autoGrabTailIfNotMine", autoGrabTailIfNotMine)
        editor.putBoolean("skipTailIfMine", skipTailIfMine)
        editor.putBoolean("onlyGrabMineCountEnabled", onlyGrabMineCountEnabled)
        editor.putBoolean("fixedMineEnabled", fixedMineEnabled)
        editor.putBoolean("onlyMultiMine", onlyMultiMine)
        editor.putBoolean("grabSpecifiedUsersEnabled", grabSpecifiedUsersEnabled)
        editor.putBoolean("skipSpecifiedUsersEnabled", skipSpecifiedUsersEnabled)
        editor.putBoolean("skipSelf", skipSelf)
        editor.putBoolean("specifiedAmountEnabled", specifiedAmountEnabled)
        editor.putBoolean("allMineMode", allMineMode)
        editor.putBoolean("cloudRedPacketSend", cloudRedPacketSend)
        editor.putBoolean("pocketMoneySend", pocketMoneySend)
        editor.putBoolean("fixedMineValueEnabled", fixedMineValueEnabled)
        editor.putBoolean("loopMineValueEnabled", loopMineValueEnabled)
        editor.putBoolean("randomMineEnabled", randomMineEnabled)
        editor.putBoolean("addPreciseCrowdEnabled", addPreciseCrowdEnabled)
        editor.putBoolean("autoCompensation", autoCompensation)
        editor.putBoolean("hideCompensationBlessing", hideCompensationBlessing)

        editor.putInt("onlyGrabMineCount", onlyGrabMineCount)
        editor.putInt("fixedMineIndex", fixedMineIndex)
        editor.putInt("grabDelayMs", grabDelayMs)
        editor.putInt("packetCount", if (packetCount <= 0) 9 else packetCount)
        editor.putString("packetCountText", packetCountText.ifBlank { packetCount.toString() })
        editor.putInt("randomMineIndex", randomMineIndex)
        editor.putInt("fanIntervalSec", fanIntervalSec.coerceAtLeast(5))
        editor.putInt("compensationDelaySec", compensationDelaySec)
        editor.putInt("memoryTimeSec", memoryTimeSec)

        editor.putFloat("autoSendInterval", (if (autoSendInterval <= 0) 5.0 else autoSendInterval).toFloat())

        editor.putString("grabSpecifiedUsers", grabSpecifiedUsers)
        editor.putString("grabSpecifiedUsersDisplay", grabSpecifiedUsersDisplay)
        editor.putString("skipSpecifiedUsers", skipSpecifiedUsers)
        editor.putString("skipSpecifiedUsersDisplay", skipSpecifiedUsersDisplay)
        editor.putString("specifiedAmount", specifiedAmount)
        editor.putString("sendAmount", sendAmount)
        editor.putString("greetingAmount", greetingAmount)
        editor.putString("separator", separator)
        editor.putString("fixedMineValue", fixedMineValue)
        editor.putString("loopMineValue", loopMineValue)
        editor.putString("fanGreeting", fanGreeting)
        editor.putString("skipAmountAbove", skipAmountAbove)
        editor.putString("skipPacketCount", skipPacketCount)
        editor.putString("pluginTitle", pluginTitle)

        editor.putString("compensationMatrix", gson.toJson(compensationMatrix))
        editor.putString("discountMatrix", gson.toJson(discountMatrix))
        editor.putString("grabSessionIds", gson.toJson(grabSessionIds))
        editor.putString("compensationSessionIds", gson.toJson(compensationSessionIds))
        editor.apply()
    }

    fun isAuthorized(): Boolean = true

    fun onMasterSwitchTurnedOn() {
        secondSwitch = true
        sendSwitch = true
        compensationSwitch = true
        fanSwitch = true
    }

    fun settingsTitle(overrideTitle: String? = null): String {
        overrideTitle?.takeIf { it.isNotBlank() }?.let { return it }
        pluginTitle.takeIf { it.isNotBlank() }?.let { return it }
        val display = host?.appDisplayName()?.takeIf { it.isNotBlank() } ?: "助手"
        return display + "闪电"
    }

    fun amountHint(): String =
        greetingAmount.takeIf { it.isNotBlank() } ?: sendAmount

    fun syncPacketCountFromText() {
        val first = packetCountText.split("/")
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            ?.toIntOrNull()
        if (first != null && first > 0) {
            packetCount = first
        }
        if (packetCountText.isBlank()) {
            packetCountText = packetCount.toString()
        }
    }

    fun effectiveAutoSendIntervalSec(): Double =
        if (autoSendInterval <= 0) 5.0 else autoSendInterval

    fun toggleGrabSession(sessionId: String): Boolean {
        if (grabSessionIds.contains(sessionId)) {
            grabSessionIds.remove(sessionId)
            return false
        }
        grabSessionIds.add(sessionId)
        return true
    }

    fun isGrabArmed(sessionId: String): Boolean = grabSessionIds.contains(sessionId)

    fun disarmGrabSession(sessionId: String) {
        if (sessionId.isBlank()) return
        grabSessionIds.remove(sessionId)
    }

    fun disarmAllGrabSessions() {
        grabSessionIds.clear()
    }

    fun disarmAllCompensationSessions() {
        compensationSessionIds.clear()
    }

    fun toggleCompensationSession(sessionId: String): Boolean {
        if (compensationSessionIds.contains(sessionId)) {
            compensationSessionIds.remove(sessionId)
            return false
        }
        compensationSessionIds.add(sessionId)
        return true
    }

    fun isCompensationArmed(sessionId: String): Boolean =
        compensationSessionIds.contains(sessionId)

    fun disarmCompensationSession(sessionId: String) {
        if (sessionId.isBlank()) return
        compensationSessionIds.remove(sessionId)
    }

    fun getCompensationFactor(packetCount: Int, mineCount: Int): Double {
        val key = "${packetCount - 4}-$mineCount"
        val legacyKey = "${packetCount}_$mineCount"
        val raw = compensationMatrix[key]?.trim()
            ?: compensationMatrix[legacyKey]?.trim()
        if (raw.isNullOrEmpty()) return 1.0
        return raw.toDoubleOrNull() ?: 1.0
    }

    fun setCompensationFactor(packetCount: Int, mineCount: Int, value: String) {
        compensationMatrix["${packetCount - 4}-$mineCount"] = value
    }

    fun getCompensationMatrix(): Map<String, String> = compensationMatrix.toMap()

    fun setCompensationMatrix(map: Map<String, String>) {
        compensationMatrix = map.toMutableMap()
    }

    fun getDiscountEntry(index: Int): Pair<String, String> {
        val entry = discountMatrix["$index"] ?: return "" to ""
        return (entry["left"] ?: "") to (entry["right"] ?: "")
    }

    fun setDiscountEntry(index: Int, left: String, right: String) {
        discountMatrix["$index"] = mapOf("left" to left, "right" to right)
    }

    fun getDiscountMatrix(): Map<String, Map<String, String>> = discountMatrix.toMap()

    fun setDiscountMatrix(map: Map<String, Map<String, String>>) {
        discountMatrix = map.toMutableMap()
    }

    fun mapDiscountFace(faceRaw: Double): Double {
        for (i in 0..11) {
            val (left, right) = getDiscountEntry(i)
            val leftVal = left.toDoubleOrNull() ?: continue
            val rightVal = right.toDoubleOrNull() ?: continue
            if (kotlin.math.abs(faceRaw - leftVal) < 0.001) return rightVal
        }
        return faceRaw
    }

    fun skipPacketCountSet(): Set<Int> =
        skipPacketCount.split(",", "，")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()

    fun grabSpecifiedUserSet(): Set<String> =
        grabSpecifiedUsers.split(",", "，").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    fun skipSpecifiedUserSet(): Set<String> =
        skipSpecifiedUsers.split(",", "，").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    fun addGrabSpecifiedUser(userId: String, displayName: String) {
        val ids = splitCsv(grabSpecifiedUsers)
        val names = splitCsv(grabSpecifiedUsersDisplay)
        if (!ids.contains(userId)) {
            ids.add(userId)
            names.add(displayName)
        }
        grabSpecifiedUsers = joinCsv(ids)
        grabSpecifiedUsersDisplay = joinCsv(names)
        grabSpecifiedUsersEnabled = true
        removeSkipSpecifiedUser(userId, disableIfEmpty = true)
    }

    fun removeGrabSpecifiedUser(userId: String) {
        val ids = splitCsv(grabSpecifiedUsers)
        val names = splitCsv(grabSpecifiedUsersDisplay)
        val idx = ids.indexOf(userId)
        if (idx >= 0) {
            ids.removeAt(idx)
            if (idx < names.size) names.removeAt(idx)
        }
        grabSpecifiedUsers = joinCsv(ids)
        grabSpecifiedUsersDisplay = joinCsv(names)
        if (ids.isEmpty()) grabSpecifiedUsersEnabled = false
    }

    fun addSkipSpecifiedUser(userId: String, displayName: String) {
        removeGrabSpecifiedUser(userId)
        val ids = splitCsv(skipSpecifiedUsers)
        val names = splitCsv(skipSpecifiedUsersDisplay)
        if (!ids.contains(userId)) {
            ids.add(userId)
            names.add(displayName)
        }
        skipSpecifiedUsers = joinCsv(ids)
        skipSpecifiedUsersDisplay = joinCsv(names)
        skipSpecifiedUsersEnabled = true
    }

    fun removeSkipSpecifiedUser(userId: String, disableIfEmpty: Boolean = true) {
        val ids = splitCsv(skipSpecifiedUsers)
        val names = splitCsv(skipSpecifiedUsersDisplay)
        val idx = ids.indexOf(userId)
        if (idx >= 0) {
            ids.removeAt(idx)
            if (idx < names.size) names.removeAt(idx)
        }
        skipSpecifiedUsers = joinCsv(ids)
        skipSpecifiedUsersDisplay = joinCsv(names)
        if (disableIfEmpty && ids.isEmpty()) skipSpecifiedUsersEnabled = false
    }

    private fun splitCsv(value: String): MutableList<String> =
        value.split(",", "，").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()

    private fun joinCsv(list: List<String>): String = list.joinToString(",")

    private fun parseExpire(exp: String): Long? =
        try {
            expireFormat.parse(exp)?.time
        } catch (_: Exception) {
            null
        }

    private fun readStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun readStringMap(json: String?): MutableMap<String, String> {
        if (json.isNullOrBlank()) return mutableMapOf()
        return try {
            gson.fromJson<MutableMap<String, String>>(
                json,
                object : TypeToken<MutableMap<String, String>>() {}.type
            )
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun readDiscountMap(json: String?): MutableMap<String, Map<String, String>> {
        if (json.isNullOrBlank()) return mutableMapOf()
        return try {
            gson.fromJson(
                json,
                object : TypeToken<MutableMap<String, Map<String, String>>>() {}.type
            )
        } catch (_: Exception) {
            mutableMapOf()
        }
    }
}
