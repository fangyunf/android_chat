package com.machinesecond.engine

import android.content.Context
import com.google.gson.JsonObject
import com.machinesecond.config.MsConfig
import com.machinesecond.host.HostBridge
import com.machinesecond.host.RedInbound
import com.machinesecond.net.RedApi
import com.machinesecond.title.MineTitleParser
import com.machinesecond.util.DiagLogStore
import com.machinesecond.util.GrabLogStore
import com.machinesecond.util.MainHandler
import com.machinesecond.util.MsToast
import com.machinesecond.util.MsVoiceAlert
import java.util.Locale

enum class DenyReason {
    GLOBAL, MODULE, NOT_ARMED, SKIP_SELF, TYPE_OFF, NOT_FOR_ME,
    PINNED, SKIP_COMP_TITLE, UNKNOWN_TYPE, SKIP_USER, NOT_IN_GRAB_LIST,
    AMOUNT, MINE_COUNT, FIXED_MINE, MULTI_MINE
}

class GrabEngine(
    private val context: Context,
    private val host: HostBridge,
    private val config: MsConfig
) {

    var onGrabSuccessForCompensation: ((RedInbound, Double) -> Unit)? = null

    private val processedIds = LinkedHashSet<String>()
    private val grabbingIds = mutableSetOf<String>()
    private val maxSetSize = 500

    fun clearState() {
        processedIds.clear()
        grabbingIds.clear()
    }

    fun onRecv(msg: RedInbound) {
        if (msg.claimed || msg.redPacketId.isBlank()) return

        val deny = shouldGrab(msg)
        if (deny != null) {
            DiagLogStore.append(context, "Grab", "deny ${msg.redPacketId} $deny")
            return
        }

        val id = msg.redPacketId
        if (processedIds.contains(id) || grabbingIds.contains(id)) return

        addToSet(grabbingIds, id)

        val delayMs = maxOf(0, config.grabDelayMs).toLong()
        MainHandler.postDelayed(delayMs) {
            performGrab(msg)
        }
    }

    fun shouldGrab(msg: RedInbound): DenyReason? {
        val me = host.currentUserId()
        val type = msg.customType
        val sessionId = msg.sessionId
        val from = msg.fromUserId
        val title = msg.title
        val amountYuan = msg.amountYuan
        val toUserId = msg.toUserId

        if (!config.masterSwitch) return DenyReason.GLOBAL
        if (!config.secondSwitch) return DenyReason.MODULE
        if (type != 22 && !config.grabSessionIds.contains(sessionId)) return DenyReason.NOT_ARMED
        if (config.skipSelf && from == me) return DenyReason.SKIP_SELF

        when (type) {
            21 -> {
                if (!config.grabTargetedRedPacket) return DenyReason.TYPE_OFF
                if (!toUserId.isNullOrEmpty() && toUserId != me) return DenyReason.NOT_FOR_ME
            }
            22 -> {
                if (!config.grabPrivateRedPacket) return DenyReason.TYPE_OFF
                return null
            }
            23 -> {
                if (config.onlyPinnedChats && !host.isSessionPinned(sessionId)) return DenyReason.PINNED
                if (MineTitleParser.containsChinese(title) && config.skipCompensationPacket) {
                    return DenyReason.SKIP_COMP_TITLE
                }
                if (!config.grabNormalRedPacket) return DenyReason.TYPE_OFF
            }
            28, 503 -> {
                if (!config.autoReceiveTransfer) return DenyReason.TYPE_OFF
                if (!toUserId.isNullOrEmpty() && toUserId != me) return DenyReason.NOT_FOR_ME
            }
            else -> return DenyReason.UNKNOWN_TYPE
        }

        if (config.skipSpecifiedUsersEnabled && from in config.skipSpecifiedUserSet()) {
            return DenyReason.SKIP_USER
        }
        if (config.grabSpecifiedUsersEnabled && from !in config.grabSpecifiedUserSet()) {
            return DenyReason.NOT_IN_GRAB_LIST
        }
        if (config.specifiedAmountEnabled) {
            val specified = config.specifiedAmount.toDoubleOrNull() ?: 0.0
            if (amountYuan < specified) return DenyReason.AMOUNT
        }

        val sep = config.separator
        val hint = config.amountHint()
        val mc = MineTitleParser.mineCount(title, sep, hint)
        if (config.onlyGrabMineCountEnabled && config.onlyGrabMineCount > 0 && mc < config.onlyGrabMineCount) {
            return DenyReason.MINE_COUNT
        }
        if (config.fixedMineEnabled && mc != config.fixedMineIndex + 1) return DenyReason.FIXED_MINE
        if (config.onlyMultiMine && mc < 2) return DenyReason.MULTI_MINE

        return null
    }

    private fun performGrab(msg: RedInbound) {
        val id = msg.redPacketId
        val type = msg.customType
        val me = host.currentUserId()

        if (!config.masterSwitch || !config.secondSwitch) {
            finishGrab(id)
            return
        }
        if (type != 22 && !config.grabSessionIds.contains(msg.sessionId)) {
            finishGrab(id)
            return
        }

        if (type == 28 || type == 503) {
            host.markMessageClaimed(id)
            msg.rawMessageRef?.let { host.updateMessageUI(it) }
            val grabbed = msg.amountYuan
            if (config.redPacketReminder) remind(type, grabbed)
            GrabLogStore.append(
                context,
                "抢包成功: ${msg.redPacketId} ${String.format(Locale.US, "%.2f", grabbed)}元",
                success = true
            )
            onGrabSuccessForCompensation?.invoke(msg, grabbed)
            finishGrab(id)
            DiagLogStore.append(context, "Grab", "transfer claimed $id")
            return
        }

        val path = RedApi.grabPathByType(type)
        if (path == null) {
            finishGrab(id)
            return
        }

        val body = mapOf(
            "redpacketId" to msg.redPacketId,
            "createTime" to msg.createTime
        )

        host.postRed(path, body, silent = true, onOk = { response ->
            MainHandler.post {
                handleGrabResponse(msg, response, me)
                finishGrab(id)
            }
        }, onFail = { error ->
            MainHandler.post {
                DiagLogStore.append(context, "Grab", "fail $id ${error.message}")
                GrabLogStore.append(context, "抢包失败: $id ${error.message}", success = false)
                finishGrab(id)
            }
        })
    }

    private fun handleGrabResponse(msg: RedInbound, response: JsonObject, me: String) {
        if (!RedApi.isSuccessCode(response)) {
            val msgText = RedApi.responseMessage(response)
            DiagLogStore.append(context, "Grab", "code fail ${msg.redPacketId} $msgText")
            GrabLogStore.append(context, "抢包失败: ${msg.redPacketId} $msgText", success = false)
            return
        }

        host.markMessageClaimed(msg.redPacketId)
        msg.rawMessageRef?.let { host.updateMessageUI(it) }

        val personalYuan = RedApi.parsePersonalAmount(response, me, msg.customType)
        onGrabSuccessForCompensation?.invoke(msg, personalYuan)
        remindSuccess(msg.customType, personalYuan)

        GrabLogStore.append(
            context,
            "抢包成功: ${msg.redPacketId} ${String.format(Locale.US, "%.2f", personalYuan)}元",
            success = true
        )
        DiagLogStore.append(context, "Grab", "success ${msg.redPacketId} $personalYuan")
    }

    private fun remind(type: Int, yuan: Double) {
        remindSuccess(type, yuan)
    }

    private fun remindSuccess(type: Int, yuan: Double) {
        if (!config.redPacketReminder) return
        val hasAmount = yuan > 0
        val amountStr = if (hasAmount) String.format(Locale.US, "%.2f", yuan) else ""
        // 对齐 qiangbao：0=普通抢到 22=私聊 23=专属 28=转账
        val voiceKind = when (type) {
            22 -> 22
            28, 503 -> 28
            21 -> 23
            else -> 0
        }
        val text = when (voiceKind) {
            22 -> if (hasAmount) "领私聊:${amountStr}元" else "领私聊红包成功"
            28 -> if (hasAmount) "领转账:${amountStr}元" else "领转账成功"
            23 -> if (hasAmount) "领专属:${amountStr}元" else "领专属红包成功"
            else -> if (hasAmount) "抢到红包:${amountStr}元" else "抢到红包"
        }
        if (!hasAmount) {
            DiagLogStore.append(context, "Grab", "success amount empty type=$type")
        }
        MsToast.show(context, text)
        if (hasAmount) {
            MsVoiceAlert.speakAmount(context, voiceKind, yuan)
        }
    }

    private fun finishGrab(id: String) {
        grabbingIds.remove(id)
        addToSet(processedIds, id)
    }

    private fun addToSet(set: MutableSet<String>, id: String) {
        if (set.size >= maxSetSize && set.isNotEmpty()) {
            val first = set.iterator().next()
            set.remove(first)
        }
        set.add(id)
    }
}
