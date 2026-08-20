package com.machinesecond.engine

import android.content.Context
import com.machinesecond.config.MsConfig
import com.machinesecond.host.HostBridge
import com.machinesecond.net.RedApi
import com.machinesecond.util.DiagLogStore
import com.machinesecond.util.MainHandler
import com.machinesecond.util.MsToast

/**
 * 自动发包：点一次开启后按间隔连续发，再点关闭。
 * 发包中不因短暂 pause / chatVisible 抖动而停表。
 */
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

    /** 自动发包钉死的群，开启后只往这里发，直到关闭 */
    private var pinnedSendGroupId: String? = null

    private var sendTimerRunnable: Runnable? = null
    private var sendingInFlight = false
    private var inFlightUnlockRunnable: Runnable? = null
    private var amountCycleIndex = 0
    private var countCycleIndex = 0
    private var greetingCycleIndex = 0

    fun clearState() {
        invalidateTimer()
        autoSendActive = false
        sendingInFlight = false
        pinnedSendGroupId = null
        activeGroupId = null
        chatVisible = false
        amountCycleIndex = 0
        countCycleIndex = 0
        greetingCycleIndex = 0
    }

    fun setActiveGroupId(groupId: String?, visible: Boolean) {
        if (autoSendActive) {
            if (!groupId.isNullOrBlank()) {
                activeGroupId = groupId
            }
            // 发包中：短暂离开表面只记状态，不停表；真正退出由 stopAutoSend / onLeaveChat 处理
            if (visible) {
                chatVisible = true
                if (sendTimerRunnable == null) scheduleSendTimer()
            }
            return
        }
        if (!groupId.isNullOrBlank()) {
            activeGroupId = groupId
        }
        chatVisible = visible
        if (!visible) {
            invalidateTimer()
        }
    }

    fun startAutoSend(groupId: String) {
        if (!isSendConfigured() || !config.sendSwitch || groupId.isBlank()) return
        activeGroupId = groupId
        pinnedSendGroupId = groupId
        chatVisible = true
        sendingInFlight = false
        autoSendActive = true
        DiagLogStore.append(context, "Send", "startAutoSend pin=$groupId")
        scheduleSendTimer()
        sendOnce()
    }

    fun stopAutoSend() {
        val was = autoSendActive
        autoSendActive = false
        pinnedSendGroupId = null
        invalidateTimer()
        sendingInFlight = false
        inFlightUnlockRunnable?.let { MainHandler.removeCallbacks(it) }
        inFlightUnlockRunnable = null
        if (was) {
            DiagLogStore.append(context, "Send", "stopAutoSend")
        }
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
        if (config.autoSendPassword.length != 6) return false
        val amount = config.sendAmount.split("/").map { it.trim() }.firstOrNull { it.isNotEmpty() }
            ?.toDoubleOrNull() ?: 0.0
        val count = config.packetCountText.split("/").map { it.trim() }
            .firstOrNull { it.isNotEmpty() }?.toIntOrNull() ?: 0
        if (amount <= 0 || count <= 0) return false
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

    private fun takeAmount(): Double {
        val parts = config.sendAmount.split("/").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return 0.0
        val v = parts[amountCycleIndex % parts.size].toDoubleOrNull() ?: 0.0
        amountCycleIndex++
        return v
    }

    private fun takeCount(): Int {
        val raw = config.packetCountText.ifBlank { config.packetCount.toString() }
        val parts = raw.split("/").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return 0
        val v = parts[countCycleIndex % parts.size].toIntOrNull() ?: 0
        countCycleIndex++
        return v
    }

    private fun takeGreeting(): String {
        val parts = config.greetingAmount.split("/").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return ""
        val v = parts[greetingCycleIndex % parts.size]
        greetingCycleIndex++
        return v
    }

    fun sendOnce() {
        if (!autoSendActive || sendingInFlight) return
        val gid = pinnedSendGroupId?.takeIf { it.isNotBlank() } ?: return
        // 发包中不因 SEM 误报 visible=false 而跳过
        chatVisible = true
        if (sendTimerRunnable == null) scheduleSendTimer()

        if (!config.masterSwitch || !config.sendSwitch) {
            stopAutoSend()
            return
        }

        val sendYuan = takeAmount()
        if (sendYuan <= 0) {
            MsToast.show(context, "红包金额无效")
            return
        }

        val count = takeCount()
        if (count <= 0) {
            MsToast.show(context, "红包个数无效")
            return
        }
        val perPacket = sendYuan / count
        if (perPacket <= 0.01) {
            MsToast.show(context, "单包需大于0.01元")
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

        // silent：失败时自己 toast，且绝不因接口报错关掉连续发包（只有用户再点才关）
        host.postRed(RedApi.SEND_GROUP, body, silent = true, onOk = { response ->
            MainHandler.post {
                sendingInFlight = false
                inFlightUnlockRunnable?.let { MainHandler.removeCallbacks(it) }
                if (!autoSendActive) return@post
                if (!RedApi.isSuccessCode(response)) {
                    val msg = RedApi.responseMessage(response).ifBlank { "发包失败" }
                    MsToast.show(context, msg)
                    DiagLogStore.append(context, "Send", "bizFail keepOn $gid $msg")
                } else {
                    DiagLogStore.append(context, "Send", "success $gid $title")
                }
            }
        }, onFail = { error ->
            MainHandler.post {
                sendingInFlight = false
                inFlightUnlockRunnable?.let { MainHandler.removeCallbacks(it) }
                if (!autoSendActive) return@post
                MsToast.show(context, error.message ?: "发包失败")
                DiagLogStore.append(context, "Send", "netFail keepOn $gid ${error.message}")
            }
        })
    }

    private fun buildSendTitle(mine: String): String {
        if (config.allMineMode) {
            if (mine.isNotEmpty()) return mine
            val g = takeGreeting().ifBlank { config.greetingAmount }
            if (g.isNotBlank()) return g
            return "恭喜发财，大吉大利"
        }
        val greeting = takeGreeting().ifBlank { config.greetingAmount }
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
        val intervalMs = (config.effectiveAutoSendIntervalSec() * 1000).toLong().coerceAtLeast(500L)
        val runnable = object : Runnable {
            override fun run() {
                if (!autoSendActive) return
                sendOnce()
                if (autoSendActive && sendTimerRunnable === this) {
                    MainHandler.postDelayed(intervalMs, this)
                }
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
