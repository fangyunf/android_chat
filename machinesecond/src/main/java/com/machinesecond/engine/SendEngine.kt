package com.machinesecond.engine

import android.content.Context
import com.google.gson.JsonObject
import com.machinesecond.config.MsConfig
import com.machinesecond.host.HostBridge
import com.machinesecond.net.RedApi
import com.machinesecond.util.DiagLogStore
import com.machinesecond.util.MainHandler
import com.machinesecond.util.MsToast

class SendEngine(
    private val context: Context,
    private val host: HostBridge,
    private val config: MsConfig
) {

    var activeGroupId: String? = null
        private set
    var chatVisible: Boolean = false
        private set
    var autoSendActive: Boolean = false
        private set

    private var sendTimerRunnable: Runnable? = null
    private var sendingInFlight = false
    private var inFlightUnlockRunnable: Runnable? = null

    fun clearState() {
        invalidateTimer()
        autoSendActive = false
        sendingInFlight = false
        activeGroupId = null
        chatVisible = false
    }

    fun setActiveGroupId(groupId: String?, visible: Boolean) {
        if (!groupId.isNullOrBlank()) {
            activeGroupId = groupId
        }
        chatVisible = visible
        if (!visible) {
            invalidateTimer()
        } else if (autoSendActive && sendTimerRunnable == null) {
            scheduleSendTimer()
        }
    }

    fun startAutoSend(groupId: String) {
        if (!isSendConfigured() || !config.sendSwitch || groupId.isBlank()) return
        activeGroupId = groupId
        autoSendActive = true
        if (chatVisible) {
            scheduleSendTimer()
            sendOnce()
        }
    }

    fun stopAutoSend() {
        autoSendActive = false
        invalidateTimer()
        sendingInFlight = false
        inFlightUnlockRunnable?.let { MainHandler.removeCallbacks(it) }
        inFlightUnlockRunnable = null
    }

    fun toggleAutoSend(groupId: String): Boolean {
        return if (autoSendActive) {
            stopAutoSend()
            false
        } else {
            startAutoSend(groupId)
            true
        }
    }

    fun hasMineValueSource(): Boolean =
        (config.loopMineValueEnabled && config.loopMineValue.isNotBlank()) ||
            config.randomMineEnabled ||
            (config.fixedMineValueEnabled && config.fixedMineValue.isNotBlank()) ||
            config.fixedMineValue.isNotBlank()

    fun isSendConfigured(): Boolean {
        if (config.autoSendPassword.length != 6 || config.packetCount <= 0) return false
        if (config.sendAmount.isBlank() || (config.sendAmount.toDoubleOrNull() ?: 0.0) <= 0) return false
        if (config.allMineMode) return hasMineValueSource()
        return true
    }

    fun nextMineValue(): String {
        if (config.loopMineValueEnabled && config.loopMineValue.isNotBlank()) {
            val parts = config.loopMineValue.split("/").map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.isNotEmpty()) {
                val v = parts[config.loopMineIndex % parts.size]
                config.loopMineIndex++
                return v
            }
        }
        if (config.randomMineEnabled) {
            val n = (config.randomMineIndex + 1).coerceIn(1, 9)
            return (1..n).map { ('0'..'9').random() }.joinToString("")
        }
        if (config.fixedMineValueEnabled && config.fixedMineValue.isNotBlank()) return config.fixedMineValue
        return config.fixedMineValue
    }

    fun sendOnce() {
        if (!autoSendActive || sendingInFlight) return
        val gid = activeGroupId
        if (gid.isNullOrBlank() || !chatVisible) return

        if (!config.masterSwitch || !config.sendSwitch) {
            stopAutoSend()
            return
        }

        val sendYuan = config.sendAmount.toDoubleOrNull() ?: 0.0
        if (sendYuan <= 0) {
            stopAutoSend()
            return
        }

        if (config.packetCount <= 0) {
            stopAutoSend()
            return
        }
        val count = config.packetCount
        val perPacket = sendYuan / count
        if (perPacket <= 0.01) {
            MsToast.show(context, "单包需大于0.01元")
            stopAutoSend()
            return
        }

        val mine = nextMineValue()
        if (config.allMineMode && mine.isEmpty()) return

        val title = buildSendTitle(mine)
        val body = mapOf(
            "groupId" to gid,
            "title" to title,
            "amount" to RedApi.yuanToFenString(sendYuan),
            "num" to count,
            "tradePassword" to config.autoSendPassword
        )

        sendingInFlight = true
        scheduleInFlightUnlock()

        host.postRed(RedApi.SEND_GROUP, body, silent = false, onOk = { response ->
            MainHandler.post {
                sendingInFlight = false
                inFlightUnlockRunnable?.let { MainHandler.removeCallbacks(it) }
                if (!RedApi.isSuccessCode(response)) {
                    val msg = RedApi.responseMessage(response).ifBlank { "发包失败" }
                    MsToast.show(context, msg)
                    stopAutoSend()
                } else {
                    DiagLogStore.append(context, "Send", "success $gid $title")
                }
            }
        }, onFail = { error ->
            MainHandler.post {
                sendingInFlight = false
                inFlightUnlockRunnable?.let { MainHandler.removeCallbacks(it) }
                MsToast.show(context, error.message ?: "发包失败")
                stopAutoSend()
            }
        })
    }

    private fun buildSendTitle(mine: String): String {
        if (config.allMineMode) {
            if (mine.isNotEmpty()) return mine
            if (config.greetingAmount.isNotBlank()) return config.greetingAmount
            return "恭喜发财，大吉大利"
        }
        val greeting = config.greetingAmount
        val sep = config.separator
        return when {
            mine.isNotEmpty() && greeting.isNotBlank() -> greeting + sep + mine
            greeting.isNotBlank() -> greeting
            mine.isNotEmpty() -> mine
            else -> "恭喜发财，大吉大利"
        }
    }

    private fun scheduleSendTimer() {
        invalidateTimer()
        val intervalMs = (config.effectiveAutoSendIntervalSec() * 1000).toLong()
        val runnable = object : Runnable {
            override fun run() {
                if (!autoSendActive || !chatVisible) return
                sendOnce()
                sendTimerRunnable = this
                MainHandler.postDelayed(intervalMs, this)
            }
        }
        sendTimerRunnable = runnable
        MainHandler.postDelayed(intervalMs, runnable)
    }

    private fun invalidateTimer() {
        sendTimerRunnable?.let { MainHandler.removeCallbacks(it) }
        sendTimerRunnable = null
    }

    private fun scheduleInFlightUnlock() {
        inFlightUnlockRunnable?.let { MainHandler.removeCallbacks(it) }
        val unlock = Runnable { sendingInFlight = false }
        inFlightUnlockRunnable = unlock
        MainHandler.postDelayed(30_000, unlock)
    }
}
