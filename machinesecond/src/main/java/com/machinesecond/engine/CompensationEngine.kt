package com.machinesecond.engine

import android.content.Context
import com.google.gson.JsonObject
import com.machinesecond.config.MsConfig
import com.machinesecond.host.HostBridge
import com.machinesecond.host.RedInbound
import com.machinesecond.net.RedApi
import com.machinesecond.title.MineTitleParser
import com.machinesecond.util.DiagLogStore
import com.machinesecond.util.MainHandler
import com.machinesecond.util.MsToast

data class CompTrack(
    val redPacketId: String,
    val sessionId: String,
    var title: String,
    var toUserId: String,
    var senderName: String?,
    val mineDigits: Set<Int>,
    val mineCount: Int,
    var faceValue: Double,
    var packetCount: Int,
    val appeared: MutableSet<Int> = mutableSetOf(),
    var iHit: Boolean = false,
    var scheduled: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis()
)

class CompensationEngine(
    private val context: Context,
    private val host: HostBridge,
    private val config: MsConfig
) {

    private val tracks = mutableMapOf<String, CompTrack>()
    private val pollCounts = mutableMapOf<String, Int>()

    fun clearState() {
        tracks.clear()
        pollCounts.clear()
    }

    fun pruneExpiredTracks() {
        val it = tracks.entries.iterator()
        while (it.hasNext()) {
            val t = it.next().value
            if (isMemoryExpired(t)) {
                MsToast.show(context, "已过记忆时间")
                it.remove()
            }
        }
    }

    fun ensureTrack(msg: RedInbound) {
        if (!config.compensationSwitch || !config.autoCompensation) return
        if (!config.isCompensationArmed(msg.sessionId)) return
        if (config.compensationPassword.isEmpty()) return

        val sep = config.separator
        val hint = config.amountHint()
        val title = msg.title
        if (!MineTitleParser.isCombinedGreeting(title, sep, hint)) return

        val digits = MineTitleParser.mineDigits(title, sep, hint)
        if (digits.isEmpty()) return

        val mc = MineTitleParser.mineCount(title, sep, hint)
        val fv = MineTitleParser.faceValue(title, sep, hint, config.greetingAmount)
        var face = fv
        if (face <= 0 && msg.amountYuan > 0) face = msg.amountYuan

        val track = tracks.getOrPut(msg.redPacketId) {
            CompTrack(
                redPacketId = msg.redPacketId,
                sessionId = msg.sessionId,
                title = title,
                toUserId = msg.fromUserId,
                senderName = msg.senderDisplayName,
                mineDigits = digits,
                mineCount = mc,
                faceValue = face,
                packetCount = config.packetCount
            )
        }
        if (track.toUserId.isBlank()) track.toUserId = msg.fromUserId
        if (track.senderName.isNullOrBlank()) track.senderName = msg.senderDisplayName
        track.title = title
        if (face > 0) track.faceValue = face
    }

    fun afterGrab(msg: RedInbound, personalYuan: Double) {
        if (!config.compensationSwitch || !config.autoCompensation) return
        if (!config.isCompensationArmed(msg.sessionId)) return
        if (config.compensationPassword.isEmpty()) return

        ensureTrack(msg)
        val track = tracks[msg.redPacketId] ?: return

        if (personalYuan > 0) {
            val digit = MineTitleParser.lastDigitOfAmountYuan(personalYuan)
            if (digit in track.mineDigits) {
                track.iHit = true
                track.appeared.add(digit)
            }
        }

        refreshDetailOnce(track) {
            trySchedule(track)
            if (track.iHit && !isAllMinesAppeared(track)) {
                pollDetail(track, 0)
            }
        }
    }

    private fun refreshDetailOnce(track: CompTrack, onDone: () -> Unit) {
        host.postRed(
            RedApi.DETAIL,
            mapOf("redpacketId" to track.redPacketId),
            silent = true,
            onOk = { response ->
                MainHandler.post {
                    if (RedApi.isSuccessCode(response)) {
                        val total = RedApi.parseTotalNum(response)
                        if (total > 0) track.packetCount = total
                        track.appeared.addAll(RedApi.appearedDigitsFromDetail(response))
                    }
                    onDone()
                }
            },
            onFail = {
                MainHandler.post { onDone() }
            }
        )
    }

    private fun pollDetail(track: CompTrack, attempt: Int) {
        if (!track.iHit) return
        if (isAllMinesAppeared(track)) {
            trySchedule(track)
            return
        }
        if (attempt >= 8) {
            tracks.remove(track.redPacketId)
            pollCounts.remove(track.redPacketId)
            return
        }

        if (isMemoryExpired(track)) {
            MsToast.show(context, "已过记忆时间")
            tracks.remove(track.redPacketId)
            pollCounts.remove(track.redPacketId)
            return
        }

        MainHandler.postDelayed(1500) {
            host.postRed(
                RedApi.DETAIL,
                mapOf("redpacketId" to track.redPacketId),
                silent = true,
                onOk = { response ->
                    MainHandler.post {
                        if (RedApi.isSuccessCode(response)) {
                            val total = RedApi.parseTotalNum(response)
                            if (total > 0) track.packetCount = total
                            track.appeared.addAll(RedApi.appearedDigitsFromDetail(response))
                        }
                        if (isAllMinesAppeared(track)) {
                            trySchedule(track)
                        } else {
                            pollDetail(track, attempt + 1)
                        }
                    }
                },
                onFail = {
                    MainHandler.post { pollDetail(track, attempt + 1) }
                }
            )
        }
    }

    private fun isAllMinesAppeared(track: CompTrack): Boolean =
        track.mineDigits.all { it in track.appeared }

    /** 对齐 iOS sendBlock / sendExclusiveCompensationAmount：延迟到期与拆包前二次复检 */
    private fun canStillCompensate(sessionId: String): Boolean {
        return config.masterSwitch &&
            config.compensationSwitch &&
            config.autoCompensation &&
            config.isCompensationArmed(sessionId) &&
            config.compensationPassword.isNotEmpty()
    }


    private fun trySchedule(track: CompTrack) {
        if (!track.iHit) {
            DiagLogStore.append(context, "Comp", "schedule skip notHit rid=${track.redPacketId}")
            return
        }
        if (!isAllMinesAppeared(track)) return
        if (track.scheduled) return
        track.scheduled = true
        val delayMs = maxOf(0, config.compensationDelaySec) * 1000L
        MainHandler.postDelayed(delayMs) {
            execute(track)
        }
    }

    private fun execute(track: CompTrack) {
        if (!track.iHit || !isAllMinesAppeared(track)) {
            DiagLogStore.append(context, "Comp", "execute skip gate rid=${track.redPacketId} iHit=${track.iHit} all=${isAllMinesAppeared(track)}")
            tracks.remove(track.redPacketId)
            return
        }
        if (!canStillCompensate(track.sessionId)) {
            tracks.remove(track.redPacketId)
            return
        }
        if (isMemoryExpired(track)) {
            MsToast.show(context, "已过记忆时间")
            tracks.remove(track.redPacketId)
            return
        }

        val pkt = track.packetCount.takeIf { it > 0 } ?: config.packetCount
        if (pkt !in 5..11) {
            MsToast.show(context, "红包数不符合雷包")
            tracks.remove(track.redPacketId)
            return
        }

        if (pkt in config.skipPacketCountSet()) {
            tracks.remove(track.redPacketId)
            return
        }

        var face = config.mapDiscountFace(track.faceValue)
        val skipAbove = config.skipAmountAbove.toDoubleOrNull()
        if (skipAbove != null && face > skipAbove) {
            tracks.remove(track.redPacketId)
            return
        }

        val factor = config.getCompensationFactor(pkt, track.mineCount)
        if (factor <= 0) {
            tracks.remove(track.redPacketId)
            return
        }

        val pay = face * factor
        val title = buildCompTitle(track.senderName)
        // track 在最后一笔发完 / 失败时再移除（对齐 iOS）
        sendExclusiveSplit(track, pay, title)
    }

    private fun buildCompTitle(senderName: String?): String {
        if (config.hideCompensationBlessing) return ""
        if (!senderName.isNullOrBlank()) return "赔付$senderName"
        return "赔付"
    }

    private fun sendExclusiveSplit(track: CompTrack, totalPay: Double, title: String) {
        var remaining = totalPay
        sendNextChunk(track, remaining, title) { sent ->
            remaining = sent
            if (remaining > 0.001) {
                MainHandler.postDelayed(1000) {
                    sendExclusiveSplit(track, remaining, title)
                }
            }
        }
    }

    private fun sendNextChunk(track: CompTrack, amount: Double, title: String, onDone: (Double) -> Unit) {
        if (!canStillCompensate(track.sessionId) ||
            amount <= 0.0001 ||
            track.sessionId.isBlank() ||
            track.toUserId.isBlank() ||
            config.compensationPassword.isEmpty()
        ) {
            tracks.remove(track.redPacketId)
            onDone(0.0)
            return
        }
        val chunk = if (amount > 200) 200.0 else amount
        val remain = amount - chunk
        val body = mapOf(
            "groupId" to track.sessionId,
            "title" to title,
            "amount" to RedApi.yuanToFenString(chunk),
            "toUserId" to track.toUserId,
            "password" to config.compensationPassword
        )
        host.postRed(RedApi.SEND_EXCLUSIVE, body, silent = true, onOk = { response ->
            MainHandler.post {
                if (!RedApi.isSuccessCode(response)) {
                    val msg = RedApi.responseMessage(response)
                    if (msg.isNotBlank()) MsToast.show(context, msg)
                    tracks.remove(track.redPacketId)
                    onDone(0.0)
                    return@post
                }
                DiagLogStore.append(context, "Comp", "sent ${track.redPacketId} $chunk")
                if (remain > 0.0001) {
                    onDone(remain)
                } else {
                    tracks.remove(track.redPacketId)
                    onDone(0.0)
                }
            }
        }, onFail = { _ ->
            MainHandler.post {
                // 对齐 iOS：网络失败静默，清 track，有余下也不继续
                tracks.remove(track.redPacketId)
                onDone(0.0)
            }
        })
    }

    private fun isMemoryExpired(track: CompTrack): Boolean {
        if (config.memoryTimeSec <= 0) return false
        val elapsed = System.currentTimeMillis() - track.createdAtMs
        return elapsed > config.memoryTimeSec * 1000L
    }
}
