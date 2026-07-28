package com.machinesecond.fan

import com.machinesecond.config.MsConfig
import com.machinesecond.host.FanUser
import com.machinesecond.host.HostBridge
import com.machinesecond.util.MainHandler

class FanAddQueue(
    private val host: HostBridge,
    private val config: MsConfig
) {

    @Volatile
    private var running = false

    @Volatile
    private var stopRequested = false

    private var countdownToken = 0

    /** current/total；nextDelaySec=0 表示正在添加，>0 表示等待间隔 */
    var onProgress: ((current: Int, total: Int, nextDelaySec: Int) -> Unit)? = null
    /** 倒计时秒数（对齐 iOS countdown） */
    var onCountdown: ((seconds: Int) -> Unit)? = null
    var onRunningChanged: ((running: Boolean, total: Int) -> Unit)? = null
    var onFinished: (() -> Unit)? = null
    var onItemStatus: ((userId: String, status: FanAddStatus, message: String) -> Unit)? = null

    fun isRunning(): Boolean = running

    fun stop() {
        stopRequested = true
        countdownToken++
    }

    fun start(users: List<FanUser>, greeting: String) {
        if (running) return
        if (!config.masterSwitch || !config.isAuthorized()) return
        if (users.isEmpty()) return

        running = true
        stopRequested = false
        countdownToken++
        val intervalSec = maxOf(5, config.fanIntervalSec)
        val msg = greeting.ifBlank { config.fanGreeting.ifBlank { "加我通过下" } }
        for (user in users) {
            onItemStatus?.invoke(user.userId, FanAddStatus.Pending, "待添加")
        }
        onRunningChanged?.invoke(true, users.size)
        processNext(users, 0, msg, intervalSec)
    }

    private fun finish() {
        running = false
        countdownToken++
        onCountdown?.invoke(0)
        onRunningChanged?.invoke(false, 0)
        onFinished?.invoke()
    }

    private fun processNext(users: List<FanUser>, index: Int, greeting: String, intervalSec: Int) {
        if (stopRequested || index >= users.size) {
            finish()
            return
        }

        val user = users[index]
        onProgress?.invoke(index + 1, users.size, 0)
        onCountdown?.invoke(0)

        addOne(user, greeting) {
            if (stopRequested || index + 1 >= users.size) {
                finish()
                return@addOne
            }
            onProgress?.invoke(index + 1, users.size, intervalSec)
            startCountdown(intervalSec) {
                if (stopRequested) {
                    finish()
                } else {
                    processNext(users, index + 1, greeting, intervalSec)
                }
            }
        }
    }

    private fun startCountdown(seconds: Int, onDone: () -> Unit) {
        val token = ++countdownToken
        fun tick(left: Int) {
            if (token != countdownToken || stopRequested) return
            onCountdown?.invoke(left)
            if (left <= 0) {
                onDone()
                return
            }
            MainHandler.postDelayed(1000L) { tick(left - 1) }
        }
        tick(seconds)
    }

    private fun isTerminalBusinessFailure(message: String?): Boolean {
        val text = message?.trim().orEmpty()
        if (text.isEmpty()) return false
        return listOf(
            "好友不存在",
            "用户不存在",
            "账号不存在",
            "会员不存在",
            "该用户不存在",
            "不存在"
        ).any { text.contains(it) }
    }

    private fun addOne(user: FanUser, greeting: String, cb: () -> Unit) {
        val userId = user.userId
        if (host.isFriend(userId)) {
            onItemStatus?.invoke(userId, FanAddStatus.AlreadyFriend, "已是好友")
            cb()
            return
        }
        onItemStatus?.invoke(userId, FanAddStatus.Adding, "添加中…")
        host.addFriendBusiness(userId, user.memberCode, greeting) { ok, businessMsg ->
            if (ok) {
                onItemStatus?.invoke(userId, FanAddStatus.Success, "已添加")
                cb()
            } else {
                if (isTerminalBusinessFailure(businessMsg)) {
                    onItemStatus?.invoke(userId, FanAddStatus.Failed, businessMsg ?: "失败")
                    cb()
                    return@addFriendBusiness
                }
                host.addFriendIm(userId, greeting) { imOk, imMsg ->
                    if (imOk) {
                        onItemStatus?.invoke(userId, FanAddStatus.Success, "已添加")
                    } else {
                        val failMsg = imMsg?.takeIf { it.isNotBlank() }
                            ?: businessMsg?.takeIf { it.isNotBlank() }
                            ?: "失败"
                        onItemStatus?.invoke(userId, FanAddStatus.Failed, failMsg)
                    }
                    cb()
                }
            }
        }
    }
}
